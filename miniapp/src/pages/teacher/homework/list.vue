<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherHomeworks } from '@/api/teacher';
import type { TeacherHomeworkListItem } from '@/types/api';
import AppTabBar from '@/components/AppTabBar.vue';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

type HomeworkFilter = 'ALL' | 'PENDING' | 'PUBLISHED' | 'DRAFT';

const homeworks = ref<TeacherHomeworkListItem[]>([]);
const loading = ref(false);
const activeFilter = ref<HomeworkFilter>('ALL');

const filterTabs: Array<{ label: string; value: HomeworkFilter }> = [
  { label: '全部', value: 'ALL' },
  { label: '待点评', value: 'PENDING' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '草稿', value: 'DRAFT' },
];

const pendingCount = computed(() => homeworks.value.reduce((sum, item) => sum + (item.pendingSubmissions || 0), 0));
const publishedCount = computed(() => homeworks.value.filter((item) => item.status === 'PUBLISHED').length);
const draftCount = computed(() => homeworks.value.filter((item) => item.status === 'DRAFT').length);
const filteredHomeworks = computed(() => {
  if (activeFilter.value === 'ALL') return homeworks.value;
  if (activeFilter.value === 'PENDING') return homeworks.value.filter((item) => (item.pendingSubmissions || 0) > 0);
  return homeworks.value.filter((item) => item.status === activeFilter.value);
});

async function fetchHomeworks() {
  loading.value = true;
  try {
    homeworks.value = await getTeacherHomeworks();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchHomeworks();
});

onPullDownRefresh(() => {
  fetchHomeworks().finally(() => uni.stopPullDownRefresh());
});

function toCreate() {
  uni.navigateTo({ url: '/pages/teacher/homework/create' });
}

function toDetail(id: number) {
  uni.navigateTo({ url: `/pages/teacher/homework/detail?id=${id}` });
}

function formatDate(date?: string) {
  return date ? date.substring(0, 10) : '未设置';
}

function statusLabel(status: string) {
  if (status === 'PUBLISHED') return '已发布';
  if (status === 'DRAFT') return '草稿';
  return status;
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Homework" title="作业管理" subtitle="发布、跟进、点评学生作业">
      <template #action>
        <button class="publish-button" @tap="toCreate">发布</button>
      </template>
      <view class="hero-metrics">
        <view class="metric">
          <text class="metric-num">{{ homeworks.length }}</text>
          <text class="metric-label">全部</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ pendingCount }}</text>
          <text class="metric-label">待点评</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ publishedCount }}</text>
          <text class="metric-label">已发布</text>
        </view>
      </view>
    </TeacherHeroCard>

    <view class="filter-tabs">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeFilter === tab.value }"
        @tap="activeFilter = tab.value"
      >
        {{ tab.label }}
      </view>
    </view>

    <view class="list">
      <view
        v-for="item in filteredHomeworks"
        :key="item.id"
        class="homework-card"
        @tap="toDetail(item.id)"
      >
        <view class="card-top">
          <view class="card-copy">
            <text class="homework-title">{{ item.title }}</text>
            <text class="homework-class">{{ item.className || '未关联班级' }}</text>
          </view>
          <text class="status-pill" :class="{ draft: item.status === 'DRAFT' }">
            {{ statusLabel(item.status) }}
          </text>
        </view>
        <text class="homework-content">{{ item.content || '暂无作业说明' }}</text>
        <view class="card-footer">
          <text>截止 {{ formatDate(item.deadline) }}</text>
          <text v-if="item.checkinEnabled">打卡</text>
          <text v-if="item.attachmentCount">{{ item.attachmentCount }} 附件</text>
          <text v-if="item.pendingSubmissions" class="pending">待点评 {{ item.pendingSubmissions }}</text>
        </view>
      </view>

      <TeacherEmptyState
        v-if="!filteredHomeworks.length"
        :title="loading ? '正在加载作业' : '暂无作业'"
        description="可以发布一份班级作业，学生提交后会出现在这里。"
        action-text="发布作业"
        @action="toCreate"
      />
    </view>

    <view class="draft-count" v-if="draftCount">
      <text>{{ draftCount }} 份草稿尚未发布</text>
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

button {
  margin: 0;
  padding: 0;
}

button::after {
  border: 0;
}

.publish-button {
  width: 116rpx;
  height: 62rpx;
  border-radius: 999rpx;
  background: rgba(255, 252, 245, 0.16);
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 62rpx;
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

.filter-tabs {
  margin-top: 22rpx;
  padding: 6rpx;
  display: flex;
  gap: 6rpx;
  border-radius: 20rpx;
  background: #e8e2d8;
}

.filter-tab {
  flex: 1;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  color: #747970;
  font-size: 23rpx;
  font-weight: 900;
}

.filter-tab.active {
  background: #fffcf5;
  color: #17211d;
  box-shadow: 0 8rpx 18rpx rgba(54, 43, 30, 0.06);
}

.list {
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.homework-card {
  padding: 26rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.homework-card:active {
  opacity: 0.76;
}

.card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.card-copy {
  flex: 1;
  min-width: 0;
}

.homework-title {
  display: block;
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.26;
}

.homework-class {
  display: block;
  margin-top: 8rpx;
  color: #6e756f;
  font-size: 23rpx;
  font-weight: 800;
}

.status-pill {
  flex-shrink: 0;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e7f0ed;
  color: #1f5a44;
  font-size: 21rpx;
  font-weight: 900;
}

.status-pill.draft {
  background: #eee8de;
  color: #777268;
}

.homework-content {
  display: block;
  margin-top: 18rpx;
  color: #39403a;
  font-size: 25rpx;
  line-height: 1.5;
}

.card-footer {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.card-footer text {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #f4efe6;
  color: #7d827c;
  font-size: 21rpx;
  font-weight: 800;
}

.card-footer .pending {
  background: #fff1d4;
  color: #9a6710;
}

.draft-count {
  margin-top: 18rpx;
  color: #8a8d86;
  font-size: 22rpx;
  text-align: center;
}
</style>
