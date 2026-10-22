import React, { useState } from "react";
import { Alert, ImageBackground, KeyboardAvoidingView, Platform, ScrollView, StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Button, Surface, Text, TextInput } from "react-native-paper";
import { BrandMark } from "../../../components/BrandMark";
import { useAuth } from "../hooks/useAuth";
import { isPassword, isPhone } from "../../../utils/validators";
import { parseApiError } from "../../../services/apiClient";
import { colors, spacing, typography } from "../../../theme";

export function RegisterScreen({ navigation }: { navigation: { goBack: () => void } }) {
  const { signUp } = useAuth();
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);

  async function submit() {
    if (!isPhone(phone)) return Alert.alert("Invalid phone", "Enter a valid phone number.");
    if (!isPassword(password)) return Alert.alert("Invalid password", "Password must be at least 8 characters.");
    setBusy(true);
    try { await signUp(phone, email, displayName, password); }
    catch (error) { Alert.alert("Registration failed", parseApiError(error).message); }
    finally { setBusy(false); }
  }

  return (
    <ImageBackground source={require("../../../assets/securepay-login-art.png")} style={styles.background} resizeMode="cover">
      <View style={styles.scrim} />
      <SafeAreaView style={styles.safe}>
        <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : "height"} style={styles.safe}>
          <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
            <View style={styles.brand}><BrandMark size={34} /><Text style={styles.brandName}>SecurePay</Text></View>
            <Surface elevation={3} style={styles.card}>
              <Text style={styles.eyebrow}>Workspace access</Text>
              <Text style={styles.title}>Create your account</Text>
              <Text style={styles.subtitle}>Set up access to your payment operations workspace.</Text>
              <TextInput label="Phone number" value={phone} onChangeText={setPhone} keyboardType="phone-pad" autoCapitalize="none" mode="outlined" style={styles.input} />
              <TextInput label="Email address" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" mode="outlined" style={styles.input} />
              <TextInput label="Name" value={displayName} onChangeText={setDisplayName} mode="outlined" style={styles.input} />
              <TextInput label="Password" value={password} onChangeText={setPassword} secureTextEntry={!showPassword} mode="outlined" style={styles.input} right={<TextInput.Icon icon={showPassword ? "eye-off" : "eye"} onPress={() => setShowPassword(value => !value)} />} />
              <Button mode="contained" onPress={submit} disabled={busy} loading={busy} contentStyle={styles.buttonContent} style={styles.button}>Create account</Button>
              <Button mode="text" onPress={navigation.goBack} style={styles.back}>Back to sign in</Button>
            </Surface>
          </ScrollView>
        </KeyboardAvoidingView>
      </SafeAreaView>
    </ImageBackground>
  );
}

const styles = StyleSheet.create({
  background: { flex: 1, backgroundColor: colors.background },
  scrim: { ...StyleSheet.absoluteFillObject, backgroundColor: "rgba(247,244,238,0.84)" },
  safe: { flex: 1 },
  content: { flexGrow: 1, justifyContent: "center", padding: spacing.lg },
  brand: { flexDirection: "row", alignItems: "center", gap: spacing.sm, marginBottom: spacing.lg },
  brandName: { ...typography.brand, color: colors.ink },
  card: { padding: spacing.lg, borderRadius: 18, backgroundColor: colors.card },
  eyebrow: { ...typography.small, color: colors.primary, textTransform: "uppercase", letterSpacing: 1.2, marginBottom: spacing.xs },
  title: { ...typography.title, color: colors.ink },
  subtitle: { color: colors.muted, lineHeight: 21, marginTop: spacing.xs, marginBottom: spacing.lg },
  input: { marginBottom: spacing.sm, backgroundColor: colors.card },
  button: { marginTop: spacing.sm },
  buttonContent: { minHeight: 48 },
  back: { marginTop: spacing.xs },
});
