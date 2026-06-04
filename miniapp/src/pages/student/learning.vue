<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getHomeworks, getMaterialCategories, getMaterials } from '@/api/student';
import type { HomeworkListItem, MaterialCategory, MaterialSummary } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';
import AppTabBar from '@/components/AppTabBar.vue';

const loading = ref(false);
const activeMode = ref<'HOMEWORK' | 'MATERIAL'>('HOMEWORK');
const homeworks = ref<HomeworkListItem[]>([]);
const materials = ref<MaterialSummary[]>([]);
const categories = ref<MaterialCategory[]>([]);
const activeStatus = ref('ALL');
const activeCategoryId = ref<number>();

const filteredHomeworks = computed(() => {
  if (activeStatus.value === 'ALL') {
    return homeworks.value;
  }
  return homeworks.value.filter((item) => item.studentStatus === activeStatus.value);
});

const filteredMaterials = computed(() => {
  if (!activeCategoryId.value) {
    return materials.value;
  }
  return materials.value.filter((item) => item.categoryId === activeCategoryId.value);
});

const homeworkTabs = [
  { value: 'ALL', label: '全部' },
  { value: 'TO_SUBMIT', label: '待提交' },
  { value: 'SUBMITTED', label: '待点评' },
  { value: 'COMMENTED', label: '已点评' },
];

onShow(() => {
  if (requireStudentAccess()) {
    loadData();
  }
});

async function loadData() {
  loading.value = true;
  try {
    if (activeMode.value === 'HOMEWORK') {
      homeworks.value = await getHomeworks();
    } else {
      const [categoryList, materialList] = await Promise.all([getMaterialCategories(), getMaterials()]);
      categories.value = categoryList;
      materials.value = materialList;
    }
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function switchMode(mode: 'HOMEWORK' | 'MATERIAL') {
  activeMode.value = mode;
  loadData();
}

function statusLabel(status: string) {
  return {
    TO_SUBMIT: '待提交',
    OVERDUE: '已逾期',
    SUBMITTED: '待点评',
    COMMENTED: '已点评',
  }[status] || status;
}

function statusClass(status: string) {
  return {
    TO_SUBMIT: 'green',
    OVERDUE: 'red',
    SUBMITTED: 'blue',
    COMMENTED: 'dark',
  }[status] || 'dark';
}

function resourceLabel(type: string) {
  return { PDF: 'PDF', VIDEO: '视频', AUDIO: '音频', IMAGE: '图片', LINK: '链接' }[type] || type;
}

function openHomework(id: number) {
  uni.navigateTo({ url: `/pages/student/homework/detail?id=${id}` });
}

function openMaterial(id: number) {
  uni.navigateTo({ url: `/pages/student/material/detail?id=${id}` });
}
</script>

<template>
  <view class="page">
    <view class="headline">
      <text class="caption">LEARNING CENTER</text>
      <text class="title">学习中心</text>
    </view>

    <view class="mode-tabs">
      <button class="mode" :class="{ active: activeMode === 'HOMEWORK' }" @tap="switchMode('HOMEWORK')">作业</button>
      <button class="mode" :class="{ active: activeMode === 'MATERIAL' }" @tap="switchMode('MATERIAL')">资料</button>
    </view>

    <view v-if="activeMode === 'HOMEWORK'">
      <view class="tabs">
        <button
          v-for="tab in homeworkTabs"
          :key="tab.value"
          class="tab"
          :class="{ active: activeStatus === tab.value }"
          @tap="activeStatus = tab.value"
        >
          {{ tab.label }}
        </button>
      </view>

      <view v-if="filteredHomeworks.length" class="list">
        <view v-for="item in filteredHomeworks" :key="item.id" class="homework-card" @tap="openHomework(item.id)">
          <view class="card-head">
            <text class="homework-title">{{ item.title }}</text>
            <text class="status" :class="statusClass(item.studentStatus)">{{ statusLabel(item.studentStatus) }}</text>
          </view>
          <text class="summary">{{ item.content }}</text>
          <view class="meta-row">
            <text>{{ item.teacherName }}</text>
            <text>{{ item.deadline ? `截止 ${item.deadline.slice(0, 16).replace('T', ' ')}` : '无截止时间' }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty">{{ loading ? '加载中...' : '暂无作业' }}</view>
    </view>

    <view v-else>
      <scroll-view scroll-x class="category-strip">
        <button class="category" :class="{ active: !activeCategoryId }" @tap="activeCategoryId = undefined">全部</button>
        <button
          v-for="category in categories"
          :key="category.id"
          class="category"
          :class="{ active: activeCategoryId === category.id }"
          @tap="activeCategoryId = category.id"
        >
          {{ category.name }}
        </button>
      </scroll-view>

      <view v-if="filteredMaterials.length" class="list">
        <view v-for="item in filteredMaterials" :key="item.id" class="material-card" @tap="openMaterial(item.id)">
          <view class="material-icon">{{ resourceLabel(item.resourceType) }}</view>
          <view class="material-main">
            <view class="card-head">
              <text class="homework-title">{{ item.title }}</text>
              <text class="status" :class="item.studyType === 'REQUIRED' ? 'red' : 'blue'">
                {{ item.studyType === 'REQUIRED' ? '必学' : '选学' }}
              </text>
            </view>
            <text class="summary">{{ item.description || item.fileName }}</text>
            <view class="meta-row">
              <text>{{ item.categoryName }}</text>
              <text>{{ item.allowDownload ? '可下载' : '仅预览' }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty">{{ loading ? '加载中...' : '暂无资料' }}</view>
    </view>
    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 160rpx;
  background: #f6f1e8;
  color: #17211d;
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
}

.mode-tabs,
.tabs {
  margin-top: 24rpx;
  display: flex;
  gap: 12rpx;
}

.mode,
.tab,
.category {
  height: 62rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  border: 2rpx solid #e0ddd6;
  background: transparent;
  color: #6f756f;
  font-size: 23rpx;
  font-weight: 800;
}

.mode {
  flex: 1;
}

.mode.active,
.tab.active,
.category.active {
  background: #17211d;
  color: #fff;
  border-color: #17211d;
}

.category-strip {
  width: 100%;
  margin-top: 24rpx;
  white-space: nowrap;
}

.category {
  display: inline-flex;
  align-items: center;
  margin-right: 12rpx;
}

.list {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.homework-card,
.material-card {
  padding: 26rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.material-card {
  display: flex;
  gap: 20rpx;
}

.material-icon {
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #285c7f;
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
}

.material-main {
  flex: 1;
  min-width: 0;
}

.card-head,
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.homework-title {
  flex: 1;
  min-width: 0;
  font-size: 29rpx;
  font-weight: 900;
}

.status {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
}

.status.green { background: #22624c; }
.status.red   { background: #e07b54; }
.status.blue  { background: #285c7f; }
.status.dark  { background: #17211d; }

.summary {
  display: -webkit-box;
  margin-top: 14rpx;
  overflow: hidden;
  color: #909090;
  font-size: 24rpx;
  line-height: 1.55;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta-row {
  margin-top: 16rpx;
  color: #aaa;
  font-size: 22rpx;
}

.empty {
  margin-top: 80rpx;
  color: #aaa;
  font-size: 26rpx;
  text-align: center;
}
</style>
