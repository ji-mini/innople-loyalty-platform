const TOKEN_KEY = 'innople.member.accessToken';

export function loadAccessToken(): string | null {
  try {
    if (typeof localStorage === 'undefined') {
      return null;
    }
    const token = localStorage.getItem(TOKEN_KEY);
    return token && token.trim().length > 0 ? token : null;
  } catch {
    return null;
  }
}

export function saveAccessToken(token: string): void {
  try {
    if (typeof localStorage === 'undefined') {
      return;
    }
    localStorage.setItem(TOKEN_KEY, token);
  } catch {
    // ignore storage errors
  }
}

export function clearAccessToken(): void {
  try {
    if (typeof localStorage === 'undefined') {
      return;
    }
    localStorage.removeItem(TOKEN_KEY);
  } catch {
    // ignore storage errors
  }
}
