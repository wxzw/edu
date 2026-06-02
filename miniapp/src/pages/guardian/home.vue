<script setup lang="ts">
import { computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useAuthStore } from '@/stores/auth';
import { requireIdentity } from '@/utils/auth-flow';

const auth = useAuthStore();
const identity = computed(() => auth.selectedIdentity);
const children = computed(() => identity.value?.children || []);

onShow(() => {
  requireIdentity('GUARDIAN');
});

function toMine() {
  uni.navigateTo({ url: '/pages/mine/index' });
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
  padding: 36rpx 30rpx 60rpx;
  background: #f8f3ea;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.caption {
  display: block;
  color: #22624c;
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
  border-radius: 999rpx;
  background: #17211d;
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
}

.summary-card {
  margin-top: 36rpx;
  min-height: 232rpx;
  padding: 34rpx;
  border-radius: 26rpx;
  background: #285c7f;
  color: #fff;
}

.number {
  display: block;
  font-size: 72rpx;
  font-weight: 900;
  letter-spacing: 0;
}

.summary-label {
  display: block;
  margin-top: 10rpx;
  color: #eaf2f6;
  font-size: 28rpx;
}

.children {
  margin-top: 32rpx;
}

.section-title {
  display: block;
  margin-bottom: 18rpx;
  color: #17211d;
  font-size: 32rpx;
  font-weight: 900;
}

.empty {
  padding: 34rpx;
  border-radius: 22rpx;
  background: #fffcf5;
  color: #6f756f;
  font-size: 26rpx;
}

.child-card {
  min-height: 128rpx;
  padding: 24rpx;
  margin-bottom: 18rpx;
  display: flex;
  align-items: center;
  gap: 22rpx;
  border: 2rpx solid #ded3bf;
  border-radius: 22rpx;
  background: #fffcf5;
}

.child-avatar {
  width: 78rpx;
  height: 78rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.child-info {
  flex: 1;
  min-width: 0;
}

.child-name {
  display: block;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.child-meta {
  display: block;
  margin-top: 8rpx;
  color: #6f756f;
  font-size: 24rpx;
}
</style>
