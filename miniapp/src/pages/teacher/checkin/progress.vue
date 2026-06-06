<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherHomeworkDetail } from '@/api/teacher';
import type { TeacherHomeworkDetail } from '@/types/api';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

const homework = ref<TeacherHomeworkDetail | null>(null);
const homeworkId = ref(0);
const loading = ref(false);

const submissions = computed(() => homework.value?.submissions || []);
const commentedCount = computed(() => submissions.value.filter((item) => item.status === 'COMMENTED').length);

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const id = options?.id ? Number(options.id) : 0;
  if (id) {
    homeworkId.value = id;
    fetchDetail(id);
  }
});

onPullDownRefresh(() => {
  if (!homeworkId.value) {
    uni.stopPullDownRefresh();
    return;
  }
  fetchDetail(homeworkId.value).finally(() => uni.stopPullDownRefresh());
});

async function fetchDetail(id: number) {
  loading.value = true;
  try {
    homework.value = await getTeacherHomeworkDetail(id);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function formatDate(date?: string) {
  return date ? date.substring(0, 10) : '未提交';
}

function statusLabel(status: string) {
  if (status === 'SUBMITTED') return '已提交';
  if (status === 'COMMENTED') return '已点评';
  if (status === 'RESUBMIT_REQUIRED') return '需重交';
  return status || '未知';
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard
      eyebrow="Check-in Progress"
      :title="homework?.title || '打卡进度'"
      :subtitle="homework?.className || '班级打卡'"
    >
      <view class="hero-metrics">
        <view class="metric">
          <text class="metric-num">{{ submissions.length }}</text>
          <text class="metric-label">提交</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ commentedCount }}</text>
          <text class="metric-label">已点评</text>
        </view>
      </view>
    </TeacherHeroCard>

    <view class="panel">
      <view class="panel-head">
        <text class="panel-title">学生打卡</text>
        <text class="panel-subtitle">{{ submissions.length }} 条记录</text>
      </view>
      <view
        v-for="item in submissions"
        :key="item.id"
        class="sub-row"
      >
        <image
          v-if="item.studentAvatarUrl"
          class="avatar"
          :src="item.studentAvatarUrl"
          mode="aspectFill"
        />
        <view v-else class="avatar avatar-fallback">{{ item.studentName.slice(0, 1) }}</view>
        <view class="sub-main">
          <text class="sub-name">{{ item.studentName }}</text>
          <text class="sub-time">最近打卡 {{ formatDate(item.submittedAt) }}</text>
        </view>
        <text class="status-pill" :class="{ done: item.status === 'COMMENTED' }">
          {{ statusLabel(item.status) }}
        </text>
      </view>
      <TeacherEmptyState
        v-if="!submissions.length"
        :title="loading ? '正在加载进度' : '暂无打卡记录'"
        description="学生完成打卡后，会在这里展示提交进度。"
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

.panel {
  margin-top: 24rpx;
  padding: 26rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 12rpx;
}

.panel-title {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.panel-subtitle {
  color: #858982;
  font-size: 22rpx;
  font-weight: 800;
}

.sub-row {
  min-height: 116rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  border-top: 1rpx solid #eee5d8;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  flex-shrink: 0;
  border-radius: 24rpx;
  background: #e7f0ed;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f5a44;
  font-size: 27rpx;
  font-weight: 900;
}

.sub-main {
  flex: 1;
  min-width: 0;
}

.sub-name,
.sub-time {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub-name {
  color: #17211d;
  font-size: 28rpx;
  font-weight: 900;
}

.sub-time {
  margin-top: 7rpx;
  color: #858982;
  font-size: 22rpx;
}

.status-pill {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #fff1d4;
  color: #9a6710;
  font-size: 21rpx;
  font-weight: 900;
}

.status-pill.done {
  background: #e7f0ed;
  color: #1f5a44;
}
</style>
