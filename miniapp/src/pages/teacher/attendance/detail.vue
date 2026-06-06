<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { batchAttendance, deductLessonHours, getAttendanceDetail } from '@/api/teacher';
import type { AttendanceDetail, AttendanceStatus, AttendanceStudentItem } from '@/types/api';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';

type LocalAttendanceStatus = AttendanceStatus | 'UNSET';

interface AttendanceStatusMeta {
  label: string;
  value: AttendanceStatus;
  tone: string;
  deduct: boolean;
  remarkRequired: boolean;
}

const statusOptions: AttendanceStatusMeta[] = [
  { label: '出勤', value: 'PRESENT', tone: 'present', deduct: true, remarkRequired: false },
  { label: '迟到', value: 'LATE', tone: 'late', deduct: true, remarkRequired: false },
  { label: '早退', value: 'LEAVE_EARLY', tone: 'leave-early', deduct: true, remarkRequired: false },
  { label: '缺勤', value: 'ABSENT', tone: 'absent', deduct: true, remarkRequired: true },
  { label: '病假', value: 'SICK_LEAVE', tone: 'sick-leave', deduct: false, remarkRequired: true },
  { label: '事假', value: 'PERSONAL_LEAVE', tone: 'personal-leave', deduct: false, remarkRequired: true },
  { label: '补课', value: 'MAKEUP', tone: 'makeup', deduct: true, remarkRequired: false },
];

const detail = ref<AttendanceDetail | null>(null);
const loading = ref(false);
const submitting = ref(false);
const showPendingOnly = ref(false);
const currentScheduleId = ref(0);
const activeStudentId = ref<number>();
const draftStatus = ref<AttendanceStatus>('PRESENT');
const draftRemark = ref('');
const studentStatuses = ref<Record<number, LocalAttendanceStatus>>({});
const studentRemarks = ref<Record<number, string>>({});

const students = computed(() => detail.value?.students || []);
const activeStudent = computed(() => students.value.find((item) => item.studentId === activeStudentId.value));
const hasStatusSheet = computed(() => Boolean(activeStudent.value));
const filteredStudents = computed(() => {
  if (!showPendingOnly.value) return students.value;
  return students.value.filter((item) => getStudentStatus(item.studentId) === 'UNSET');
});
const handledCount = computed(() => students.value.filter((item) => getStudentStatus(item.studentId) !== 'UNSET').length);
const totalCount = computed(() => students.value.length);
const pendingCount = computed(() => Math.max(0, totalCount.value - handledCount.value));
const progressPercent = computed(() => totalCount.value ? Math.round((handledCount.value / totalCount.value) * 100) : 0);
const deductCount = computed(() => students.value.filter((item) => statusMeta(getStudentStatus(item.studentId))?.deduct).length);
const presentCount = computed(() => countByStatus('PRESENT'));
const leaveCount = computed(() => countByStatus('SICK_LEAVE') + countByStatus('PERSONAL_LEAVE'));
const absentCount = computed(() => countByStatus('ABSENT'));
const makeupCount = computed(() => countByStatus('MAKEUP'));
const courseTime = computed(() => `${formatDate(detail.value?.lessonDate)} ${formatTime(detail.value?.startTime)}-${formatTime(detail.value?.endTime)}`);
const bottomHint = computed(() => pendingCount.value ? `剩余 ${pendingCount.value} 人待处理` : `预计扣课 ${deductCount.value} 人`);

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const rawScheduleId = options?.scheduleId || options?.id;
  const scheduleId = rawScheduleId ? Number(rawScheduleId) : 0;
  if (scheduleId) {
    currentScheduleId.value = scheduleId;
    fetchDetail(scheduleId);
  } else {
    uni.showToast({ title: '缺少课程信息', icon: 'none' });
  }
});

onPullDownRefresh(() => {
  if (!currentScheduleId.value) {
    uni.stopPullDownRefresh();
    return;
  }
  fetchDetail(currentScheduleId.value).finally(() => uni.stopPullDownRefresh());
});

