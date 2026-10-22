import React from "react";
import { Appbar } from "react-native-paper";

type Props = {
  title: string;
  subtitle?: string;
  actions?: React.ReactNode;
};

export function Header({ title, subtitle, actions }: Props) {
  return (
    <Appbar.Header>
      <Appbar.Content title={title} subtitle={subtitle} />
      {actions}
    </Appbar.Header>
  );
}

