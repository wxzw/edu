<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherClassDetail } from '@/api/teacher';
import type { ClassDetail } from '@/types/api';

const classDetail = ref<ClassDetail | null>(null);
const loading = ref(false);

async function fetchDetail(id: number) {
  loading.value = true;
  try {
    const res = await getTeacherClassDetail(id);
    classDetail.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const id = options?.id ? parseInt(options.id, 10) : 0;
  if (id) fetchDetail(id);
});

function toStudentProfile(studentId: number) {
  uni.navigateTo({ url: `/pages/teacher/student/profile?id=${studentId}` });
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Class Detail</text>
      <text class="title">{{ classDetail?.name }}</text>
      <text class="subtitle">{{ classDetail?.courseName }}</text>
    </view>

    <view class="panel">
      <text class="panel-title">
        学生列表 ({{ classDetail?.students?.length || 0 }}人)
      </text>
      <view
        class="student-row"
        v-for="item in classDetail?.students"
        :key="item.studentId"
        @tap="toStudentProfile(item.studentId)"
      >
        <image
          class="avatar"
          :src="item.avatarUrl || '/static/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="student-info">
          <text class="student-name">{{ item.name }}</text>
          <text class="student-meta" v-if="item.grade || item.school">
            {{ item.grade }} {{ item.school }}
          </text>
        </view>
        <view class="student-right">
          <text class="hours" v-if="item.remainingHours !== undefined">
            剩{{ item.remainingHours }}课时
          </text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="empty-row" v-if="!classDetail?.students?.length">
        <text class="empty-text">暂无学生</text>
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

.student-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 0;
  border-top: 1px solid #F0EAE0;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #eee;
}

.student-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.student-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #17211d;
}

.student-meta {
  font-size: 22rpx;
  color: #999;
}

.student-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.hours {
  font-size: 22rpx;
  color: #e6a23c;
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
