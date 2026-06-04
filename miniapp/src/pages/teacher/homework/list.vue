<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherHomeworks } from '@/api/teacher';
import type { TeacherHomeworkListItem } from '@/types/api';
import AppTabBar from '@/components/AppTabBar.vue';

const homeworks = ref<TeacherHomeworkListItem[]>([]);
const loading = ref(false);

async function fetchHomeworks() {
  loading.value = true;
  try {
    const res = await getTeacherHomeworks();
    homeworks.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchHomeworks();
});

function toCreate() {
  uni.navigateTo({ url: '/pages/teacher/homework/create' });
}

function toDetail(id: number) {
  uni.navigateTo({ url: `/pages/teacher/homework/detail?id=${id}` });
}

function formatDate(date?: string) {
  if (!date) return '';
  return date.substring(0, 10);
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <view>
        <text class="eyebrow">Homework</text>
        <text class="title">作业管理</text>
      </view>
      <button class="round-button" @tap="toCreate">+ 发布</button>
    </view>

    <view class="panel">
      <view
        class="hw-row"
        v-for="item in homeworks"
        :key="item.id"
        @tap="toDetail(item.id)"
      >
        <view class="hw-info">
          <text class="hw-title">{{ item.title }}</text>
          <text class="hw-class" v-if="item.className">{{ item.className }}</text>
          <text class="hw-meta">
            截止 {{ formatDate(item.deadline) }}
            <text v-if="item.checkinEnabled"> · 打卡</text>
          </text>
        </view>
        <view class="hw-right">
          <text class="hw-status" :class="item.status">{{ item.status === 'PUBLISHED' ? '已发布' : '草稿' }}</text>
          <text class="hw-num" v-if="item.pendingSubmissions">待点评 {{ item.pendingSubmissions }}</text>
        </view>
      </view>
      <view class="empty-row" v-if="!homeworks.length">
        <text class="empty-text">暂无作业</text>
      </view>
    </view>
    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 160rpx;
  background: #f6f1e8;
}

.hero {
  padding: 34rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #1B3A2D 0%, #2D6A4F 100%);
  color: #fff;
}

.eyebrow {
  display: block;
  color: #f0b84d;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
}

.title {
  display: block;
  margin-top: 14rpx;
  font-size: 40rpx;
  font-weight: 900;
}

.round-button {
  width: 140rpx;
  height: 64rpx;
  border-radius: 999rpx;
  background: rgba(255,255,255,0.2);
  color: #fff;
  font-size: 24rpx;
  font-weight: 800;
}

.panel {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.hw-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24rpx 0;
  border-top: 1px solid #F0EAE0;
}

.hw-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.hw-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #17211d;
}

.hw-class {
  font-size: 24rpx;
  color: #666;
}

.hw-meta {
  font-size: 22rpx;
  color: #999;
}

.hw-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.hw-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.hw-status.PUBLISHED {
  background: #e8f5e9;
  color: #22624c;
}

.hw-status.DRAFT {
  background: #f5f5f5;
  color: #999;
}

.hw-num {
  font-size: 22rpx;
  color: #e6a23c;
  font-weight: 700;
}

.empty-row {
  padding: 40rpx 0;
  display: flex;
  justify-content: center;
}

.empty-text {
  font-size: 26rpx;
  color: #999;
}
</style>
