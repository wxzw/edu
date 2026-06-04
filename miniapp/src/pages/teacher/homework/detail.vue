<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { getTeacherHomeworkDetail, commentTeacherHomework } from '@/api/teacher';
import type { TeacherHomeworkDetail, CommentHomeworkRequest } from '@/types/api';

const homework = ref<TeacherHomeworkDetail | null>(null);
const loading = ref(false);
const commentForm = ref<CommentHomeworkRequest>({
  commentText: '',
  rating: 5,
  status: 'COMMENTED',
});
const showComment = ref(false);
const currentSubmissionId = ref(0);

async function fetchDetail(id: number) {
  loading.value = true;
  try {
    const res = await getTeacherHomeworkDetail(id);
    homework.value = res;
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onLoad((options) => {
  if (!requireIdentity('TEACHER')) return;
  const id = options?.id ? parseInt(options.id, 10) : 0;
  if (id) fetchDetail(id);
});

function openComment(submissionId: number) {
  currentSubmissionId.value = submissionId;
  showComment.value = true;
}

async function submitComment() {
  if (!homework.value) return;
  try {
    await commentTeacherHomework(homework.value.id, currentSubmissionId.value, commentForm.value);
    uni.showToast({ title: '点评成功', icon: 'success' });
    showComment.value = false;
    fetchDetail(homework.value.id);
  } catch (e) {
    uni.showToast({ title: '点评失败', icon: 'none' });
  }
}

function formatDate(date?: string) {
  if (!date) return '';
  return date.substring(0, 10);
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Homework Detail</text>
      <text class="title">{{ homework?.title }}</text>
      <text class="subtitle" v-if="homework?.className">{{ homework.className }}</text>
    </view>

    <view class="panel">
      <text class="panel-title">作业内容</text>
      <text class="content-text">{{ homework?.content || '无内容' }}</text>
      <view class="meta-bar">
        <text class="meta">截止 {{ formatDate(homework?.deadline) }}</text>
        <text class="meta" v-if="homework?.checkinEnabled"> · 打卡</text>
        <text class="meta"> · {{ homework?.status === 'PUBLISHED' ? '已发布' : '草稿' }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">
        提交情况 ({{ homework?.submissions?.length || 0 }})
      </text>
      <view
        class="sub-row"
        v-for="item in homework?.submissions"
        :key="item.id"
      >
        <view class="sub-info">
          <text class="sub-name">{{ item.studentName }}</text>
          <text class="sub-content" v-if="item.content">{{ item.content }}</text>
          <text class="sub-time">{{ formatDate(item.submittedAt) }}</text>
        </view>
        <view class="sub-right">
          <text class="sub-status" :class="item.status">{{ item.status }}</text>
          <button
            class="comment-btn"
            v-if="item.status === 'SUBMITTED'"
            @tap="openComment(item.id)"
          >
            点评
          </button>
        </view>
      </view>
      <view class="empty-row" v-if="!homework?.submissions?.length">
        <text class="empty-text">暂无提交</text>
      </view>
    </view>

    <view class="modal" v-if="showComment">
      <view class="modal-mask" @tap="showComment = false" />
      <view class="modal-content">
        <text class="modal-title">作业点评</text>
        <textarea
          class="modal-textarea"
          v-model="commentForm.commentText"
          placeholder="请输入点评内容"
          :maxlength="500"
        />
        <view class="modal-row">
          <text class="modal-label">评分</text>
          <picker mode="selector" :range="[1,2,3,4,5]" @change="(e: any) => commentForm.rating = e.detail.value + 1">
            <text class="modal-picker">{{ commentForm.rating }} 星</text>
          </picker>
        </view>
        <view class="modal-row">
          <text class="modal-label">状态</text>
          <picker mode="selector" :range="['COMMENTED', 'RESUBMIT_REQUIRED']" @change="(e: any) => commentForm.status = e.detail.value === 0 ? 'COMMENTED' : 'RESUBMIT_REQUIRED'">
            <text class="modal-picker">{{ commentForm.status === 'COMMENTED' ? '已点评' : '需重交' }}</text>
          </picker>
        </view>
        <view class="modal-actions">
          <button class="btn-secondary" @tap="showComment = false">取消</button>
          <button class="btn-primary" @tap="submitComment">提交</button>
        </view>
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

.content-text {
  display: block;
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
}

.meta-bar {
  margin-top: 16rpx;
  display: flex;
  gap: 12rpx;
}

.meta {
  font-size: 22rpx;
  color: #999;
}

.sub-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20rpx 0;
  border-top: 1px solid #F0EAE0;
}

.sub-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.sub-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #17211d;
}

.sub-content {
  font-size: 24rpx;
  color: #666;
}

.sub-time {
  font-size: 22rpx;
  color: #999;
}

.sub-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.sub-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.sub-status.SUBMITTED {
  background: #fff3e0;
  color: #e6a23c;
}

.sub-status.COMMENTED {
  background: #e8f5e9;
  color: #22624c;
}

.comment-btn {
  width: 100rpx;
  height: 52rpx;
  border-radius: 10rpx;
  background: #22624c;
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
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

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: relative;
  width: 80%;
  padding: 40rpx;
  border-radius: 24rpx;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 900;
  color: #17211d;
  text-align: center;
}

.modal-textarea {
  height: 200rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #17211d;
}

.modal-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-label {
  font-size: 26rpx;
  color: #666;
}

.modal-picker {
  font-size: 26rpx;
  color: #17211d;
  font-weight: 700;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.btn-secondary {
  flex: 1;
  height: 80rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 28rpx;
  font-weight: 700;
}

.btn-primary {
  flex: 1;
  height: 80rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}
</style>
