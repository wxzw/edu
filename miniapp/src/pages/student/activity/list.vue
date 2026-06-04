<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getActivities } from '@/api/student';
import type { ActivitySummary } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const loading = ref(false);
const activities = ref<ActivitySummary[]>([]);

onShow(() => {
  if (requireStudentAccess()) {
    loadActivities();
  }
});

async function loadActivities() {
  loading.value = true;
  try {
    activities.value = await getActivities();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function openActivity(id: number) {
  uni.navigateTo({ url: `/pages/student/activity/detail?id=${id}` });
}

function statusLabel(item: ActivitySummary) {
  if (item.registrationId) return '已报名';
  return { REGISTERING: '报名中', FULL: '已满员', ENDED: '已结束' }[item.activityStatus] || item.activityStatus;
}

function formatTime(value: string) {
  return value?.slice(5, 16).replace('T', ' ') || '';
}
</script>

<template>
  <view class="page">
    <view class="headline">
      <text class="caption">CAMPUS EVENTS</text>
      <text class="title">校区活动</text>
    </view>

    <view v-if="activities.length" class="list">
      <view v-for="item in activities" :key="item.id" class="activity-card" @tap="openActivity(item.id)">
        <view class="card-head">
          <text class="activity-title">{{ item.title }}</text>
          <text class="status">{{ statusLabel(item) }}</text>
        </view>
        <text class="copy">{{ item.description || item.location }}</text>
        <view class="meta-row">
          <text>{{ formatTime(item.startTime) }}</text>
          <text>￥{{ item.fee }} · {{ item.registeredCount }}/{{ item.quota || '不限' }}</text>
        </view>
      </view>
    </view>

    <view v-else class="empty">{{ loading ? '加载中...' : '暂无活动' }}</view>
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
  color: #285c7f;
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

.activity-card {
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

.activity-title {
  flex: 1;
  min-width: 0;
  font-size: 31rpx;
  font-weight: 900;
}

.status {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #22624c;
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
}

.copy,
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
