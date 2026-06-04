<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getHomeworkDetail, submitHomework } from '@/api/student';
import type { HomeworkDetail } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const homeworkId = ref<number>();
const detail = ref<HomeworkDetail>();
const content = ref('');
const submitting = ref(false);

onLoad((query) => {
  homeworkId.value = Number(query?.id);
  if (requireStudentAccess() && homeworkId.value) {
    loadDetail();
  }
});

async function loadDetail() {
  if (!homeworkId.value) {
    return;
  }
  try {
    const data = await getHomeworkDetail(homeworkId.value);
    detail.value = data;
    content.value = data.submission?.content || '';
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  }
}

async function submit() {
  if (!homeworkId.value) {
    return;
  }
  if (!content.value.trim()) {
    uni.showToast({ title: '请填写作业内容', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    detail.value = await submitHomework(homeworkId.value, { content: content.value.trim(), files: [] });
    uni.showToast({ title: '已提交', icon: 'success' });
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

function statusLabel(status?: string) {
  return {
    TO_SUBMIT: '待提交',
    OVERDUE: '已逾期',
    SUBMITTED: '已提交，待点评',
    COMMENTED: '已点评',
  }[status || ''] || status || '';
}

function copyUrl(url?: string) {
  if (!url) {
    return;
  }
  uni.setClipboardData({ data: url });
}
</script>

<template>
  <view class="page">
    <view v-if="detail" class="content">
      <view class="header-card">
        <text class="status">{{ statusLabel(detail.studentStatus) }}</text>
        <text class="title">{{ detail.title }}</text>
        <text class="meta">{{ detail.teacherName }} · {{ detail.className || '班级作业' }}</text>
        <text class="meta">{{ detail.deadline ? `截止 ${detail.deadline.slice(0, 16).replace('T', ' ')}` : '无截止时间' }}</text>
      </view>

      <view class="section">
        <text class="section-title">作业要求</text>
        <text class="copy">{{ detail.content }}</text>
        <view v-if="detail.attachments.length" class="files">
          <view v-for="file in detail.attachments" :key="file.fileId" class="file-row" @tap="copyUrl(file.url)">
            <text>{{ file.fileName }}</text>
            <text>复制链接</text>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-title">我的提交</text>
        <textarea v-model="content" class="textarea" placeholder="填写文字作业或说明；语音/图片附件后续接入上传能力" maxlength="1000" />
        <button class="submit" :loading="submitting" @tap="submit">
          {{ detail.submission ? '更新提交' : '提交作业' }}
        </button>
        <text v-if="detail.submission?.submittedAt" class="submitted">上次提交：{{ detail.submission.submittedAt.slice(0, 16).replace('T', ' ') }}</text>
      </view>

      <view class="section">
        <text class="section-title">老师点评</text>
        <view v-if="detail.comments.length" class="comment-list">
          <view v-for="comment in detail.comments" :key="comment.id" class="comment">
            <text class="comment-title">{{ comment.teacherName || '老师' }} · {{ comment.rating ? `${comment.rating}星` : '已点评' }}</text>
            <text class="copy">{{ comment.commentText }}</text>
            <button v-if="comment.voiceUrl" class="voice" @tap="copyUrl(comment.voiceUrl)">复制语音链接</button>
          </view>
        </view>
        <view v-else class="empty">老师还没有点评</view>
      </view>
    </view>

    <view v-else class="empty">加载中...</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 30rpx 32rpx 60rpx;
  background: #f6f1e8;
  color: #17211d;
}

.header-card,
.section {
  padding: 28rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.header-card {
  border: 2rpx solid #17211d;
}

.status {
  display: inline-flex;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: #22624c;
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
}

.title {
  display: block;
  margin-top: 18rpx;
  font-size: 36rpx;
  font-weight: 900;
}

.meta,
.submitted,
.empty {
  display: block;
  margin-top: 10rpx;
  color: #909090;
  font-size: 23rpx;
}

.section {
  margin-top: 24rpx;
}

.section-title {
  display: block;
  margin-bottom: 16rpx;
  font-size: 29rpx;
  font-weight: 900;
}

.copy {
  display: block;
  color: #555;
  font-size: 25rpx;
  line-height: 1.65;
}

.files {
  margin-top: 16rpx;
}

.file-row {
  min-height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #F0EAE0;
  color: #285c7f;
  font-size: 23rpx;
  font-weight: 800;
}

.textarea {
  width: 100%;
  min-height: 220rpx;
  box-sizing: border-box;
  padding: 20rpx;
  border-radius: 14rpx;
  background: #F8F5EE;
  color: #17211d;
  font-size: 25rpx;
  line-height: 1.5;
}

.submit {
  height: 84rpx;
  margin-top: 18rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 27rpx;
  font-weight: 800;
}

.submit:active { opacity: 0.85; }

.comment {
  padding: 18rpx 0;
  border-top: 1px solid #F0EAE0;
}

.comment-title {
  display: block;
  margin-bottom: 8rpx;
  font-size: 25rpx;
  font-weight: 900;
}

.voice {
  height: 60rpx;
  margin-top: 12rpx;
  border-radius: 999rpx;
  background: #285c7f;
  color: #fff;
  font-size: 23rpx;
  font-weight: 800;
}

.voice:active { opacity: 0.85; }
</style>
