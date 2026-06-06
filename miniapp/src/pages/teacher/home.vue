<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '@/stores/auth';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherDashboard } from '@/api/teacher';
import type { TeacherDashboard, TodaySchedule } from '@/types/api';
import AppTabBar from '@/components/AppTabBar.vue';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';

const auth = useAuthStore();
const identity = computed(() => auth.selectedIdentity);

const dashboard = ref<TeacherDashboard | null>(null);
const loading = ref(false);

const profile = computed(() => dashboard.value?.profile);
const displayName = computed(() => profile.value?.name || identity.value?.displayName || '老师');
const teacherInitial = computed(() => displayName.value.slice(0, 1));
const campusName = computed(() => profile.value?.campusShortName || profile.value?.campusName || '未选择校区');
const phoneText = computed(() => maskPhone(profile.value?.phone || identity.value?.phone));
const todaySchedules = computed(() => dashboard.value?.todaySchedules || []);
const nextSchedule = computed(() => todaySchedules.value[0]);
const todoItems = computed(() => [
  {
    label: '待考勤',
    value: dashboard.value?.todoStats?.pendingAttendance || 0,
    tone: 'blue',
    action: toAttendanceList,
  },
  {
    label: '待点评作业',
    value: dashboard.value?.todoStats?.pendingComment || 0,
    tone: 'green',
    action: toHomeworkList,
  },
  {
    label: '待审核资料',
    value: dashboard.value?.todoStats?.pendingAudit || 0,
    tone: 'amber',
    action: toMaterialList,
  },
]);
const quickStats = computed(() => [
  {
    label: '我的班级',
    value: dashboard.value?.quickStats?.classCount || 0,
    action: toClassList,
  },
  {
    label: '学生总数',
    value: dashboard.value?.quickStats?.studentCount || 0,
    action: toClassList,
  },
  {
    label: '本周课程',
    value: dashboard.value?.quickStats?.weekScheduleCount || 0,
    action: toSchedulePage,
  },
]);

