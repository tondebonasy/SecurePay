import React, { createContext, useContext, useMemo, useRef, useState } from "react";
import { Portal, Snackbar, Text } from "react-native-paper";

type ToastTone = "info" | "success" | "danger";

type ToastValue = {
  showToast: (message: string, tone?: ToastTone, dedupeKey?: string) => void;
};

const ToastContext = createContext<ToastValue>({
  showToast: () => {},
});

const toneColors: Record<ToastTone, string> = {
  info: "#1f3b5b",
  success: "#14532d",
  danger: "#7f1d1d",
};

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [visible, setVisible] = useState(false);
  const [message, setMessage] = useState("");
  const [tone, setTone] = useState<ToastTone>("info");
  const lastToastRef = useRef<{ key: string; at: number } | null>(null);

  const value = useMemo(
    () => ({
      showToast(nextMessage: string, nextTone: ToastTone = "info", dedupeKey?: string) {
        if (dedupeKey) {
          const now = Date.now();
          if (
            lastToastRef.current &&
            lastToastRef.current.key === dedupeKey &&
            now - lastToastRef.current.at < 5000
          ) {
            return;
          }
          lastToastRef.current = { key: dedupeKey, at: now };
        }
        setMessage(nextMessage);
        setTone(nextTone);
        setVisible(true);
      },
    }),
    []
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Portal>
        <Snackbar
          visible={visible}
          onDismiss={() => setVisible(false)}
          duration={3200}
          style={{ backgroundColor: toneColors[tone], marginBottom: 20 }}
        >
          <Text style={{ color: "#ffffff" }}>{message}</Text>
        </Snackbar>
      </Portal>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
