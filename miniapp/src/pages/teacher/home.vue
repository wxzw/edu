<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '@/stores/auth';
import { requireIdentity } from '@/utils/auth-flow';

const auth = useAuthStore();
const identity = computed(() => auth.selectedIdentity);

onShow(() => {
  requireIdentity('TEACHER');
});

function toMine() {
  uni.navigateTo({ url: '/pages/mine/index' });
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <view>
        <text class="eyebrow">Teacher Desk</text>
        <text class="title">{{ identity?.displayName }}</text>
      </view>
      <button class="round-button" @tap="toMine">我的</button>
    </view>

    <view class="status-card">
      <view>
        <text class="label">校区</text>
        <text class="value">{{ identity?.campusId }}</text>
      </view>
      <view>
        <text class="label">身份</text>
        <text class="value">老师</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">今日待办</text>
      <view class="todo-row">
        <text class="dot blue" />
        <text class="todo-text">课表同步后展示今日课程</text>
      </view>
      <view class="todo-row">
        <text class="dot green" />
        <text class="todo-text">作业点评入口预留</text>
      </view>
    </view>

    <view class="quick-grid">
      <view class="quick-item">
        <text class="quick-num">0</text>
        <text class="quick-label">待考勤</text>
      </view>
      <view class="quick-item">
        <text class="quick-num">0</text>
        <text class="quick-label">待点评</text>
      </view>
      <view class="quick-item">
        <text class="quick-num">0</text>
        <text class="quick-label">资料</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 30rpx 60rpx;
  background: #eef4ee;
}

.hero {
  min-height: 236rpx;
  padding: 34rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-radius: 24rpx;
  background: #17392d;
  color: #fff;
}

.eyebrow {
  display: block;
  color: #f0b84d;
  font-size: 22rpx;
  font-weight: 800;
}

.title {
  display: block;
  margin-top: 18rpx;
  font-size: 48rpx;
  font-weight: 900;
  letter-spacing: 0;
}

.round-button {
  width: 96rpx;
  height: 62rpx;
  border-radius: 999rpx;
  background: #fffcf5;
  color: #17392d;
  font-size: 24rpx;
  font-weight: 900;
}

.status-card {
  margin-top: -54rpx;
  margin-left: 24rpx;
  margin-right: 24rpx;
  padding: 28rpx;
  display: flex;
  justify-content: space-between;
  border-radius: 20rpx;
  background: #fffcf5;
  box-shadow: 0 16rpx 38rpx rgba(23, 33, 29, 0.12);
}

.label {
  display: block;
  color: #6f756f;
  font-size: 22rpx;
}

.value {
  display: block;
  margin-top: 8rpx;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.panel {
  margin-top: 34rpx;
  padding: 30rpx;
  border-radius: 22rpx;
  background: #fffcf5;
}

.panel-title {
  display: block;
  margin-bottom: 22rpx;
  color: #17211d;
  font-size: 32rpx;
  font-weight: 900;
}

.todo-row {
  height: 72rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  border-top: 2rpx solid #eee4d4;
}

.dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
}

.blue {
  background: #285c7f;
}

.green {
  background: #22624c;
}

.todo-text {
  color: #3b453f;
  font-size: 26rpx;
}

.quick-grid {
  margin-top: 24rpx;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
}

.quick-item {
  min-height: 144rpx;
  padding: 22rpx;
  border-radius: 20rpx;
  background: #fffcf5;
}

.quick-num {
  display: block;
  color: #22624c;
  font-size: 42rpx;
  font-weight: 900;
}

.quick-label {
  display: block;
  margin-top: 8rpx;
  color: #6f756f;
  font-size: 23rpx;
}
</style>
