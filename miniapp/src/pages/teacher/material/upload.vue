<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

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
});

const loading = ref(false);
const resourceTypeIndex = computed(() => Math.max(0, resourceTypes.findIndex((item) => item.value === form.value.resourceType)));
const visibilityIndex = computed(() => Math.max(0, visibilityOptions.findIndex((item) => item.value === form.value.visibility)));
const studyTypeIndex = computed(() => Math.max(0, studyTypes.findIndex((item) => item.value === form.value.studyType)));
const resourceTypeLabel = computed(() => resourceTypes[resourceTypeIndex.value]?.label || 'PDF');
const visibilityLabel = computed(() => visibilityOptions[visibilityIndex.value]?.label || '校区可见');
const studyTypeLabel = computed(() => studyTypes[studyTypeIndex.value]?.label || '必修');

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
});

function onResourceTypeChange(e: any) {
  form.value.resourceType = resourceTypes[Number(e.detail.value)]?.value || 'PDF';
}

function onVisibilityChange(e: any) {
  form.value.visibility = visibilityOptions[Number(e.detail.value)]?.value || 'CAMPUS';
}

function onStudyTypeChange(e: any) {
  form.value.studyType = studyTypes[Number(e.detail.value)]?.value || 'REQUIRED';
}

function submit() {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入资料标题', icon: 'none' });
    return;
  }
  uni.showToast({ title: '资料上传接口暂未开放', icon: 'none' });
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Upload" title="上传资料" subtitle="先整理资料信息，接口开放后可直接接入">
      <view class="hero-note">
        <text>建议优先上传课堂讲义、朗读音频和复习资料。</text>
      </view>
    </TeacherHeroCard>

    <view class="panel">
      <view class="form-row">
        <text class="form-label">资料标题</text>
        <input
          v-model="form.title"
          class="form-input"
          placeholder="例如：自然拼读L1复习讲义"
          maxlength="80"
        />
      </view>

      <view class="form-row">
        <text class="form-label">资料描述</text>
        <textarea
          v-model="form.description"
          class="form-textarea"
          placeholder="说明适用班级、使用场景和学习目标"
          :maxlength="500"
        />
      </view>

      <view class="form-row">
        <text class="form-label">资源类型</text>
        <picker
          mode="selector"
          :range="resourceTypes"
          range-key="label"
          :value="resourceTypeIndex"
          @change="onResourceTypeChange"
        >
          <view class="form-picker">
            <text>{{ resourceTypeLabel }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">可见范围</text>
        <picker
          mode="selector"
          :range="visibilityOptions"
          range-key="label"
          :value="visibilityIndex"
          @change="onVisibilityChange"
        >
          <view class="form-picker">
            <text>{{ visibilityLabel }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">学习类型</text>
        <picker
          mode="selector"
          :range="studyTypes"
          range-key="label"
          :value="studyTypeIndex"
          @change="onStudyTypeChange"
        >
          <view class="form-picker">
            <text>{{ studyTypeLabel }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-primary" :disabled="loading" @tap="submit">提交资料</button>
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
