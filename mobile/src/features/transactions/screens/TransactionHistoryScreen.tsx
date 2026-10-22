import React, { useMemo, useState } from "react";
import { ScrollView, Text, View } from "react-native";
import { Button } from "../../../components/Button";
import { Card } from "../../../components/Card";
import { EmptyState } from "../../../components/EmptyState";
import { Loading } from "../../../components/Loading";
import { Screen } from "../../../components/Screen";
import { colors, spacing, typography } from "../../../theme";
import { useTransactions } from "../hooks/useTransactions";
import { TransactionCard } from "../components/TransactionCard";

const filters = ["ALL", "SUCCESS", "FAILED", "PENDING"] as const;

export function TransactionHistoryScreen({ navigation }: any) {
  const { loading, error, items, refresh } = useTransactions();
  const [filter, setFilter] = useState<(typeof filters)[number]>("ALL");

  const list = useMemo(() => {
    if (filter === "ALL") return items;
    if (filter === "SUCCESS") return items.filter((t) => t.status === "NOTIFIED");
    if (filter === "FAILED") return items.filter((t) => t.status === "FAILED" || t.status === "PAYNOW_FAILED");
    return items.filter((t) => t.status !== "FAILED" && t.status !== "NOTIFIED" && t.status !== "PAYNOW_FAILED");
  }, [items, filter]);

  return (
    <Screen>
      <View style={{ position: "absolute", top: -60, right: -40, width: 180, height: 180, borderRadius: 90, backgroundColor: colors.backgroundA }} />
      <View style={{ position: "absolute", bottom: -120, left: -60, width: 240, height: 240, borderRadius: 120, backgroundColor: colors.backgroundB }} />
      <View style={{ position: "absolute", top: 88, left: 0, right: 0, backgroundColor: colors.paper, paddingHorizontal: spacing.md, paddingBottom: spacing.sm, zIndex: 2 }}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: spacing.sm }}>
          {filters.map((f) => {
            const active = filter === f;
            const count =
              f === "ALL"
                ? items.length
                : f === "SUCCESS"
                ? items.filter((t) => t.status === "NOTIFIED").length
                : f === "FAILED"
                ? items.filter((t) => t.status === "FAILED" || t.status === "PAYNOW_FAILED").length
                : items.filter((t) => t.status !== "FAILED" && t.status !== "NOTIFIED" && t.status !== "PAYNOW_FAILED").length;
            return (
              <Button
                key={f}
                label={`${f} ${count}`}
                variant={active ? "primary" : "ghost"}
                onPress={() => setFilter(f)}
                style={{
                  borderRadius: 999,
                  borderWidth: 1,
                  borderColor: colors.border,
                  backgroundColor: active ? colors.primary : "rgba(255,255,255,0.65)",
                }}
                contentStyle={{ paddingVertical: 4, paddingHorizontal: 8 }}
                labelStyle={{ color: active ? "#ffffff" : colors.ink, fontSize: 12 }}
              />
            );
          })}
        </ScrollView>
      </View>
      <ScrollView contentContainerStyle={{ paddingBottom: spacing.xl, paddingTop: spacing.lg }}>
        <View style={{ paddingHorizontal: spacing.md, marginBottom: spacing.md }}>
          <Text style={{ ...typography.title, color: colors.ink }}>History</Text>
          <Text style={{ color: colors.muted, marginTop: spacing.xs }}>All your verified payments.</Text>
        </View>
        <View style={{ height: 56 }} />
        <Card style={{ marginHorizontal: spacing.md }}>
          {loading ? <Loading /> : null}
          {error ? <Button label="Retry" variant="ghost" onPress={refresh} /> : null}
          {!loading && list.length === 0 ? (
            <EmptyState title="No transactions" subtitle="Create a payment to see it here." />
          ) : null}
          {list.map((tx) => (
            <TransactionCard
              key={tx.id}
              tx={tx}
              showProofStatus
              onPress={() => navigation.navigate("TransactionDetail", { txId: tx.id })}
            />
          ))}
        </Card>
      </ScrollView>
    </Screen>
  );
}
