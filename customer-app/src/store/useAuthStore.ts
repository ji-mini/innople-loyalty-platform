import { create } from 'zustand';

type AuthState = {
  accessToken: string | null;
  userName: string;
  isAuthenticated: boolean;
  loginAsDemo: () => void;
  logout: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  userName: '김이노',
  isAuthenticated: false,
  loginAsDemo: () =>
    set({
      accessToken: 'demo-customer-token',
      userName: '김이노',
      isAuthenticated: true,
    }),
  logout: () =>
    set({
      accessToken: null,
      userName: '김이노',
      isAuthenticated: false,
    }),
}));
