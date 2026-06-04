<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';

const form = ref({
  title: '',
  description: '',
  categoryId: 0,
  resourceType: 'PDF',
  visibility: 'CAMPUS',
  studyType: 'REQUIRED',
});

const loading = ref(false);

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
});

function submit() {
  uni.showToast({ title: '上传功能开发中', icon: 'none' });
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">Upload</text>
      <text class="title">上传资料</text>
    </view>

    <view class="panel">
      <view class="form-row">
        <text class="form-label">资料标题</text>
        <input class="form-input" v-model="form.title" placeholder="请输入资料标题" />
      </view>
      <view class="form-row">
        <text class="form-label">资料描述</text>
        <textarea class="form-textarea" v-model="form.description" placeholder="请输入资料描述" />
      </view>
      <view class="form-row">
        <text class="form-label">资源类型</text>
        <picker mode="selector" :range="['PDF', 'VIDEO', 'AUDIO', 'IMAGE']" @change="(e: any) => form.resourceType = ['PDF', 'VIDEO', 'AUDIO', 'IMAGE'][e.detail.value]">
          <view class="form-picker">{{ form.resourceType }}</view>
        </picker>
      </view>
      <view class="form-row">
        <text class="form-label">可见范围</text>
        <picker mode="selector" :range="['校区', '班级', '仅自己']" @change="(e: any) => form.visibility = ['CAMPUS', 'CLASS', 'SELF'][e.detail.value]">
          <view class="form-picker">
            {{ form.visibility === 'CAMPUS' ? '校区' : form.visibility === 'CLASS' ? '班级' : '仅自己' }}
          </view>
        </picker>
      </view>
      <view class="form-row">
        <text class="form-label">学习类型</text>
        <picker mode="selector" :range="['必修', '选修']" @change="(e: any) => form.studyType = e.detail.value === 0 ? 'REQUIRED' : 'OPTIONAL'">
          <view class="form-picker">
            {{ form.studyType === 'REQUIRED' ? '必修' : '选修' }}
          </view>
        </picker>
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-primary" @tap="submit">提交</button>
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

.panel {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 20rpx 0;
  border-top: 1px solid #F0EAE0;
}

.form-label {
  font-size: 26rpx;
  font-weight: 700;
  color: #17211d;
}

.form-input {
  height: 72rpx;
  padding: 0 20rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #17211d;
}

.form-textarea {
  height: 160rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #17211d;
}

.form-picker {
  height: 72rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  border-radius: 12rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #17211d;
}

.action-bar {
  margin-top: 40rpx;
}

.btn-primary {
  width: 100%;
  height: 88rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
}
</style>
