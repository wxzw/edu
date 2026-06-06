<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherStudentProfile } from '@/api/teacher';
import type { TeacherStudentProfile } from '@/types/api';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

const profile = ref<TeacherStudentProfile | null>(null);
const loading = ref(false);
const studentId = ref(0);

const remainingTotal = computed(() => profile.value?.lessonAccounts?.reduce((sum, item) => sum + (item.remainingHours || 0), 0) || 0);
const classNames = computed(() => profile.value?.classNames?.join('、') || '-');

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const id = options?.id ? Number(options.id) : 0;
  if (id) {
    studentId.value = id;
    fetchProfile(id);
  }
});

onPullDownRefresh(() => {
  if (!studentId.value) {
    uni.stopPullDownRefresh();
    return;
  }
  fetchProfile(studentId.value).finally(() => uni.stopPullDownRefresh());
});

async function fetchProfile(id: number) {
  loading.value = true;
  try {
    profile.value = await getTeacherStudentProfile(id);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function formatDate(date?: string) {
  return date ? date.substring(0, 10) : '';
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard
      eyebrow="Student Profile"
      :title="profile?.name || '学生档案'"
      :subtitle="profile?.nickname || classNames"
    >
      <view class="profile-tags">
        <text>{{ profile?.grade || '未填年级' }}</text>
        <text>{{ profile?.school || '未填学校' }}</text>
        <text>{{ profile?.campusName || '当前校区' }}</text>
      </view>
    </TeacherHeroCard>

    <view class="summary-grid">
      <view class="summary-card">
        <text class="summary-value">{{ remainingTotal }}</text>
        <text class="summary-label">剩余课时</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ profile?.attendanceStats?.attendanceRate || 0 }}%</text>
        <text class="summary-label">出勤率</text>
      </view>
      <view class="summary-card">
        <text class="summary-value">{{ profile?.homeworkStats?.completionRate || 0 }}%</text>
        <text class="summary-label">作业完成</text>
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
        <text class="info-value">{{ classNames }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">课时概览</text>
      <view
        v-for="item in profile?.lessonAccounts"
        :key="item.id"
        class="account-row"
      >
        <text class="account-name">{{ item.courseName }}</text>
        <text class="account-hours">{{ item.remainingHours }} 课时</text>
      </view>
      <TeacherEmptyState
        v-if="!profile?.lessonAccounts?.length"
        :title="loading ? '正在加载课时' : '暂无课时记录'"
      />
    </view>

    <view class="panel">
      <text class="panel-title">学习统计</text>
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
          <text class="stat-num">{{ profile?.homeworkStats?.submittedCount || 0 }}</text>
          <text class="stat-label">已提交</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ profile?.homeworkStats?.commentedCount || 0 }}</text>
          <text class="stat-label">已点评</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">最近课消记录</text>
      <view
        v-for="item in profile?.lessonRecords"
        :key="item.id"
        class="record-row"
      >
        <view class="record-main">
          <text class="record-name">{{ item.courseName }}</text>
          <text class="record-topic">{{ item.lessonTopic || item.className || '课时变动' }}</text>
        </view>
        <view class="record-side">
          <text class="record-delta" :class="{ negative: item.hoursDelta < 0 }">
            {{ item.hoursDelta > 0 ? '+' : '' }}{{ item.hoursDelta }}
          </text>
          <text class="record-date">{{ formatDate(item.occurredAt) }}</text>
        </view>
      </view>
      <TeacherEmptyState
        v-if="!profile?.lessonRecords?.length"
        :title="loading ? '正在加载记录' : '暂无课消记录'"
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

.profile-tags {
  margin-top: 24rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.profile-tags text {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 252, 245, 0.12);
  color: rgba(255, 255, 255, 0.8);
  font-size: 21rpx;
  font-weight: 800;
}

.summary-grid {
  margin-top: 22rpx;
  display: flex;
  gap: 14rpx;
}

.summary-card {
  flex: 1;
  min-height: 120rpx;
  padding: 20rpx 18rpx;
  box-sizing: border-box;
  border-radius: 26rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.summary-value,
.summary-label {
  display: block;
}

.summary-value {
  color: #1f5a44;
  font-size: 36rpx;
  font-weight: 900;
}

.summary-label {
  margin-top: 8rpx;
  color: #8c8f88;
  font-size: 21rpx;
  font-weight: 800;
}

.panel {
  margin-top: 22rpx;
  padding: 26rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.panel-title {
  display: block;
  margin-bottom: 14rpx;
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.info-row,
.account-row,
.record-row {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  border-top: 1rpx solid #eee5d8;
}

.info-label {
  color: #777d76;
  font-size: 24rpx;
  font-weight: 800;
}

.info-value {
  flex: 1;
  min-width: 0;
  color: #17211d;
  font-size: 24rpx;
  font-weight: 900;
  text-align: right;
  word-break: break-all;
}

.account-name {
  color: #17211d;
  font-size: 26rpx;
  font-weight: 900;
}

.account-hours {
  color: #9a6710;
  font-size: 25rpx;
  font-weight: 900;
}

.stats-grid {
  display: flex;
  gap: 12rpx;
}

.stat-item {
  flex: 1;
  min-height: 112rpx;
  padding: 18rpx 8rpx;
  box-sizing: border-box;
  border-radius: 22rpx;
  background: #f5f0e8;
  text-align: center;
}

.stat-num,
.stat-label {
  display: block;
}

.stat-num {
  color: #1f5a44;
  font-size: 31rpx;
  font-weight: 900;
}

.stat-label {
  margin-top: 8rpx;
  color: #858982;
  font-size: 20rpx;
  font-weight: 800;
}

.record-row {
  min-height: 92rpx;
}

.record-main {
  flex: 1;
  min-width: 0;
}

.record-name,
.record-topic,
.record-date {
  display: block;
}

.record-name {
  color: #17211d;
  font-size: 26rpx;
  font-weight: 900;
}

.record-topic {
  margin-top: 6rpx;
  color: #858982;
  font-size: 22rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-side {
  flex-shrink: 0;
  text-align: right;
}

.record-delta {
  color: #1f5a44;
  font-size: 27rpx;
  font-weight: 900;
}

.record-delta.negative {
  color: #9a6710;
}

.record-date {
  margin-top: 6rpx;
  color: #9a9d96;
  font-size: 21rpx;
}
</style>