async function fetchDetail(scheduleId: number) {
  loading.value = true;
  try {
    const res = await getAttendanceDetail(scheduleId);
    detail.value = res;
    const nextStatuses: Record<number, LocalAttendanceStatus> = {};
    const nextRemarks: Record<number, string> = {};
    res.students.forEach((student) => {
      nextStatuses[student.studentId] = normalizeStatus(student.status);
      nextRemarks[student.studentId] = student.remark || '';
    });
    studentStatuses.value = nextStatuses;
    studentRemarks.value = nextRemarks;
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function normalizeStatus(status?: string): LocalAttendanceStatus {
  if (status === 'LEAVE') return 'PERSONAL_LEAVE';
  return statusOptions.some((item) => item.value === status) ? status as AttendanceStatus : 'UNSET';
}

function getStudentStatus(studentId: number): LocalAttendanceStatus {
  return studentStatuses.value[studentId] || 'UNSET';
}

function statusMeta(status: LocalAttendanceStatus) {
  if (status === 'UNSET') return undefined;
  return statusOptions.find((item) => item.value === status);
}

function statusLabel(status: LocalAttendanceStatus) {
  return status === 'UNSET' ? '待处理' : statusMeta(status)?.label || '待处理';
}

function statusClass(status: LocalAttendanceStatus) {
  const meta = statusMeta(status);
  return meta ? `tone-${meta.tone}` : 'tone-unset';
}

function countByStatus(status: AttendanceStatus) {
  return students.value.filter((item) => getStudentStatus(item.studentId) === status).length;
}

function setAllPresent() {
  markStudents('PRESENT', true);
}

function bulkMark() {
  uni.showActionSheet({
    itemList: ['未处理设为出勤', '未处理设为缺勤', '全部设为出勤'],
    success: (res) => {
      if (res.tapIndex === 0) markStudents('PRESENT', true);
      if (res.tapIndex === 1) markStudents('ABSENT', true);
      if (res.tapIndex === 2) markStudents('PRESENT', false);
    },
  });
}

function markStudents(status: AttendanceStatus, onlyPending: boolean) {
  const next = { ...studentStatuses.value };
  students.value.forEach((student) => {
    if (!onlyPending || next[student.studentId] === 'UNSET') {
      next[student.studentId] = status;
    }
  });
  studentStatuses.value = next;
}

function togglePendingFilter() {
  showPendingOnly.value = !showPendingOnly.value;
}

function openStudentSheet(student: AttendanceStudentItem) {
  activeStudentId.value = student.studentId;
  const current = getStudentStatus(student.studentId);
  draftStatus.value = current === 'UNSET' ? 'PRESENT' : current;
  draftRemark.value = studentRemarks.value[student.studentId] || '';
}

function closeStatusSheet() {
  activeStudentId.value = undefined;
  draftRemark.value = '';
  draftStatus.value = 'PRESENT';
}

function selectDraftStatus(status: AttendanceStatus) {
  draftStatus.value = status;
}

function saveStudentSheet() {
  if (!activeStudent.value) return;
  if (statusMeta(draftStatus.value)?.remarkRequired && !draftRemark.value.trim()) {
    uni.showToast({ title: '请填写原因备注', icon: 'none' });
    return;
  }
  studentStatuses.value = {
    ...studentStatuses.value,
    [activeStudent.value.studentId]: draftStatus.value,
  };
  studentRemarks.value = {
    ...studentRemarks.value,
    [activeStudent.value.studentId]: draftRemark.value.trim(),
  };
  closeStatusSheet();
}

async function completeAttendance() {
  if (!detail.value || submitting.value) return;
  const pendingStudents = students.value.filter((student) => getStudentStatus(student.studentId) === 'UNSET');
  if (pendingStudents.length) {
    uni.showToast({ title: `还有 ${pendingStudents.length} 人待处理`, icon: 'none' });
    return;
  }
  const missingRemark = students.value.find((student) => {
    const status = getStudentStatus(student.studentId);
    return Boolean(statusMeta(status)?.remarkRequired) && !studentRemarks.value[student.studentId]?.trim();
  });
  if (missingRemark) {
    uni.showToast({ title: `${missingRemark.studentName} 需填写原因`, icon: 'none' });
    return;
  }

  const confirmed = await confirmComplete();
  if (!confirmed) return;

  submitting.value = true;
  try {
    await batchAttendance(detail.value.scheduleId, {
      attendances: students.value.map((student) => ({
        studentId: student.studentId,
        status: getStudentStatus(student.studentId) as AttendanceStatus,
        remark: studentRemarks.value[student.studentId] || '',
      })),
    });
    try {
      await deductLessonHours(detail.value.scheduleId, { scheduleId: detail.value.scheduleId });
      uni.showToast({ title: '考勤完成', icon: 'success' });
    } catch (error) {
      uni.showToast({ title: '考勤已保存，扣课失败', icon: 'none' });
    }
    await fetchDetail(detail.value.scheduleId);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

function confirmComplete() {
  return new Promise<boolean>((resolve) => {
    uni.showModal({
      title: '确认完成考勤',
      content: `出勤${presentCount.value}人，请假${leaveCount.value}人，缺勤${absentCount.value}人，补课${makeupCount.value}人。预计扣课${deductCount.value}人。`,
      confirmText: '完成',
      cancelText: '再检查',
      success: (res) => resolve(Boolean(res.confirm)),
      fail: () => resolve(false),
    });
  });
}

function formatTime(time?: string) {
  return time ? time.substring(0, 5) : '--:--';
}

function formatDate(date?: string) {
  if (!date) return '--';
  const [, month, day] = date.split('-');
  return `${Number(month)}月${Number(day)}日`;
}
</script>

<template>
  <view class="page">
    <view v-if="detail" class="content">
      <view class="course-card">
        <view class="course-top">
          <view class="course-copy">
            <text class="eyebrow">ATTENDANCE</text>
            <text class="course-title">{{ detail.topic }}</text>
          </view>
          <view class="progress-badge">
            <text class="progress-number">{{ progressPercent }}%</text>
            <text class="progress-label">完成</text>
          </view>
        </view>
        <text class="course-line">{{ detail.courseName || detail.className }}</text>
        <view class="course-meta">
          <text>{{ detail.className }}</text>
          <text>{{ courseTime }}</text>
          <text>{{ detail.classroom || '教室待定' }}</text>
          <text>{{ detail.lessonHours }} 课时</text>
        </view>
        <view class="progress-track">
          <view class="progress-fill" :style="{ width: `${progressPercent}%` }" />
        </view>
      </view>

      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-value">{{ handledCount }}/{{ totalCount }}</text>
          <text class="stat-label">已处理</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ leaveCount }}</text>
          <text class="stat-label">请假</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ deductCount }}</text>
          <text class="stat-label">扣课</text>
        </view>
      </view>

      <view class="toolbar">
        <button class="tool-button primary" @tap="setAllPresent">一键全勤</button>
        <button class="tool-button" @tap="bulkMark">批量标记</button>
        <button class="tool-button" :class="{ active: showPendingOnly }" @tap="togglePendingFilter">
          未处理
        </button>
      </view>

      <view class="student-panel">
        <view class="panel-head">
          <text class="panel-title">学生列表</text>
          <text class="panel-count">{{ filteredStudents.length }} 人</text>
        </view>

        <view
          v-for="student in filteredStudents"
          :key="student.studentId"
          class="student-row"
          @tap="openStudentSheet(student)"
        >
          <image
            v-if="student.studentAvatarUrl"
            class="avatar"
            :src="student.studentAvatarUrl"
            mode="aspectFill"
          />
          <view v-else class="avatar avatar-fallback">{{ student.studentName.slice(0, 1) }}</view>
          <view class="student-main">
            <view class="student-name-row">
              <text class="student-name">{{ student.studentName }}</text>
              <text class="status-pill" :class="statusClass(getStudentStatus(student.studentId))">
                {{ statusLabel(getStudentStatus(student.studentId)) }}
              </text>
            </view>
            <text v-if="studentRemarks[student.studentId]" class="remark-line">{{ studentRemarks[student.studentId] }}</text>
            <text v-else-if="statusMeta(getStudentStatus(student.studentId))?.remarkRequired" class="remark-required">需要填写原因</text>
            <text v-else class="remark-line muted">未填写备注</text>
          </view>
        </view>

        <TeacherEmptyState
          v-if="!filteredStudents.length"
          :title="showPendingOnly ? '没有待处理学生' : '暂无学生'"
        />
      </view>
    </view>

    <TeacherEmptyState
      v-else
      :title="loading ? '正在加载考勤' : '暂无课程信息'"
      description="请从今日考勤或课表进入课程考勤详情。"
    />

    <view class="bottom-space" />
    <view class="bottom-bar">
      <view class="bottom-copy">
        <text class="bottom-title">{{ handledCount }}/{{ totalCount }} 已处理</text>
        <text class="bottom-subtitle">{{ bottomHint }}</text>
      </view>
      <button
        class="complete-button"
        :class="{ disabled: submitting || !totalCount }"
        :disabled="submitting || !totalCount"
        @tap="completeAttendance"
      >
        {{ submitting ? '处理中' : '一键完成' }}
      </button>
    </view>

    <view v-if="hasStatusSheet" class="sheet-mask" @tap="closeStatusSheet">
      <view class="status-sheet" @tap.stop>
        <view class="sheet-handle" />
        <view class="sheet-head">
          <view>
            <text class="sheet-title">{{ activeStudent?.studentName }}</text>
            <text class="sheet-subtitle">选择本节课考勤状态</text>
          </view>
          <button class="sheet-close" @tap="closeStatusSheet">×</button>
        </view>
        <view class="status-grid">
          <button
            v-for="option in statusOptions"
            :key="option.value"
            class="status-option"
            :class="[statusClass(option.value), { active: draftStatus === option.value }]"
            @tap="selectDraftStatus(option.value)"
          >
            <text>{{ option.label }}</text>
            <text class="option-meta">{{ option.deduct ? '扣课' : '不扣课' }}</text>
          </button>
        </view>
        <textarea
          v-model="draftRemark"
          class="remark-input"
          :placeholder="statusMeta(draftStatus)?.remarkRequired ? '请填写原因，提交前必填' : '备注选填'"
        />
        <button class="save-status" @tap="saveStudentSheet">保存状态</button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 0;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

