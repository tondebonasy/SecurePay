import React, { useState } from "react";
import { Alert, ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Text, Appbar, TextInput, useTheme } from "react-native-paper";
import { Button } from "../../../components/Button";
import { Card } from "../../../components/Card";
import * as txApi from "../api/transactionApi";
import { parseApiError } from "../../../services/apiClient";
import { colors, spacing, typography } from "../../../theme";

export function InitiatePaymentScreen({ navigation }: any) {
  const [amount, setAmount] = useState("10.00");
  const [currency, setCurrency] = useState("USD");
  const [description, setDescription] = useState("");
  const [paynowMethod, setPaynowMethod] = useState<"ecocash" | "onemoney" | null>(null);
  const [paynowPhone, setPaynowPhone] = useState("");
  const [payeePhone, setPayeePhone] = useState("");
  const [paynowBusy, setPaynowBusy] = useState(false);
  const theme = useTheme();
  const inputStyle = { marginBottom: spacing.sm, backgroundColor: "rgba(255,255,255,0.92)" };
  const amountNumber = Number(amount);
  const amountValid = !Number.isNaN(amountNumber) && amountNumber > 0;

  function selectCurrency(value: string) {
    setCurrency(value);
  }

  async function payNowSubmit() {
    if (!paynowMethod) {
      Alert.alert("PayNow", "Select EcoCash or OneMoney.");
      return;
    }
    if (!paynowPhone.trim()) {
      Alert.alert("PayNow", "Enter the mobile wallet phone number.");
      return;
    }
    if (!payeePhone.trim()) {
      Alert.alert("PayNow", "Enter the receiving user's phone number.");
      return;
    }
    setPaynowBusy(true);
    try {
      const tx = await txApi.paynowRemote(
        Number(amount),
        currency,
        description,
        paynowPhone.trim(),
        paynowMethod,
        payeePhone.trim()
      );
      navigation.navigate("PaymentStatus", { txId: tx.id });
    } catch (e) {
      const err = parseApiError(e);
      Alert.alert("PayNow failed", err.message);
    } finally {
      setPaynowBusy(false);
    }
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.background }}>
      <Appbar.Header>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="New Payment" subtitle="Initiate a PayNow payment." />
      </Appbar.Header>
      <View style={{ position: "absolute", top: -60, right: -40, width: 180, height: 180, borderRadius: 90, backgroundColor: colors.backgroundA }} />
      <View style={{ position: "absolute", bottom: -120, left: -60, width: 240, height: 240, borderRadius: 120, backgroundColor: colors.backgroundB }} />
      <ScrollView contentContainerStyle={{ paddingBottom: spacing.xl, paddingHorizontal: spacing.md, paddingTop: spacing.md }}>
        <View style={{ marginBottom: spacing.md }}>
          <View style={{ padding: spacing.lg, borderRadius: 18, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border }}>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Create a payment</Text>
            <Text style={{ color: colors.muted, marginTop: spacing.xs }}>
              Send a PayNow prompt and track status.
            </Text>
          </View>
        </View>
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
            {amountValid ? "Enter the amount in your selected currency." : "Amount must be greater than 0."}
          </Text>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Currency</Text>
          <View style={{ flexDirection: "row", gap: spacing.sm, marginBottom: spacing.sm }}>
            {["USD", "ZIG"].map((c) => {
              const active = currency === c;
              return (
                <Button
                  key={c}
                  label={c}
                  onPress={() => selectCurrency(c)}
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
          <Text style={{ color: colors.muted, marginBottom: spacing.sm }}>Choose the currency you will charge.</Text>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Description (optional)</Text>
          <TextInput
            label="Description"
            value={description}
            onChangeText={setDescription}
            mode="outlined"
            outlineColor={colors.border}
            activeOutlineColor={colors.primary}
            style={inputStyle}
          />
          <Text style={{ color: colors.muted, marginBottom: spacing.sm }}>Shown to the payer on checkout.</Text>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Receiving user phone</Text>
          <TextInput
            label="e.g. 0771234567"
            value={payeePhone}
            onChangeText={setPayeePhone}
            keyboardType="phone-pad"
            mode="outlined"
            outlineColor={colors.border}
            activeOutlineColor={colors.primary}
            style={inputStyle}
          />
          <Text style={{ color: colors.muted, marginBottom: spacing.sm }}>
            Use the registered phone of the person getting paid so the transaction appears on their device.
          </Text>

          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>PayNow method</Text>
          <View style={{ flexDirection: "row", gap: spacing.sm, marginBottom: spacing.sm }}>
            <Button
              label="EcoCash"
              onPress={() => setPaynowMethod("ecocash")}
              variant={paynowMethod === "ecocash" ? "primary" : "secondary"}
              style={{ marginBottom: spacing.sm }}
            />
            <Button
              label="OneMoney"
              onPress={() => setPaynowMethod("onemoney")}
              variant={paynowMethod === "onemoney" ? "primary" : "secondary"}
            />
          </View>
          <Text style={{ color: colors.muted, marginBottom: spacing.xs }}>Wallet phone number</Text>
          <TextInput
            label="e.g. 0771234567"
            value={paynowPhone}
            onChangeText={setPaynowPhone}
            keyboardType="phone-pad"
            mode="outlined"
            outlineColor={colors.border}
            activeOutlineColor={colors.primary}
            style={inputStyle}
          />
          <Text style={{ color: colors.muted, marginBottom: spacing.sm }}>
            Use the wallet number registered for EcoCash/OneMoney.
          </Text>
          <Button
            label="Send PayNow Prompt"
            onPress={payNowSubmit}
            disabled={paynowBusy || !amountValid}
            loading={paynowBusy}
            style={{ marginTop: spacing.sm }}
          />
        </Card>
        <Card>
          <Text style={{ ...typography.subtitle, color: colors.ink }}>Tip</Text>
          <Text style={{ color: colors.muted, marginTop: spacing.xs }}>
            Keep amounts small while testing wallet prompts.
          </Text>
        </Card>
      </ScrollView>
    </SafeAreaView>
  );
}
