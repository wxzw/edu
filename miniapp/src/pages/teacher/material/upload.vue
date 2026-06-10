<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';
import { uploadTeacherFile, getTeacherMaterialCategories, createTeacherMaterial } from '@/api/teacher';
import type { MaterialCategory } from '@/types/api';

const resourceTypes = [
  { label: 'PDF', value: 'PDF' },
  { label: '视频', value: 'VIDEO' },
  { label: '音频', value: 'AUDIO' },
  { label: '图片', value: 'IMAGE' },
];
const visibilityOptions = [
  { label: '校区可见', value: 'CAMPUS' },
  { label: '班级可见', value: 'CLASS' },
  { label: '仅自己', value: 'SELF' },
];
const studyTypes = [
  { label: '必修', value: 'REQUIRED' },
  { label: '选修', value: 'OPTIONAL' },
];

const form = ref({
  title: '',
  description: '',
  categoryId: 0,
  resourceType: 'PDF',
  visibility: 'CAMPUS',
  studyType: 'REQUIRED',
  allowDownload: true,
});

const fileInfo = ref<{ path: string; name: string; size: number; type: string } | null>(null);
const uploadedFileId = ref<number>(0);
const categories = ref<MaterialCategory[]>([]);
const loading = ref(false);
const uploading = ref(false);

const resourceTypeIndex = computed(() => Math.max(0, resourceTypes.findIndex((item) => item.value === form.value.resourceType)));
const visibilityIndex = computed(() => Math.max(0, visibilityOptions.findIndex((item) => item.value === form.value.visibility)));
const studyTypeIndex = computed(() => Math.max(0, studyTypes.findIndex((item) => item.value === form.value.studyType)));
const resourceTypeLabel = computed(() => resourceTypes[resourceTypeIndex.value]?.label || 'PDF');
const visibilityLabel = computed(() => visibilityOptions[visibilityIndex.value]?.label || '校区可见');
const studyTypeLabel = computed(() => studyTypes[studyTypeIndex.value]?.label || '必修');

const categoryNames = computed(() => categories.value.map((c) => c.name));
const categoryIndex = computed(() => categories.value.findIndex((c) => c.id === form.value.categoryId));
const categoryLabel = computed(() => categories.value.find((c) => c.id === form.value.categoryId)?.name || '请选择分类');

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  loadCategories();
});

async function loadCategories() {
  try {
    categories.value = await getTeacherMaterialCategories();
    if (categories.value.length > 0 && !form.value.categoryId) {
      form.value.categoryId = categories.value[0].id;
    }
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载分类失败', icon: 'none' });
  }
}

function chooseFile() {
  const type = form.value.resourceType;
  if (type === 'IMAGE') {
    uni.chooseImage({
      count: 1,
      success: (res) => {
        const path = res.tempFilePaths[0];
        uni.getFileInfo({ filePath: path, success: (fi) => {
          fileInfo.value = { path, name: '图片文件', size: fi.size, type: 'IMAGE' };
        }, fail: () => {
          fileInfo.value = { path, name: '图片文件', size: 0, type: 'IMAGE' };
        }});
      },
    });
  } else if (type === 'VIDEO') {
    uni.chooseVideo({
      success: (res) => {
        fileInfo.value = { path: res.tempFilePath, name: res.name || '视频文件', size: res.size || 0, type: 'VIDEO' };
      },
    });
  } else {
    // #ifdef MP-WEIXIN
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      success: (res: any) => {
        const item = res.tempFiles[0];
        fileInfo.value = { path: item.path, name: item.name, size: item.size || 0, type };
      },
      fail: () => {
        uni.showToast({ title: '请选择文件', icon: 'none' });
      },
    });
    // #endif
    // #ifndef MP-WEIXIN
    uni.chooseFile({
      count: 1,
      success: (res: any) => {
        const path = res.tempFilePaths[0];
        const name = res.tempFiles?.[0]?.name || '文件';
        fileInfo.value = { path, name, size: 0, type };
      },
    });
    // #endif
  }
}

function removeFile() {
  fileInfo.value = null;
  uploadedFileId.value = 0;
}

function formatSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function onResourceTypeChange(e: any) {
  form.value.resourceType = resourceTypes[Number(e.detail.value)]?.value || 'PDF';
  fileInfo.value = null;
  uploadedFileId.value = 0;
}

function onVisibilityChange(e: any) {
  form.value.visibility = visibilityOptions[Number(e.detail.value)]?.value || 'CAMPUS';
}

function onStudyTypeChange(e: any) {
  form.value.studyType = studyTypes[Number(e.detail.value)]?.value || 'REQUIRED';
}

function onCategoryChange(e: any) {
  const idx = Number(e.detail.value);
  if (categories.value[idx]) {
    form.value.categoryId = categories.value[idx].id;
  }
}

