import { request } from '@/api/http';
import type {
  MiniappLoginRequest,
  MiniappLoginResponse,
  MiniappMeResponse,
  IdentityType,
} from '@/types/api';

export function miniappLogin(data: MiniappLoginRequest) {
  return request<MiniappLoginResponse>({
    url: '/api/miniapp/auth/login',
    method: 'POST',
    data,
  });
}

export function miniappMe() {
  return request<MiniappMeResponse>({
    url: '/api/miniapp/me',
    method: 'GET',
  });
}

export function selectIdentity(identityType: IdentityType, identityId: number) {
  return request<MiniappMeResponse>({
    url: '/api/miniapp/identity/select',
    method: 'POST',
    data: {
      identityType,
      identityId,
    },
  });
}
