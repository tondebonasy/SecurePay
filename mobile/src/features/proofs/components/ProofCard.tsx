import React from "react";
import { Alert, Text, View } from "react-native";
import * as Clipboard from "expo-clipboard";
import * as WebBrowser from "expo-web-browser";
import { Button } from "../../../components/Button";
import { colors, spacing, typography } from "../../../theme";
import { Proof } from "../types";

export function ProofCard({ proof }: { proof: Proof }) {
  async function copy(value?: string | null) {
    if (!value) return;
    await Clipboard.setStringAsync(value);
    Alert.alert("Copied", "Value copied to clipboard.");
  }

  async function openStellarTx() {
    if (!proof.stellarTxHash || !/^[a-fA-F0-9]{64}$/.test(proof.stellarTxHash)) return;
    const url = `https://stellar.expert/explorer/testnet/tx/${proof.stellarTxHash}`;
    await WebBrowser.openBrowserAsync(url);
  }

  return (
    <View>
      <Text style={{ ...typography.subtitle, color: colors.ink }}>Proof Hash</Text>
      <Text style={{ ...typography.mono, color: colors.ink }}>{proof.proofHash}</Text>
      <Button label="Copy Proof Hash" onPress={() => copy(proof.proofHash)} variant="ghost" style={{ marginTop: spacing.sm }} />
      <Text style={{ ...typography.subtitle, color: colors.ink, marginTop: spacing.md }}>Stellar Tx Hash</Text>
      <Text style={{ ...typography.mono, color: colors.ink }}>{proof.stellarTxHash || "No chain reference"}</Text>
      <Button disabled={!proof.stellarTxHash} label="Copy Stellar Hash" onPress={() => copy(proof.stellarTxHash)} variant="ghost" style={{ marginTop: spacing.sm }} />
      <Button disabled={!proof.stellarTxHash || !/^[a-fA-F0-9]{64}$/.test(proof.stellarTxHash)} label="View on Stellar" onPress={openStellarTx} variant="secondary" style={{ marginTop: spacing.sm }} />
    </View>
  );
}
