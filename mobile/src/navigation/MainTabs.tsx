import React from "react";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { Ionicons } from "@expo/vector-icons";
import { HomeScreen } from "../features/transactions/screens/HomeScreen";
import { InitiatePaymentScreen } from "../features/transactions/screens/InitiatePaymentScreen";
import { RequestPaymentScreen } from "../features/transactions/screens/RequestPaymentScreen";
import { ScanPaymentRequestScreen } from "../features/transactions/screens/ScanPaymentRequestScreen";
import { TransactionHistoryScreen } from "../features/transactions/screens/TransactionHistoryScreen";
import { SettingsScreen } from "../features/notifications/screens/SettingsScreen";
import { colors } from "../theme";

const Tab = createBottomTabNavigator();

export function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.muted,
        tabBarStyle: {
          backgroundColor: colors.paper,
          borderTopColor: colors.border,
          height: 68,
          paddingTop: 6,
          paddingBottom: 8,
        },
        tabBarIcon: ({ color, size, focused }) => {
          const iconName =
            route.name === "Home"
              ? focused ? "home" : "home-outline"
              : route.name === "NewPayment"
              ? focused ? "card" : "card-outline"
              : route.name === "Request"
              ? focused ? "qr-code" : "qr-code-outline"
              : route.name === "Scan"
              ? focused ? "scan" : "scan-outline"
              : route.name === "History"
              ? focused ? "time" : "time-outline"
              : focused ? "settings" : "settings-outline";

          return <Ionicons name={iconName} size={size} color={color} />;
        },
      })}
    >
      <Tab.Screen name="Home" component={HomeScreen} />
      <Tab.Screen name="NewPayment" component={InitiatePaymentScreen} options={{ title: "New Payment" }} />
      <Tab.Screen name="Request" component={RequestPaymentScreen} options={{ title: "Request QR" }} />
      <Tab.Screen name="Scan" component={ScanPaymentRequestScreen} options={{ title: "Scan QR" }} />
      <Tab.Screen name="History" component={TransactionHistoryScreen} />
      <Tab.Screen name="Settings" component={SettingsScreen} />
    </Tab.Navigator>
  );
}
