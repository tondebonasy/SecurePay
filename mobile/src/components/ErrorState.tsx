import React from "react";
import { Text, View } from "react-native";
import { colors, spacing, typography } from "../theme";
import { Button } from "./Button";

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <View style={{ padding: spacing.lg, alignItems: "center" }}>
      <Text style={{ ...typography.subtitle, color: colors.danger }}>Something went wrong</Text>
      <Text style={{ color: colors.muted, marginTop: spacing.xs, textAlign: "center" }}>{message}</Text>
      {onRetry ? <Button label="Retry" onPress={onRetry} variant="ghost" style={{ marginTop: spacing.md, width: 160 }} /> : null}
    </View>
  );
}
