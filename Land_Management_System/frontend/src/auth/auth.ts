import { api } from "../api/client";
import type { Me } from "../api/types";

export async function login(username: string, password: string): Promise<void> {
  const res = await api.post("/auth/login", { username, password });
  const token = res.data?.access_token as string | undefined;
  if (!token) throw new Error("No token returned");
  localStorage.setItem("access_token", token);
}

export async function fetchMe(): Promise<Me> {
  const res = await api.get<Me>("/me");
  return res.data;
}

export function logout(): void {
  localStorage.removeItem("access_token");
}

export function hasToken(): boolean {
  return Boolean(localStorage.getItem("access_token"));
}