button {
  margin: 0;
  padding: 0;
  border: 0;
  line-height: 1;
}

button::after {
  border: 0;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.course-card {
  padding: 30rpx;
  border-radius: 30rpx;
  background: #1f5a44;
  color: #fff;
  box-shadow: 0 18rpx 36rpx rgba(31, 90, 68, 0.16);
}

.course-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 22rpx;
}

.course-copy {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  display: block;
  color: #f0b84d;
  font-size: 21rpx;
  font-weight: 900;
  letter-spacing: 3rpx;
}

.course-title {
  display: block;
  margin-top: 10rpx;
  color: #fff;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.16;
  word-break: break-all;
}

.progress-badge {
  width: 116rpx;
  height: 116rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.12);
}

.progress-number {
  color: #f0b84d;
  font-size: 32rpx;
  font-weight: 900;
}

.progress-label {
  margin-top: 4rpx;
  font-size: 19rpx;
  font-weight: 800;
}

.course-line {
  display: block;
  margin-top: 16rpx;
  color: rgba(255, 255, 255, 0.82);
  font-size: 25rpx;
  font-weight: 800;
}

.course-meta {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.course-meta text {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.86);
  font-size: 21rpx;
  font-weight: 800;
}

.progress-track {
  height: 12rpx;
  margin-top: 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: #f0b84d;
}

