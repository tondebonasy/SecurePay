import React from "react";
import { Text, View } from "react-native";
import { colors, spacing, typography } from "../../../theme";
import { TxStatus } from "../types";

const paypalSteps: { key: TxStatus; label: string }[] = [
  { key: "CREATED", label: "Created" },
  { key: "PAYPAL_ORDER_CREATED", label: "PayPal Order Created" },
  { key: "PAYPAL_CAPTURED", label: "PayPal Captured" },
  { key: "PROOF_SUBMITTED", label: "Proof Submitted" },
  { key: "NOTIFIED", label: "User Notified" },
];

const paynowSteps: { key: TxStatus; label: string }[] = [
  { key: "CREATED", label: "Created" },
  { key: "PAYNOW_REQUESTED", label: "PayNow Requested" },
  { key: "PAYNOW_PAID", label: "PayNow Paid" },
  { key: "PROOF_SUBMITTED", label: "Proof Submitted" },
  { key: "NOTIFIED", label: "User Notified" },
];

export function StatusStepper({ status }: { status: TxStatus }) {
  const steps = status.startsWith("PAYNOW") ? paynowSteps : paypalSteps;
  return (
    <View style={{ gap: spacing.sm }}>
      {steps.map((s, idx) => {
        const done = stepDone(status, s.key, steps);
        const isFailed = (status === "FAILED" || status === "PAYNOW_FAILED") && idx >= 1;
        return (
          <View key={s.key} style={{ flexDirection: "row", alignItems: "center" }}>
            <View style={{
              width: 10,
              height: 10,
              borderRadius: 5,
              backgroundColor: isFailed ? colors.danger : done ? colors.success : "rgba(12,17,24,0.2)",
              marginRight: spacing.sm,
            }} />
            <Text style={{ ...typography.body, color: isFailed ? colors.danger : colors.ink }}>{s.label}</Text>
          </View>
        );
      })}
    </View>
  );
}

function stepDone(current: TxStatus, target: TxStatus, order: { key: TxStatus }[]) {
  if (current === "FAILED" || current === "PAYNOW_FAILED") return false;
  const list = order.map((s) => s.key);
  return list.indexOf(current) >= list.indexOf(target);
}
