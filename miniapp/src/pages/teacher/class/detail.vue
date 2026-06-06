<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherClassDetail } from '@/api/teacher';
import type { ClassDetail } from '@/types/api';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

const classDetail = ref<ClassDetail | null>(null);
const loading = ref(false);
const classId = ref(0);

const students = computed(() => classDetail.value?.students || []);
const remainingTotal = computed(() => students.value.reduce((sum, item) => sum + (item.remainingHours || 0), 0));

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const id = options?.id ? Number(options.id) : 0;
  if (id) {
    classId.value = id;
    fetchDetail(id);
  }
});

onPullDownRefresh(() => {
  if (!classId.value) {
    uni.stopPullDownRefresh();
    return;
  }
  fetchDetail(classId.value).finally(() => uni.stopPullDownRefresh());
});

async function fetchDetail(id: number) {
  loading.value = true;
  try {
    classDetail.value = await getTeacherClassDetail(id);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function toStudentProfile(studentId: number) {
  uni.navigateTo({ url: `/pages/teacher/student/profile?id=${studentId}` });
}

function statusLabel(status?: string) {
  if (status === 'ACTIVE') return '开班中';
  if (status === 'FINISHED') return '已结课';
  if (status === 'DISABLED') return '停用';
  return status || '未知';
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard
      eyebrow="Class Detail"
      :title="classDetail?.name || '班级详情'"
      :subtitle="classDetail?.courseName || '课程信息'"
    >
      <view class="hero-metrics">
        <view class="metric">
          <text class="metric-num">{{ students.length }}</text>
          <text class="metric-label">学生</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ remainingTotal }}</text>
          <text class="metric-label">剩余课时</text>
        </view>
        <view class="metric">
          <text class="metric-num">{{ statusLabel(classDetail?.status) }}</text>
          <text class="metric-label">状态</text>
        </view>
      </view>
    </TeacherHeroCard>

    <view class="panel">
      <view class="panel-head">
        <text class="panel-title">学生列表</text>
        <text class="panel-subtitle">{{ students.length }} 人</text>
      </view>

      <view
        v-for="item in students"
        :key="item.studentId"
        class="student-row"
        @tap="toStudentProfile(item.studentId)"
      >
        <image
          v-if="item.avatarUrl"
          class="avatar"
          :src="item.avatarUrl"
          mode="aspectFill"
        />
        <view v-else class="avatar avatar-fallback">{{ item.name.slice(0, 1) }}</view>
        <view class="student-main">
          <text class="student-name">{{ item.name }}</text>
          <text class="student-meta">{{ item.grade || '未填年级' }} · {{ item.school || '未填学校' }}</text>
          <text v-if="item.lastAttendanceStatus" class="student-meta">
            上次考勤 {{ item.lastAttendanceStatus }}
          </text>
        </view>
        <view class="student-side">
          <text class="hours" v-if="item.remainingHours !== undefined">
            {{ item.remainingHours }} 课时
          </text>
          <text class="arrow">›</text>
        </view>
      </view>

      <TeacherEmptyState
        v-if="!students.length"
        :title="loading ? '正在加载学生' : '暂无学生'"
        description="班级绑定学生后，会在这里展示课时和学习档案入口。"
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
  min-width: 0;
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
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.25;
  word-break: break-all;
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

.student-row {
  min-height: 126rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  border-top: 1rpx solid #eee5d8;
}

.student-row:active {
  opacity: 0.76;
}

.avatar {
  width: 76rpx;
  height: 76rpx;
  flex-shrink: 0;
  border-radius: 24rpx;
  background: #e7f0ed;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f5a44;
  font-size: 28rpx;
  font-weight: 900;
}

.student-main {
  flex: 1;
  min-width: 0;
}

.student-name,
.student-meta {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.student-name {
  color: #17211d;
  font-size: 29rpx;
  font-weight: 900;
}

.student-meta {
  margin-top: 7rpx;
  color: #8b8d87;
  font-size: 22rpx;
}

.student-side {
  width: 116rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.hours {
  color: #9a6710;
  font-size: 22rpx;
  font-weight: 900;
}

.arrow {
  color: #b6b0a6;
  font-size: 42rpx;
  line-height: 1;
}
</style>