async function fetchDashboard() {
  loading.value = true;
  try {
    dashboard.value = await getTeacherDashboard();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
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
  uni.reLaunch({ url: '/pages/teacher/class/list' });
}

function toHomeworkList() {
  uni.navigateTo({ url: '/pages/teacher/homework/list' });
}

function toAttendanceList() {
  uni.navigateTo({ url: '/pages/teacher/attendance/list' });
}

function toMaterialList() {
  uni.navigateTo({ url: '/pages/teacher/material/list' });
}

function toSchedulePage() {
  uni.reLaunch({ url: '/pages/teacher/schedule/index' });
}

function toScheduleDetail(id: number) {
  uni.navigateTo({ url: `/pages/teacher/attendance/detail?scheduleId=${id}` });
}

function formatTime(time?: string) {
  return time ? time.substring(0, 5) : '--:--';
}

function scheduleTime(item?: TodaySchedule) {
  if (!item) return '今日暂无排课';
  return `${formatTime(item.startTime)}-${formatTime(item.endTime)}`;
}

function attendanceLabel(item: TodaySchedule) {
  const studentCount = item.studentCount || 0;
  const attendanceCount = item.attendanceCount || 0;
  if (!studentCount) return '暂无学生';
  return `${attendanceCount}/${studentCount} 已考勤`;
}

function maskPhone(phone?: string) {
  if (!phone) return '未登记';
  if (phone.length < 7) return phone;
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}
</script>

<template>
  <view class="page">
    <view class="desk-card">
      <view class="teacher-row">
        <image
          v-if="profile?.avatarUrl"
          class="avatar"
          :src="profile.avatarUrl"
          mode="aspectFill"
        />
        <view v-else class="avatar avatar-fallback">
          {{ teacherInitial }}
        </view>
        <view class="teacher-copy">
          <text class="eyebrow">Teacher Desk</text>
          <text class="teacher-name">{{ displayName }}</text>
          <text class="teacher-title">{{ profile?.title || '主班老师' }}</text>
        </view>
      </view>

      <view class="identity-strip">
        <view class="identity-item wide">
          <text class="identity-label">校区</text>
          <text class="identity-value">{{ campusName }}</text>
        </view>
        <view class="identity-item">
          <text class="identity-label">身份</text>
          <text class="identity-value">老师</text>
        </view>
        <view class="identity-item">
          <text class="identity-label">电话</text>
          <text class="identity-value">{{ phoneText }}</text>
        </view>
      </view>
    </view>

    <view class="focus-panel" v-if="nextSchedule" @tap="toScheduleDetail(nextSchedule.id)">
      <view class="focus-copy">
        <text class="section-kicker">NEXT CLASS</text>
        <text class="focus-title">{{ nextSchedule.topic }}</text>
        <text class="focus-meta">
          {{ scheduleTime(nextSchedule) }} · {{ nextSchedule.className }} · {{ nextSchedule.classroom || '教室待定' }}
        </text>
      </view>
      <view class="focus-badge">
        <text class="badge-number">{{ todaySchedules.length }}</text>
        <text class="badge-label">今日</text>
      </view>
    </view>

    <view class="panel" v-else>
      <view class="panel-head">
        <text class="panel-title">今日课程</text>
      </view>
      <TeacherEmptyState
        title="今日暂无课程"
        description="可以去课表查看本周安排，或稍后下拉刷新。"
        action-text="查看课表"
        @action="toSchedulePage"
      />
    </view>

    <view class="panel" v-if="todaySchedules.length">
      <view class="panel-head">
        <text class="panel-title">今日课程</text>
        <text class="panel-link" @tap="toSchedulePage">全部</text>
      </view>
      <view
        v-for="item in todaySchedules"
        :key="item.id"
        class="schedule-row"
        @tap="toScheduleDetail(item.id)"
      >
        <view class="time-block">
          <text class="time-start">{{ formatTime(item.startTime) }}</text>
          <text class="time-end">{{ formatTime(item.endTime) }}</text>
        </view>
        <view class="schedule-main">
          <text class="schedule-title">{{ item.topic }}</text>
          <text class="schedule-sub">{{ item.className }} · {{ item.courseName }}</text>
          <text class="schedule-room">{{ item.classroom || '教室待定' }}</text>
        </view>
        <view class="schedule-status" :class="{ warn: (item.studentCount || 0) > (item.attendanceCount || 0) }">
          <text>{{ attendanceLabel(item) }}</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <view class="panel-head">
        <text class="panel-title">待办事项</text>
        <text class="panel-subtitle">{{ loading ? '刷新中' : '今日工作' }}</text>
      </view>
      <view
        v-for="item in todoItems"
        :key="item.label"
        class="todo-row"
        @tap="item.action"
      >
        <text class="todo-dot" :class="item.tone" />
        <text class="todo-label">{{ item.label }}</text>
        <text class="todo-value">{{ item.value }}</text>
      </view>
    </view>

    <view class="quick-strip">
      <view
        v-for="item in quickStats"
        :key="item.label"
        class="quick-card"
        @tap="item.action"
      >
        <text class="quick-value">{{ item.value }}</text>
        <text class="quick-label">{{ item.label }}</text>
      </view>
    </view>

    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 160rpx;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

button::after {
  border: 0;
}

.desk-card {
  padding: 32rpx;
  border-radius: 32rpx;
  background:
    linear-gradient(135deg, rgba(25, 58, 44, 0.98), rgba(31, 90, 68, 0.98)),
    #1f5a44;
  box-shadow: 0 18rpx 42rpx rgba(31, 90, 68, 0.18);
}

.teacher-row {
  display: flex;
  align-items: center;
  gap: 22rpx;
}

.avatar {
  width: 108rpx;
  height: 108rpx;
  flex-shrink: 0;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.16);
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f7d58c;
  font-size: 48rpx;
  font-weight: 900;
}

.teacher-copy {
  flex: 1;
  min-width: 0;
}

.eyebrow,
.section-kicker {
  display: block;
  color: #f0b84d;
  font-size: 21rpx;
  font-weight: 900;
  letter-spacing: 3rpx;
}

.teacher-name {
  display: block;
  margin-top: 10rpx;
  color: #fff;
  font-size: 46rpx;
  font-weight: 900;
  line-height: 1.12;
  word-break: break-all;
}

