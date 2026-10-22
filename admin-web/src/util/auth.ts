const KEY = "securepay_admin_auth_v1";
const PERSISTENT_KEY = "securepay_admin_auth_persistent_v1";

export type AuthState = {
  accessToken: string;
  refreshToken: string;
  sessionId: string;
  deviceId: string;
};

export function getDeviceId(): string {
  const k = "securepay_device_id_v1";
  let id = localStorage.getItem(k);
  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(k, id);
  }
  return id;
}

export function setAuth(state: Omit<AuthState, "deviceId">, remember = false) {
  const deviceId = getDeviceId();
  const full: AuthState = { ...state, deviceId };
  sessionStorage.removeItem(KEY);
  localStorage.removeItem(PERSISTENT_KEY);
  (remember ? localStorage : sessionStorage).setItem(
    remember ? PERSISTENT_KEY : KEY,
    JSON.stringify(full),
  );
}

export function getAuth(): AuthState | null {
  const raw = sessionStorage.getItem(KEY) || localStorage.getItem(PERSISTENT_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthState;
  } catch {
    return null;
  }
}

export function clearAuth() {
  sessionStorage.removeItem(KEY);
  localStorage.removeItem(PERSISTENT_KEY);
}

export function isAuthed() {
  return !!getAuth()?.accessToken;
}
