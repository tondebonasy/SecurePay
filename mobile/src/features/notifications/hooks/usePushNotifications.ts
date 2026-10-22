import { useEffect, useRef, useState } from "react";
import * as Notifications from "expo-notifications";
import { registerExpoToken } from "../api/notificationApi";
import { getExpoPushToken } from "../utils/expoPush";
import { Platform } from "react-native";
import { navigationRef } from "../../../navigation/AppNavigator";
import { useToast } from "../store/toastStore";
import { notifyTransactionUpdated } from "../../transactions/store/transactionEvents";
import * as txApi from "../../transactions/api/transactionApi";

export function usePushNotifications() {
  const [enabled, setEnabled] = useState(false);
  const [registered, setRegistered] = useState(false);
  const { showToast } = useToast();
  const knownStatusesRef = useRef<Map<string, string>>(new Map());
  const seededRef = useRef(false);

  useEffect(() => {
    Notifications.setNotificationHandler({
      handleNotification: async () => ({
        shouldShowAlert: true,
        shouldShowBanner: true,
        shouldShowList: true,
        shouldPlaySound: false,
        shouldSetBadge: false,
      }),
    });

    const receivedSub = Notifications.addNotificationReceivedListener((notification) => {
      const title = notification.request.content.title || "SecurePay";
      const body = notification.request.content.body || "You have a new transaction update.";
      const txId = String(notification.request.content.data?.txId || "");
      const status = String(notification.request.content.data?.status || "").toUpperCase();
      if (txId) notifyTransactionUpdated();
      const tone = status === "NOTIFIED" ? "success" : status.includes("FAILED") ? "danger" : "info";
      const dedupeKey = txId && status ? `${txId}:${status}` : undefined;
      showToast(`${title}: ${body}`, tone, dedupeKey);
    });

    const responseSub = Notifications.addNotificationResponseReceivedListener((resp) => {
      const txId = resp.notification.request.content.data?.txId;
      const qrLink = resp.notification.request.content.data?.qrLink;
      if (txId) notifyTransactionUpdated();
      if (txId && navigationRef.isReady()) {
        navigationRef.navigate("TransactionDetail", { txId });
        return;
      }
      if (qrLink && navigationRef.isReady()) {
        navigationRef.navigate("MainTabs", {
          screen: "Scan",
          params: { qrLink },
        });
      }
    });
    return () => {
      receivedSub.remove();
      responseSub.remove();
    };
  }, [showToast]);

  useEffect(() => {
    let active = true;
    let timer: ReturnType<typeof setTimeout> | null = null;

    const syncTransactions = async () => {
      try {
        const list = await txApi.listTransactions();
        if (!active) return;

        const nextStatuses = new Map<string, string>();
        for (const tx of list) {
          nextStatuses.set(tx.id, tx.status);
          const prevStatus = knownStatusesRef.current.get(tx.id);
          const isTerminal = tx.status === "NOTIFIED" || tx.status === "FAILED" || tx.status === "PAYNOW_FAILED";
          const changed = prevStatus !== tx.status;

          if (seededRef.current && changed && isTerminal) {
            notifyTransactionUpdated();
            if (tx.status === "NOTIFIED") {
              showToast("Payment verified.", "success", `${tx.id}:${tx.status}`);
            } else {
              showToast("Payment failed.", "danger", `${tx.id}:${tx.status}`);
            }
          }
        }

        knownStatusesRef.current = nextStatuses;
        seededRef.current = true;
      } catch {
        // Ignore polling failures; the next interval will retry.
      } finally {
        if (active) {
          timer = setTimeout(syncTransactions, 4000);
        }
      }
    };

    syncTransactions();

    return () => {
      active = false;
      if (timer) clearTimeout(timer);
    };
  }, [showToast]);

  async function register() {
    const token = await getExpoPushToken();
    if (!token) {
      setEnabled(false);
      setRegistered(false);
      return;
    }
    setEnabled(true);
    await registerExpoToken(token, Platform.OS);
    setRegistered(true);
  }

  return { register, enabled, registered };
}
