import { defineStore } from 'pinia';
import type { IdentityType, MiniappIdentity, MiniappLoginResponse, MiniappMeResponse, UserInfo } from '@/types/api';

const STORAGE_KEY = 'edu-miniapp-auth';

interface AuthState {
  accessToken: string;
  refreshToken: string;
  userInfo?: UserInfo;
  identities: MiniappIdentity[];
  selectedIdentity?: MiniappIdentity;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    accessToken: '',
    refreshToken: '',
    identities: [],
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.accessToken),
    displayName: (state) => state.selectedIdentity?.displayName || state.userInfo?.realName || '',
  },
  actions: {
    hydrate() {
      const cached = uni.getStorageSync(STORAGE_KEY) as AuthState | '';
      if (!cached) {
        return;
      }
      this.$patch(cached);
    },
    applyLogin(response: MiniappLoginResponse) {
      this.accessToken = response.accessToken;
      this.refreshToken = response.refreshToken;
      this.userInfo = response.userInfo;
      this.identities = response.availableIdentities || [];
      this.selectedIdentity = response.selectedIdentity;
      this.persist();
    },
    applyMe(response: MiniappMeResponse) {
      this.userInfo = response.userInfo;
      this.identities = response.availableIdentities || [];
      this.selectedIdentity = response.selectedIdentity;
      this.persist();
    },
    setSelectedIdentity(identity: MiniappIdentity) {
      this.selectedIdentity = identity;
      this.persist();
    },
    hasIdentity(identityType: IdentityType) {
      return this.identities.some((identity) => identity.identityType === identityType);
    },
    clear() {
      this.accessToken = '';
      this.refreshToken = '';
      this.userInfo = undefined;
      this.identities = [];
      this.selectedIdentity = undefined;
      uni.removeStorageSync(STORAGE_KEY);
    },
    persist() {
      uni.setStorageSync(STORAGE_KEY, {
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        userInfo: this.userInfo,
        identities: this.identities,
        selectedIdentity: this.selectedIdentity,
      });
    },
  },
});
