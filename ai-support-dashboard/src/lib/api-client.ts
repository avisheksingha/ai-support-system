import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { defaultTokenManager } from "./token-manager";
import { v4 as uuidv4 } from "uuid";

// Define our typed API Error
export interface ApiError {
  status: number;
  message: string;
  error?: string;
  path?: string;
  timestamp?: string;
  validationErrors?: Record<string, string>;
  response?: any;
}

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request Interceptor: Attach token and correlation ID
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = defaultTokenManager.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Add Correlation ID to trace requests across microservices
    config.headers["X-Correlation-ID"] = uuidv4();
    
    return config;
  },
  (error) => Promise.reject(error)
);

// Refresh Logic State
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response Interceptor: Global error handling and automatic token refresh
apiClient.interceptors.response.use(
  (response) => {
    // Proactive token refresh if backend signals token is about to expire
    if (response.headers["x-access-token-refresh"] === "true" && !isRefreshing) {
      isRefreshing = true;
      const refreshToken = defaultTokenManager.getRefreshToken();
      if (refreshToken) {
        axios.post(
          `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
          { refreshToken },
          { headers: { "Content-Type": "application/json" } }
        ).then(refreshResponse => {
          const newAccessToken = refreshResponse.data.accessToken;
          const newRefreshToken = refreshResponse.data.refreshToken;
          defaultTokenManager.setAccessToken(newAccessToken);
          defaultTokenManager.setRefreshToken(newRefreshToken);
          processQueue(null, newAccessToken);
        }).catch(err => {
          processQueue(err, null);
          defaultTokenManager.clear();
          window.dispatchEvent(new Event("auth:unauthorized"));
        }).finally(() => {
          isRefreshing = false;
        });
      } else {
        isRefreshing = false;
      }
    }
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Handle Network Errors (backend unavailable or internet disconnected)
    if (!error.response) {
      console.error("Network Error: Backend might be down or unreachable", error);
      return Promise.reject({
        status: 0,
        message: "Network Error: Could not connect to the server.",
      } as ApiError);
    }

    // Handle 401 Unauthorized for token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Don't intercept refresh token failures to prevent infinite loops.
      // But still format as ApiError so callers can read err.message correctly.
      if (originalRequest.url?.includes("/auth/refresh") || originalRequest.url?.includes("/auth/login")) {
        const data = error.response?.data as any;
        return Promise.reject({
          status: error.response?.status || 401,
          message: data?.message || data?.error || error.message,
          error: data?.error,
          path: data?.path,
        } as ApiError);
      }

      if (isRefreshing) {
        return new Promise(function (resolve, reject) {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;
      const refreshToken = defaultTokenManager.getRefreshToken();

      if (!refreshToken) {
        isRefreshing = false;
        defaultTokenManager.clear();
        // Option to trigger a global event here to navigate to login
        window.dispatchEvent(new Event("auth:unauthorized"));
        return Promise.reject(error);
      }

      try {
        // Direct axios call to avoid our own interceptors interfering
        const refreshResponse = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
          { refreshToken },
          { headers: { "Content-Type": "application/json" } }
        );

        const newAccessToken = refreshResponse.data.accessToken;
        const newRefreshToken = refreshResponse.data.refreshToken;

        defaultTokenManager.setAccessToken(newAccessToken);
        defaultTokenManager.setRefreshToken(newRefreshToken);

        processQueue(null, newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        defaultTokenManager.clear();
        window.dispatchEvent(new Event("auth:unauthorized"));
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // Format remaining API Errors
    const apiError: ApiError = {
      status: error.response?.status || 500,
      message: (error.response?.data as any)?.message || error.message,
      error: (error.response?.data as any)?.error,
      path: (error.response?.data as any)?.path,
      validationErrors: (error.response?.data as any)?.validationErrors,
      response: error.response,
    };
    
    return Promise.reject(apiError);
  }
);
