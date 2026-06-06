<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherClasses } from '@/api/teacher';
import type { ClassListItem } from '@/types/api';
import AppTabBar from '@/components/AppTabBar.vue';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

const classes = ref<ClassListItem[]>([]);
const loading = ref(false);

const studentTotal = computed(() => classes.value.reduce((sum, item) => sum + (item.studentCount || 0), 0));
const activeClassCount = computed(() => classes.value.filter((item) => item.status !== 'DISABLED').length);

async function fetchClasses() {
  loading.value = true;
  try {
    classes.value = await getTeacherClasses();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchClasses();
});

onPullDownRefresh(() => {
  fetchClasses().finally(() => uni.stopPullDownRefresh());
});

function toClassDetail(id: number) {
  uni.navigateTo({ url: `/pages/teacher/class/detail?id=${id}` });
}

function statusLabel(status: string) {
  if (status === 'ACTIVE') return '开班中';
  if (status === 'FINISHED') return '已结课';
  if (status === 'DISABLED') return '停用';
  return status || '未知';
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Classes" title="我的班级" subtitle="查看学生、课时和班级资料">
      <view class="hero-metrics">
        <view class="metric">
          <text class="metric-num">{{ activeClassCount }}</text>
          <text class="metric-label">班级</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ studentTotal }}</text>
          <text class="metric-label">学生</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ loading ? '--' : classes.length }}</text>
          <text class="metric-label">全部</text>
        </view>
      </view>
    </TeacherHeroCard>

    <view class="list">
      <view
        v-for="item in classes"
        :key="item.id"
        class="class-card"
        @tap="toClassDetail(item.id)"
      >
        <view class="class-main">
          <text class="class-name">{{ item.name }}</text>
          <text class="class-course">{{ item.courseName }}</text>
          <text v-if="item.courseSystem" class="class-system">{{ item.courseSystem }}</text>
        </view>
        <view class="class-side">
          <text class="status-pill">{{ statusLabel(item.status) }}</text>
          <text class="student-count">{{ item.studentCount || 0 }} 人</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <TeacherEmptyState
        v-if="!classes.length"
        :title="loading ? '正在加载班级' : '暂无班级'"
        description="有班级安排后，学生名单和课时概览会显示在这里。"
      />
    </view>

    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 160rpx;
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

.class-card {
  min-height: 150rpx;
  padding: 26rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.class-card:active {
  opacity: 0.76;
}

.class-main {
  flex: 1;
  min-width: 0;
}

.class-name,
.class-course,
.class-system {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.class-name {
  color: #17211d;
  font-size: 32rpx;
  font-weight: 900;
}

.class-course {
  margin-top: 10rpx;
  color: #58615b;
  font-size: 24rpx;
  font-weight: 800;
}

.class-system {
  margin-top: 7rpx;
  color: #969890;
  font-size: 22rpx;
}

.class-side {
  width: 130rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.status-pill {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e7f0ed;
  color: #1f5a44;
  font-size: 21rpx;
  font-weight: 900;
}

.student-count {
  color: #8a8d86;
  font-size: 22rpx;
  font-weight: 800;
}

.arrow {
  color: #b6b0a6;
  font-size: 42rpx;
  line-height: 1;
}
</style>
