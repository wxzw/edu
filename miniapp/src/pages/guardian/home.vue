<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '@/stores/auth';
import { requireIdentity } from '@/utils/auth-flow';

const auth = useAuthStore();
const identity = computed(() => auth.selectedIdentity);
const children = computed(() => identity.value?.children || []);

onShow(() => {
  if (requireIdentity('GUARDIAN')) {
    uni.reLaunch({ url: '/pages/student/home' });
  }
});

function toMine() {
  uni.reLaunch({ url: '/pages/mine/index' });
}
</script>

<template>
  <view class="page">
    <view class="header">
      <view>
        <text class="caption">Guardian Hub</text>
        <text class="name">{{ identity?.displayName }}</text>
      </view>
      <button class="mine" @tap="toMine">我的</button>
    </view>

    <view class="summary-card">
      <text class="number">{{ children.length }}</text>
      <text class="summary-label">绑定学生</text>
    </view>

    <view class="children">
      <text class="section-title">学生</text>
      <view v-if="children.length === 0" class="empty">暂无绑定学生</view>
      <view v-for="child in children" :key="child.studentId" class="child-card">
        <view class="child-avatar">{{ child.name.slice(0, 1) }}</view>
        <view class="child-info">
          <text class="child-name">{{ child.name }}</text>
          <text class="child-meta">{{ child.grade || '未设置年级' }} · {{ child.relation || '监护人' }}</text>
        </view>
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

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28rpx;
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
  margin-top: 10rpx;
  color: #17211d;
  font-size: 40rpx;
  font-weight: 900;
}

.mine {
  width: 100rpx;
  height: 64rpx;
  border-radius: 999rpx;
  background: #17211d;
  color: #fff;
  font-size: 24rpx;
  font-weight: 800;
}

.summary-card {
  padding: 34rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #1E3A5F 0%, #285c7f 100%);
  color: #fff;
}

.number {
  display: block;
  font-size: 68rpx;
  font-weight: 900;
}

.summary-label {
  display: block;
  margin-top: 8rpx;
  color: rgba(255,255,255,0.7);
  font-size: 26rpx;
}

.children {
  margin-top: 28rpx;
}

.section-title {
  display: block;
  margin-bottom: 16rpx;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.empty {
  padding: 34rpx;
  border-radius: 18rpx;
  background: #fffcf5;
  color: #AAA;
  font-size: 26rpx;
}

.child-card {
  min-height: 120rpx;
  padding: 24rpx;
  margin-bottom: 14rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.child-avatar {
  width: 74rpx;
  height: 74rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 28rpx;
  font-weight: 900;
}

.child-info {
  flex: 1;
  min-width: 0;
}

.child-name {
  display: block;
  color: #17211d;
  font-size: 29rpx;
  font-weight: 900;
}

.child-meta {
  display: block;
  margin-top: 6rpx;
  color: #AAA;
  font-size: 23rpx;
}
</style>
