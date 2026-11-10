import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { Me } from "../api/types";
import { fetchMe, hasToken, logout } from "./auth";

type AuthState = {
  me: Me | null;
  loading: boolean;
  refresh: () => Promise<void>;
  signOut: () => void;
};

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [me, setMe] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = async () => {
    if (!hasToken()) {
      setMe(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const m = await fetchMe();
      setMe(m);
    } catch {
      logout();
      setMe(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refresh();
  }, []);

  const signOut = () => {
    logout();
    setMe(null);
  };

  const value = useMemo(() => ({ me, loading, refresh, signOut }), [me, loading]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}


