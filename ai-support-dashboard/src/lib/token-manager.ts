export interface TokenManager {
  getAccessToken(): string | null;
  setAccessToken(token: string): void;
  getRefreshToken(): string | null;
  setRefreshToken(token: string): void;
  clear(): void;
}

// In-memory variable for the access token to prevent XSS theft
let memoryAccessToken: string | null = null;
const REFRESH_TOKEN_KEY = "ai_support_refresh_token";

export const defaultTokenManager: TokenManager = {
  getAccessToken() {
    return memoryAccessToken;
  },
  
  setAccessToken(token: string) {
    memoryAccessToken = token;
  },
  
  getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },
  
  setRefreshToken(token: string) {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  },
  
  clear() {
    memoryAccessToken = null;
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
};
