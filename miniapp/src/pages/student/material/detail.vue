<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { apiBaseUrl, authHeaders } from '@/api/http';
import { getMaterialDetail } from '@/api/student';
import type { MaterialSummary } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';

const materialId = ref<number>();
const detail = ref<MaterialSummary>();
const loading = ref(false);

onLoad((query) => {
  materialId.value = Number(query?.id);
  if (requireStudentAccess()) {
    loadDetail();
  }
});

async function loadDetail() {
  if (!materialId.value) return;
  loading.value = true;
  try {
    detail.value = await getMaterialDetail(materialId.value);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function resourceLabel(type?: string) {
  return { PDF: 'PDF', VIDEO: '视频', AUDIO: '音频', IMAGE: '图片', LINK: '链接' }[type || ''] || type || '';
}

function fetchFile(path?: string, openAfterDownload = true) {
  if (!path) return;
  uni.showLoading({ title: '准备文件' });
  uni.downloadFile({
    url: `${apiBaseUrl()}${path}`,
    header: authHeaders(),
    success: (response) => {
      if (response.statusCode && response.statusCode >= 400) {
        uni.showToast({ title: '文件不可用', icon: 'none' });
        return;
      }
      if (!openAfterDownload) {
        uni.showToast({ title: '已下载', icon: 'success' });
        return;
      }
      uni.openDocument({
        filePath: response.tempFilePath,
        showMenu: true,
        fail: () => uni.showToast({ title: '请在浏览器或文件管理器中打开', icon: 'none' }),
      });
    },
    fail: () => uni.showToast({ title: '下载失败', icon: 'none' }),
    complete: () => uni.hideLoading(),
  });
}
</script>

<template>
  <view class="page">
    <view v-if="detail" class="hero">
      <text class="tag">{{ resourceLabel(detail.resourceType) }} · {{ detail.studyType === 'REQUIRED' ? '必学' : '选学' }}</text>
      <text class="title">{{ detail.title }}</text>
      <text class="meta">{{ detail.categoryName }} · {{ detail.fileName }}</text>
    </view>

    <view v-if="detail" class="section">
      <text class="section-title">资料说明</text>
      <text class="copy">{{ detail.description || '暂无说明' }}</text>
      <view class="file-line">
        <text>{{ detail.contentType || '文件' }}</text>
        <text>{{ detail.allowDownload ? '允许下载' : '仅允许预览' }}</text>
      </view>
    </view>

    <view v-if="detail" class="actions">
      <button class="primary" @tap="fetchFile(detail.previewPath, true)">预览</button>
      <button class="secondary" :disabled="!detail.allowDownload" @tap="fetchFile(detail.downloadPath, false)">
        {{ detail.allowDownload ? '下载' : '不可下载' }}
      </button>
    </view>

    <view v-if="loading" class="empty">加载中...</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36rpx 30rpx 70rpx;
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
  box-shadow: 8rpx 8rpx 0 #9ac2b0;
}

.tag {
  display: inline-flex;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #285c7f;
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
}

.title {
  display: block;
  margin-top: 20rpx;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.2;
}

.meta,
.copy,
.file-line,
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

.file-line {
  padding-top: 18rpx;
  display: flex;
  justify-content: space-between;
  border-top: 2rpx solid #eee4d4;
}

.actions {
  margin-top: 32rpx;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
}

.primary,
.secondary {
  height: 90rpx;
  border-radius: 20rpx;
  color: #fff;
  font-size: 29rpx;
  font-weight: 900;
}

.primary { background: #22624c; }
.secondary { background: #285c7f; }
.secondary[disabled] {
  background: #c7c1b7;
  color: #fff;
}

.empty {
  text-align: center;
}
</style>
