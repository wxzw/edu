<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import { createTeacherHomework, getTeacherClasses, publishTeacherHomework } from '@/api/teacher';
import type { ClassListItem, CreateHomeworkRequest } from '@/types/api';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

const classes = ref<ClassListItem[]>([]);
const selectedClassIndex = ref(-1);
const loading = ref(false);
const classLoading = ref(false);

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

const selectedClass = computed(() => {
  if (selectedClassIndex.value < 0) return undefined;
  return classes.value[selectedClassIndex.value];
});
const deadlineText = computed(() => form.value.deadline ? form.value.deadline.substring(0, 10) : '请选择截止日期');

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchClasses();
});

async function fetchClasses() {
  classLoading.value = true;
  try {
    classes.value = await getTeacherClasses();
    if (classes.value.length && selectedClassIndex.value < 0) {
      selectClass(0);
    }
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '班级加载失败', icon: 'none' });
  } finally {
    classLoading.value = false;
  }
}

function onClassChange(e: any) {
  selectClass(Number(e.detail.value));
}

function selectClass(index: number) {
  const target = classes.value[index];
  if (!target) return;
  selectedClassIndex.value = index;
  form.value.targetType = 'CLASS';
  form.value.targetClassIds = [target.id];
  form.value.targetStudentIds = [];
}

function onCheckinChange(e: any) {
  form.value.checkinEnabled = Boolean(e.detail.value);
}

function onDeadlineChange(e: any) {
  form.value.deadline = `${e.detail.value}T23:59:59`;
}

function normalizePayload(): CreateHomeworkRequest {
  return {
    ...form.value,
    title: form.value.title.trim(),
    content: form.value.content?.trim(),
    checkinDays: form.value.checkinEnabled ? Math.max(1, Number(form.value.checkinDays || 1)) : undefined,
  };
}

async function submit(publish: boolean) {
  const payload = normalizePayload();
  if (!payload.title) {
    uni.showToast({ title: '请输入作业标题', icon: 'none' });
    return;
  }
  if (!payload.targetClassIds?.length) {
    uni.showToast({ title: '请选择发布班级', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    const res = await createTeacherHomework(payload);
    if (publish && res.id) {
      await publishTeacherHomework(res.id);
      uni.showToast({ title: '发布成功', icon: 'success' });
    } else {
      uni.showToast({ title: '保存成功', icon: 'success' });
    }
    setTimeout(() => {
      uni.navigateBack();
    }, 700);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '操作失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="New Homework" title="发布作业" subtitle="首版支持按班级发布">
      <view class="hero-note">
        <text>学生提交后会进入作业详情页，老师可用星级和模板快速点评。</text>
      </view>
    </TeacherHeroCard>

    <view class="panel">
      <view class="form-row">
        <text class="form-label">作业标题</text>
        <input
          v-model="form.title"
          class="form-input"
          placeholder="例如：短元音a朗读打卡"
          maxlength="80"
        />
      </view>

      <view class="form-row">
        <text class="form-label">作业内容</text>
        <textarea
          v-model="form.content"
          class="form-textarea"
          placeholder="写清楚朗读、录音、图片或文本提交要求"
          :maxlength="2000"
        />
      </view>

      <view class="form-row">
        <text class="form-label">发布班级</text>
        <picker
          v-if="classes.length"
          mode="selector"
          :range="classes"
          range-key="name"
          :value="Math.max(selectedClassIndex, 0)"
          @change="onClassChange"
        >
          <view class="form-picker">
            <view>
              <text class="picker-main">{{ selectedClass?.name || '请选择班级' }}</text>
              <text class="picker-sub">{{ selectedClass?.courseName || '班级课程' }}</text>
            </view>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
        <TeacherEmptyState
          v-else
          :title="classLoading ? '正在加载班级' : '暂无可发布班级'"
          description="请先在后台或老师班级页确认班级数据。"
        />
      </view>

      <view class="form-row">
        <text class="form-label">截止时间</text>
        <picker mode="date" @change="onDeadlineChange">
          <view class="form-picker single">
            <text>{{ deadlineText }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="switch-row">
        <view>
          <text class="form-label">启用打卡</text>
          <text class="form-help">适合朗读、背诵、每日练习类作业</text>
        </view>
        <switch :checked="form.checkinEnabled" color="#1f5a44" @change="onCheckinChange" />
      </view>

      <view class="form-row" v-if="form.checkinEnabled">
        <text class="form-label">打卡天数</text>
        <input
          v-model.number="form.checkinDays"
          class="form-input"
          type="number"
          placeholder="例如：7"
        />
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-secondary" :disabled="loading" @tap="submit(false)">
        保存草稿
      </button>
      <button class="btn-primary" :disabled="loading" @tap="submit(true)">
        {{ loading ? '处理中' : '立即发布' }}
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

.form-row,
.switch-row {
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

.form-help {
  display: block;
  margin-top: 8rpx;
  color: #8b8d87;
  font-size: 22rpx;
  line-height: 1.45;
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
  min-height: 220rpx;
  padding: 22rpx;
  line-height: 1.5;
}

.form-picker {
  min-height: 84rpx;
  padding: 18rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.form-picker.single {
  min-height: 78rpx;
}

.picker-main,
.picker-sub {
  display: block;
}

.picker-main {
  color: #17211d;
  font-size: 27rpx;
  font-weight: 900;
}

.picker-sub {
  margin-top: 6rpx;
  color: #858982;
  font-size: 22rpx;
  font-weight: 800;
}

.picker-arrow {
  color: #b6b0a6;
  font-size: 44rpx;
  line-height: 1;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 16rpx;
  background: rgba(255, 252, 245, 0.96);
  border-top: 1rpx solid #e6ded0;
}

.btn-secondary,
.btn-primary {
  flex: 1;
  height: 86rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 900;
}

.btn-secondary {
  background: #f1ebe2;
  color: #5f655f;
}

.btn-primary {
  background: #1f5a44;
  color: #fff;
}

.btn-secondary[disabled],
.btn-primary[disabled] {
  opacity: 0.56;
}
</style>
