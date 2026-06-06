<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { commentTeacherHomework, getTeacherHomeworkDetail } from '@/api/teacher';
import type { CommentHomeworkRequest, HomeworkComment, HomeworkSubmissionItem, TeacherHomeworkDetail } from '@/types/api';
import StarRating from '@/components/StarRating.vue';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

type SubmissionFilter = 'ALL' | 'SUBMITTED' | 'COMMENTED' | 'RESUBMIT_REQUIRED';

const homework = ref<TeacherHomeworkDetail | null>(null);
const loading = ref(false);
const submitting = ref(false);
const homeworkId = ref(0);
const showComment = ref(false);
const currentSubmissionId = ref(0);
const activeFilter = ref<SubmissionFilter>('ALL');
const commentForm = ref<CommentHomeworkRequest>({
  commentText: '',
  rating: 5,
  status: 'COMMENTED',
});

const filterTabs: Array<{ label: string; value: SubmissionFilter }> = [
  { label: '全部', value: 'ALL' },
  { label: '待点评', value: 'SUBMITTED' },
  { label: '已点评', value: 'COMMENTED' },
  { label: '需重交', value: 'RESUBMIT_REQUIRED' },
];

const commentTemplates = [
  '发音清晰，朗读节奏很好',
  '连读和重音可以再练习',
  '单词拼读准确，继续保持',
  '语音提交完整，完成度不错',
  '请按老师要求补充后重交',
];

const submissions = computed(() => homework.value?.submissions || []);
const filteredSubmissions = computed(() => {
  if (activeFilter.value === 'ALL') return submissions.value;
  return submissions.value.filter((item) => item.status === activeFilter.value);
});
const pendingCount = computed(() => submissions.value.filter((item) => item.status === 'SUBMITTED').length);
const commentedCount = computed(() => submissions.value.filter((item) => item.status === 'COMMENTED').length);
const resubmitCount = computed(() => submissions.value.filter((item) => item.status === 'RESUBMIT_REQUIRED').length);
const currentSubmission = computed(() => submissions.value.find((item) => item.id === currentSubmissionId.value));
const needsCommentText = computed(() => Number(commentForm.value.rating || 0) <= 3 || commentForm.value.status === 'RESUBMIT_REQUIRED');
const progressText = computed(() => {
  const total = submissions.value.length;
  if (!total) return '暂无提交';
  return `${commentedCount.value + resubmitCount.value}/${total} 已处理`;
});

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

function openComment(item: HomeworkSubmissionItem) {
  const latest = latestComment(item);
  currentSubmissionId.value = item.id;
  commentForm.value = {
    commentText: latest?.commentText || '',
    rating: normalizeRating(latest?.rating),
    status: item.status === 'RESUBMIT_REQUIRED' ? 'RESUBMIT_REQUIRED' : 'COMMENTED',
  };
  showComment.value = true;
}

function closeComment() {
  if (submitting.value) return;
  showComment.value = false;
  currentSubmissionId.value = 0;
}

function normalizeRating(value?: number) {
  const rating = Number(value || 5);
  if (Number.isNaN(rating)) return 5;
  return Math.min(5, Math.max(1, rating));
}

function latestComment(item: HomeworkSubmissionItem): HomeworkComment | undefined {
  const comments = item.comments || [];
  return comments.length ? comments[comments.length - 1] : undefined;
}

function appendTemplate(text: string) {
  const current = commentForm.value.commentText?.trim();
  commentForm.value.commentText = current ? `${current}；${text}` : text;
}

function setCommentStatus(status: string) {
  commentForm.value.status = status;
}

