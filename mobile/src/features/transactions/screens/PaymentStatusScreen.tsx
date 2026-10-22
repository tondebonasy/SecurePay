import React, { useEffect, useMemo, useState } from "react";
import { Alert, ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import {
  Button,
  Card,
  Text,
  Appbar,
  ActivityIndicator,
  useTheme,
} from "react-native-paper";
import { Badge } from "../../../components/Badge";
import { StatusStepper } from "../components/StatusStepper";
import * as txApi from "../api/transactionApi";
import { Transaction } from "../types";
import { colors, spacing, typography } from "../../../theme";
import { formatCurrency, formatDate } from "../../../utils/format";
import { useToast } from "../../notifications/store/toastStore";

const terminal = new Set(["NOTIFIED", "FAILED", "PAYNOW_FAILED"]);

export function PaymentStatusScreen({ route, navigation }: any) {
  const { txId } = route.params;
  const [tx, setTx] = useState<Transaction | null>(null);
  const [busy, setBusy] = useState(false);
  const theme = useTheme();
  const { showToast } = useToast();
  const tone = tx?.status === "NOTIFIED" ? "success" : tx?.status === "FAILED" || tx?.status === "PAYNOW_FAILED" ? "danger" : "warning";
  const counterpartyPhone = tx?.payeePhone || tx?.paynowPhone || "-";
  const methodLabel = useMemo(() => {
    if (!tx) return "-";
    if (!tx.paynowReference) return "PayPal";
    const method = tx.paynowMethod?.toLowerCase();
    if (method === "ecocash") return "PayNow (EcoCash)";
    if (method === "onemoney") return "PayNow (OneMoney)";
    return "PayNow";
  }, [tx]);

  useEffect(() => {
    let active = true;
    let timer: any;
    const poll = async () => {
      try {
        let latest = await txApi.getTransaction(txId);
        if (latest.status === "PAYNOW_REQUESTED") {
          latest = await txApi.paynowPoll(txId);
        }
        if (!active) return;
        setTx(latest);
        if (!terminal.has(latest.status)) {
          timer = setTimeout(poll, 4000);
        }
      } catch {}
    };
    poll();
    return () => {
      active = false;
      if (timer) clearTimeout(timer);
    };
  }, [txId]);

  useEffect(() => {
    if (!tx || !terminal.has(tx.status)) return;
    if (tx.status === "NOTIFIED") {
      showToast("Payment verified.", "success", `${tx.id}:${tx.status}`);
      return;
    }
    showToast("Payment failed.", "danger", `${tx.id}:${tx.status}`);
  }, [showToast, tx?.id, tx?.status]);

  async function confirm() {
    setBusy(true);
    try {
      const updated = await txApi.confirm(txId);
      setTx(updated);
    } catch (e: any) {
      Alert.alert("Confirm failed", e?.response?.data?.error?.message || "Error");
    } finally {
      setBusy(false);
    }
  }

  async function tracePaynow() {
    if (!tx) return;
    setBusy(true);
    try {
      const updated = await txApi.paynowTrace(tx.id);
      setTx(updated);
    } finally {
      setBusy(false);
    }
  }

  const isPaynow = tx?.status?.startsWith("PAYNOW");
  const statusMessage = useMemo(() => {
    if (!tx) return "";
    switch (tx.status) {
      case "PAYNOW_REQUESTED":
        return tx.paynowPhone
          ? `We sent a PayNow prompt to ${tx.paynowPhone}. Complete it to continue.`
          : "We sent a PayNow prompt. Complete it to continue.";
      case "PAYNOW_PAID":
        return "Payment confirmed. Finalizing proof and notifications.";
      case "PAYNOW_FAILED":
        return "PayNow failed or was cancelled. Try again.";
      case "FAILED":
        return "Payment failed. Try again.";
      case "PAYPAL_ORDER_CREATED":
        return "PayPal order created. Complete PayPal checkout.";
      case "PAYPAL_CAPTURED":
        return "Payment captured. Finalizing proof.";
      case "PROOF_SUBMITTED":
        return "Proof submitted. Sending notification.";
      case "NOTIFIED":
        return "Payment verified and confirmed.";
      default:
        return "Processing your payment.";
    }
  }, [tx]);

  const etaHint = useMemo(() => {
    if (!tx) return "";
    if (tx.status === "PAYNOW_REQUESTED" || tx.status === "PAYPAL_ORDER_CREATED") return "Typically 15–60 seconds";
    if (tx.status === "PAYNOW_PAID" || tx.status === "PAYPAL_CAPTURED" || tx.status === "PROOF_SUBMITTED") return "Finishing in under a minute";
    return "";
  }, [tx]);
  function InfoRow({ label, value }: { label: string; value?: string }) {
    return (
      <View style={{ flexDirection: "row", justifyContent: "space-between", marginTop: spacing.xs }}>
        <Text style={{ color: colors.muted }}>{label}</Text>
        <Text style={{ color: colors.ink, maxWidth: "60%", textAlign: "right" }}>{value || "-"}</Text>
      </View>
    );
  }

  function formatParty(name?: string, phone?: string) {
    if (name && phone) return `${name} (${phone})`;
    return phone || name || "-";
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.background }}>
      <Appbar.Header>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="Payment Status" subtitle={`Transaction ${txId.slice(0, 8)}`} />
      </Appbar.Header>
      <ScrollView contentContainerStyle={{ paddingBottom: spacing.xl, paddingHorizontal: spacing.md }}>
        <View style={{ marginTop: spacing.md, marginBottom: spacing.md }}>
          <View style={{ padding: spacing.lg, borderRadius: 18, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border }}>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Payment status</Text>
            <Text style={{ color: colors.muted, marginTop: spacing.xs }}>Track confirmation and proof.</Text>
            {tx ? (
              <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginTop: spacing.sm }}>
                <Badge label={tx.status} tone={tone as any} />
                {etaHint ? <Text style={{ color: colors.muted }}>ETA: {etaHint}</Text> : null}
              </View>
            ) : null}
          </View>
        </View>
        <Card style={{ borderRadius: 18, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }}>
          <Card.Content>
            {!tx ? (
              <ActivityIndicator animating={true} color={theme.colors.primary} size="large" />
            ) : (
              <>
                <Card style={{ borderRadius: 14, borderWidth: 1, borderColor: colors.border, backgroundColor: "rgba(255,255,255,0.7)", marginBottom: spacing.md }}>
                  <Card.Content>
                    <Text style={{ ...typography.subtitle, color: colors.ink }}>Confirmation details</Text>
                    <InfoRow label="Amount" value={formatCurrency(tx.amount, tx.currency)} />
                    <InfoRow label="Method" value={methodLabel} />
                    <InfoRow label="Paid by" value={formatParty(tx.paidByName, tx.paidByPhone)} />
                    <InfoRow label="Date" value={formatDate(tx.updatedAt || tx.createdAt)} />
                  </Card.Content>
                </Card>
                <StatusStepper status={tx.status} />
                <View style={{ height: spacing.md }} />
                <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant }}>
                  Status: {tx.status}
                </Text>
                <View style={{ marginTop: spacing.sm }}>
                  <InfoRow label="Provider" value={isPaynow ? "PayNow" : "PayPal"} />
                  <InfoRow label="Currency" value={tx.currency} />
                  {isPaynow ? (
                    <>
                      <InfoRow label="Method" value={tx.paynowMethod || "-"} />
                      <InfoRow label="Phone" value={counterpartyPhone} />
                      <InfoRow label="Reference" value={tx.paynowReference || "-"} />
                    </>
                  ) : (
                    <>
                      <InfoRow label="Order ID" value={tx.paypalOrderId || "-"} />
                      <InfoRow label="Capture ID" value={tx.paypalCaptureId || "-"} />
                    </>
                  )}
                </View>
                {tx.status === "FAILED" ? (
                  <Button
                    mode="text"
                    onPress={confirm}
                    style={{ marginTop: spacing.md, alignSelf: "flex-start" }}
                  >
                    Retry Confirm
                  </Button>
                ) : null}
                {tx.status === "PAYPAL_ORDER_CREATED" ? (
                  <Button
                    mode="contained"
                    onPress={confirm}
                    disabled={busy}
                    loading={busy}
                    style={{ marginTop: spacing.md, alignSelf: "flex-start" }}
                  >
                    Confirm Payment
                  </Button>
                ) : null}
                {statusMessage ? (
                  <Text style={{ color: colors.muted, marginTop: spacing.md }}>
                    {statusMessage}
                  </Text>
                ) : null}
                {isPaynow ? (
                  <Button
                    mode="outlined"
                    onPress={tracePaynow}
                    disabled={busy}
                    loading={busy}
                    style={{ marginTop: spacing.md, alignSelf: "flex-start" }}
                  >
                    Refresh PayNow Status
                  </Button>
                ) : null}
                {terminal.has(tx.status) ? (
                  <Button
                    mode="outlined"
                    onPress={() => navigation.navigate("TransactionDetail", { txId })}
                    style={{ marginTop: spacing.md, alignSelf: "flex-start" }}
                  >
                    View Details
                  </Button>
                ) : null}
              </>
            )}
          </Card.Content>
        </Card>
      </ScrollView>
    </SafeAreaView>
  );
}
