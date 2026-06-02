import { http } from './http';
import type { LoginRequest, LoginResponse, UserInfo } from '@/types/api';

export const loginApi = (payload: LoginRequest) => http.post<LoginResponse, LoginResponse>('/api/auth/login', payload);

export const refreshApi = (refreshToken: string) =>
  http.post<LoginResponse, LoginResponse>('/api/auth/refresh', { refreshToken });

export const logoutApi = (refreshToken?: string) => http.post<void, void>('/api/auth/logout', { refreshToken });

export const currentUserApi = () => http.get<UserInfo, UserInfo>('/api/auth/me');
