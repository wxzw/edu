<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { selectIdentity } from '@/api/miniapp';
import { useAuthStore } from '@/stores/auth';
import type { MiniappIdentity } from '@/types/api';
import { requireLogin } from '@/utils/auth-flow';
import { switchToHome } from '@/utils/routes';

const auth = useAuthStore();
const current = computed(() => auth.selectedIdentity);

onShow(() => {
  requireLogin();
});

function label(identity?: MiniappIdentity) {
  if (!identity) {
    return '';
  }
  return {
    TEACHER: '老师',
    STUDENT: '学生',
    GUARDIAN: '家长',
  }[identity.identityType];
}

async function switchIdentity(identity: MiniappIdentity) {
  try {
    const response = await selectIdentity(identity.identityType, identity.identityId);
    auth.applyMe(response);
    switchToHome(response.selectedIdentity.identityType);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '切换失败', icon: 'none' });
  }
}

function logout() {
  auth.clear();
  uni.reLaunch({ url: '/pages/login/index' });
}
</script>

<template>
  <view class="page">
    <view class="profile">
      <view class="avatar">{{ current?.displayName?.slice(0, 1) || '我' }}</view>
      <view class="profile-main">
        <text class="name">{{ current?.displayName }}</text>
        <text class="meta">{{ label(current) }} · 校区 {{ current?.campusId }}</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">身份</text>
      <button
        v-for="identity in auth.identities"
        :key="`${identity.identityType}-${identity.identityId}`"
        class="identity-row"
        :class="{ active: current?.identityType === identity.identityType && current?.identityId === identity.identityId }"
        @tap="switchIdentity(identity)"
      >
        <text class="identity-name">{{ identity.displayName }}</text>
        <text class="identity-type">{{ label(identity) }}</text>
      </button>
    </view>

    <view class="section">
      <text class="section-title">账号</text>
      <view class="info-row">
        <text class="info-label">用户名</text>
        <text class="info-value">{{ auth.userInfo?.username }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">手机号</text>
        <text class="info-value">{{ auth.userInfo?.phone || '-' }}</text>
      </view>
    </view>

    <button class="logout" @tap="logout">退出登录</button>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36rpx 30rpx 60rpx;
  background: #f6f1e8;
}

.profile {
  padding: 32rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  border-radius: 24rpx;
  background: #17211d;
  color: #fff;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 22rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 36rpx;
  font-weight: 900;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 34rpx;
  font-weight: 900;
}

.meta {
  display: block;
  margin-top: 10rpx;
  color: #d8e0d9;
  font-size: 24rpx;
}

.section {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 22rpx;
  background: #fffcf5;
}

.section-title {
  display: block;
  margin-bottom: 18rpx;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.identity-row,
.info-row {
  min-height: 82rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 2rpx solid #eee4d4;
  text-align: left;
}

.identity-row.active {
  color: #22624c;
}

.identity-name,
.info-label {
  color: #17211d;
  font-size: 27rpx;
  font-weight: 800;
}

.identity-type,
.info-value {
  color: #6f756f;
  font-size: 24rpx;
}

.logout {
  height: 90rpx;
  margin-top: 34rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background: #b64334;
  color: #fff;
  font-size: 29rpx;
  font-weight: 900;
}
</style>