.teacher-title {
  display: block;
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.74);
  font-size: 24rpx;
  font-weight: 800;
}

.identity-strip {
  margin-top: 30rpx;
  display: flex;
  gap: 12rpx;
}

.identity-item {
  min-width: 0;
  flex: 1;
  padding: 18rpx;
  border-radius: 22rpx;
  background: rgba(255, 252, 245, 0.12);
}

.identity-item.wide {
  flex: 1.3;
}

.identity-label,
.identity-value {
  display: block;
}

.identity-label {
  color: rgba(255, 255, 255, 0.58);
  font-size: 20rpx;
  font-weight: 800;
}

.identity-value {
  margin-top: 8rpx;
  color: #fff;
  font-size: 25rpx;
  font-weight: 900;
  line-height: 1.28;
  word-break: break-all;
}

.focus-panel,
.panel,
.quick-card {
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.focus-panel {
  margin-top: 24rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  gap: 22rpx;
  border-radius: 30rpx;
}

.focus-copy {
  flex: 1;
  min-width: 0;
}

.focus-title {
  display: block;
  margin-top: 10rpx;
  color: #17211d;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.24;
}

.focus-meta {
  display: block;
  margin-top: 10rpx;
  color: #7d827c;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.45;
}

.focus-badge {
  width: 108rpx;
  height: 108rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 30rpx;
  background: #f4efe6;
}

.badge-number {
  color: #1f5a44;
  font-size: 38rpx;
  font-weight: 900;
}

.badge-label {
  margin-top: 4rpx;
  color: #8a8c86;
  font-size: 20rpx;
  font-weight: 900;
}

.panel {
  margin-top: 24rpx;
  padding: 26rpx;
  border-radius: 30rpx;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 12rpx;
}

.panel-title {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.panel-link,
.panel-subtitle {
  color: #7c817b;
  font-size: 22rpx;
  font-weight: 800;
}

.panel-link {
  color: #1f5a44;
}

.schedule-row {
  min-height: 130rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  border-top: 1rpx solid #eee5d8;
}

.time-block {
  width: 96rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  background: #f4efe6;
  padding: 14rpx 0;
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
.schedule-sub,
.schedule-room {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-title {
  color: #17211d;
  font-size: 28rpx;
  font-weight: 900;
}

.schedule-sub {
  margin-top: 8rpx;
  color: #666d66;
  font-size: 23rpx;
  font-weight: 700;
}

.schedule-room {
  margin-top: 6rpx;
  color: #a09a91;
  font-size: 21rpx;
}

.schedule-status {
  max-width: 128rpx;
  padding: 9rpx 12rpx;
  border-radius: 999rpx;
  background: #e7f0ed;
  color: #1f5a44;
  font-size: 20rpx;
  font-weight: 900;
  text-align: center;
  line-height: 1.2;
}

.schedule-status.warn {
  background: #fff1d4;
  color: #9a6710;
}

.todo-row {
  height: 76rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  border-top: 1rpx solid #eee5d8;
}

.todo-dot {
  width: 16rpx;
  height: 16rpx;
  flex-shrink: 0;
  border-radius: 50%;
}

.todo-dot.blue { background: #285c7f; }
.todo-dot.green { background: #1f5a44; }
.todo-dot.amber { background: #f0b84d; }

.todo-label {
  flex: 1;
  color: #5f655f;
  font-size: 26rpx;
  font-weight: 800;
}

.todo-value {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.quick-strip {
  margin-top: 20rpx;
  display: flex;
  gap: 14rpx;
}

.quick-card {
  flex: 1;
  min-width: 0;
  min-height: 132rpx;
  padding: 22rpx 18rpx;
  box-sizing: border-box;
  border-radius: 26rpx;
}

.quick-card:active,
.focus-panel:active,
.schedule-row:active,
.todo-row:active {
  opacity: 0.76;
}

.quick-value {
  display: block;
  color: #1f5a44;
  font-size: 40rpx;
  font-weight: 900;
}

.quick-label {
  display: block;
  margin-top: 8rpx;
  color: #8c8f88;
  font-size: 22rpx;
  font-weight: 800;
}
</style>