async function submitComment() {
  if (!homework.value || !currentSubmissionId.value || submitting.value) return;
  const rating = normalizeRating(commentForm.value.rating);
  const commentText = commentForm.value.commentText?.trim() || '';

  if (needsCommentText.value && !commentText) {
    uni.showToast({ title: '请填写点评说明', icon: 'none' });
    return;
  }

  submitting.value = true;
  try {
    await commentTeacherHomework(homework.value.id, currentSubmissionId.value, {
      commentText: commentText || undefined,
      rating,
      status: commentForm.value.status,
    });
    uni.showToast({ title: '点评成功', icon: 'success' });
    showComment.value = false;
    await fetchDetail(homework.value.id);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '点评失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

function statusLabel(status?: string) {
  const map: Record<string, string> = {
    SUBMITTED: '待点评',
    COMMENTED: '已点评',
    RESUBMIT_REQUIRED: '需重交',
    TO_SUBMIT: '未提交',
    OVERDUE: '已逾期',
  };
  return status ? map[status] || status : '--';
}

function statusClass(status?: string) {
  if (status === 'COMMENTED' || status === 'PUBLISHED') return 'success';
  if (status === 'RESUBMIT_REQUIRED') return 'danger';
  if (status === 'SUBMITTED') return 'warning';
  return 'muted';
}

function formatDate(date?: string) {
  return date ? date.substring(0, 10) : '未设置';
}

function formatDateTime(date?: string) {
  if (!date) return '未提交';
  return date.replace('T', ' ').substring(0, 16);
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard
      eyebrow="Homework Detail"
      :title="homework?.title || '作业详情'"
      :subtitle="homework?.className || '班级作业'"
    >
      <view class="hero-stats">
        <view class="hero-stat">
          <text class="stat-value">{{ submissions.length }}</text>
          <text class="stat-label">提交</text>
        </view>
        <view class="hero-stat">
          <text class="stat-value">{{ pendingCount }}</text>
          <text class="stat-label">待点评</text>
        </view>
        <view class="hero-stat">
          <text class="stat-value">{{ progressText }}</text>
          <text class="stat-label">进度</text>
        </view>
      </view>
    </TeacherHeroCard>

    <view class="panel">
      <view class="panel-head">
        <text class="panel-title">作业内容</text>
        <text class="status-pill" :class="statusClass(homework?.status)">
          {{ homework?.status === 'PUBLISHED' ? '已发布' : '草稿' }}
        </text>
      </view>
      <text class="content-text">{{ homework?.content || '暂无作业内容' }}</text>
      <view class="meta-row">
        <text>截止 {{ formatDate(homework?.deadline) }}</text>
        <text v-if="homework?.checkinEnabled">打卡作业</text>
        <text v-if="homework?.attachmentCount">{{ homework?.attachmentCount }} 个附件</text>
      </view>
    </view>

    <view class="panel">
      <view class="panel-head">
        <text class="panel-title">提交情况</text>
        <text class="panel-subtitle">{{ filteredSubmissions.length }} 人</text>
      </view>

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

      <view
        v-for="item in filteredSubmissions"
        :key="item.id"
        class="submission-card"
        @tap="openComment(item)"
      >
        <image
          v-if="item.studentAvatarUrl"
          class="avatar"
          :src="item.studentAvatarUrl"
          mode="aspectFill"
        />
        <view v-else class="avatar avatar-fallback">{{ item.studentName.slice(0, 1) }}</view>
        <view class="submission-main">
          <view class="submission-top">
            <text class="student-name">{{ item.studentName }}</text>
            <text class="status-pill" :class="statusClass(item.status)">
              {{ statusLabel(item.status) }}
            </text>
          </view>
          <text class="submission-time">{{ formatDateTime(item.submittedAt) }}</text>
          <text v-if="item.content" class="submission-content">{{ item.content }}</text>
          <text v-if="item.files?.length" class="file-count">{{ item.files.length }} 个附件</text>
          <view v-if="latestComment(item)" class="comment-preview">
            <StarRating :model-value="normalizeRating(latestComment(item)?.rating)" readonly />
            <text v-if="latestComment(item)?.commentText" class="comment-text">
              {{ latestComment(item)?.commentText }}
            </text>
          </view>
        </view>
      </view>

      <TeacherEmptyState
        v-if="!filteredSubmissions.length"
        title="暂无对应提交"
        description="切换筛选条件，或稍后下拉刷新作业提交状态。"
      />
    </view>

    <view v-if="showComment" class="sheet-mask" @tap="closeComment">
      <view class="comment-sheet" @tap.stop>
        <view class="sheet-handle" />
        <view class="sheet-head">
          <view>
            <text class="sheet-title">作业点评</text>
            <text class="sheet-subtitle">{{ currentSubmission?.studentName || '学生' }}</text>
          </view>
          <button class="sheet-close" @tap="closeComment">×</button>
        </view>

        <view class="rating-row">
          <text class="field-label">评分</text>
          <StarRating v-model="commentForm.rating" size="large" />
        </view>

        <view class="status-segment">
          <view
            class="status-option"
            :class="{ active: commentForm.status === 'COMMENTED' }"
            @tap="setCommentStatus('COMMENTED')"
          >
            完成点评
          </view>
          <view
            class="status-option danger"
            :class="{ active: commentForm.status === 'RESUBMIT_REQUIRED' }"
            @tap="setCommentStatus('RESUBMIT_REQUIRED')"
          >
            需重交
          </view>
        </view>

        <view class="template-wrap">
          <text class="field-label">快捷点评</text>
          <view class="template-list">
            <text
              v-for="item in commentTemplates"
              :key="item"
              class="template-chip"
              @tap="appendTemplate(item)"
            >
              {{ item }}
            </text>
          </view>
        </view>

        <textarea
          v-model="commentForm.commentText"
          class="comment-input"
          :maxlength="500"
          placeholder="输入点评内容，或点击上方模板快速补充"
        />
        <text v-if="needsCommentText" class="required-hint">
          低分或需重交时必须填写点评说明。
        </text>

        <view class="sheet-actions">
          <button class="btn-secondary" @tap="closeComment">取消</button>
          <button class="btn-primary" :disabled="submitting" @tap="submitComment">
            {{ submitting ? '提交中' : '提交点评' }}
          </button>
        </view>
      </view>
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

button {
  margin: 0;
  padding: 0;
  border: 0;
  line-height: 1;
}

button::after {
  border: 0;
}

.hero-stats {
  margin-top: 28rpx;
  display: flex;
  gap: 12rpx;
}

.hero-stat {
  flex: 1;
  min-width: 0;
  padding: 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 252, 245, 0.12);
}

.stat-value,
.stat-label {
  display: block;
}

.stat-value {
  color: #fff;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1.25;
  word-break: break-all;
}

.stat-label {
  margin-top: 6rpx;
  color: rgba(255, 255, 255, 0.62);
  font-size: 19rpx;
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
  margin-bottom: 16rpx;
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

.content-text {
  display: block;
  color: #343b36;
  font-size: 27rpx;
  line-height: 1.62;
}

.meta-row {
  margin-top: 18rpx;
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

.filter-tabs {
  padding: 6rpx;
  display: flex;
  gap: 6rpx;
  border-radius: 20rpx;
  background: #f4efe6;
}

.filter-tab {
  flex: 1;
  height: 62rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  color: #7d827c;
  font-size: 23rpx;
  font-weight: 900;
}

.filter-tab.active {
  background: #fffcf5;
  color: #17211d;
  box-shadow: 0 8rpx 18rpx rgba(54, 43, 30, 0.06);
}

.submission-card {
  min-height: 132rpx;
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
  padding: 24rpx 0;
  border-top: 1rpx solid #eee5d8;
}

.submission-card:first-of-type {
  margin-top: 14rpx;
}

.submission-card:active {
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

.submission-main {
  flex: 1;
  min-width: 0;
}

.submission-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.student-name {
  color: #17211d;
  font-size: 29rpx;
  font-weight: 900;
}

.status-pill {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
  font-weight: 900;
  white-space: nowrap;
}

.status-pill.success {
  background: #e7f0ed;
  color: #1f5a44;
}

.status-pill.warning {
  background: #fff1d4;
  color: #9a6710;
}

.status-pill.danger {
  background: #f9ded8;
  color: #b23f31;
}

.status-pill.muted {
  background: #eee8de;
  color: #78736a;
}

.submission-time,
.submission-content,
.file-count,
.comment-text {
  display: block;
  margin-top: 8rpx;
  color: #7d827c;
  font-size: 23rpx;
  line-height: 1.45;
}

.submission-content {
  color: #343b36;
}

.file-count {
  color: #1f5a44;
  font-weight: 800;
}

.comment-preview {
  margin-top: 14rpx;
  padding: 16rpx;
  border-radius: 20rpx;
  background: #f7f2ea;
}

.comment-text {
  margin-top: 8rpx;
}

.sheet-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  background: rgba(23, 33, 29, 0.42);
}

.comment-sheet {
  width: 100%;
  max-height: 88vh;
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
  font-size: 35rpx;
  font-weight: 900;
}

.sheet-subtitle {
  display: block;
  margin-top: 6rpx;
  color: #858982;
  font-size: 23rpx;
  font-weight: 800;
}

.sheet-close {
  width: 58rpx;
  height: 58rpx;
  border-radius: 18rpx;
  background: #f2ece2;
  color: #17211d;
  font-size: 36rpx;
  font-weight: 700;
}

.rating-row,
.template-wrap {
  margin-top: 26rpx;
}

.field-label {
  display: block;
  margin-bottom: 14rpx;
  color: #17211d;
  font-size: 25rpx;
  font-weight: 900;
}

.status-segment {
  margin-top: 24rpx;
  padding: 6rpx;
  display: flex;
  gap: 6rpx;
  border-radius: 20rpx;
  background: #f4efe6;
}

.status-option {
  flex: 1;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  color: #6d756f;
  font-size: 25rpx;
  font-weight: 900;
}

.status-option.active {
  background: #1f5a44;
  color: #fff;
}

.status-option.danger.active {
  background: #b23f31;
}

.template-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.template-chip {
  padding: 12rpx 16rpx;
  border-radius: 999rpx;
  background: #f4efe6;
  color: #59615b;
  font-size: 22rpx;
  font-weight: 800;
}

.comment-input {
  width: 100%;
  min-height: 190rpx;
  margin-top: 20rpx;
  padding: 22rpx;
  box-sizing: border-box;
  border-radius: 24rpx;
  background: #f5f0e8;
  color: #17211d;
  font-size: 26rpx;
  line-height: 1.5;
}

.required-hint {
  display: block;
  margin-top: 10rpx;
  color: #b23f31;
  font-size: 22rpx;
  font-weight: 800;
}

.sheet-actions {
  margin-top: 24rpx;
  display: flex;
  gap: 16rpx;
}

.btn-secondary,
.btn-primary {
  flex: 1;
  height: 86rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 900;
}

.btn-secondary {
  background: #f1ebe2;
  color: #5f655f;
}

.btn-primary {
  background: #1f5a44;
  color: #fff;
}

.btn-primary[disabled] {
  opacity: 0.56;
}
</style>
