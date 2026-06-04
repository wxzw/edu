<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getStudentDashboard } from '@/api/student';
import { useAuthStore } from '@/stores/auth';
import type { ChildStudent, StudentDashboard } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';
import AppTabBar from '@/components/AppTabBar.vue';

const auth = useAuthStore();
const loading = ref(false);
const dashboard = ref<StudentDashboard>();

const profile = computed(() => dashboard.value?.profile);
const children = computed(() => dashboard.value?.children || []);
const activeChildIndex = computed(() => {
  const index = children.value.findIndex((child) => child.studentId === auth.currentStudentId);
  return index >= 0 ? index : 0;
});
const remaining = computed(() => dashboard.value?.lessonSummary?.totalRemainingHours ?? 0);
const nextLesson = computed(() => dashboard.value?.nextLesson);
const todos = computed(() => dashboard.value?.todos || []);
const activeGroup = computed(() => dashboard.value?.activeGroupRequest);

onShow(() => {
  if (requireStudentAccess()) {
    loadDashboard();
  }
});

async function loadDashboard() {
  loading.value = true;
  try {
    const data = await getStudentDashboard();
    dashboard.value = data;
    if (data.currentStudentId) {
      auth.setCurrentStudent(data.currentStudentId);
    }
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function changeChild(event: { detail: { value: number } }) {
  const child = children.value[event.detail.value] as ChildStudent | undefined;
  if (!child) {
    return;
  }
  auth.setCurrentStudent(child.studentId);
  loadDashboard();
}

function goLearning() {
  uni.reLaunch({ url: '/pages/student/learning' });
}

function goCourse() {
  uni.reLaunch({ url: '/pages/student/course' });
}

function goMine() {
  uni.reLaunch({ url: '/pages/mine/index' });
}

function goGroupCreate() {
  uni.navigateTo({ url: '/pages/student/group/create' });
}

function goActivities() {
  uni.navigateTo({ url: '/pages/student/activity/list' });
}

function goNotifications() {
  uni.navigateTo({ url: '/pages/mine/notifications' });
}

function goGroupDetail(id?: number) {
  if (!id) {
    return;
  }
  uni.navigateTo({ url: `/pages/student/group/detail?id=${id}` });
}

function goHomework(id?: number) {
  if (!id) {
    return;
  }
  uni.navigateTo({ url: `/pages/student/homework/detail?id=${id}` });
}

function openTodo(todo: { bizType: string; bizId?: number }) {
  if (todo.bizType.includes('HOMEWORK')) {
    goHomework(todo.bizId);
    return;
  }
  if (todo.bizType.includes('ACTIVITY') && todo.bizId) {
    uni.navigateTo({ url: `/pages/student/activity/detail?id=${todo.bizId}` });
    return;
  }
  goNotifications();
}

function displayName() {
  return profile.value?.nickname || profile.value?.name || auth.displayName || '同学';
}

function formatLessonTime() {
  if (!nextLesson.value) {
    return '暂无待上课程';
  }
  return `${nextLesson.value.lessonDate} ${nextLesson.value.startTime?.slice(0, 5)}-${nextLesson.value.endTime?.slice(0, 5)}`;
}

function groupStatusLabel(status?: string) {
  return {
    FORMING: '拼班中',
    WAITING_CAMPUS: '等待校区安排',
    TRIAL_ARRANGED: '试听已安排',
    TRIAL_COMPLETED: '试听已完成',
    FAILED: '拼班失败',
    CANCELLED: '已取消',
  }[status || ''] || status || '';
}
</script>

<template>
  <view class="page">
    <view class="topbar">
      <view>
        <text class="caption">STUDY DESK</text>
        <text class="name">{{ displayName() }}</text>
        <text class="meta">{{ profile?.campusShortName || profile?.campusName || '校区' }} · {{ profile?.classNames || '暂未分班' }}</text>
      </view>
      <button class="mine" @tap="goMine">我的</button>
    </view>

    <picker v-if="children.length > 1" :range="children" range-key="name" :value="activeChildIndex" @change="changeChild">
      <view class="child-switch">
        <text>当前孩子</text>
        <text>{{ children[activeChildIndex]?.name }}</text>
      </view>
    </picker>

    <view class="hero">
      <view>
        <text class="hero-label">剩余课时</text>
        <text class="hero-number">{{ remaining }}</text>
      </view>
      <view class="hero-mark" :class="{ warning: dashboard?.lessonSummary?.lowBalance }">
        {{ dashboard?.lessonSummary?.lowBalance ? '需关注' : '稳定' }}
      </view>
    </view>

    <view class="section next">
      <view class="section-head">
        <text class="section-title">最近课程</text>
        <button class="ghost" @tap="goCourse">全部</button>
      </view>
      <view v-if="nextLesson" class="lesson-card">
        <text class="lesson-time">{{ formatLessonTime() }}</text>
        <text class="lesson-topic">{{ nextLesson.topic }}</text>
        <text class="lesson-meta">{{ nextLesson.teacherName }} · {{ nextLesson.classroom || nextLesson.className }}</text>
      </view>
      <view v-else class="empty">暂无待上课程</view>
    </view>

    <view class="group-panel">
      <view>
        <text class="group-title">我要拼班</text>
        <text class="group-copy">同龄同目标，满员后由校区安排试听</text>
      </view>
      <button class="group-button" @tap="goGroupCreate">发起</button>
    </view>

    <view v-if="activeGroup" class="section active-group" @tap="goGroupDetail(activeGroup.id)">
      <view class="section-head">
        <text class="section-title">{{ activeGroup.targetSystem }}</text>
        <text class="status-pill">{{ groupStatusLabel(activeGroup.status) }}</text>
      </view>
      <view class="progress">
        <view class="progress-bar" :style="{ width: `${Math.min(100, activeGroup.currentMembers / activeGroup.requiredMembers * 100)}%` }" />
      </view>
      <text class="group-copy">已加入 {{ activeGroup.currentMembers }}/{{ activeGroup.requiredMembers }} 人</text>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">待办提醒</text>
        <button class="ghost" @tap="goLearning">作业</button>
      </view>
      <view v-if="todos.length" class="todo-list">
        <view v-for="todo in todos" :key="`${todo.bizType}-${todo.bizId || todo.id}`" class="todo-row" @tap="openTodo(todo)">
          <view class="dot" />
          <view class="todo-main">
            <text class="todo-title">{{ todo.title }}</text>
            <text class="todo-copy">{{ todo.content || todo.bizType }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty">今天没有新的待办</view>
    </view>

    <view class="quick-grid">
      <button class="quick" @tap="goLearning">作业</button>
      <button class="quick yellow" @tap="goCourse">课表</button>
      <button class="quick blue" @tap="goActivities">活动</button>
      <button class="quick green" @tap="goGroupCreate">拼班</button>
    </view>

    <view v-if="loading" class="loading">加载中...</view>
    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 160rpx;
  background: #f6f1e8;
  color: #17211d;
}

.topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 36rpx;
}

.caption {
  display: block;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
}

.name {
  display: block;
  margin-top: 8rpx;
  font-size: 44rpx;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.meta {
  display: block;
  margin-top: 6rpx;
  color: #909090;
  font-size: 24rpx;
}

.mine {
  width: 100rpx;
  height: 64rpx;
  border: 2rpx solid #17211d;
  border-radius: 999rpx;
  background: #fffcf5;
  color: #17211d;
  font-size: 24rpx;
  font-weight: 800;
}

/* 子账号切换 */
.child-switch {
  min-height: 72rpx;
  margin-bottom: 24rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 16rpx;
  background: #E8F0EA;
  color: #22624c;
  font-size: 26rpx;
  font-weight: 800;
}

/* 课时卡片 */
.hero {
  padding: 36rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #1B3A2D 0%, #2D6A4F 100%);
  color: #fff;
  margin-bottom: 28rpx;
}

.hero-label {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  opacity: 0.85;
}

.hero-number {
  display: block;
  margin-top: 6rpx;
  font-size: 72rpx;
  font-weight: 900;
  line-height: 1;
}

.hero-mark {
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255,255,255,0.2);
  color: #FFF;
  font-size: 22rpx;
  font-weight: 700;
}

.hero-mark.warning {
  background: rgba(231, 111, 81, 0.4);
}

/* 通用区块 */
.section {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fffcf5;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.section-title,
.group-title {
  font-size: 30rpx;
  font-weight: 900;
}

.ghost {
  height: 56rpx;
  padding: 0 20rpx;
  border: 2rpx solid #DDD;
  border-radius: 999rpx;
  background: transparent;
  color: #666;
  font-size: 22rpx;
  font-weight: 700;
}

.ghost:active {
  background: #F5F5F5;
}

/* 最近课程 */
.lesson-card {
  padding: 24rpx;
  border-radius: 16rpx;
  background: #F8F5EE;
}

.lesson-time {
  display: block;
  color: #22624c;
  font-size: 24rpx;
  font-weight: 700;
}

.lesson-topic {
  display: block;
  margin: 8rpx 0;
  font-size: 30rpx;
  font-weight: 900;
}

.lesson-meta {
  display: block;
  color: #909090;
  font-size: 23rpx;
}

/* 拼班面板 */
.group-panel {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #17211d;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
}

.group-copy {
  display: block;
  margin-top: 6rpx;
  color: #A0A8A2;
  font-size: 23rpx;
}

.group-button {
  width: 120rpx;
  height: 68rpx;
  border-radius: 999rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 27rpx;
  font-weight: 900;
}

/* 活跃拼班 */
.active-group {
  border: 2rpx solid #f0b84d;
}

.status-pill {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: #E8F0EA;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 800;
}

.progress {
  height: 14rpx;
  margin: 18rpx 0 12rpx;
  border-radius: 999rpx;
  background: #E8E8E8;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: inherit;
  background: #22624c;
  transition: width 0.5s ease;
}

/* 待办列表 */
.todo-list {
  margin-top: 4rpx;
}

.todo-row {
  min-height: 80rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  border-top: 1px solid #F0EAE0;
}

.todo-row:active {
  background: #F8F5EE;
  margin: 0 -28rpx;
  padding: 0 28rpx;
}

.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #f0b84d;
  flex-shrink: 0;
}

.todo-main {
  flex: 1;
  min-width: 0;
}

.todo-title {
  display: block;
  font-size: 27rpx;
  font-weight: 800;
}

.todo-copy {
  display: block;
  margin-top: 4rpx;
  color: #909090;
  font-size: 22rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 底部快捷 */
.quick-grid {
  margin-top: 28rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.quick {
  height: 92rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 28rpx;
  font-weight: 800;
}

.quick:active {
  opacity: 0.85;
}

.quick.yellow {
  background: #f0b84d;
  color: #17211d;
}

.quick.blue {
  background: #285c7f;
}

.quick.green {
  background: #2d6a4f;
}

.empty,
.loading {
  margin-top: 16rpx;
  color: #AAA;
  font-size: 24rpx;
}
</style>
