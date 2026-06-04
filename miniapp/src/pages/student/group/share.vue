<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app';
import { getGroupRequestByShareCode, joinGroupRequest } from '@/api/student';
import { useAuthStore } from '@/stores/auth';
import type { GroupRequestDetail } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const auth = useAuthStore();
const shareCode = ref('');
const detail = ref<GroupRequestDetail>();
const joining = ref(false);

const progressPercent = computed(() => {
  if (!detail.value?.requiredMembers) {
    return 0;
  }
  return Math.min(100, detail.value.currentMembers / detail.value.requiredMembers * 100);
});

onLoad((query) => {
  auth.hydrate();
  shareCode.value = String(query?.shareCode || '');
  loadShare();
});

onShareAppMessage(() => ({
  title: `${detail.value?.targetSystem || '英语'}拼班邀请`,
  path: `/pages/student/group/share?shareCode=${shareCode.value}`,
}));

async function loadShare() {
  if (!shareCode.value) {
    uni.showToast({ title: '邀请链接无效', icon: 'none' });
    return;
  }
  try {
    detail.value = await getGroupRequestByShareCode(shareCode.value);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  }
}

async function join() {
  if (!detail.value) {
    return;
  }
  if (!auth.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' });
    return;
  }
  if (!requireStudentAccess()) {
    return;
  }
  joining.value = true;
  try {
    detail.value = await joinGroupRequest(detail.value.id);
    uni.showToast({ title: '已参与', icon: 'success' });
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '参与失败', icon: 'none' });
  } finally {
    joining.value = false;
  }
}

function statusLabel(status?: string) {
  return {
    FORMING: '拼班中',
    WAITING_CAMPUS: '等待校区安排',
    TRIAL_ARRANGED: '试听已安排',
    TRIAL_COMPLETED: '试听已完成',
    FAILED: '拼班失败',
    CANCELLED: '已取消',
  }[status || ''] || status || '';
}
</script>

<template>
  <view class="page">
    <view v-if="detail" class="invite">
      <text class="caption">CLASS INVITE</text>
      <text class="title">{{ detail.targetSystem }}</text>
      <text class="subtitle">{{ detail.grade }} · {{ detail.englishLevel }}</text>
      <view class="progress">
        <view class="progress-bar" :style="{ width: `${progressPercent}%` }" />
      </view>
      <text class="subtitle">{{ statusLabel(detail.status) }} · {{ detail.currentMembers }}/{{ detail.requiredMembers }} 人</text>
      <button class="join" :loading="joining" @tap="join">{{ auth.isLoggedIn ? '我要参与' : '登录后参与' }}</button>
    </view>

    <view v-if="detail" class="section">
      <text class="section-title">已加入成员</text>
      <view class="member-line">
        <text v-for="member in detail.members" :key="member.id" class="bubble">{{ member.nickname?.slice(0, 1) || '同' }}</text>
      </view>
    </view>

    <view v-else class="empty">加载中...</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 70rpx;
  background: #f6f1e8;
  color: #17211d;
}

.invite,
.section {
  padding: 32rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.invite {
  border: 2rpx solid #17211d;
}

.caption {
  display: block;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
}

.title {
  display: block;
  margin-top: 12rpx;
  font-size: 42rpx;
  font-weight: 900;
}

.subtitle,
.empty {
  display: block;
  margin-top: 10rpx;
  color: #909090;
  font-size: 23rpx;
  line-height: 1.5;
}

.progress {
  height: 14rpx;
  margin-top: 24rpx;
  border-radius: 999rpx;
  background: #E8E8E8;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: inherit;
  background: #22624c;
  transition: width 0.5s ease;
}

.join {
  height: 88rpx;
  margin-top: 28rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 29rpx;
  font-weight: 800;
}

.join:active { opacity: 0.85; }

.section {
  margin-top: 26rpx;
}

.section-title {
  display: block;
  margin-bottom: 18rpx;
  font-size: 29rpx;
  font-weight: 900;
}

.member-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.bubble {
  width: 58rpx;
  height: 58rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14rpx;
  background: #285c7f;
  color: #fff;
  font-size: 23rpx;
  font-weight: 900;
}
</style>
