<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getRegistrations } from '@/api/student';
import type { RegistrationItem } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const loading = ref(false);
const registrations = ref<RegistrationItem[]>([]);

onShow(() => {
  if (requireStudentAccess()) {
    loadRegistrations();
  }
});

async function loadRegistrations() {
  loading.value = true;
  try {
    registrations.value = await getRegistrations();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function openActivity(id: number) {
  uni.navigateTo({ url: `/pages/student/activity/detail?id=${id}` });
}

function formatTime(value: string) {
  return value?.slice(0, 16).replace('T', ' ') || '-';
}
</script>

<template>
  <view class="page">
    <view class="headline">
      <text class="caption">MY SIGNUPS</text>
      <text class="title">我的报名</text>
    </view>

    <view v-if="registrations.length" class="list">
      <view v-for="item in registrations" :key="item.id" class="registration-card" @tap="openActivity(item.activityId)">
        <view class="card-head">
          <text class="card-title">{{ item.activityTitle }}</text>
          <text class="status">{{ item.status }}</text>
        </view>
        <text class="line">{{ formatTime(item.startTime) }} · {{ item.location }}</text>
        <view class="meta-row">
          <text>{{ item.registrationNo }}</text>
          <text>￥{{ item.amount }} · {{ item.payStatus || 'FREE' }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">{{ loading ? '加载中...' : '暂无报名记录' }}</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 80rpx;
  background: #f6f1e8;
  color: #17211d;
}

.caption {
  display: block;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 900;
}

.title {
  display: block;
  margin-top: 8rpx;
  font-size: 44rpx;
  font-weight: 900;
}

.list {
  margin-top: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.registration-card {
  padding: 28rpx;
  border-radius: 22rpx;
  background: #fffcf5;
}

.card-head,
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.card-title {
  flex: 1;
  min-width: 0;
  font-size: 30rpx;
  font-weight: 900;
}

.status {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #285c7f;
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
}

.line,
.meta-row,
.empty {
  margin-top: 14rpx;
  color: #6f756f;
  font-size: 24rpx;
  line-height: 1.5;
}

.empty {
  margin-top: 90rpx;
  text-align: center;
}
</style>
