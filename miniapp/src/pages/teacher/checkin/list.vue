<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherHomeworks } from '@/api/teacher';
import type { TeacherHomeworkListItem } from '@/types/api';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

const checkins = ref<TeacherHomeworkListItem[]>([]);
const loading = ref(false);

const participantTotal = computed(() => checkins.value.reduce((sum, item) => sum + (item.totalSubmissions || 0), 0));

async function fetchCheckins() {
  loading.value = true;
  try {
    const res = await getTeacherHomeworks();
    checkins.value = res.filter((item) => item.checkinEnabled);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchCheckins();
});

onPullDownRefresh(() => {
  fetchCheckins().finally(() => uni.stopPullDownRefresh());
});

function toProgress(id: number) {
  uni.navigateTo({ url: `/pages/teacher/checkin/progress?id=${id}` });
}

function formatDate(date?: string) {
  return date ? date.substring(0, 10) : '未设置';
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Check-in" title="打卡任务" subtitle="跟进朗读、背诵和每日练习">
      <view class="hero-metrics">
        <view class="metric">
          <text class="metric-num">{{ checkins.length }}</text>
          <text class="metric-label">任务</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ participantTotal }}</text>
          <text class="metric-label">提交</text>
        </view>
      </view>
    </TeacherHeroCard>

    <view class="list">
      <view
        v-for="item in checkins"
        :key="item.id"
        class="checkin-card"
        @tap="toProgress(item.id)"
      >
        <view class="card-main">
          <text class="checkin-title">{{ item.title }}</text>
          <text class="checkin-class">{{ item.className || '未关联班级' }}</text>
          <view class="meta-row">
            <text>截止 {{ formatDate(item.deadline) }}</text>
            <text>{{ item.totalSubmissions || 0 }} 人参与</text>
          </view>
        </view>
        <text class="arrow">›</text>
      </view>

      <TeacherEmptyState
        v-if="!checkins.length"
        :title="loading ? '正在加载打卡' : '暂无打卡任务'"
        description="发布作业时启用打卡后，任务会出现在这里。"
      />
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 70rpx;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

.hero-metrics {
  margin-top: 28rpx;
  display: flex;
  gap: 12rpx;
}

.metric {
  flex: 1;
  padding: 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 252, 245, 0.12);
}

.metric-num,
.metric-label {
  display: block;
}

.metric-num {
  color: #fff;
  font-size: 32rpx;
  font-weight: 900;
}

.metric-label {
  margin-top: 4rpx;
  color: rgba(255, 255, 255, 0.66);
  font-size: 20rpx;
  font-weight: 800;
}

.list {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.checkin-card {
  min-height: 138rpx;
  padding: 26rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.checkin-card:active {
  opacity: 0.76;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.checkin-title,
.checkin-class {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.checkin-title {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.checkin-class {
  margin-top: 8rpx;
  color: #6e756f;
  font-size: 23rpx;
  font-weight: 800;
}

.meta-row {
  margin-top: 14rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.meta-row text {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #f4efe6;
  color: #7d827c;
  font-size: 21rpx;
  font-weight: 800;
}

.arrow {
  color: #b6b0a6;
  font-size: 42rpx;
  line-height: 1;
}
</style>
