<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { useAuthStore } from '@/stores/auth';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherDashboard } from '@/api/teacher';
import type { TeacherDashboard } from '@/types/api';
import AppTabBar from '@/components/AppTabBar.vue';

const auth = useAuthStore();
const identity = computed(() => auth.selectedIdentity);

const dashboard = ref<TeacherDashboard | null>(null);
const loading = ref(false);

async function fetchDashboard() {
  loading.value = true;
  try {
    const res = await getTeacherDashboard();
    dashboard.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchDashboard();
});

onPullDownRefresh(() => {
  fetchDashboard().finally(() => uni.stopPullDownRefresh());
});

function toClassList() {
  uni.navigateTo({ url: '/pages/teacher/class/list' });
}

function toHomeworkList() {
  uni.navigateTo({ url: '/pages/teacher/homework/list' });
}

function toAttendanceList() {
  uni.navigateTo({ url: '/pages/teacher/attendance/list' });
}

function toScheduleDetail(id: number) {
  uni.navigateTo({ url: `/pages/teacher/attendance/detail?id=${id}` });
}

function formatTime(time?: string) {
  if (!time) return '';
  return time.substring(0, 5);
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <view>
        <text class="eyebrow">Teacher Desk</text>
        <text class="title">{{ dashboard?.profile?.name || identity?.displayName }}</text>
        <text class="subtitle">{{ dashboard?.profile?.title }}</text>
      </view>
    </view>

    <view class="status-card">
      <view>
        <text class="label">校区</text>
        <text class="value">{{ dashboard?.profile?.campusShortName || '--' }}</text>
      </view>
      <view>
        <text class="label">身份</text>
        <text class="value">老师</text>
      </view>
    </view>

    <view class="panel" v-if="dashboard?.todaySchedules?.length">
      <text class="panel-title">今日课程</text>
      <view
        class="schedule-row"
        v-for="item in dashboard.todaySchedules"
        :key="item.id"
        @tap="toScheduleDetail(item.id)"
      >
        <view class="schedule-time">
          <text class="time-text">{{ formatTime(item.startTime) }}</text>
          <text class="time-end">{{ formatTime(item.endTime) }}</text>
        </view>
        <view class="schedule-info">
          <text class="schedule-topic">{{ item.topic }}</text>
          <text class="schedule-class">{{ item.className }} · {{ item.courseName }}</text>
          <text class="schedule-room" v-if="item.classroom">{{ item.classroom }}</text>
        </view>
        <view class="schedule-meta">
          <text class="meta-text">{{ item.studentCount || 0 }}人</text>
          <text class="meta-text" :class="{ 'meta-warn': (item.studentCount || 0) > (item.attendanceCount || 0) }">
            考勤{{ item.attendanceCount || 0 }}
          </text>
        </view>
      </view>
    </view>

    <view class="panel" v-else>
      <text class="panel-title">今日课程</text>
      <view class="empty-row">
        <text class="empty-text">今日暂无课程</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">待办事项</text>
      <view class="todo-row" @tap="toAttendanceList">
        <text class="dot blue" />
        <text class="todo-text">待考勤</text>
        <text class="todo-num">{{ dashboard?.todoStats?.pendingAttendance || 0 }}</text>
      </view>
      <view class="todo-row" @tap="toHomeworkList">
        <text class="dot green" />
        <text class="todo-text">待点评作业</text>
        <text class="todo-num">{{ dashboard?.todoStats?.pendingComment || 0 }}</text>
      </view>
      <view class="todo-row">
        <text class="dot orange" />
        <text class="todo-text">待审核资料</text>
        <text class="todo-num">{{ dashboard?.todoStats?.pendingAudit || 0 }}</text>
      </view>
    </view>

    <view class="quick-grid">
      <view class="quick-item" @tap="toClassList">
        <text class="quick-num">{{ dashboard?.quickStats?.classCount || 0 }}</text>
        <text class="quick-label">我的班级</text>
      </view>
      <view class="quick-item" @tap="toClassList">
        <text class="quick-num">{{ dashboard?.quickStats?.studentCount || 0 }}</text>
        <text class="quick-label">学生总数</text>
      </view>
      <view class="quick-item" @tap="toAttendanceList">
        <text class="quick-num">{{ dashboard?.quickStats?.weekScheduleCount || 0 }}</text>
        <text class="quick-label">本周课程</text>
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

.subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.status-card {
  margin-top: -50rpx;
  margin-left: 20rpx;
  margin-right: 20rpx;
  padding: 26rpx;
  display: flex;
  justify-content: space-between;
  border-radius: 18rpx;
  background: #fffcf5;
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
}

.label {
  display: block;
  color: #AAA;
  font-size: 22rpx;
}

.value {
  display: block;
  margin-top: 6rpx;
  color: #17211d;
  font-size: 28rpx;
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

.schedule-room {
  font-size: 22rpx;
  color: #999;
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

.todo-row {
  height: 68rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  border-top: 1px solid #F0EAE0;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}

.blue   { background: #285c7f; }
.green  { background: #22624c; }
.orange { background: #e6a23c; }

.todo-text {
  flex: 1;
  color: #555;
  font-size: 25rpx;
}

.todo-num {
  color: #17211d;
  font-size: 28rpx;
  font-weight: 800;
}

.quick-grid {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.quick-item {
  min-height: 136rpx;
  padding: 22rpx;
  border-radius: 16rpx;
  background: #fffcf5;
  position: relative;
  transition: transform 0.15s;
}

.quick-item:active {
  transform: scale(0.97);
}

.quick-item::after {
  content: '';
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-left: 10rpx solid #ccc;
  border-top: 8rpx solid transparent;
  border-bottom: 8rpx solid transparent;
}

.quick-num {
  display: block;
  color: #22624c;
  font-size: 40rpx;
  font-weight: 900;
}

.quick-label {
  display: block;
  margin-top: 6rpx;
  color: #AAA;
  font-size: 23rpx;
}
</style>
