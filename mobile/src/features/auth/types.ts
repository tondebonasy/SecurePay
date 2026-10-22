export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
  sessionId: string;
};

export type AuthUser = {
  id: string;
  phone: string;
  email?: string;
  displayName?: string;
  roles: string[];
};
