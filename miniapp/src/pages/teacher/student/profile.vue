<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherStudentProfile } from '@/api/teacher';
import type { TeacherStudentProfile } from '@/types/api';

const profile = ref<TeacherStudentProfile | null>(null);
const loading = ref(false);

async function fetchProfile(id: number) {
  loading.value = true;
  try {
    const res = await getTeacherStudentProfile(id);
    profile.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const id = options?.id ? parseInt(options.id, 10) : 0;
  if (id) fetchProfile(id);
});
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Student Profile</text>
      <text class="title">{{ profile?.name }}</text>
      <text class="subtitle">{{ profile?.nickname }}</text>
    </view>

    <view class="status-card">
      <view>
        <text class="label">年级</text>
        <text class="value">{{ profile?.grade || '-' }}</text>
      </view>
      <view>
        <text class="label">学校</text>
        <text class="value">{{ profile?.school || '-' }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">基本信息</text>
      <view class="info-row">
        <text class="info-label">英语水平</text>
        <text class="info-value">{{ profile?.englishLevel || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">家长电话</text>
        <text class="info-value">{{ profile?.parentPhone || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">所在班级</text>
        <text class="info-value">{{ profile?.classNames?.join('、') || '-' }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">课时概览</text>
      <view
        class="account-row"
        v-for="item in profile?.lessonAccounts"
        :key="item.id"
      >
        <text class="account-name">{{ item.courseName }}</text>
        <text class="account-hours">剩 {{ item.remainingHours }} 课时</text>
      </view>
      <view class="empty-row" v-if="!profile?.lessonAccounts?.length">
        <text class="empty-text">暂无课时记录</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">出勤统计</text>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-num">{{ profile?.attendanceStats?.totalClasses || 0 }}</text>
          <text class="stat-label">总课程</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ profile?.attendanceStats?.presentCount || 0 }}</text>
          <text class="stat-label">出勤</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ profile?.attendanceStats?.absentCount || 0 }}</text>
          <text class="stat-label">缺勤</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ profile?.attendanceStats?.attendanceRate || 0 }}%</text>
          <text class="stat-label">出勤率</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">作业统计</text>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-num">{{ profile?.homeworkStats?.totalAssigned || 0 }}</text>
          <text class="stat-label">布置作业</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ profile?.homeworkStats?.submittedCount || 0 }}</text>
          <text class="stat-label">已提交</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ profile?.homeworkStats?.commentedCount || 0 }}</text>
          <text class="stat-label">已点评</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ profile?.homeworkStats?.completionRate || 0 }}%</text>
          <text class="stat-label">完成率</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">最近课消记录</text>
      <view
        class="record-row"
        v-for="item in profile?.lessonRecords"
        :key="item.id"
      >
        <view class="record-info">
          <text class="record-name">{{ item.courseName }}</text>
          <text class="record-topic" v-if="item.lessonTopic">{{ item.lessonTopic }}</text>
        </view>
        <view class="record-right">
          <text class="record-delta" :class="{ 'delta-neg': item.hoursDelta < 0 }">
            {{ item.hoursDelta > 0 ? '+' : '' }}{{ item.hoursDelta }}
          </text>
          <text class="record-date">{{ item.occurredAt?.substring(0, 10) }}</text>
        </view>
      </view>
      <view class="empty-row" v-if="!profile?.lessonRecords?.length">
        <text class="empty-text">暂无课消记录</text>
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

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-top: 1px solid #F0EAE0;
}

.info-label {
  font-size: 25rpx;
  color: #666;
}

.info-value {
  font-size: 25rpx;
  color: #17211d;
  font-weight: 600;
}

.account-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-top: 1px solid #F0EAE0;
}

.account-name {
  font-size: 26rpx;
  color: #17211d;
}

.account-hours {
  font-size: 26rpx;
  color: #e6a23c;
  font-weight: 700;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 0;
}

.stat-num {
  font-size: 32rpx;
  font-weight: 900;
  color: #22624c;
}

.stat-label {
  font-size: 22rpx;
  color: #999;
}

.record-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-top: 1px solid #F0EAE0;
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.record-name {
  font-size: 26rpx;
  color: #17211d;
}

.record-topic {
  font-size: 22rpx;
  color: #999;
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.record-delta {
  font-size: 26rpx;
  font-weight: 700;
  color: #22624c;
}

.delta-neg {
  color: #e6a23c;
}

.record-date {
  font-size: 22rpx;
  color: #999;
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
