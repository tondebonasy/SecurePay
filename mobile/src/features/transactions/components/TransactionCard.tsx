import React from "react";
import { Alert, Pressable, Text, View } from "react-native";
import * as Clipboard from "expo-clipboard";
import { colors, spacing, typography } from "../../../theme";
import { Badge } from "../../../components/Badge";
import { Transaction } from "../types";
import { formatCurrency, formatDate } from "../../../utils/format";

export function TransactionCard({
  tx,
  onPress,
  showProofStatus = false,
}: {
  tx: Transaction;
  onPress: () => void;
  showProofStatus?: boolean;
}) {
  const tone = tx.status === "NOTIFIED" ? "success" : tx.status === "FAILED" || tx.status === "PAYNOW_FAILED" ? "danger" : "warning";
  const provider = tx.paynowReference ? "PayNow" : "PayPal";
  const statusDot = tone === "success" ? colors.success : tone === "danger" ? colors.danger : colors.warning;
  const proofAnchored = tx.status === "NOTIFIED";

  async function copyId() {
    await Clipboard.setStringAsync(tx.id);
    Alert.alert("Copied", "Transaction ID copied.");
  }

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        {
          padding: spacing.md,
          borderRadius: 12,
          borderWidth: 1,
          borderColor: colors.border,
          backgroundColor: "rgba(255,255,255,0.9)",
          marginBottom: spacing.sm,
        },
        pressed ? { transform: [{ translateY: -1 }] } : null,
      ]}
    >
      <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "center" }}>
        <View style={{ flexDirection: "row", alignItems: "center", gap: spacing.sm }}>
          <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: statusDot }} />
          <Text style={{ ...typography.subtitle, color: colors.ink }}>{formatCurrency(tx.amount, tx.currency)}</Text>
        </View>
        <Badge label={tx.status} tone={tone as any} />
      </View>
      <View style={{ flexDirection: "row", justifyContent: "space-between", marginTop: spacing.xs }}>
        <Text style={{ color: colors.muted }}>{provider}</Text>
        <Text style={{ color: colors.muted }}>{formatDate(tx.createdAt)}</Text>
      </View>
      {showProofStatus && proofAnchored ? (
        <View style={{ alignSelf: "flex-start", marginTop: spacing.xs }}>
          <Badge label="Proof anchored" tone="success" />
        </View>
      ) : null}
      <View style={{ flexDirection: "row", justifyContent: "space-between", marginTop: spacing.xs, alignItems: "center" }}>
        <Text style={{ color: colors.muted }}>ID: {tx.id.slice(0, 8)}</Text>
        <Text onPress={copyId} style={{ color: colors.primary }}>Copy</Text>
      </View>
    </Pressable>
  );
}
