import "./global.css";
import "react-native-gesture-handler";
import React from "react";
import { ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useFonts } from "expo-font";
import { Fraunces_600SemiBold, Fraunces_700Bold } from "@expo-google-fonts/fraunces";
import { Manrope_400Regular, Manrope_500Medium, Manrope_600SemiBold } from "@expo-google-fonts/manrope";
import { AuthProvider } from "./src/features/auth/store/authStore";
import { AppNavigator } from "./src/navigation/AppNavigator";
import { Provider as PaperProvider } from "react-native-paper";
import { colors, theme } from "./src/theme";
import { ToastProvider } from "./src/features/notifications/store/toastStore";

export default function App() {
  const [fontsLoaded] = useFonts({
    Fraunces_600SemiBold,
    Fraunces_700Bold,
    Manrope_400Regular,
    Manrope_500Medium,
    Manrope_600SemiBold,
  });

  if (!fontsLoaded) {
    return (
      <SafeAreaView style={{ flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: colors.paper }}>
        <ActivityIndicator size="small" color={colors.primary} />
      </SafeAreaView>
    );
  }

  return (
    <AuthProvider>
      <PaperProvider theme={theme}>
        <ToastProvider>
          <AppNavigator />
        </ToastProvider>
      </PaperProvider>
    </AuthProvider>
  );
}

