import * as SecureStore from "expo-secure-store";

const KEY = "mg_auth_v2";
const DEVICE_KEY = "mg_device_id_v1";

export type StoredAuth = {
  accessToken: string;
  refreshToken: string;
  sessionId: string;
};

export async function saveAuth(auth: StoredAuth) {
  await SecureStore.setItemAsync(KEY, JSON.stringify(auth));
}

export async function loadAuth(): Promise<StoredAuth | null> {
  const raw = await SecureStore.getItemAsync(KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredAuth;
  } catch {
    return null;
  }
}

export async function clearAuth() {
  await SecureStore.deleteItemAsync(KEY);
}

export async function getDeviceId(): Promise<string> {
  const existing = await SecureStore.getItemAsync(DEVICE_KEY);
  if (existing) return existing;
  const id = globalThis.crypto?.randomUUID?.() || `mg-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  await SecureStore.setItemAsync(DEVICE_KEY, id);
  return id;
}
