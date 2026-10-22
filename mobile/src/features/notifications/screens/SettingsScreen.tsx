import React, { useEffect, useState } from "react";
import { Alert, ScrollView, Text, View } from "react-native";
import * as Clipboard from "expo-clipboard";
import { Button } from "../../../components/Button";
import { Card } from "../../../components/Card";
import { Screen } from "../../../components/Screen";
import { colors, spacing, typography } from "../../../theme";
import { useAuth } from "../../auth/hooks/useAuth";
import { getDeviceId } from "../../../services/tokenStorage";
import { getExpoPushToken } from "../utils/expoPush";
import { registerExpoToken } from "../api/notificationApi";
import { Platform } from "react-native";

export function SettingsScreen() {
  const { user, signOut } = useAuth();
  const [deviceId, setDeviceId] = useState<string | null>(null);
  const [enabled, setEnabled] = useState(false);
  const [registered, setRegistered] = useState(false);

  useEffect(() => {
    register();
  }, []);

  useEffect(() => {
    getDeviceId().then(setDeviceId);
  }, []);

  async function copyDeviceId() {
    if (!deviceId) return;
    await Clipboard.setStringAsync(deviceId);
    Alert.alert("Copied", "Device ID copied.");
  }

  async function register() {
    const token = await getExpoPushToken();
    if (!token) {
      setEnabled(false);
      setRegistered(false);
      return;
    }
    setEnabled(true);
    await registerExpoToken(token, Platform.OS);
    setRegistered(true);
  }

  function InfoRow({ label, value }: { label: string; value?: string }) {
    return (
      <View style={{ flexDirection: "row", justifyContent: "space-between", marginTop: spacing.xs }}>
        <Text style={{ color: colors.muted }}>{label}</Text>
        <Text style={{ color: colors.ink, maxWidth: "60%", textAlign: "right" }}>{value || "-"}</Text>
      </View>
    );
  }

  return (
    <Screen>
      <View style={{ position: "absolute", top: -60, right: -40, width: 180, height: 180, borderRadius: 90, backgroundColor: colors.backgroundA }} />
      <View style={{ position: "absolute", bottom: -120, left: -60, width: 240, height: 240, borderRadius: 120, backgroundColor: colors.backgroundB }} />
      <ScrollView contentContainerStyle={{ paddingBottom: spacing.xl, paddingTop: spacing.lg }}>
        <View style={{ paddingHorizontal: spacing.md, marginBottom: spacing.md }}>
          <Text style={{ ...typography.title, color: colors.ink }}>Settings</Text>
          <Text style={{ color: colors.muted, marginTop: spacing.xs }}>Account and notifications.</Text>
        </View>
        <Card style={{ marginHorizontal: spacing.md, marginBottom: spacing.md }}>
          <Text style={{ ...typography.subtitle, color: colors.ink }}>Account</Text>
          <InfoRow label="Name" value={user?.displayName || user?.phone} />
          <InfoRow label="Email" value={user?.email || "-"} />
        </Card>
        <Card style={{ marginHorizontal: spacing.md, marginBottom: spacing.md }}>
          <Text style={{ ...typography.subtitle, color: colors.ink }}>Notifications</Text>
          <InfoRow label="Enabled" value={enabled ? "Yes" : "No"} />
          <InfoRow label="Token registered" value={registered ? "Yes" : "No"} />
          <View style={{ height: spacing.md }} />
          <Button label="Refresh Push Status" onPress={register} variant="ghost" />
        </Card>
        <Card style={{ marginHorizontal: spacing.md }}>
          <Text style={{ ...typography.subtitle, color: colors.ink }}>Security</Text>
          <Text style={{ color: colors.muted, marginTop: spacing.xs, marginBottom: spacing.md }}>
            Manage your session and access.
          </Text>
          <InfoRow label="Device ID" value={deviceId || "-"} />
          <View style={{ height: spacing.md }} />
          <Button label="Copy Device ID" onPress={copyDeviceId} variant="ghost" />
          <View style={{ height: spacing.md }} />
          <Button label="Logout" onPress={signOut} variant="ghost" />
        </Card>
      </ScrollView>
    </Screen>
  );
}
