<script setup lang="ts">
import { ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { getMaterials } from '@/api/student';
import { requireStudentAccess } from '@/utils/auth-flow';
import type { MaterialSummary } from '@/types/api';

const materials = ref<MaterialSummary[]>([]);
const loading = ref(false);

onShow(() => {
  if (requireStudentAccess()) {
    fetchMaterials();
  }
});

onPullDownRefresh(() => {
  fetchMaterials().finally(() => uni.stopPullDownRefresh());
});

async function fetchMaterials() {
  loading.value = true;
  try {
    materials.value = await getMaterials();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/student/material/detail?id=${id}` });
}

function resourceLabel(type?: string) {
  return { PDF: 'PDF', VIDEO: '视频', AUDIO: '音频', IMAGE: '图片', LINK: '链接' }[type || ''] || type || '';
}

function studyLabel(type?: string) {
  return type === 'REQUIRED' ? '必学' : '选学';
}

function formatDate(iso?: string) {
  if (!iso) return '';
  const d = new Date(iso);
  return `${d.getMonth() + 1}月${d.getDate()}日`;
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="caption">MATERIALS</text>
      <text class="title">教学资料</text>
      <text class="subtitle">查看老师分享的学习资料</text>
    </view>

    <view v-if="loading" class="loading">加载中...</view>

    <view v-else-if="materials.length === 0" class="empty-state">
      <text class="empty-title">暂无资料</text>
      <text class="empty-desc">老师还没有发布教学资料，稍后再来看看吧。</text>
    </view>

    <view v-else class="material-list">
      <view
        v-for="item in materials"
        :key="item.id"
        class="material-card"
        @tap="goDetail(item.id)"
      >
        <view class="card-top">
          <view class="type-tag">{{ resourceLabel(item.resourceType) }}</view>
          <view class="study-tag" :class="item.studyType === 'REQUIRED' ? 'required' : 'optional'">
            {{ studyLabel(item.studyType) }}
          </view>
        </view>
        <text class="card-title">{{ item.title }}</text>
        <text class="card-desc">{{ item.description || '暂无说明' }}</text>
        <view class="card-meta">
          <text class="meta-item">{{ item.categoryName || '未分类' }}</text>
          <text class="meta-item">{{ formatDate(item.updatedAt) }}</text>
          <text v-if="item.allowDownload" class="meta-item download">可下载</text>
          <text v-else class="meta-item preview-only">仅预览</text>
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
  color: #17211d;
}

.header {
  margin-bottom: 28rpx;
}

.caption {
  display: block;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
}

.title {
  display: block;
  margin-top: 8rpx;
  font-size: 44rpx;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.subtitle {
  display: block;
  margin-top: 6rpx;
  color: #909090;
  font-size: 24rpx;
}

.loading {
  text-align: center;
  padding: 80rpx 0;
  color: #AAA;
  font-size: 26rpx;
}

.empty-state {
  padding: 100rpx 40rpx;
  text-align: center;
}

.empty-title {
  display: block;
  font-size: 32rpx;
  font-weight: 900;
  color: #17211d;
}

.empty-desc {
  display: block;
  margin-top: 12rpx;
  color: #909090;
  font-size: 24rpx;
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.material-card {
  padding: 28rpx;
  border-radius: 24rpx;
  background: #fffcf5;
}

.material-card:active {
  opacity: 0.8;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 14rpx;
}

.type-tag {
  padding: 6rpx 14rpx;
  border-radius: 10rpx;
  background: #e7f0ed;
  color: #22624c;
  font-size: 20rpx;
  font-weight: 900;
}

.study-tag {
  padding: 6rpx 14rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  font-weight: 900;
}

.study-tag.required {
  background: #fff1d4;
  color: #9a6710;
}

.study-tag.optional {
  background: #f0f0f0;
  color: #666;
}

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.3;
}

.card-desc {
  display: block;
  margin-top: 8rpx;
  color: #7d827c;
  font-size: 24rpx;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  margin-top: 14rpx;
  display: flex;
  gap: 16rpx;
}

.meta-item {
  color: #9a958d;
  font-size: 21rpx;
  font-weight: 800;
}

.meta-item.download {
  color: #22624c;
}

.meta-item.preview-only {
  color: #9a958d;
}
</style>
