<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShow, onShareAppMessage } from '@dcloudio/uni-app';
import { createGroupPoster, getGroupRequestDetail } from '@/api/student';
import type { GroupRequestDetail } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const groupId = ref<number>();
const detail = ref<GroupRequestDetail>();

const progressPercent = computed(() => {
  if (!detail.value?.requiredMembers) {
    return 0;
  }
  return Math.min(100, detail.value.currentMembers / detail.value.requiredMembers * 100);
});

onLoad((query) => {
  groupId.value = Number(query?.id);
});

onShow(() => {
  if (requireStudentAccess() && groupId.value) {
    loadDetail();
  }
});

onShareAppMessage(() => ({
  title: `${detail.value?.targetSystem || '英语'}拼班邀请`,
  path: detail.value?.sharePath || `/pages/student/group/detail?id=${groupId.value}`,
}));

async function loadDetail() {
  if (!groupId.value) {
    return;
  }
  try {
    detail.value = await getGroupRequestDetail(groupId.value);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  }
}

async function makePoster() {
  if (!groupId.value) {
    return;
  }
  try {
    const poster = await createGroupPoster(groupId.value);
    uni.setClipboardData({ data: poster.sharePath });
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '生成失败', icon: 'none' });
  }
}

function statusLabel(status?: string) {
  return {
    FORMING: '拼班中',
    WAITING_CAMPUS: '等待校区安排',
    TRIAL_ARRANGED: '试听已安排',
    TRIAL_COMPLETED: '试听已完成',
    FAILED: '拼班失败',
    CANCELLED: '已取消',
  }[status || ''] || status || '';
}
</script>

<template>
  <view class="page">
    <view v-if="detail">
      <view class="hero">
        <text class="status">{{ statusLabel(detail.status) }}</text>
        <text class="title">{{ detail.targetSystem }}</text>
        <text class="subtitle">{{ detail.grade }} · {{ detail.englishLevel }}</text>
        <view class="progress">
          <view class="progress-bar" :style="{ width: `${progressPercent}%` }" />
        </view>
        <text class="subtitle">已加入 {{ detail.currentMembers }}/{{ detail.requiredMembers }} 人</text>
      </view>

      <view class="actions">
        <button open-type="share" class="action">转发邀请</button>
        <button class="action yellow" @tap="makePoster">复制链接</button>
      </view>

      <view class="section">
        <text class="section-title">成员</text>
        <view v-for="member in detail.members" :key="member.id" class="member">
          <view class="avatar">{{ member.nickname?.slice(0, 1) || '同' }}</view>
          <view>
            <text class="member-name">{{ member.nickname }}</text>
            <text class="member-meta">{{ member.joinedAt?.slice(0, 16).replace('T', ' ') }}</text>
          </view>
        </view>
      </view>

      <view v-if="detail.trial" class="section trial">
        <text class="section-title">试听安排</text>
        <text class="line">{{ detail.trial.trialTime.slice(0, 16).replace('T', ' ') }}</text>
        <text class="line">{{ detail.trial.location }}</text>
        <text class="line">{{ detail.trial.teacherName || '待定老师' }} · {{ detail.trial.className || '试听班' }}</text>
        <image v-if="detail.trial.wechatQrUrl" class="qr" :src="detail.trial.wechatQrUrl" mode="aspectFit" />
      </view>

      <view class="section">
        <text class="section-title">试听反馈</text>
        <view v-if="detail.feedbacks.length">
          <view v-for="feedback in detail.feedbacks" :key="feedback.id" class="feedback">
            <text class="line">{{ feedback.feedback }}</text>
            <text class="line">{{ feedback.nextAction || feedback.result }}</text>
          </view>
        </view>
        <view v-else class="empty">试听完成后，校区会在这里写反馈</view>
      </view>
    </view>

    <view v-else class="empty">加载中...</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 30rpx 32rpx 70rpx;
  background: #f6f1e8;
  color: #17211d;
}

.hero,
.section {
  padding: 28rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.hero {
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
  margin-top: 16rpx;
  font-size: 40rpx;
  font-weight: 900;
}

.subtitle,
.line,
.empty,
.member-meta {
  display: block;
  margin-top: 8rpx;
  color: #909090;
  font-size: 23rpx;
  line-height: 1.5;
}

.progress {
  height: 14rpx;
  margin-top: 24rpx;
  border-radius: 999rpx;
  background: #E8E8E8;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: inherit;
  background: #22624c;
  transition: width 0.5s ease;
}

.actions {
  margin-top: 24rpx;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.action {
  height: 84rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 27rpx;
  font-weight: 800;
}

.action:active { opacity: 0.85; }

.action.yellow {
  background: #f0b84d;
  color: #17211d;
}

.section {
  margin-top: 24rpx;
}

.section-title {
  display: block;
  margin-bottom: 14rpx;
  font-size: 29rpx;
  font-weight: 900;
}

.member {
  min-height: 86rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  border-top: 1px solid #F0EAE0;
}

.avatar {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14rpx;
  background: #285c7f;
  color: #fff;
  font-size: 23rpx;
  font-weight: 900;
}

.member-name {
  display: block;
  font-size: 26rpx;
  font-weight: 900;
}

.qr {
  width: 220rpx;
  height: 220rpx;
  margin-top: 18rpx;
  border-radius: 18rpx;
  background: #f6f1e8;
}

.feedback {
  padding: 16rpx 0;
  border-top: 1px solid #F0EAE0;
}
</style>
