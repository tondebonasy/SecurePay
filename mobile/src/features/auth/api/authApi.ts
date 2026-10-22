import { apiClient } from "../../../services/apiClient";
import { AuthTokens, AuthUser } from "../types";

export async function login(phone: string, password: string): Promise<AuthTokens> {
  const resp = await apiClient.post("/v1/auth/login", { phone, password });
  return resp.data.data as AuthTokens;
}

export async function register(phone: string, email: string, displayName: string, password: string): Promise<AuthTokens> {
  const resp = await apiClient.post("/v1/auth/register", { phone, email, displayName, password });
  return resp.data.data as AuthTokens;
}

export async function me(): Promise<AuthUser> {
  const resp = await apiClient.get("/v1/auth/me");
  return resp.data.data as AuthUser;
}

export async function refresh(sessionId: string, refreshToken: string): Promise<AuthTokens> {
  const resp = await apiClient.post("/v1/auth/refresh", { sessionId, refreshToken });
  return resp.data.data as AuthTokens;
}
