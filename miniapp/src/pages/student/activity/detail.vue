<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getActivityDetail, joinActivity, payOrder } from '@/api/student';
import type { ActivityDetail } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const activityId = ref<number>();
const detail = ref<ActivityDetail>();
const loading = ref(false);
const submitting = ref(false);

onLoad((query) => {
  activityId.value = Number(query?.id);
});

onShow(() => {
  if (requireStudentAccess()) {
    loadDetail();
  }
});

async function loadDetail() {
  if (!activityId.value) return;
  loading.value = true;
  try {
    detail.value = await getActivityDetail(activityId.value);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

async function join() {
  if (!detail.value || submitting.value) return;
  submitting.value = true;
  try {
    const result = await joinActivity(detail.value.id);
    if (result.payRequired && result.orderId) {
      uni.showModal({
        title: '模拟支付',
        content: `需支付 ￥${result.amount}，确认后将完成 Mock 支付。`,
        success: async (modal) => {
          if (modal.confirm && result.orderId) {
            await payOrder(result.orderId);
            uni.showToast({ title: '支付成功', icon: 'success' });
            await loadDetail();
          }
        },
      });
    } else {
      uni.showToast({ title: result.message || '报名成功', icon: 'success' });
      await loadDetail();
    }
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '报名失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

function actionText() {
  if (!detail.value) return '加载中';
  if (detail.value.registrationId) return '已报名';
  if (detail.value.activityStatus === 'FULL') return '名额已满';
  if (detail.value.activityStatus === 'ENDED') return '已结束';
  return detail.value.fee > 0 ? '报名并支付' : '立即报名';
}

function canJoin() {
  return Boolean(detail.value && !detail.value.registrationId && detail.value.activityStatus === 'REGISTERING');
}

function formatTime(value?: string) {
  return value?.slice(0, 16).replace('T', ' ') || '-';
}
</script>

<template>
  <view class="page">
    <view v-if="detail">
      <view class="hero">
        <text class="tag">{{ detail.fee > 0 ? `￥${detail.fee}` : '免费活动' }}</text>
        <text class="title">{{ detail.title }}</text>
        <text class="meta">{{ formatTime(detail.startTime) }} - {{ formatTime(detail.endTime) }}</text>
      </view>

      <view class="section">
        <text class="section-title">活动信息</text>
        <text class="line">{{ detail.location }}</text>
        <text class="line">已报名 {{ detail.registeredCount }}/{{ detail.quota || '不限' }}</text>
        <text class="copy">{{ detail.description || '暂无更多说明' }}</text>
      </view>

      <button class="join" :disabled="!canJoin()" :loading="submitting" @tap="join">{{ actionText() }}</button>
    </view>

    <view v-else class="empty">{{ loading ? '加载中...' : '活动不存在' }}</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36rpx 30rpx 90rpx;
  background: #f6f1e8;
  color: #17211d;
}

.hero,
.section {
  padding: 32rpx;
  border-radius: 24rpx;
  background: #fffcf5;
}

.hero {
  border: 2rpx solid #17211d;
  box-shadow: 8rpx 8rpx 0 #f0b84d;
}

.tag {
  display: inline-flex;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #22624c;
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
}

.title {
  display: block;
  margin-top: 20rpx;
  font-size: 42rpx;
  font-weight: 900;
}

.meta,
.line,
.copy,
.empty {
  display: block;
  margin-top: 14rpx;
  color: #6f756f;
  font-size: 25rpx;
  line-height: 1.55;
}

.section {
  margin-top: 28rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
}

.join {
  height: 94rpx;
  margin-top: 32rpx;
  border-radius: 22rpx;
  background: #22624c;
  color: #fff;
  font-size: 30rpx;
  font-weight: 900;
}

.join[disabled] {
  background: #c7c1b7;
}

.empty {
  text-align: center;
}
</style>
