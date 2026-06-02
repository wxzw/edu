import { useAuthStore } from '@/stores/auth';
import type { IdentityType } from '@/types/api';
import { switchToHome } from '@/utils/routes';

export function requireLogin() {
  const auth = useAuthStore();
  auth.hydrate();
  if (!auth.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/index' });
    return false;
  }
  return true;
}

export function requireIdentity(identityType: IdentityType) {
  if (!requireLogin()) {
    return false;
  }
  const auth = useAuthStore();
  if (auth.selectedIdentity?.identityType !== identityType) {
    switchToHome(auth.selectedIdentity?.identityType);
    return false;
  }
  return true;
}
