<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import { getTeacherMaterials, deleteTeacherMaterial } from '@/api/teacher';
import type { TeacherMaterialItem } from '@/types/api';

const tabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
];

const materials = ref<TeacherMaterialItem[]>([]);
const activeTab = ref(0);
const loading = ref(false);

const activeFilter = computed(() => tabs[activeTab.value]?.value || '');

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchMaterials();
});

onPullDownRefresh(() => {
  fetchMaterials().finally(() => uni.stopPullDownRefresh());
});

async function fetchMaterials() {
  loading.value = true;
  try {
    materials.value = await getTeacherMaterials(activeFilter.value);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function switchTab(index: number) {
  activeTab.value = index;
  fetchMaterials();
}

function toUpload() {
  uni.navigateTo({ url: '/pages/teacher/material/upload' });
}

function confirmDelete(item: TeacherMaterialItem) {
  uni.showModal({
    title: '确认删除',
    content: `确定删除「${item.title}」吗？`,
    confirmColor: '#c0392b',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteTeacherMaterial(item.id);
          uni.showToast({ title: '已删除', icon: 'success' });
          fetchMaterials();
        } catch (error) {
          uni.showToast({ title: error instanceof Error ? error.message : '删除失败', icon: 'none' });
        }
      }
    },
  });
}

function auditStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
  };
  return map[status] || status;
}

function auditStatusClass(status: string) {
  const map: Record<string, string> = {
    PENDING: 'amber',
    APPROVED: 'green',
    REJECTED: 'red',
  };
  return map[status] || '';
}

function resourceTypeLabel(type: string) {
  const map: Record<string, string> = {
    PDF: 'PDF',
    VIDEO: '视频',
    AUDIO: '音频',
    IMAGE: '图片',
    DOCUMENT: '文档',
  };
  return map[type] || type;
}

function formatDate(iso?: string) {
  if (!iso) return '';
  const d = new Date(iso);
  return `${d.getMonth() + 1}月${d.getDate()}日`;
}

function formatSize(bytes?: number) {
  if (!bytes) return '';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Materials" title="我的资料" subtitle="查看和管理你上传的教学资料">
      <view class="hero-note">
        <text>资料提交后需要管理后台审核，审核通过后学生即可查看。</text>
      </view>
    </TeacherHeroCard>

    <view class="panel">
      <view class="tab-bar">
        <view
          v-for="(tab, index) in tabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeTab === index }"
          @tap="switchTab(index)"
        >
          <text class="tab-label">{{ tab.label }}</text>
        </view>
      </view>

      <view v-if="loading" class="loading-text">加载中...</view>

      <view v-else-if="materials.length === 0" class="empty-wrap">
        <TeacherEmptyState
          title="暂无资料"
          description="还没有上传过教学资料，点击下方按钮开始上传。"
          action-text="上传资料"
          @action="toUpload"
        />
      </view>

      <view v-else class="material-list">
        <view v-for="item in materials" :key="item.id" class="material-card">
          <view class="card-header">
            <text class="resource-tag">{{ resourceTypeLabel(item.resourceType) }}</text>
            <text class="audit-badge" :class="auditStatusClass(item.auditStatus)">
              {{ auditStatusLabel(item.auditStatus) }}
            </text>
          </view>
          <text class="card-title">{{ item.title }}</text>
          <text class="card-desc">{{ item.description || '无描述' }}</text>
          <view class="card-meta">
            <text class="meta-item">{{ item.categoryName || '未分类' }}</text>
            <text class="meta-item">{{ formatDate(item.createdAt) }}</text>
            <text class="meta-item">{{ formatSize(item.fileSize) }}</text>
          </view>
          <view v-if="item.rejectedReason" class="reject-reason">
            <text>驳回原因：{{ item.rejectedReason }}</text>
          </view>
          <view class="card-actions">
            <text
              v-if="item.auditStatus === 'PENDING' || item.auditStatus === 'REJECTED'"
              class="action-delete"
              @tap="confirmDelete(item)"
            >删除</text>
          </view>
        </view>
      </view>
    </view>

    <view class="fab" @tap="toUpload">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 60rpx;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

.hero-note {
  margin-top: 24rpx;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  background: rgba(255, 252, 245, 0.12);
  color: rgba(255, 255, 255, 0.78);
  font-size: 23rpx;
  line-height: 1.45;
  font-weight: 800;
}

.panel {
  margin-top: 24rpx;
  padding: 26rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.tab-bar {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.tab-item {
  flex: 1;
  min-width: 0;
  padding: 14rpx 0;
  border-radius: 22rpx;
  background: #f5f0e8;
  text-align: center;
}

.tab-item.active {
  background: #1f5a44;
}

.tab-label {
  color: #8a847a;
  font-size: 24rpx;
  font-weight: 900;
}

.tab-item.active .tab-label {
  color: #fff;
}

.loading-text {
  text-align: center;
  padding: 60rpx 0;
  color: #8a847a;
  font-size: 26rpx;
  font-weight: 800;
}

.empty-wrap {
  padding: 40rpx 0;
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.material-card {
  padding: 22rpx;
  border-radius: 24rpx;
  background: #f5f0e8;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.resource-tag {
  padding: 6rpx 14rpx;
  border-radius: 10rpx;
  background: #e7f0ed;
  color: #1f5a44;
  font-size: 20rpx;
  font-weight: 900;
}

.audit-badge {
  padding: 6rpx 14rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  font-weight: 900;
}

.audit-badge.amber {
  background: #fff1d4;
  color: #9a6710;
}

.audit-badge.green {
  background: #e7f0ed;
  color: #1f5a44;
}

.audit-badge.red {
  background: #fce8e6;
  color: #c0392b;
}

.card-title {
  display: block;
  color: #17211d;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.3;
}

.card-desc {
  display: block;
  margin-top: 8rpx;
  color: #7d827c;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  margin-top: 12rpx;
  display: flex;
  gap: 16rpx;
}

.meta-item {
  color: #9a958d;
  font-size: 21rpx;
  font-weight: 800;
}

.reject-reason {
  margin-top: 12rpx;
  padding: 12rpx 16rpx;
  border-radius: 14rpx;
  background: #fce8e6;
  color: #c0392b;
  font-size: 22rpx;
  font-weight: 800;
  line-height: 1.4;
}

.card-actions {
  margin-top: 14rpx;
  display: flex;
  justify-content: flex-end;
}

.action-delete {
  color: #c0392b;
  font-size: 24rpx;
  font-weight: 900;
}

.fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(28rpx + env(safe-area-inset-bottom));
  z-index: 30;
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #1f5a44;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(31, 90, 68, 0.32);
}

.fab-icon {
  color: #fff;
  font-size: 48rpx;
  font-weight: 300;
}
</style>
