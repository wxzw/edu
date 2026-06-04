<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherHomeworks } from '@/api/teacher';
import type { TeacherHomeworkListItem } from '@/types/api';

const checkins = ref<TeacherHomeworkListItem[]>([]);
const loading = ref(false);

async function fetchCheckins() {
  loading.value = true;
  try {
    const res = await getTeacherHomeworks();
    checkins.value = res.filter(h => h.checkinEnabled);
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchCheckins();
});

function toProgress(id: number) {
  uni.navigateTo({ url: `/pages/teacher/checkin/progress?id=${id}` });
}

function formatDate(date?: string) {
  if (!date) return '';
  return date.substring(0, 10);
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Check-in</text>
      <text class="title">打卡任务</text>
    </view>

    <view class="panel">
      <view
        class="checkin-row"
        v-for="item in checkins"
        :key="item.id"
        @tap="toProgress(item.id)"
      >
        <view class="checkin-info">
          <text class="checkin-title">{{ item.title }}</text>
          <text class="checkin-class" v-if="item.className">{{ item.className }}</text>
          <text class="checkin-meta">
            截止 {{ formatDate(item.deadline) }}
            · {{ item.totalSubmissions || 0 }}人参与
          </text>
        </view>
        <view class="checkin-right">
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="empty-row" v-if="!checkins.length">
        <text class="empty-text">暂无打卡任务</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 60rpx;
  background: #f6f1e8;
}

.hero {
  padding: 34rpx;
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

.panel {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.checkin-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-top: 1px solid #F0EAE0;
}

.checkin-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.checkin-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #17211d;
}

.checkin-class {
  font-size: 24rpx;
  color: #666;
}

.checkin-meta {
  font-size: 22rpx;
  color: #999;
}

.checkin-right {
  display: flex;
  align-items: center;
}

.arrow {
  font-size: 32rpx;
  color: #AAA;
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
