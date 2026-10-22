import { Platform } from "react-native";
import * as Device from "expo-device";

const rawBaseUrl = process.env.EXPO_PUBLIC_API_URL || process.env.EXPO_PUBLIC_API_BASE_URL;
const rawPort = process.env.EXPO_PUBLIC_API_PORT;

function normalizePort(value?: string) {
  const trimmed = value?.trim();
  return trimmed && trimmed.length > 0 ? trimmed : "8080";
}

function getDefaultHost() {
  if (Platform.OS === "android") {
    return Device.isDevice ? "localhost" : "10.0.2.2";
  }
  return "localhost";
}

const port = normalizePort(rawPort);
const defaultUsbBaseUrl = `http://${getDefaultHost()}:${port}`;
const lanBaseUrl = rawBaseUrl?.trim() || null;

function sanityCheck() {
  if (!__DEV__ && lanBaseUrl && !lanBaseUrl.startsWith("https://")) throw new Error("Release API URL must use HTTPS");
  if (!lanBaseUrl && !__DEV__) {
    throw new Error("Missing EXPO_PUBLIC_API_URL (or EXPO_PUBLIC_API_BASE_URL) in env");
  }
}

export function getApiBaseUrl() {
  sanityCheck();
  return lanBaseUrl || defaultUsbBaseUrl;
}

let resolvedBaseUrl: string | null = null;
let detectionPromise: Promise<string> | null = null;

async function canReachUrl(url: string) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 1200);
  try {
    await fetch(url, { method: "GET", cache: "no-store", signal: controller.signal });
    return true;
  } catch {
    return false;
  } finally {
    clearTimeout(timeout);
  }
}

async function detectBaseUrl() {
  if (detectionPromise) {
    return detectionPromise;
  }
  detectionPromise = (async () => {
    if (lanBaseUrl) { resolvedBaseUrl = lanBaseUrl; return lanBaseUrl; }
    if (await canReachUrl(defaultUsbBaseUrl)) {
      resolvedBaseUrl = defaultUsbBaseUrl;
      return defaultUsbBaseUrl;
    }
    if (lanBaseUrl) {
      resolvedBaseUrl = lanBaseUrl;
      return lanBaseUrl;
    }
    return defaultUsbBaseUrl;
  })();
  detectionPromise.finally(() => {
    detectionPromise = null;
  });
  return detectionPromise;
}

export async function resolveApiBaseUrl() {
  sanityCheck();
  if (!__DEV__ || resolvedBaseUrl === lanBaseUrl) {
    return getApiBaseUrl();
  }
  if (resolvedBaseUrl) {
    return resolvedBaseUrl;
  }
  const detected = await detectBaseUrl();
  if (resolvedBaseUrl) {
    return resolvedBaseUrl;
  }
  return detected;
}
