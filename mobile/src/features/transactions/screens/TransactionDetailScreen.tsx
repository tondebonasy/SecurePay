import React, { useEffect, useMemo, useState } from "react";
import { Alert, Linking, ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import * as Clipboard from "expo-clipboard";
import {
  Button,
  Card,
  Text,
  Appbar,
  ActivityIndicator,
  useTheme,
} from "react-native-paper";
import { Badge } from "../../../components/Badge";
import * as txApi from "../api/transactionApi";
import * as proofApi from "../../proofs/api/proofApi";
import { Transaction } from "../types";
import { Proof } from "../../proofs/types";
import { formatCurrency, formatDate } from "../../../utils/format";
import { colors, spacing, typography } from "../../../theme";

export function TransactionDetailScreen({ route, navigation }: any) {
  const { txId } = route.params;
  const [tx, setTx] = useState<Transaction | null>(null);
  const [proof, setProof] = useState<Proof | null>(null);
  const [verifying, setVerifying] = useState(false);
  const [verification, setVerification] = useState<{ verified: boolean; reason?: string } | null>(null);
  const [loading, setLoading] = useState(true);
  const theme = useTheme();
  const tone = tx?.status === "NOTIFIED" ? "success" : tx?.status === "FAILED" ? "danger" : "warning";
  const provider = tx?.paynowReference ? "PayNow" : "PayPal";
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
    (async () => {
      try {
        const t = await txApi.getTransaction(txId);
        setTx(t);
        try {
          const p = await proofApi.getProof(txId);
          setProof(p);
        } catch {
          setProof(null);
        }
      } finally {
        setLoading(false);
      }
    })();
  }, [txId]);

  async function copy(value?: string) {
    if (!value) return;
    await Clipboard.setStringAsync(value);
    Alert.alert("Copied", "Value copied to clipboard.");
  }

  async function verifyProof() {
    if (!proof) return;
    setVerifying(true);
    try {
      const result = await proofApi.verifyProof(txId);
      setVerification({ verified: result.verified, reason: result.reason });
    } finally {
      setVerifying(false);
    }
  }

  function openStellarTx() {
    if (!proof?.stellarTxHash) return;
    const url = `https://stellar.expert/explorer/testnet/tx/${proof.stellarTxHash}`;
    Linking.openURL(url);
  }

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

  if (loading || !tx) {
    return (
      <SafeAreaView style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: theme.colors.background }}>
        <ActivityIndicator animating={true} color={theme.colors.primary} size="large" />
        <Text style={{ marginTop: 10 }}>Loading transaction details...</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.background }}>
      <Appbar.Header>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="Transaction Detail" subtitle={`ID ${tx.id.slice(0, 8)}`} />
      </Appbar.Header>
      <ScrollView contentContainerStyle={{ paddingBottom: spacing.xl, paddingHorizontal: spacing.md }}>
        <View style={{ marginTop: spacing.md, marginBottom: spacing.md }}>
          <View style={{ padding: spacing.lg, borderRadius: 18, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border }}>
            <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "center" }}>
              <Text style={{ ...typography.title, color: colors.ink }}>{formatCurrency(tx.amount, tx.currency)}</Text>
              <Badge label={tx.status} tone={tone as any} />
            </View>
            <Text style={{ color: colors.muted, marginTop: spacing.xs }}>Created {formatDate(tx.createdAt)}</Text>
          </View>
        </View>
        <Card style={{ marginBottom: spacing.md, borderRadius: 18, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }}>
          <Card.Content>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Payment details</Text>
            <InfoRow label="Provider" value={provider} />
            <InfoRow label="Method" value={methodLabel} />
            <InfoRow label="Paid by" value={formatParty(tx.paidByName, tx.paidByPhone)} />
            <InfoRow label="Transaction ID" value={tx.id.slice(0, 12)} />
            <InfoRow label="Currency" value={tx.currency} />
            {tx.paynowReference ? (
              <>
                <InfoRow label="Reference" value={tx.paynowReference} />
                <InfoRow label="Phone" value={counterpartyPhone} />
              </>
            ) : (
              <>
                <InfoRow label="Order ID" value={tx.paypalOrderId || "-"} />
                <InfoRow label="Capture ID" value={tx.paypalCaptureId || "-"} />
                <Button
                  mode="outlined"
                  onPress={() => copy(tx.paypalOrderId)}
                  style={{ marginTop: spacing.sm, alignSelf: "flex-start" }}
                >
                  Copy Order ID
                </Button>
              </>
            )}
          </Card.Content>
        </Card>
        <Card style={{ borderRadius: 18, borderWidth: 1, borderColor: colors.border, backgroundColor: colors.card }}>
          <Card.Content>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Proof</Text>
            {proof ? (
              <>
                <InfoRow label="Proof hash" value={proof.proofHash} />
                <InfoRow label="Stellar tx" value={proof.stellarTxHash ?? undefined} />
                <View style={{ flexDirection: "row", gap: spacing.sm, marginTop: spacing.sm }}>
                  <Button
                    mode="contained"
                    onPress={() => navigation.navigate("ProofViewer", { txId })}
                  >
                    View Proof
                  </Button>
                  <Button
                    mode="outlined"
                    onPress={verifyProof}
                    disabled={verifying}
                    loading={verifying}
                  >
                    Verify Proof
                  </Button>
                </View>
                {verification ? (
                  <View style={{ marginTop: spacing.sm }}>
                    <View style={{ flexDirection: "row", alignItems: "center", gap: spacing.sm }}>
                      <Badge label={verification.verified ? "Verified" : "Not verified"} tone={verification.verified ? "success" : "danger"} />
                      {verification.verified ? (
                        <Text style={{ color: theme.colors.primary }} onPress={openStellarTx}>
                          Verified on Stellar
                        </Text>
                      ) : null}
                    </View>
                    {verification.reason ? (
                      <Text style={{ color: colors.muted, marginTop: spacing.xs }}>{verification.reason}</Text>
                    ) : null}
                  </View>
                ) : null}
              </>
            ) : (
              <Text style={{ color: colors.muted, marginTop: spacing.xs }}>No proof submitted yet.</Text>
            )}
          </Card.Content>
        </Card>
      </ScrollView>
    </SafeAreaView>
  );
}
