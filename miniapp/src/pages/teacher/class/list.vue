<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherClasses } from '@/api/teacher';
import type { ClassListItem } from '@/types/api';
import AppTabBar from '@/components/AppTabBar.vue';

const classes = ref<ClassListItem[]>([]);
const loading = ref(false);

async function fetchClasses() {
  loading.value = true;
  try {
    const res = await getTeacherClasses();
    classes.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchClasses();
});

function toClassDetail(id: number) {
  uni.navigateTo({ url: `/pages/teacher/class/detail?id=${id}` });
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Classes</text>
      <text class="title">我的班级</text>
    </view>

    <view class="panel">
      <view
        class="class-row"
        v-for="item in classes"
        :key="item.id"
        @tap="toClassDetail(item.id)"
      >
        <view class="class-info">
          <text class="class-name">{{ item.name }}</text>
          <text class="class-course">{{ item.courseName }}</text>
          <text class="class-system" v-if="item.courseSystem">{{ item.courseSystem }}</text>
        </view>
        <view class="class-meta">
          <text class="meta-text">{{ item.studentCount || 0 }}人</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="empty-row" v-if="!classes.length">
        <text class="empty-text">暂无班级</text>
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

.class-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-top: 1px solid #F0EAE0;
}

.class-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.class-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #17211d;
}

.class-course {
  font-size: 24rpx;
  color: #666;
}

.class-system {
  font-size: 22rpx;
  color: #999;
}

.class-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #666;
}

.arrow {
  font-size: 32rpx;
  color: #AAA;
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
