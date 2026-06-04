<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { createTeacherHomework, publishTeacherHomework } from '@/api/teacher';
import type { CreateHomeworkRequest } from '@/types/api';

const form = ref<CreateHomeworkRequest>({
  title: '',
  content: '',
  targetType: 'CLASS',
  targetClassIds: [],
  targetStudentIds: [],
  deadline: '',
  checkinEnabled: false,
  checkinDays: 7,
  attachments: [],
});

const loading = ref(false);

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
});

function onTargetTypeChange(e: any) {
  form.value.targetType = e.detail.value;
  form.value.targetClassIds = [];
  form.value.targetStudentIds = [];
}

function onCheckinChange(e: any) {
  form.value.checkinEnabled = e.detail.value;
}

async function submit(publish: boolean) {
  if (!form.value.title.trim()) {
    uni.showToast({ title: '请输入作业标题', icon: 'none' });
    return;
  }
  if (form.value.targetType === 'CLASS' && !form.value.targetClassIds?.length) {
    uni.showToast({ title: '请选择目标班级', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    const res = await createTeacherHomework(form.value);
    const homeworkId = res.id;
    if (publish && homeworkId) {
      await publishTeacherHomework(homeworkId);
      uni.showToast({ title: '发布成功', icon: 'success' });
    } else {
      uni.showToast({ title: '保存成功', icon: 'success' });
    }
    setTimeout(() => {
      uni.navigateBack();
    }, 800);
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">New Homework</text>
      <text class="title">发布作业</text>
    </view>

    <view class="panel">
      <view class="form-row">
        <text class="form-label">作业标题</text>
        <input
          class="form-input"
          v-model="form.title"
          placeholder="请输入作业标题"
        />
      </view>

      <view class="form-row">
        <text class="form-label">作业内容</text>
        <textarea
          class="form-textarea"
          v-model="form.content"
          placeholder="请输入作业内容"
          :maxlength="2000"
        />
      </view>

      <view class="form-row">
        <text class="form-label">目标类型</text>
        <picker mode="selector" :range="['班级', '学生']" @change="onTargetTypeChange">
          <view class="form-picker">
            {{ form.targetType === 'CLASS' ? '班级' : '学生' }}
          </view>
        </picker>
      </view>

      <view class="form-row" v-if="form.targetType === 'CLASS'">
        <text class="form-label">选择班级</text>
        <text class="form-hint">（此处应展示班级选择器，需接入班级列表接口）</text>
      </view>

      <view class="form-row">
        <text class="form-label">截止时间</text>
        <picker mode="date" @change="(e: any) => form.deadline = e.detail.value + 'T23:59:59'">
          <view class="form-picker">
            {{ form.deadline ? form.deadline.substring(0, 10) : '请选择' }}
          </view>
        </picker>
      </view>

      <view class="form-row">
        <text class="form-label">启用打卡</text>
        <switch :checked="form.checkinEnabled" @change="onCheckinChange" />
      </view>

      <view class="form-row" v-if="form.checkinEnabled">
        <text class="form-label">打卡天数</text>
        <input
          class="form-input"
          type="number"
          v-model.number="form.checkinDays"
          placeholder="打卡天数"
        />
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-secondary" @tap="submit(false)">保存草稿</button>
      <button class="btn-primary" @tap="submit(true)">立即发布</button>
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
  height: 200rpx;
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

.form-hint {
  font-size: 22rpx;
  color: #999;
}

.action-bar {
  margin-top: 40rpx;
  display: flex;
  gap: 20rpx;
}

.btn-secondary {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 30rpx;
  font-weight: 700;
}

.btn-primary {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  background: #22624c;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
}
</style>
