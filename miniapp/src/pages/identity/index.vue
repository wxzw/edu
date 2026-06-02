<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { selectIdentity } from '@/api/miniapp';
import { useAuthStore } from '@/stores/auth';
import type { MiniappIdentity } from '@/types/api';
import { switchToHome } from '@/utils/routes';
import { requireLogin } from '@/utils/auth-flow';

const auth = useAuthStore();

const identities = computed(() => auth.identities);

onShow(() => {
  requireLogin();
});

function roleLabel(identity: MiniappIdentity) {
  const labels = {
    TEACHER: '老师',
    STUDENT: '学生',
    GUARDIAN: '家长',
  };
  return labels[identity.identityType];
}

async function choose(identity: MiniappIdentity) {
  try {
    const response = await selectIdentity(identity.identityType, identity.identityId);
    auth.applyMe(response);
    switchToHome(response.selectedIdentity.identityType);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '切换失败', icon: 'none' });
  }
}
</script>

<template>
  <view class="page">
    <view class="topline">
      <text class="title">选择今日身份</text>
      <text class="subtitle">{{ auth.userInfo?.realName }}</text>
    </view>

    <view class="identity-list">
      <button v-for="identity in identities" :key="`${identity.identityType}-${identity.identityId}`" class="identity-card" @tap="choose(identity)">
        <view class="avatar">{{ roleLabel(identity).slice(0, 1) }}</view>
        <view class="content">
          <view class="row">
            <text class="name">{{ identity.displayName }}</text>
            <text class="badge">{{ roleLabel(identity) }}</text>
          </view>
          <text class="meta">校区 {{ identity.campusId }}</text>
          <text v-if="identity.children?.length" class="meta">绑定学生 {{ identity.children.length }} 人</text>
        </view>
      </button>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 30rpx;
  background: #f6f1e8;
}

.topline {
  padding: 18rpx 4rpx 28rpx;
}

.title {
  display: block;
  color: #17211d;
  font-size: 44rpx;
  font-weight: 900;
  letter-spacing: 0;
}

.subtitle {
  display: block;
  margin-top: 10rpx;
  color: #6f756f;
  font-size: 26rpx;
}

.identity-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.identity-card {
  min-height: 168rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  border: 2rpx solid #17211d;
  border-radius: 22rpx;
  background: #fffcf5;
  box-shadow: 7rpx 7rpx 0 #17211d;
  text-align: left;
}

.avatar {
  width: 92rpx;
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 34rpx;
  font-weight: 900;
}

.content {
  flex: 1;
  min-width: 0;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.name {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.badge {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #22624c;
  color: #fff;
  font-size: 21rpx;
  font-weight: 800;
}

.meta {
  display: block;
  margin-top: 10rpx;
  color: #6f756f;
  font-size: 24rpx;
}
</style>
