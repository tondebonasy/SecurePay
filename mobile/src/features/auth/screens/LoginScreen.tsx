import React, { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Animated,
  ImageBackground,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Button, TextInput, Text, Surface } from "react-native-paper";
import { Ionicons } from "@expo/vector-icons";
import { useAuth } from "../hooks/useAuth";
import { isPassword, isPhone } from "../../../utils/validators";
import { parseApiError } from "../../../services/apiClient";
import { colors, spacing, typography } from "../../../theme";
import { BrandMark } from "../../../components/BrandMark";

const loginArt = require("../../../assets/securepay-login-art.png");

export function LoginScreen({ navigation }: any) {
  const { signIn } = useAuth();
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const fadeIn = useMemo(() => new Animated.Value(0), []);
  const slideUp = useMemo(() => new Animated.Value(18), []);

  useEffect(() => {
    Animated.parallel([
      Animated.timing(fadeIn, { toValue: 1, duration: 480, useNativeDriver: true }),
      Animated.spring(slideUp, { toValue: 0, damping: 18, stiffness: 120, useNativeDriver: true }),
    ]).start();
  }, [fadeIn, slideUp]);

  async function submit() {
    if (!isPhone(phone)) return Alert.alert("Invalid phone", "Enter a valid phone.");
    if (!isPassword(password)) return Alert.alert("Invalid password", "Password must be at least 8 characters.");
    setBusy(true);
    try {
      await signIn(phone, password);
    } catch (e) {
      const err = parseApiError(e);
      Alert.alert("Login failed", err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: colors.paper }}>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : "height"} style={{ flex: 1 }}>
        <ScrollView
          keyboardShouldPersistTaps="handled"
          contentContainerStyle={{ paddingBottom: spacing.xl }}
          showsVerticalScrollIndicator={false}
        >
          <Animated.View style={{ opacity: fadeIn, transform: [{ translateY: slideUp }] }}>
            <ImageBackground source={loginArt} resizeMode="cover" imageStyle={{ opacity: 0.98 }} style={{ height: 280 }}>
              <View style={{ flex: 1, justifyContent: "space-between", paddingHorizontal: spacing.md, paddingTop: spacing.sm, paddingBottom: spacing.lg, backgroundColor: "rgba(8, 20, 48, 0.08)" }}>
                <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "center" }}>
                  <View style={{ flexDirection: "row", alignItems: "center", gap: spacing.sm }}>
                    <BrandMark size={34} />
                    <Text style={{ ...typography.brand, color: colors.ink }}>SecurePay</Text>
                  </View>
                </View>
              </View>
            </ImageBackground>

            <View style={{ paddingHorizontal: spacing.md, marginTop: -28 }}>
              <Surface elevation={3} style={{ borderRadius: 24, padding: spacing.lg, backgroundColor: colors.card }}>
                <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginBottom: spacing.md }}>
                  <View>
                    <Text style={{ fontSize: 23, lineHeight: 28, fontFamily: "Manrope_600SemiBold", color: colors.ink }}>Welcome back</Text>
                    <Text style={{ ...typography.body, color: colors.muted, marginTop: 4 }}>Sign in to your workspace.</Text>
                  </View>
                  <View style={{ width: 38, height: 38, borderRadius: 12, alignItems: "center", justifyContent: "center", backgroundColor: "rgba(37,99,235,0.10)" }}>
                    <Ionicons name="arrow-forward" size={18} color={colors.primary} />
                  </View>
                </View>

                <TextInput
                  label="Phone number"
                  value={phone}
                  onChangeText={setPhone}
                  keyboardType="phone-pad"
                  autoCapitalize="none"
                  mode="outlined"
                  outlineColor={colors.border}
                  activeOutlineColor={colors.primary}
                  style={{ marginBottom: spacing.sm, backgroundColor: colors.card }}
                  left={<TextInput.Icon icon="phone-outline" color={colors.muted} />}
                />
                <TextInput
                  label="Password"
                  value={password}
                  onChangeText={setPassword}
                  secureTextEntry={!showPassword}
                  mode="outlined"
                  outlineColor={colors.border}
                  activeOutlineColor={colors.primary}
                  style={{ marginBottom: spacing.md, backgroundColor: colors.card }}
                  left={<TextInput.Icon icon="lock-outline" color={colors.muted} />}
                  right={<TextInput.Icon icon={showPassword ? "eye-off-outline" : "eye-outline"} onPress={() => setShowPassword((current) => !current)} />}
                />

                <Button
                  mode="contained"
                  onPress={submit}
                  disabled={busy}
                  loading={busy}
                  contentStyle={{ paddingVertical: 7 }}
                  labelStyle={{ fontSize: 14, fontFamily: "Manrope_600SemiBold" }}
                  style={{ borderRadius: 14 }}
                >
                  Sign in securely
                </Button>

                <View style={{ flexDirection: "row", justifyContent: "center", alignItems: "center", marginTop: spacing.md }}>
                  <Text style={{ ...typography.body, color: colors.muted }}>New to SecurePay?</Text>
                  <Button mode="text" compact onPress={() => navigation.navigate("Register")} labelStyle={{ fontSize: 13, fontFamily: "Manrope_600SemiBold" }}>
                    Create account
                  </Button>
                </View>
              </Surface>

              <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 6, marginTop: spacing.lg }}>
                <Ionicons name="shield-checkmark-outline" size={15} color={colors.muted} />
                <Text style={{ ...typography.small, color: colors.muted }}>Your session is encrypted end to end.</Text>
              </View>
            </View>
          </Animated.View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
