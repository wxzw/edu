<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTodaySchedulesForAttendance } from '@/api/teacher';
import type { AttendanceListItem } from '@/types/api';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

const schedules = ref<AttendanceListItem[]>([]);
const loading = ref(false);

const pendingCount = computed(() => schedules.value.filter((item) => (item.studentCount || 0) > (item.attendanceCount || 0)).length);
const studentTotal = computed(() => schedules.value.reduce((sum, item) => sum + (item.studentCount || 0), 0));
const handledTotal = computed(() => schedules.value.reduce((sum, item) => sum + (item.attendanceCount || 0), 0));

async function fetchSchedules() {
  loading.value = true;
  try {
    schedules.value = await getTodaySchedulesForAttendance();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchSchedules();
});

onPullDownRefresh(() => {
  fetchSchedules().finally(() => uni.stopPullDownRefresh());
});

function toAttendanceDetail(scheduleId: number) {
  uni.navigateTo({ url: `/pages/teacher/attendance/detail?scheduleId=${scheduleId}` });
}

function formatTime(time?: string) {
  return time ? time.substring(0, 5) : '--:--';
}

function progressText(item: AttendanceListItem) {
  const total = item.studentCount || 0;
  const count = item.attendanceCount || 0;
  if (!total) return '暂无学生';
  return `${count}/${total}`;
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Attendance" title="今日考勤" subtitle="按课程进入学生考勤处理">
      <view class="hero-metrics">
        <view class="metric">
          <text class="metric-num">{{ schedules.length }}</text>
          <text class="metric-label">课程</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ pendingCount }}</text>
          <text class="metric-label">待处理</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ handledTotal }}/{{ studentTotal }}</text>
          <text class="metric-label">已考勤</text>
        </view>
      </view>
    </TeacherHeroCard>

    <view class="list">
      <view
        v-for="item in schedules"
        :key="item.scheduleId"
        class="schedule-card"
        @tap="toAttendanceDetail(item.scheduleId)"
      >
        <view class="time-block">
          <text class="time-start">{{ formatTime(item.startTime) }}</text>
          <text class="time-end">{{ formatTime(item.endTime) }}</text>
        </view>
        <view class="schedule-main">
          <text class="schedule-title">{{ item.topic }}</text>
          <text class="schedule-sub">{{ item.className }}</text>
          <view class="progress-track">
            <view
              class="progress-fill"
              :style="{ width: `${(item.studentCount || 0) ? Math.round((item.attendanceCount || 0) / (item.studentCount || 1) * 100) : 0}%` }"
            />
          </view>
        </view>
        <view class="schedule-side">
          <text class="progress-pill" :class="{ warn: (item.studentCount || 0) > (item.attendanceCount || 0) }">
            {{ progressText(item) }}
          </text>
          <text class="arrow">›</text>
        </view>
      </view>

      <TeacherEmptyState
        v-if="!schedules.length"
        :title="loading ? '正在加载课程' : '今日暂无课程'"
        description="今日排课出现后，可以从这里进入考勤详情。"
      />
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 70rpx;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

.hero-metrics {
  margin-top: 28rpx;
  display: flex;
  gap: 12rpx;
}

.metric {
  flex: 1;
  min-width: 0;
  padding: 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 252, 245, 0.12);
}

.metric-num,
.metric-label {
  display: block;
}

.metric-num {
  color: #fff;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.25;
}

.metric-label {
  margin-top: 4rpx;
  color: rgba(255, 255, 255, 0.66);
  font-size: 20rpx;
  font-weight: 800;
}

.list {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.schedule-card {
  min-height: 146rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.schedule-card:active {
  opacity: 0.76;
}

.time-block {
  width: 96rpx;
  flex-shrink: 0;
  padding: 14rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 24rpx;
  background: #f4efe6;
}

.time-start {
  color: #17211d;
  font-size: 27rpx;
  font-weight: 900;
}

.time-end {
  margin-top: 6rpx;
  color: #8d9089;
  font-size: 20rpx;
  font-weight: 800;
}

.schedule-main {
  flex: 1;
  min-width: 0;
}

.schedule-title,
.schedule-sub {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-title {
  color: #17211d;
  font-size: 29rpx;
  font-weight: 900;
}

.schedule-sub {
  margin-top: 8rpx;
  color: #666d66;
  font-size: 23rpx;
  font-weight: 800;
}

.progress-track {
  height: 10rpx;
  margin-top: 16rpx;
  border-radius: 999rpx;
  background: #eee5d8;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: #1f5a44;
}

.schedule-side {
  width: 110rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.progress-pill {
  padding: 8rpx 13rpx;
  border-radius: 999rpx;
  background: #e7f0ed;
  color: #1f5a44;
  font-size: 21rpx;
  font-weight: 900;
}

.progress-pill.warn {
  background: #fff1d4;
  color: #9a6710;
}

.arrow {
  color: #b6b0a6;
  font-size: 42rpx;
  line-height: 1;
}
</style>
