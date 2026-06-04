<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';

const materials = ref<any[]>([]);
const loading = ref(false);

async function fetchMaterials() {
  loading.value = true;
  try {
    // TODO: 接入 /api/teacher/materials 接口
    materials.value = [];
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchMaterials();
});

function toUpload() {
  uni.navigateTo({ url: '/pages/teacher/material/upload' });
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <view>
        <text class="eyebrow">Materials</text>
        <text class="title">教学资料</text>
      </view>
      <button class="round-button" @tap="toUpload">+ 上传</button>
    </view>

    <view class="panel">
      <view class="empty-row">
        <text class="empty-text">资料管理功能开发中</text>
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

.round-button {
  width: 140rpx;
  height: 64rpx;
  border-radius: 999rpx;
  background: rgba(255,255,255,0.2);
  color: #fff;
  font-size: 24rpx;
  font-weight: 800;
}

.panel {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 18rpx;
  background: #fffcf5;
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
