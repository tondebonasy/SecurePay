import React from "react";
import { createNavigationContainerRef, NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { AuthNavigator } from "./AuthNavigator";
import { MainTabs } from "./MainTabs";
import { PaymentStatusScreen } from "../features/transactions/screens/PaymentStatusScreen";
import { TransactionDetailScreen } from "../features/transactions/screens/TransactionDetailScreen";
import { ProofViewerScreen } from "../features/proofs/screens/ProofViewerScreen";
import { useAuth } from "../features/auth/hooks/useAuth";
import { SafeAreaView } from "react-native-safe-area-context";
import { ActivityIndicator, Text, useTheme } from "react-native-paper";
import { NotificationBootstrap } from "../features/notifications/components/NotificationBootstrap";


const Stack = createNativeStackNavigator();
export const navigationRef = createNavigationContainerRef<any>();

export function AppNavigator() {
  const { loading, tokens } = useAuth();
  const theme = useTheme();

  if (loading) {
    return (
      <SafeAreaView style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: theme.colors.background }}>
        <ActivityIndicator animating={true} color={theme.colors.primary} size="large" />
        <Text style={{ marginTop: 10 }}>Loading application...</Text>
      </SafeAreaView>
    );
  }

  return (
    <NavigationContainer ref={navigationRef}>
      {tokens ? (
        <>
          <NotificationBootstrap />
          <Stack.Navigator screenOptions={{ headerShown: false }}>
            <Stack.Screen name="MainTabs" component={MainTabs} />
            <Stack.Screen name="PaymentStatus" component={PaymentStatusScreen} />
            <Stack.Screen name="TransactionDetail" component={TransactionDetailScreen} />
            <Stack.Screen name="ProofViewer" component={ProofViewerScreen} />
          </Stack.Navigator>
        </>
      ) : (
        <AuthNavigator />
      )}
    </NavigationContainer>
  );
}
