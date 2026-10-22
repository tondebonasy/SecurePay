import axios from "axios";
import { getApiBaseUrl, resolveApiBaseUrl } from "./env";
import { loadAuth, saveAuth, clearAuth, StoredAuth, getDeviceId } from "./tokenStorage";

type RefreshHandler = (auth: StoredAuth) => Promise<StoredAuth | null>;
type LogoutHandler = () => void;

let refreshHandler: RefreshHandler | null = null;
let logoutHandler: LogoutHandler | null = null;

export function setAuthHandlers(onRefresh: RefreshHandler, onLogout: LogoutHandler) {
  refreshHandler = onRefresh;
  logoutHandler = onLogout;
}

const baseUrl = getApiBaseUrl();

export const apiClient = axios.create({
  baseURL: baseUrl,
  timeout: 15000,
});

apiClient.interceptors.request.use(async (config) => {
  const resolvedBaseUrl = await resolveApiBaseUrl();
  config.baseURL = resolvedBaseUrl;
  if (__DEV__) {
    const url = `${resolvedBaseUrl}${config.url || ""}`;
    console.log("[api] request", config.method?.toUpperCase(), url);
  }
  const auth = await loadAuth();
  if (auth?.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`;
  }
  const deviceId = await getDeviceId();
  if (deviceId) {
    config.headers["X-Device-Id"] = deviceId;
  }
  return config;
});

apiClient.interceptors.response.use(
  (r) => r,
  async (err) => {
    if (__DEV__) {
      const url = `${err?.config?.baseURL || ""}${err?.config?.url || ""}`;
      console.warn("[api] error", err?.message, url);
    }
    const original = err.config;
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true;
      const auth = await loadAuth();
      if (auth && refreshHandler) {
        const refreshed = await refreshHandler(auth);
        if (refreshed) {
          await saveAuth(refreshed);
          original.headers.Authorization = `Bearer ${refreshed.accessToken}`;
          return apiClient(original);
        }
      }
      await clearAuth();
      if (logoutHandler) logoutHandler();
    }
    return Promise.reject(err);
  }
);

export function parseApiError(err: any): { message: string; code?: string; details?: any } {
  const data = err?.response?.data?.error;
  return {
    message: data?.message || err?.message || "Request failed",
    code: data?.code,
    details: data?.details,
  };
}
