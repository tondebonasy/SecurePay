import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { loadAuth, saveAuth, clearAuth as clearStoredAuth, StoredAuth } from "../../../services/tokenStorage";
import { setAuthHandlers } from "../../../services/apiClient";
import * as authApi from "../api/authApi";
import { AuthTokens, AuthUser } from "../types";

type AuthState = {
  loading: boolean;
  tokens: AuthTokens | null;
  user: AuthUser | null;
};

type AuthContextValue = AuthState & {
  signIn: (phone: string, password: string) => Promise<void>;
  signUp: (phone: string, email: string, displayName: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  refreshTokens: (auth: StoredAuth) => Promise<StoredAuth | null>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({ loading: true, tokens: null, user: null });

  useEffect(() => {
    (async () => {
      const stored = await loadAuth();
      if (stored) {
        try {
          const user = await authApi.me();
          setState({ loading: false, tokens: stored, user });
        } catch {
          await clearStoredAuth();
          setState({ loading: false, tokens: null, user: null });
        }
      } else {
        setState({ loading: false, tokens: null, user: null });
      }
    })();
  }, []);

  useEffect(() => {
    setAuthHandlers(refreshTokens, () => {
      setState({ loading: false, tokens: null, user: null });
    });
  }, []);

  async function signIn(phone: string, password: string) {
    const tokens = await authApi.login(phone, password);
    await saveAuth(tokens);
    const user = await authApi.me();
    setState({ loading: false, tokens, user });
  }

  async function signUp(phone: string, email: string, displayName: string, password: string) {
    const tokens = await authApi.register(phone, email, displayName, password);
    await saveAuth(tokens);
    const user = await authApi.me();
    setState({ loading: false, tokens, user });
  }

  async function signOut() {
    await clearStoredAuth();
    setState({ loading: false, tokens: null, user: null });
  }

  async function refreshTokens(auth: StoredAuth): Promise<StoredAuth | null> {
    try {
      const tokens = await authApi.refresh(auth.sessionId, auth.refreshToken);
      return tokens;
    } catch {
      return null;
    }
  }

  const value = useMemo<AuthContextValue>(() => ({
    ...state,
    signIn,
    signUp,
    signOut,
    refreshTokens,
  }), [state]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
