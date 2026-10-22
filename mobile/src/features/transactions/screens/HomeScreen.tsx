import React, { useMemo } from "react";
import { ImageBackground, Pressable, ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Button, Card, Text, Surface, useTheme } from "react-native-paper";
import { Ionicons } from "@expo/vector-icons";
import { EmptyState } from "../../../components/EmptyState";
import { Loading } from "../../../components/Loading";
import { useTransactions } from "../hooks/useTransactions";
import { TransactionCard } from "../components/TransactionCard";
import { colors, spacing, typography } from "../../../theme";
import { formatCurrency } from "../../../utils/format";

const dashboardArt = require("../../../assets/securepay-login-art.png");

export function HomeScreen({ navigation }: any) {
  const { loading, error, items, refresh } = useTransactions();
  const recent = items.slice(0, 3);
  const theme = useTheme();
  const stats = useMemo(() => {
    const total = items.length;
    const success = items.filter((t) => t.status === "NOTIFIED").length;
    const failed = items.filter((t) => t.status === "FAILED" || t.status === "PAYNOW_FAILED").length;
    return { total, success, failed };
  }, [items]);
  const latestProof = useMemo(() => items.find((t) => t.status === "NOTIFIED" || t.status === "PROOF_SUBMITTED"), [items]);

  const quickActions = [
    { label: "New payment", icon: "arrow-up-outline" as const, onPress: () => navigation.navigate("NewPayment") },
    { label: "Request", icon: "qr-code-outline" as const, onPress: () => navigation.navigate("Request") },
    { label: "Scan", icon: "scan-outline" as const, onPress: () => navigation.navigate("Scan") },
  ];

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: colors.paper }}>
      <ScrollView contentContainerStyle={{ paddingBottom: spacing.xl }} showsVerticalScrollIndicator={false}>
        <ImageBackground source={dashboardArt} resizeMode="cover" imageStyle={{ opacity: 0.96 }} style={{ height: 220 }}>
          <View style={{ flex: 1, justifyContent: "space-between", paddingHorizontal: spacing.md, paddingTop: spacing.sm, paddingBottom: spacing.lg, backgroundColor: "rgba(8,20,48,0.08)" }}>
            <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between" }}>
              <View>
                <Text style={{ ...typography.brand, color: colors.ink }}>SecurePay</Text>
              </View>
            </View>
          </View>
        </ImageBackground>

        <View style={{ paddingHorizontal: spacing.md, marginTop: -28 }}>
          <Surface elevation={3} style={{ borderRadius: 22, padding: spacing.md, backgroundColor: colors.card }}>
            <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginBottom: spacing.sm }}>
              <View>
                <Text style={{ ...typography.subtitle, color: colors.ink }}>Overview</Text>
              </View>
              <View style={{ width: 34, height: 34, borderRadius: 11, alignItems: "center", justifyContent: "center", backgroundColor: "rgba(37,99,235,0.10)" }}>
                <Ionicons name="pulse-outline" size={18} color={colors.primary} />
              </View>
            </View>
            <View style={{ flexDirection: "row", gap: spacing.sm }}>
              {[
                { label: "All activity", value: stats.total, color: colors.ink },
                { label: "Completed", value: stats.success, color: colors.success },
                { label: "Needs review", value: stats.failed, color: colors.danger },
              ].map((stat) => (
                <View key={stat.label} style={{ flex: 1, padding: spacing.sm, borderRadius: 14, backgroundColor: colors.paper }}>
                  <Text style={{ ...typography.small, color: colors.muted }} numberOfLines={1}>{stat.label}</Text>
                  <Text style={{ fontSize: 22, lineHeight: 28, fontFamily: "Manrope_600SemiBold", color: stat.color, marginTop: 5 }}>{stat.value}</Text>
                </View>
              ))}
            </View>
          </Surface>

          <View style={{ marginTop: spacing.lg }}>
            <View style={{ flexDirection: "row", alignItems: "flex-end", justifyContent: "space-between", marginBottom: spacing.sm }}>
              <View>
                <Text style={{ ...typography.title, color: colors.ink }}>Quick actions</Text>
              </View>
              <Pressable onPress={() => navigation.navigate("History")} accessibilityRole="button" accessibilityLabel="View transaction history">
                <Text style={{ ...typography.small, color: colors.primary, fontWeight: "700" }}>View history</Text>
              </Pressable>
            </View>
            <View style={{ flexDirection: "row", gap: spacing.sm }}>
              {quickActions.map((action) => (
                <Pressable key={action.label} onPress={action.onPress} style={({ pressed }) => ({ flex: 1, minHeight: 92, borderRadius: 16, padding: spacing.sm, backgroundColor: pressed ? "rgba(37,99,235,0.16)" : "rgba(37,99,235,0.10)", justifyContent: "space-between" })}>
                  <Ionicons name={action.icon} size={21} color={colors.primary} />
                  <Text style={{ ...typography.small, color: colors.ink, fontWeight: "700" }}>{action.label}</Text>
                </Pressable>
              ))}
            </View>
          </View>

          {latestProof ? (
            <Surface elevation={1} style={{ marginTop: spacing.lg, padding: spacing.md, borderRadius: 18, backgroundColor: colors.ink }}>
              <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between" }}>
                <View style={{ flex: 1 }}>
                  <Text style={{ ...typography.small, color: "rgba(255,255,255,0.64)" }}>Latest transaction</Text>
                  <Text style={{ ...typography.subtitle, color: "#fff", marginTop: 4 }}>Transaction {latestProof.id.slice(0, 8)}</Text>
                  <Text style={{ ...typography.body, color: "rgba(255,255,255,0.72)", marginTop: 2 }}>{formatCurrency(latestProof.amount, latestProof.currency)}</Text>
                </View>
                <Button mode="outlined" textColor="#fff" onPress={() => navigation.navigate("TransactionDetail", { txId: latestProof.id })} style={{ borderColor: "rgba(255,255,255,0.34)" }}>
                  Open
                </Button>
              </View>
            </Surface>
          ) : null}

          <Card style={{ marginTop: spacing.lg, borderRadius: 20, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }}>
            <View style={{ flexDirection: "row", alignItems: "flex-end", justifyContent: "space-between", marginBottom: spacing.sm }}>
              <View>
                <Text variant="titleMedium" style={{ color: theme.colors.onSurface }}>Recent activity</Text>
                <Text style={{ ...typography.small, color: colors.muted, marginTop: 3 }}>Recent records.</Text>
              </View>
              {error ? <Button compact mode="text" onPress={refresh}>Retry</Button> : null}
            </View>
            {loading ? <Loading /> : null}
            {!loading && !error && recent.length === 0 ? <EmptyState title="No transactions yet" subtitle="Create your first payment to see proof." /> : null}
            {recent.map((tx) => <TransactionCard key={tx.id} tx={tx} onPress={() => navigation.navigate("TransactionDetail", { txId: tx.id })} />)}
          </Card>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
