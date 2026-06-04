<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getNotifications, readAllNotifications, readNotification } from '@/api/student';
import type { NotificationItem } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const loading = ref(false);
const activeStatus = ref<'ALL' | 'UNREAD'>('ALL');
const records = ref<NotificationItem[]>([]);
const unreadCount = ref(0);

const visibleRecords = computed(() => {
  if (activeStatus.value === 'UNREAD') {
    return records.value.filter((item) => item.status === 'UNREAD');
  }
  return records.value;
});

onShow(() => {
  if (requireStudentAccess()) {
    loadNotifications();
  }
});

async function loadNotifications() {
  loading.value = true;
  try {
    const page = await getNotifications(activeStatus.value === 'UNREAD' ? 'UNREAD' : undefined);
    records.value = page.records;
    unreadCount.value = page.unreadCount;
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

async function markRead(item: NotificationItem) {
  if (item.status !== 'UNREAD') return;
  await readNotification(item.id);
  await loadNotifications();
}

async function markAllRead() {
  await readAllNotifications();
  uni.showToast({ title: '已全部已读', icon: 'success' });
  await loadNotifications();
}

function switchStatus(status: 'ALL' | 'UNREAD') {
  activeStatus.value = status;
  loadNotifications();
}

function formatTime(value?: string) {
  return value?.slice(0, 16).replace('T', ' ') || '';
}
</script>

<template>
  <view class="page">
    <view class="headline">
      <view>
        <text class="caption">NOTIFICATIONS</text>
        <text class="title">通知中心</text>
      </view>
      <button class="read-all" @tap="markAllRead">全部已读</button>
    </view>

    <view class="tabs">
      <button class="tab" :class="{ active: activeStatus === 'ALL' }" @tap="switchStatus('ALL')">全部</button>
      <button class="tab" :class="{ active: activeStatus === 'UNREAD' }" @tap="switchStatus('UNREAD')">未读 {{ unreadCount }}</button>
    </view>

    <view v-if="visibleRecords.length" class="list">
      <view v-for="item in visibleRecords" :key="item.id" class="notice-card" :class="{ unread: item.status === 'UNREAD' }" @tap="markRead(item)">
        <view class="card-head">
          <text class="notice-title">{{ item.title }}</text>
          <text class="status">{{ item.status === 'UNREAD' ? '未读' : '已读' }}</text>
        </view>
        <text class="copy">{{ item.content || item.bizType }}</text>
        <text class="time">{{ formatTime(item.createdAt) }}</text>
      </view>
    </view>

    <view v-else class="empty">{{ loading ? '加载中...' : '暂无通知' }}</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 80rpx;
  background: #f6f1e8;
  color: #17211d;
}

.headline,
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
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

.read-all {
  height: 66rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  background: #22624c;
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
}

.tabs {
  margin-top: 24rpx;
  display: flex;
  gap: 12rpx;
}

.tab {
  height: 62rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  border: 2rpx solid #e0ddd6;
  background: transparent;
  color: #6f756f;
  font-size: 24rpx;
  font-weight: 900;
}

.tab.active {
  background: #17211d;
  color: #fff;
  border-color: #17211d;
}

.list {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.notice-card {
  padding: 26rpx;
  border-radius: 20rpx;
  background: #fffcf5;
}

.notice-card.unread {
  border-left: 8rpx solid #f0b84d;
}

.notice-title {
  flex: 1;
  min-width: 0;
  font-size: 29rpx;
  font-weight: 900;
}

.status {
  color: #22624c;
  font-size: 23rpx;
  font-weight: 900;
}

.copy,
.time,
.empty {
  display: block;
  margin-top: 12rpx;
  color: #6f756f;
  font-size: 24rpx;
  line-height: 1.5;
}

.time {
  color: #aaa;
  font-size: 22rpx;
}

.empty {
  margin-top: 90rpx;
  text-align: center;
}
</style>
