import React from "react";
import { Text, View } from "react-native";
import { colors, spacing, typography } from "../theme";

export function Badge({ label, tone = "neutral" }: { label: string; tone?: "neutral" | "success" | "danger" | "warning" }) {
  const tones: Record<string, { bg: string; fg: string }> = {
    neutral: { bg: "rgba(12,17,24,0.08)", fg: colors.ink },
    success: { bg: "rgba(22,163,74,0.15)", fg: colors.success },
    danger: { bg: "rgba(179,38,30,0.15)", fg: colors.danger },
    warning: { bg: "rgba(245,158,11,0.2)", fg: colors.warning },
  };
  const t = tones[tone];
  return (
    <View style={{ backgroundColor: t.bg, paddingHorizontal: spacing.sm, paddingVertical: 4, borderRadius: 999 }}>
      <Text style={{ ...typography.small, color: t.fg, fontWeight: "600" }}>{label}</Text>
    </View>
  );
}
