import { defineStore } from 'pinia';
import type { IdentityType, MiniappIdentity, MiniappLoginResponse, MiniappMeResponse, UserInfo } from '@/types/api';

const STORAGE_KEY = 'edu-miniapp-auth';

interface AuthState {
  accessToken: string;
  refreshToken: string;
  userInfo?: UserInfo;
  identities: MiniappIdentity[];
  selectedIdentity?: MiniappIdentity;
  currentStudentId?: number;
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
    activeStudentId: (state) => state.currentStudentId,
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
      this.syncCurrentStudent();
      this.persist();
    },
    applyMe(response: MiniappMeResponse) {
      this.userInfo = response.userInfo;
      this.identities = response.availableIdentities || [];
      this.selectedIdentity = response.selectedIdentity;
      this.syncCurrentStudent();
      this.persist();
    },
    setSelectedIdentity(identity: MiniappIdentity) {
      this.selectedIdentity = identity;
      this.syncCurrentStudent();
      this.persist();
    },
    setCurrentStudent(studentId: number) {
      this.currentStudentId = studentId;
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
      this.currentStudentId = undefined;
      uni.removeStorageSync(STORAGE_KEY);
    },
    syncCurrentStudent() {
      if (this.selectedIdentity?.identityType === 'STUDENT') {
        this.currentStudentId = this.selectedIdentity.identityId;
        return;
      }
      if (this.selectedIdentity?.identityType === 'GUARDIAN') {
        const children = this.selectedIdentity.children || [];
        if (!children.some((child) => child.studentId === this.currentStudentId)) {
          this.currentStudentId = children[0]?.studentId;
        }
        return;
      }
      this.currentStudentId = undefined;
    },
    persist() {
      uni.setStorageSync(STORAGE_KEY, {
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        userInfo: this.userInfo,
        identities: this.identities,
        selectedIdentity: this.selectedIdentity,
        currentStudentId: this.currentStudentId,
      });
    },
  },
});
