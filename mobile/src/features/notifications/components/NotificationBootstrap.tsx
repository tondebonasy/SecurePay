import React, { useEffect } from "react";
import { usePushNotifications } from "../hooks/usePushNotifications";

export function NotificationBootstrap() {
  const { register } = usePushNotifications();

  useEffect(() => {
    register();
  }, []);

  return null;
}