async function submit() {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入资料标题', icon: 'none' });
    return;
  }
  if (!form.value.categoryId) {
    uni.showToast({ title: '请选择分类', icon: 'none' });
    return;
  }
  if (!fileInfo.value) {
    uni.showToast({ title: '请选择文件', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    // 上传文件
    if (!uploadedFileId.value) {
      uploading.value = true;
      const result = await uploadTeacherFile(fileInfo.value.path, fileInfo.value.name);
      uploadedFileId.value = result.fileId;
      uploading.value = false;
    }

    // 创建资料
    await createTeacherMaterial({
      title: form.value.title.trim(),
      description: form.value.description.trim() || undefined,
      categoryId: form.value.categoryId,
      resourceType: form.value.resourceType,
      visibility: form.value.visibility,
      studyType: form.value.studyType,
      allowDownload: form.value.allowDownload,
      fileId: uploadedFileId.value,
    });

    uni.showToast({ title: '提交成功，等待审核', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1200);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' });
  } finally {
    loading.value = false;
    uploading.value = false;
  }
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Upload" title="上传资料" subtitle="上传教学资料，审核后学生即可查看">
      <view class="hero-note">
        <text>支持 PDF、视频、音频、图片等格式，文件大小不超过 50MB。</text>
      </view>
    </TeacherHeroCard>

    <view class="panel">
      <!-- 文件选择 -->
      <view class="form-row">
        <text class="form-label">选择文件</text>
        <view v-if="!fileInfo" class="file-picker" @tap="chooseFile">
          <text class="file-picker-icon">+</text>
          <text class="file-picker-text">点击选择{{ resourceTypeLabel }}文件</text>
        </view>
        <view v-else class="file-selected">
          <view class="file-info">
            <text class="file-name">{{ fileInfo.name }}</text>
            <text class="file-meta">{{ resourceTypeLabel }} · {{ formatSize(fileInfo.size) }}</text>
          </view>
          <text class="file-remove" @tap="removeFile">删除</text>
        </view>
        <text v-if="uploading" class="upload-hint">正在上传文件...</text>
      </view>

      <view class="form-row">
        <text class="form-label">资料标题</text>
        <input v-model="form.title" class="form-input" placeholder="例如：自然拼读L1复习讲义" maxlength="80" />
      </view>

      <view class="form-row">
        <text class="form-label">资料描述</text>
        <textarea v-model="form.description" class="form-textarea" placeholder="说明适用班级、使用场景和学习目标" :maxlength="500" />
      </view>

      <view class="form-row">
        <text class="form-label">分类</text>
        <picker mode="selector" :range="categoryNames" :value="categoryIndex" @change="onCategoryChange">
          <view class="form-picker">
            <text>{{ categoryLabel }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">资源类型</text>
        <picker mode="selector" :range="resourceTypes" range-key="label" :value="resourceTypeIndex" @change="onResourceTypeChange">
          <view class="form-picker">
            <text>{{ resourceTypeLabel }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">可见范围</text>
        <picker mode="selector" :range="visibilityOptions" range-key="label" :value="visibilityIndex" @change="onVisibilityChange">
          <view class="form-picker">
            <text>{{ visibilityLabel }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">学习类型</text>
        <picker mode="selector" :range="studyTypes" range-key="label" :value="studyTypeIndex" @change="onStudyTypeChange">
          <view class="form-picker">
            <text>{{ studyTypeLabel }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <view class="switch-row">
          <text class="form-label">允许下载</text>
          <switch :checked="form.allowDownload" color="#1f5a44" @change="form.allowDownload = ($event as any).detail.value" />
        </view>
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-primary" :disabled="loading || uploading" @tap="submit">
        {{ loading ? '提交中...' : '提交资料' }}
      </button>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 150rpx;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

button {
  margin: 0;
  padding: 0;
}

button::after {
  border: 0;
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

.form-row {
  padding: 24rpx 0;
  border-top: 1rpx solid #eee5d8;
}

.form-row:first-child {
  border-top: 0;
  padding-top: 0;
}

.form-label {
  display: block;
  color: #17211d;
  font-size: 27rpx;
  font-weight: 900;
}

.form-input,
.form-textarea,
.form-picker {
  width: 100%;
  margin-top: 14rpx;
  box-sizing: border-box;
  border-radius: 22rpx;
  background: #f5f0e8;
  color: #17211d;
  font-size: 26rpx;
}

.form-input {
  height: 78rpx;
  padding: 0 22rpx;
}

.form-textarea {
  min-height: 180rpx;
  padding: 22rpx;
  line-height: 1.5;
}

.form-picker {
  min-height: 78rpx;
  padding: 18rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.picker-arrow {
  color: #b6b0a6;
  font-size: 44rpx;
  line-height: 1;
}

.file-picker {
  margin-top: 14rpx;
  min-height: 160rpx;
  border-radius: 22rpx;
  border: 2rpx dashed #c7c1b7;
  background: #f5f0e8;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.file-picker-icon {
  color: #b6b0a6;
  font-size: 48rpx;
  font-weight: 300;
}

.file-picker-text {
  color: #8a847a;
  font-size: 24rpx;
  font-weight: 800;
}

.file-selected {
  margin-top: 14rpx;
  padding: 18rpx 22rpx;
  border-radius: 22rpx;
  background: #e7f0ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: block;
  color: #17211d;
  font-size: 26rpx;
  font-weight: 900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: block;
  margin-top: 6rpx;
  color: #6f756f;
  font-size: 22rpx;
}

.file-remove {
  flex-shrink: 0;
  color: #c0392b;
  font-size: 24rpx;
  font-weight: 900;
}

.upload-hint {
  display: block;
  margin-top: 10rpx;
  color: #1f5a44;
  font-size: 22rpx;
  font-weight: 800;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 252, 245, 0.96);
  border-top: 1rpx solid #e6ded0;
}

.btn-primary {
  width: 100%;
  height: 86rpx;
  border-radius: 24rpx;
  background: #1f5a44;
  color: #fff;
  font-size: 28rpx;
  font-weight: 900;
}

.btn-primary[disabled] {
  opacity: 0.56;
}
</style>
