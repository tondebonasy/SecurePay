import React from "react";
import { StyleProp, ViewStyle } from "react-native";
import { Card as PaperCard } from "react-native-paper";
import { colors, spacing } from "../theme";

type Props = {
  children: React.ReactNode;
  style?: StyleProp<ViewStyle>;
  contentStyle?: StyleProp<ViewStyle>;
};

export function Card({ children, style, contentStyle }: Props) {
  return (
    <PaperCard
      style={[
        { borderRadius: 18, backgroundColor: colors.card, borderWidth: 1, borderColor: colors.border },
        style,
      ]}
    >
      <PaperCard.Content style={[{ padding: spacing.lg }, contentStyle]}>{children}</PaperCard.Content>
    </PaperCard>
  );
}
