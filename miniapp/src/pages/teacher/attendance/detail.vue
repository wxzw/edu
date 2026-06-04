<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getAttendanceDetail, batchAttendance, deductLessonHours } from '@/api/teacher';
import type { AttendanceDetail } from '@/types/api';

const detail = ref<AttendanceDetail | null>(null);
const loading = ref(false);
const studentStatuses = ref<Record<number, string>>({});

async function fetchDetail(scheduleId: number) {
  loading.value = true;
  try {
    const res = await getAttendanceDetail(scheduleId);
    detail.value = res;
    studentStatuses.value = {};
    detail.value?.students?.forEach(s => {
      studentStatuses.value[s.studentId] = s.status === 'UNSET' ? 'PRESENT' : s.status;
    });
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const scheduleId = options?.scheduleId ? parseInt(options.scheduleId, 10) : 0;
  if (scheduleId) fetchDetail(scheduleId);
});

function setStatus(studentId: number, status: string) {
  studentStatuses.value[studentId] = status;
}

function setAllPresent() {
  detail.value?.students?.forEach(s => {
    studentStatuses.value[s.studentId] = 'PRESENT';
  });
}

async function submitAttendance() {
  if (!detail.value) return;
  const attendances = detail.value.students.map(s => ({
    studentId: s.studentId,
    status: studentStatuses.value[s.studentId] || 'PRESENT',
    remark: '',
  }));
  try {
    await batchAttendance(detail.value.scheduleId, { attendances });
    uni.showToast({ title: '考勤提交成功', icon: 'success' });
  } catch (e) {
    uni.showToast({ title: '提交失败', icon: 'none' });
  }
}

async function submitDeduct() {
  if (!detail.value) return;
  uni.showModal({
    title: '确认扣课',
    content: '考勤确认后将按规则扣减课时，是否继续？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deductLessonHours(detail.value!.scheduleId, {
            scheduleId: detail.value!.scheduleId,
          });
          uni.showToast({ title: '扣课成功', icon: 'success' });
        } catch (e) {
          uni.showToast({ title: '扣课失败', icon: 'none' });
        }
      }
    },
  });
}

function formatTime(time?: string) {
  if (!time) return '';
  return time.substring(0, 5);
}

const statusOptions = [
  { label: '出勤', value: 'PRESENT' },
  { label: '迟到', value: 'LATE' },
  { label: '早退', value: 'LEAVE_EARLY' },
  { label: '缺勤', value: 'ABSENT' },
  { label: '病假', value: 'SICK_LEAVE' },
  { label: '事假', value: 'PERSONAL_LEAVE' },
  { label: '补课', value: 'MAKEUP' },
];
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Attendance</text>
      <text class="title">{{ detail?.className }}</text>
      <text class="subtitle">{{ detail?.topic }}</text>
    </view>

    <view class="panel">
      <view class="panel-header">
        <text class="panel-title">学生考勤</text>
        <button class="btn-small" @tap="setAllPresent">一键全勤</button>
      </view>

      <view
        class="student-row"
        v-for="item in detail?.students"
        :key="item.studentId"
      >
        <image
          class="avatar"
          :src="item.studentAvatarUrl || '/static/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="student-info">
          <text class="student-name">{{ item.studentName }}</text>
          <text class="student-status">{{ studentStatuses[item.studentId] }}</text>
        </view>
        <view class="status-btns">
          <button
            v-for="opt in statusOptions"
            :key="opt.value"
            class="status-btn"
            :class="{ active: studentStatuses[item.studentId] === opt.value }"
            @tap="setStatus(item.studentId, opt.value)"
          >
            {{ opt.label }}
          </button>
        </view>
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-primary" @tap="submitAttendance">提交考勤</button>
      <button class="btn-secondary" @tap="submitDeduct">确认扣课</button>
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

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18rpx;
}

.panel-title {
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.btn-small {
  width: 140rpx;
  height: 52rpx;
  border-radius: 10rpx;
  background: #22624c;
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
}

.student-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 0;
  border-top: 1px solid #F0EAE0;
}

.avatar {
  width: 64rpx;
  height: 64rpx;
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
  font-size: 26rpx;
  font-weight: 700;
  color: #17211d;
}

.student-status {
  font-size: 22rpx;
  color: #999;
}

.status-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  max-width: 320rpx;
  justify-content: flex-end;
}

.status-btn {
  width: 72rpx;
  height: 44rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 20rpx;
  font-weight: 600;
}

.status-btn.active {
  background: #22624c;
  color: #fff;
}

.action-bar {
  margin-top: 40rpx;
  display: flex;
  gap: 20rpx;
}

.btn-primary {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
}

.btn-secondary {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  background: #e6a23c;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
}
</style>
