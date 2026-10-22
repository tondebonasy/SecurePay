import {spacing} from "../theme";
import React from "react";
import { View } from "react-native";
import { Text, useTheme } from "react-native-paper";

export function EmptyState({ title, subtitle }: { title: string; subtitle?: string }) {
  const theme = useTheme();
  return (
    <View style={{ padding: spacing.lg, alignItems: "center" }}>
      <Text variant="titleMedium" style={{ color: theme.colors.onSurface }}>{title}</Text>
      {subtitle ? <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, marginTop: spacing.xs }}>{subtitle}</Text> : null}
    </View>
  );
}
