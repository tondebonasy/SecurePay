import React, { useEffect, useState } from "react";
import { Alert, View, Linking } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Appbar, Text, useTheme } from "react-native-paper";
import { CameraView, useCameraPermissions } from "expo-camera";
import * as WebBrowser from "expo-web-browser";
import { useIsFocused } from "@react-navigation/native";
import { Button } from "../../../components/Button";
import { Card } from "../../../components/Card";
import { consumePaymentRequest } from "../api/paymentRequestApi";
import { parseApiError } from "../../../services/apiClient";
import { colors, spacing, typography } from "../../../theme";

export function ScanPaymentRequestScreen({ navigation, route }: any) {
  const theme = useTheme();
  const [permission, requestPermission] = useCameraPermissions();
  const [busy, setBusy] = useState(false);
  const [scanned, setScanned] = useState(false);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const isFocused = useIsFocused();
  const qrLink = route?.params?.qrLink as string | undefined;


  async function handleScan(data: string) {
    if (busy || scanned) return;
    setScanned(true);
    const approved = await new Promise<boolean>(resolve => Alert.alert("Open payment request?", "This will load the payment request and may open the provider for approval. Check the recipient, currency and amount before authorizing payment.", [{text:"Cancel",style:"cancel",onPress:()=>resolve(false)},{text:"Continue",onPress:()=>resolve(true)}], {cancelable:false}));
    if (!approved) {setScanned(false);return;}
    setBusy(true);
    try {
      const tx = await consumePaymentRequest(data);
      if (tx.approveUrl) {
        await WebBrowser.openBrowserAsync(tx.approveUrl);
      }
      navigation.navigate("PaymentStatus", { txId: tx.id });
    } catch (e) {
      const err = parseApiError(e);
      Alert.alert("Scan failed", err.message);
      setScanned(false);
    } finally {
      setBusy(false);
    }
  }

  async function handleLinkPay() {
    if (!qrLink || busy) return;
    setBusy(true);
    try {
      const tx = await consumePaymentRequest(qrLink);
      if (tx.approveUrl) {
        await WebBrowser.openBrowserAsync(tx.approveUrl);
      }
      navigation.navigate("PaymentStatus", { txId: tx.id });
    } catch (e) {
      const err = parseApiError(e);
      Alert.alert("Request failed", err.message);
    } finally {
      setBusy(false);
    }
  }

  const permissionGranted = permission?.granted;
  const showCamera = permissionGranted && isFocused && !cameraError;

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.background }}>
      <Appbar.Header>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="Scan Request" subtitle="Pay using a single-use QR code." />
      </Appbar.Header>
      <View style={{ position: "absolute", top: -60, right: -40, width: 180, height: 180, borderRadius: 90, backgroundColor: colors.backgroundA }} />
      <View style={{ position: "absolute", bottom: -120, left: -60, width: 240, height: 240, borderRadius: 120, backgroundColor: colors.backgroundB }} />

      <View style={{ flex: 1, padding: spacing.md }}>
        {qrLink ? (
          <Card style={{ marginBottom: spacing.md }}>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Payment request received</Text>
            <Text style={{ color: colors.muted, marginTop: spacing.xs }}>
              Open this request to proceed, or scan the QR shown by the receiver.
            </Text>
            <Button label="Open Request" onPress={handleLinkPay} style={{ marginTop: spacing.md }} loading={busy} disabled={busy} />
          </Card>
        ) : null}
        {!permissionGranted ? (
          <Card>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Camera access needed</Text>
            <Text style={{ color: colors.muted, marginTop: spacing.xs }}>
              Enable camera permissions to scan payment requests.
            </Text>
            <Button label={permission?.canAskAgain === false ? "Open camera settings" : "Grant Permission"} onPress={() => permission?.canAskAgain === false ? Linking.openSettings() : requestPermission()} style={{ marginTop: spacing.md }} />
          </Card>
        ) : cameraError ? (
          <Card>
            <Text style={{ ...typography.subtitle, color: colors.ink }}>Camera unavailable</Text>
            <Text style={{ color: colors.muted, marginTop: spacing.xs }}>
              {cameraError}
            </Text>
            <Button
              label="Try Again"
              onPress={() => {
                setCameraError(null);

              }}
              style={{ marginTop: spacing.md }}
            />
          </Card>
        ) : (
          <>
            <View
              style={{
                flex: 1,
                borderRadius: 18,
                borderWidth: 1,
                borderColor: colors.border,
                backgroundColor: "#111111",
                overflow: "hidden",
              }}
            >
              {showCamera ? (
                <CameraView
                  style={{ flex: 1 }}
                  facing="back"
                  barcodeScannerSettings={{ barcodeTypes: ["qr"] }}
                  onMountError={(err) => setCameraError(err?.message || "Camera failed to start.")}
                  onBarcodeScanned={(res) => handleScan(res.data)}
                />
              ) : (
                <View style={{ flex: 1, backgroundColor: "#111111" }} />
              )}
            </View>
            <View style={{ height: spacing.md }} />
            <Card>
              <Text style={{ ...typography.subtitle, color: colors.ink }}>Scan a QR to pay</Text>
              <Text style={{ color: colors.muted, marginTop: spacing.xs }}>
                Align the code inside the frame to continue.
              </Text>
            </Card>
          </>
        )}
      </View>
    </SafeAreaView>
  );
}
