
export interface LoginRequest {
  email: string;
  password?: string; // Sometimes optional if using OAuth, but here it's our main login
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
  expiresIn?: number;
}

export interface RefreshRequest {
  refreshToken: string;
}
