<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherHomeworkDetail } from '@/api/teacher';
import type { TeacherHomeworkDetail } from '@/types/api';

const homework = ref<TeacherHomeworkDetail | null>(null);
const loading = ref(false);

async function fetchDetail(id: number) {
  loading.value = true;
  try {
    const res = await getTeacherHomeworkDetail(id);
    homework.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const id = options?.id ? parseInt(options.id, 10) : 0;
  if (id) fetchDetail(id);
});

function formatDate(date?: string) {
  if (!date) return '';
  return date.substring(0, 10);
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Check-in Progress</text>
      <text class="title">{{ homework?.title }}</text>
    </view>

    <view class="panel">
      <text class="panel-title">
        打卡进度 ({{ homework?.submissions?.length || 0 }})
      </text>
      <view
        class="sub-row"
        v-for="item in homework?.submissions"
        :key="item.id"
      >
        <view class="sub-info">
          <text class="sub-name">{{ item.studentName }}</text>
          <text class="sub-time" v-if="item.submittedAt">
            最近打卡 {{ formatDate(item.submittedAt) }}
          </text>
        </view>
        <view class="sub-right">
          <text class="sub-status" :class="item.status">{{ item.status }}</text>
        </view>
      </view>
      <view class="empty-row" v-if="!homework?.submissions?.length">
        <text class="empty-text">暂无打卡记录</text>
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

.panel-title {
  display: block;
  margin-bottom: 18rpx;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.sub-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-top: 1px solid #F0EAE0;
}

.sub-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.sub-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #17211d;
}

.sub-time {
  font-size: 22rpx;
  color: #999;
}

.sub-right {
  display: flex;
  align-items: center;
}

.sub-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.sub-status.SUBMITTED {
  background: #fff3e0;
  color: #e6a23c;
}

.sub-status.COMMENTED {
  background: #e8f5e9;
  color: #22624c;
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
