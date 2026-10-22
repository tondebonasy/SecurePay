import {spacing} from "../theme";
import React from "react";
import { View } from "react-native";
import { ActivityIndicator, Text, useTheme } from "react-native-paper";

export function Loading({ label = "Loading..." }: { label?: string }) {
  const theme = useTheme();
  return (
    <View style={{ alignItems: "center", justifyContent: "center", padding: spacing.lg }}>
      <ActivityIndicator animating={true} color={theme.colors.primary} />
      <Text style={{ marginTop: spacing.sm, color: theme.colors.onSurfaceVariant }}>{label}</Text>
    </View>
  );
}
