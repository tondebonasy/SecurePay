import * as Device from "expo-device";
import * as Notifications from "expo-notifications";
import Constants from "expo-constants";

export async function getExpoPushToken() {
  if (!Device.isDevice) return null;
  if (Constants.appOwnership === "expo") return null;
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;
  if (existingStatus !== "granted") {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }
  if (finalStatus !== "granted") return null;
  const token = await Notifications.getExpoPushTokenAsync();
  return token.data;
}