.stats-row {
  display: flex;
  gap: 14rpx;
}

.stat-item {
  flex: 1;
  min-height: 112rpx;
  padding: 20rpx;
  box-sizing: border-box;
  border-radius: 24rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 24rpx rgba(54, 43, 30, 0.04);
}

.stat-value {
  display: block;
  color: #17211d;
  font-size: 34rpx;
  font-weight: 900;
}

.stat-label {
  display: block;
  margin-top: 6rpx;
  color: #8c8f88;
  font-size: 22rpx;
  font-weight: 800;
}

.toolbar {
  display: flex;
  gap: 12rpx;
}

.tool-button {
  flex: 1;
  height: 70rpx;
  border-radius: 20rpx;
  background: #fffcf5;
  color: #56605a;
  font-size: 24rpx;
  font-weight: 900;
}

.tool-button.primary,
.tool-button.active {
  background: #1f5a44;
  color: #fff;
}

.student-panel {
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 24rpx rgba(54, 43, 30, 0.04);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.panel-title {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.panel-count {
  color: #8c8f88;
  font-size: 22rpx;
  font-weight: 800;
}

.student-row {
  min-height: 118rpx;
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

.student-main {
  flex: 1;
  min-width: 0;
}

.student-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.student-name {
  color: #17211d;
  font-size: 28rpx;
  font-weight: 900;
}

.status-pill {
  min-width: 86rpx;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  text-align: center;
  font-size: 21rpx;
  font-weight: 900;
}

.remark-line,
.remark-required {
  display: block;
  margin-top: 8rpx;
  color: #747a74;
  font-size: 22rpx;
  line-height: 1.35;
}

.remark-line.muted {
  color: #b0aaa1;
}

.remark-required {
  color: #b23f31;
  font-weight: 800;
}

.tone-unset {
  background: #eee8de;
  color: #78736a;
}

.tone-present {
  background: #e7f0ed;
  color: #1f5a44;
}

.tone-late,
.tone-leave-early {
  background: #fff1d4;
  color: #9a6710;
}

.tone-absent {
  background: #f9ded8;
  color: #b23f31;
}

.tone-sick-leave,
.tone-personal-leave {
  background: #e7edf6;
  color: #395c8a;
}

.tone-makeup {
  background: #f4e4f0;
  color: #7f4276;
}

.bottom-space {
  height: 170rpx;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: rgba(255, 252, 245, 0.96);
  border-top: 1rpx solid #e6ded0;
  box-shadow: 0 -12rpx 30rpx rgba(23, 33, 29, 0.08);
}

.bottom-copy {
  flex: 1;
  min-width: 0;
}

.bottom-title {
  display: block;
  color: #17211d;
  font-size: 26rpx;
  font-weight: 900;
}

.bottom-subtitle {
  display: block;
  margin-top: 4rpx;
  color: #858982;
  font-size: 21rpx;
}

.complete-button {
  width: 190rpx;
  height: 84rpx;
  border-radius: 24rpx;
  background: #17211d;
  color: #fff;
  font-size: 28rpx;
  font-weight: 900;
  box-shadow: 0 14rpx 26rpx rgba(23, 33, 29, 0.18);
}

.complete-button.disabled {
  opacity: 0.42;
  box-shadow: none;
}

.sheet-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 30;
  display: flex;
  align-items: flex-end;
  background: rgba(23, 33, 29, 0.36);
}

.status-sheet {
  width: 100%;
  padding: 18rpx 28rpx calc(30rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  border-radius: 34rpx 34rpx 0 0;
  background: #fffcf5;
}

.sheet-handle {
  width: 72rpx;
  height: 8rpx;
  margin: 0 auto 22rpx;
  border-radius: 999rpx;
  background: #ddd4c7;
}

.sheet-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
}

.sheet-title {
  display: block;
  color: #17211d;
  font-size: 34rpx;
  font-weight: 900;
}

.sheet-subtitle {
  display: block;
  margin-top: 6rpx;
  color: #858982;
  font-size: 23rpx;
}

.sheet-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 18rpx;
  background: #f2ece2;
  color: #17211d;
  font-size: 36rpx;
  font-weight: 700;
}

.status-grid {
  margin-top: 24rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.status-option {
  width: calc((100% - 12rpx) / 2);
  min-height: 86rpx;
  padding: 14rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  border: 2rpx solid transparent;
  border-radius: 22rpx;
  font-size: 27rpx;
  font-weight: 900;
}

.status-option.active {
  border-color: currentColor;
  box-shadow: 0 10rpx 24rpx rgba(23, 33, 29, 0.08);
}

.option-meta {
  font-size: 20rpx;
  font-weight: 800;
  opacity: 0.72;
}

.remark-input {
  width: 100%;
  min-height: 132rpx;
  margin-top: 18rpx;
  padding: 20rpx;
  box-sizing: border-box;
  border-radius: 22rpx;
  background: #f5f0e8;
  color: #17211d;
  font-size: 26rpx;
  line-height: 1.5;
}

.save-status {
  height: 84rpx;
  margin-top: 18rpx;
  border-radius: 24rpx;
  background: #1f5a44;
  color: #fff;
  font-size: 28rpx;
  font-weight: 900;
}
</style>
