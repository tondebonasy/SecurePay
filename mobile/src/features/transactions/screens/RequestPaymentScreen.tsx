import React, { useState } from "react";
import { Alert, ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Appbar, Text, TextInput, useTheme } from "react-native-paper";
import QRCode from "react-native-qrcode-svg";
import * as Clipboard from "expo-clipboard";
import { Button } from "../../../components/Button";
import { Card } from "../../../components/Card";
import { createPaymentRequest } from "../api/paymentRequestApi";
import { parseApiError } from "../../../services/apiClient";
import { colors, spacing, typography } from "../../../theme";
import { PaymentRequest } from "../types";

const ttlOptions = [5, 15, 60];

export function RequestPaymentScreen({ navigation }: any) {
  const [amount, setAmount] = useState("10.00");
  const [currency, setCurrency] = useState("USD");
  const [note, setNote] = useState("");
  const [payerPhone, setPayerPhone] = useState("");
  const provider: "PAYNOW" = "PAYNOW";
  const [paynowMethod, setPaynowMethod] = useState<"ecocash" | "onemoney" | null>(null);
  const [ttlMinutes, setTtlMinutes] = useState(15);
  const [busy, setBusy] = useState(false);
  const [request, setRequest] = useState<PaymentRequest | null>(null);
  const theme = useTheme();
  const inputStyle = { marginBottom: spacing.sm, backgroundColor: "rgba(255,255,255,0.92)" };
  const amountNumber = Number(amount);
  const amountValid = !Number.isNaN(amountNumber) && amountNumber > 0;
  const qrLink = request ? `securepay://pay?qr=${encodeURIComponent(request.qrPayload)}` : "";

  async function submit() {
    if (!paynowMethod) {
      Alert.alert("Missing method", "Select EcoCash or OneMoney.");
      return;
    }
    if (!payerPhone.trim()) {
      Alert.alert("Missing phone", "Enter the payer phone for PayNow.");
      return;
    }
    setBusy(true);
    try {
      const payerValue = payerPhone.trim();
      const res = await createPaymentRequest(
        amountNumber,
        currency,
        note.trim() || undefined,
        ttlMinutes,
        payerValue || undefined,
        provider,
        paynowMethod || undefined,
        provider === "PAYNOW" ? payerValue || undefined : undefined
      );
      setRequest(res);
    } catch (e) {
      const err = parseApiError(e);
      Alert.alert("Request failed", err.message);
    } finally {
      setBusy(false);
    }
  }

  async function copyQr() {
    if (!request) return;
    await Clipboard.setStringAsync(qrLink);
    Alert.alert("Copied", "Payment link copied to clipboard.");
  }

  function reset() {
    setRequest(null);
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.background }}>
      <Appbar.Header>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="Request Payment" subtitle="Generate a secure QR request." />
      </Appbar.Header>
      <View style={{ position: "absolute", top: -60, right: -40, width: 180, height: 180, borderRadius: 90, backgroundColor: colors.backgroundA }} />
      <View style={{ position: "absolute", bottom: -120, left: -60, width: 240, height: 240, borderRadius: 120, backgroundColor: colors.backgroundB }} />
      <ScrollView contentContainerStyle={{ paddingBottom: spacing.xl, paddingHorizontal: spacing.md, paddingTop: spacing.md }}>
        <Card style={{ marginBottom: spacing.md }}>
          <Text style={{ ...typography.subtitle, color: colors.ink, marginBottom: spacing.sm }}>Payment details</Text>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Amount</Text>
          <TextInput
            label="Amount"
            value={amount}
            onChangeText={setAmount}
            keyboardType="decimal-pad"
            mode="outlined"
            outlineColor={colors.border}
            activeOutlineColor={colors.primary}
            style={inputStyle}
          />
          <Text style={{ color: amountValid ? colors.muted : colors.danger, marginBottom: spacing.sm }}>
            {amountValid ? "Enter the amount to request." : "Amount must be greater than 0."}
          </Text>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Currency</Text>
          <View style={{ flexDirection: "row", gap: spacing.sm, marginBottom: spacing.sm }}>
            {["USD", "ZIG"].map((c) => {
              const active = currency === c;
              return (
                <Button
                  key={c}
                  label={c}
                  onPress={() => setCurrency(c)}
                  variant={active ? "primary" : "ghost"}
                  style={{
                    borderRadius: 999,
                    borderWidth: 1,
                    borderColor: colors.border,
                    backgroundColor: active ? colors.primary : "rgba(255,255,255,0.65)",
                  }}
                  contentStyle={{ paddingVertical: 4, paddingHorizontal: 10 }}
                  labelStyle={{ color: active ? "#ffffff" : colors.ink, fontSize: 12 }}
                />
              );
            })}
          </View>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Provider</Text>
          <Text style={{ color: colors.ink, marginBottom: spacing.sm }}>PayNow</Text>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>PayNow method</Text>
          <View style={{ flexDirection: "row", gap: spacing.sm, marginBottom: spacing.sm }}>
            {(["ecocash", "onemoney"] as const).map((m) => {
              const active = paynowMethod === m;
              return (
                <Button
                  key={m}
                  label={m === "ecocash" ? "EcoCash" : "OneMoney"}
                  onPress={() => setPaynowMethod(m)}
                  variant={active ? "primary" : "ghost"}
                  style={{
                    borderRadius: 999,
                    borderWidth: 1,
                    borderColor: colors.border,
                    backgroundColor: active ? colors.primary : "rgba(255,255,255,0.65)",
                  }}
                  contentStyle={{ paddingVertical: 4, paddingHorizontal: 10 }}
                  labelStyle={{ color: active ? "#ffffff" : colors.ink, fontSize: 12 }}
                />
              );
            })}
          </View>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Note (optional)</Text>
          <TextInput
            label="Note"
            value={note}
            onChangeText={setNote}
            mode="outlined"
            outlineColor={colors.border}
            activeOutlineColor={colors.primary}
            style={inputStyle}
          />
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>
            Payer phone (required)
          </Text>
          <TextInput
            label="Payer phone"
            value={payerPhone}
            onChangeText={setPayerPhone}
            keyboardType="phone-pad"
            mode="outlined"
            outlineColor={colors.border}
            activeOutlineColor={colors.primary}
            style={inputStyle}
          />
          <Text style={{ color: colors.muted, marginBottom: spacing.sm }}>
            The phone must be registered so we can notify the payer.
          </Text>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Expiry</Text>
          <View style={{ flexDirection: "row", gap: spacing.sm, marginBottom: spacing.sm }}>
            {ttlOptions.map((ttl) => {
              const active = ttlMinutes === ttl;
              return (
                <Button
                  key={ttl}
                  label={`${ttl}m`}
                  onPress={() => setTtlMinutes(ttl)}
                  variant={active ? "primary" : "ghost"}
                  style={{
                    borderRadius: 999,
                    borderWidth: 1,
                    borderColor: colors.border,
                    backgroundColor: active ? colors.primary : "rgba(255,255,255,0.65)",
                  }}
                  contentStyle={{ paddingVertical: 4, paddingHorizontal: 10 }}
                  labelStyle={{ color: active ? "#ffffff" : colors.ink, fontSize: 12 }}
                />
              );
            })}
          </View>
          <Text style={{ color: colors.muted, marginBottom: spacing.sm }}>
            Requests expire automatically after the selected time.
          </Text>
          <Button label={payerPhone.trim() ? "Generate & Notify" : "Generate QR Request"} onPress={submit} disabled={busy || !amountValid} loading={busy} style={{ marginTop: spacing.sm }} />
        </Card>

        {request ? (
          <Card style={{ marginBottom: spacing.md }}>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Share this QR code</Text>
            <Text style={{ color: colors.muted, marginTop: spacing.xs }}>
              Single-use request ID {request.id.slice(0, 8)}. Expires {request.expiresAt ? new Date(request.expiresAt).toLocaleString() : "soon"}.
            </Text>
            <View style={{ alignItems: "center", marginTop: spacing.md, marginBottom: spacing.md }}>
              <View style={{ padding: spacing.sm, backgroundColor: "#ffffff", borderRadius: 12 }}>
                <QRCode value={qrLink} size={220} color="#111111" backgroundColor="#ffffff" />
              </View>
            </View>
            <Button label="Copy Payment Link" onPress={copyQr} />
            <View style={{ height: spacing.sm }} />
            <Button label="Create Another" variant="ghost" onPress={reset} />
          </Card>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}
