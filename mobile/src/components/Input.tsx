import React from "react";
import { ViewStyle } from "react-native";
import { TextInput, TextInputProps } from "react-native-paper";

type Props = {
  label: string;
  style?: ViewStyle;
} & TextInputProps;

export function Input({ label, style, ...rest }: Props) {
  return (
    <TextInput
      label={label}
      mode="outlined"
      style={style}
      {...rest}
    />
  );
}
