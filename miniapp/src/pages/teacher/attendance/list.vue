<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTodaySchedulesForAttendance } from '@/api/teacher';
import type { AttendanceListItem } from '@/types/api';

const schedules = ref<AttendanceListItem[]>([]);
const loading = ref(false);

async function fetchSchedules() {
  loading.value = true;
  try {
    const res = await getTodaySchedulesForAttendance();
    schedules.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchSchedules();
});

function toAttendanceDetail(scheduleId: number) {
  uni.navigateTo({ url: `/pages/teacher/attendance/detail?scheduleId=${scheduleId}` });
}

function formatTime(time?: string) {
  if (!time) return '';
  return time.substring(0, 5);
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Attendance</text>
      <text class="title">今日考勤</text>
    </view>

    <view class="panel">
      <view
        class="schedule-row"
        v-for="item in schedules"
        :key="item.scheduleId"
        @tap="toAttendanceDetail(item.scheduleId)"
      >
        <view class="schedule-time">
          <text class="time-text">{{ formatTime(item.startTime) }}</text>
          <text class="time-end">{{ formatTime(item.endTime) }}</text>
        </view>
        <view class="schedule-info">
          <text class="schedule-topic">{{ item.topic }}</text>
          <text class="schedule-class">{{ item.className }}</text>
        </view>
        <view class="schedule-meta">
          <text class="meta-text">{{ item.studentCount || 0 }}人</text>
          <text class="meta-text" :class="{ 'meta-warn': (item.studentCount || 0) > (item.attendanceCount || 0) }">
            已考勤 {{ item.attendanceCount || 0 }}
          </text>
        </view>
      </view>
      <view class="empty-row" v-if="!schedules.length">
        <text class="empty-text">今日暂无课程</text>
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

.schedule-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 0;
  border-top: 1px solid #F0EAE0;
}

.schedule-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100rpx;
}

.time-text {
  font-size: 28rpx;
  font-weight: 800;
  color: #17211d;
}

.time-end {
  font-size: 22rpx;
  color: #AAA;
}

.schedule-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.schedule-topic {
  font-size: 28rpx;
  font-weight: 700;
  color: #17211d;
}

.schedule-class {
  font-size: 24rpx;
  color: #666;
}

.schedule-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.meta-text {
  font-size: 22rpx;
  color: #666;
}

.meta-warn {
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
