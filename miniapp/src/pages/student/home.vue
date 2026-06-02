<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '@/stores/auth';
import { requireIdentity } from '@/utils/auth-flow';

const auth = useAuthStore();
const identity = computed(() => auth.selectedIdentity);

onShow(() => {
  requireIdentity('STUDENT');
});

function toMine() {
  uni.navigateTo({ url: '/pages/mine/index' });
}
</script>

<template>
  <view class="page">
    <view class="topbar">
      <view>
        <text class="caption">Student Studio</text>
        <text class="name">{{ identity?.displayName }}</text>
      </view>
      <button class="mine" @tap="toMine">我的</button>
    </view>

    <view class="learning-card">
      <text class="card-title">学习台</text>
      <text class="card-copy">课表、作业、课时会在业务接口补齐后进入这里</text>
      <view class="progress">
        <view class="progress-bar" />
      </view>
    </view>

    <view class="module-list">
      <view class="module-row">
        <text class="module-icon">L</text>
        <text class="module-name">我的课表</text>
        <text class="module-state">待接入</text>
      </view>
      <view class="module-row">
        <text class="module-icon yellow">H</text>
        <text class="module-name">我的作业</text>
        <text class="module-state">待接入</text>
      </view>
      <view class="module-row">
        <text class="module-icon blue">C</text>
        <text class="module-name">课时余额</text>
        <text class="module-state">待接入</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36rpx 30rpx 60rpx;
  background: #f6f1e8;
}

.topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.caption {
  display: block;
  color: #285c7f;
  font-size: 22rpx;
  font-weight: 900;
}

.name {
  display: block;
  margin-top: 12rpx;
  color: #17211d;
  font-size: 48rpx;
  font-weight: 900;
  letter-spacing: 0;
}

.mine {
  width: 96rpx;
  height: 62rpx;
  border: 2rpx solid #17211d;
  border-radius: 999rpx;
  background: #fffcf5;
  color: #17211d;
  font-size: 24rpx;
  font-weight: 900;
}

.learning-card {
  margin-top: 34rpx;
  padding: 34rpx;
  border: 2rpx solid #17211d;
  border-radius: 26rpx;
  background: #fffcf5;
  box-shadow: 8rpx 8rpx 0 #f0b84d;
}

.card-title {
  display: block;
  color: #17211d;
  font-size: 36rpx;
  font-weight: 900;
}

.card-copy {
  display: block;
  margin-top: 16rpx;
  color: #6f756f;
  font-size: 25rpx;
  line-height: 1.55;
}

.progress {
  height: 18rpx;
  margin-top: 32rpx;
  border-radius: 999rpx;
  background: #eee4d4;
  overflow: hidden;
}

.progress-bar {
  width: 42%;
  height: 100%;
  border-radius: inherit;
  background: #22624c;
}

.module-list {
  margin-top: 34rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.module-row {
  min-height: 112rpx;
  padding: 22rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-radius: 20rpx;
  background: #fffcf5;
}

.module-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #22624c;
  color: #fff;
  font-size: 26rpx;
  font-weight: 900;
}

.yellow {
  background: #f0b84d;
  color: #17211d;
}

.blue {
  background: #285c7f;
}

.module-name {
  flex: 1;
  color: #17211d;
  font-size: 28rpx;
  font-weight: 900;
}

.module-state {
  color: #6f756f;
  font-size: 23rpx;
}
</style>
