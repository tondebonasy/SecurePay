import axios from "axios";
import { getAuth, clearAuth, setAuth, getDeviceId } from "./auth";

const baseURL = import.meta.env.VITE_API_BASE_URL || (import.meta.env.PROD ? "/" : "http://localhost:8080");
if (import.meta.env.PROD && /^http:\/\//i.test(baseURL)) throw new Error("Production API URL must use HTTPS or a same-origin path");

export const api = axios.create({
  baseURL,
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const auth = getAuth();
  if (auth?.accessToken)
    config.headers.Authorization = `Bearer ${auth.accessToken}`;
  const deviceId = auth?.deviceId || getDeviceId();
  if (deviceId) config.headers["X-Device-Id"] = deviceId;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  async (err) => {
    const auth = getAuth();
    const original = err.config;
    if (
      err.response?.status === 401 &&
      auth?.refreshToken &&
      auth?.sessionId &&
      !original._retry
    ) {
      original._retry = true;
      try {
        const resp = await axios.post(
          `${baseURL}/v1/auth/refresh`,
          { sessionId: auth.sessionId, refreshToken: auth.refreshToken },
          { headers: { "X-Device-Id": auth.deviceId } },
        );
        const data = resp.data?.data;
        setAuth({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          sessionId: data.sessionId,
        });
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return axios(original);
      } catch {
        clearAuth();
        window.location.href = "/login";
      }
    }
    return Promise.reject(err);
  },
);

