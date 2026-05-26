import { create } from 'zustand';

import type { AuthMember } from '../api/auth';
import { login as loginApi, signup as signupApi } from '../api/auth';
import { setAccessToken } from '../api/client';
import { clearAccessToken, loadAccessToken, saveAccessToken } from '../utils/tokenStorage';

type AuthState = {
  accessToken: string | null;
  member: AuthMember | null;
  userName: string;
  isAuthenticated: boolean;
  isHydrated: boolean;
  hydrate: () => Promise<void>;
  login: (phoneNumber: string, password: string) => Promise<void>;
  signup: (payload: {
    name: string;
    email?: string;
    password: string;
    phoneNumber: string;
  }) => Promise<void>;
  logout: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: loadAccessToken(),
  member: null,
  userName: '',
  isAuthenticated: false,
  isHydrated: false,
  hydrate: async () => {
    const token = loadAccessToken();
    setAccessToken(token);
    set({
      accessToken: token,
      isAuthenticated: Boolean(token),
      isHydrated: true,
    });
  },
  login: async (phoneNumber, password) => {
    const response = await loginApi(phoneNumber, password);
    saveAccessToken(response.accessToken);
    setAccessToken(response.accessToken);
    set({
      accessToken: response.accessToken,
      member: response.member,
      userName: response.member.name,
      isAuthenticated: true,
    });
  },
  signup: async (payload) => {
    const response = await signupApi(payload);
    saveAccessToken(response.accessToken);
    setAccessToken(response.accessToken);
    set({
      accessToken: response.accessToken,
      member: response.member,
      userName: response.member.name,
      isAuthenticated: true,
    });
  },
  logout: () => {
    clearAccessToken();
    setAccessToken(null);
    set({
      accessToken: null,
      member: null,
      userName: '',
      isAuthenticated: false,
    });
  },
}));
