import { apiClient } from "../../../services/apiClient";

export async function registerExpoToken(token: string, platform: string) {
  await apiClient.post("/v1/notifications/register-token", { expoPushToken: token, platform });
}
