<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { createGroupRequest } from '@/api/student';
import { useAuthStore } from '@/stores/auth';
import { requireStudentAccess } from '@/utils/auth-flow';

const auth = useAuthStore();
const submitting = ref(false);
const errors = reactive<Record<string, string>>({});
const selectedTimeSlots = ref<string[]>([]);

const gradeOptions = ['幼儿园大班', '一年级', '二年级', '三年级', '四年级', '五年级', '六年级'];
const systemOptions = ['校内同步', '自然拼读', '剑桥 KET', '剑桥 PET', '新概念', '原版阅读'];
const levelOptions = ['零基础', '认识字母', '能读简单单词', '校内同步较弱', '有绘本阅读', '备考提升'];
const timeSlotOptions = ['周六上午', '周六下午', '周日上午', '周日下午', '工作日晚', '寒暑假白天'];

const form = reactive({
  childAge: '',
  grade: '',
  targetSystem: '',
  englishLevel: '',
  preferredTimes: '',
  remark: '',
  contactPhone: '',
});

const preferredTimeItems = computed(() => {
  const customTimes = form.preferredTimes
    .split(/[，,\n]/)
    .map((item) => item.trim())
    .filter(Boolean);
  return Array.from(new Set([...selectedTimeSlots.value, ...customTimes]));
});

const completedCount = computed(() => {
  return [
    form.childAge,
    form.grade,
    form.targetSystem,
    form.englishLevel,
    preferredTimeItems.value.length,
  ].filter(Boolean).length;
});

const isFormValid = computed(() => completedCount.value === 5);
const phonePlaceholder = computed(() => auth.userInfo?.phone || '请输入手机号');
const submitSubtitle = computed(() => {
  if (isFormValid.value) {
    return '资料已完整，可以提交';
  }
  return `还差 ${5 - completedCount.value} 项必填信息`;
});

requireStudentAccess();

function setOption(field: 'grade' | 'targetSystem' | 'englishLevel', value: string) {
  form[field] = value;
  errors[field] = '';
}

function changeAge(offset: number) {
  const current = Number(form.childAge || 0);
  const next = Math.min(18, Math.max(3, Number((current + offset).toFixed(1))));
  form.childAge = String(next);
  errors.childAge = '';
}

function toggleTimeSlot(slot: string) {
  if (selectedTimeSlots.value.includes(slot)) {
    selectedTimeSlots.value = selectedTimeSlots.value.filter((item) => item !== slot);
  } else {
    selectedTimeSlots.value = [...selectedTimeSlots.value, slot];
  }
  errors.preferredTimes = '';
}

function validate(): boolean {
  let valid = true;
  errors.childAge = '';
  errors.grade = '';
  errors.targetSystem = '';
  errors.englishLevel = '';
  errors.preferredTimes = '';

  if (!form.childAge || Number.isNaN(Number(form.childAge))) {
    errors.childAge = '请填写孩子年龄';
    valid = false;
  }
  if (!form.grade) {
    errors.grade = '请选择年级';
    valid = false;
  }
  if (!form.targetSystem) {
    errors.targetSystem = '请选择目标体系';
    valid = false;
  }
  if (!form.englishLevel) {
    errors.englishLevel = '请选择英语基础';
    valid = false;
  }
  if (!preferredTimeItems.value.length) {
    errors.preferredTimes = '请选择或填写可上课时间';
    valid = false;
  }

  return valid;
}

async function submit() {
  if (!validate()) {
    uni.showToast({ title: '请完善必填信息', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    const detail = await createGroupRequest({
      childAge: Number(form.childAge),
      grade: form.grade,
      targetSystem: form.targetSystem,
      englishLevel: form.englishLevel,
      preferredTimes: preferredTimeItems.value,
      remark: form.remark,
      contactPhone: form.contactPhone || auth.userInfo?.phone,
    });
    uni.redirectTo({ url: `/pages/student/group/detail?id=${detail.id}` });
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '发起失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <view class="hero-copy">
        <text class="eyebrow">GROUP MATCH</text>
        <text class="hero-title">我要拼班</text>
        <text class="hero-subtitle">填写孩子的学习画像，校区会按年级、目标和时间段匹配同伴。</text>
      </view>
      <view class="progress-card">
        <text class="progress-number">{{ completedCount }}/5</text>
        <text class="progress-label">必填完成</text>
      </view>
    </view>

    <view class="notice">
      <text class="notice-title">匹配规则</text>
      <text class="notice-text">人数接近后，老师会联系确认试听时间和具体班级。</text>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-index">01</text>
        <view>
          <text class="section-title">孩子基础</text>
          <text class="section-subtitle">先确定孩子所在阶段</text>
        </view>
      </view>

      <view class="field age-field" :class="{ error: errors.childAge }">
        <view class="field-head">
          <text class="field-label">孩子年龄 <text class="required">*</text></text>
          <text v-if="errors.childAge" class="error-msg">{{ errors.childAge }}</text>
        </view>
        <view class="age-control">
          <button class="age-button" @tap="changeAge(-0.5)">-</button>
          <input
            v-model="form.childAge"
            class="age-input"
            type="digit"
            placeholder="8.5"
            @input="errors.childAge = ''"
          />
          <text class="age-unit">岁</text>
          <button class="age-button" @tap="changeAge(0.5)">+</button>
        </view>
      </view>

      <view class="field" :class="{ error: errors.grade }">
        <view class="field-head">
          <text class="field-label">年级 <text class="required">*</text></text>
          <text v-if="errors.grade" class="error-msg">{{ errors.grade }}</text>
        </view>
        <view class="chip-grid">
          <button
            v-for="option in gradeOptions"
            :key="option"
            class="chip"
            :class="{ active: form.grade === option }"
            @tap="setOption('grade', option)"
          >
            {{ option }}
          </button>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-index">02</text>
        <view>
          <text class="section-title">学习目标</text>
          <text class="section-subtitle">让系统更容易找到同需求家庭</text>
        </view>
      </view>

      <view class="field" :class="{ error: errors.targetSystem }">
        <view class="field-head">
          <text class="field-label">目标体系 <text class="required">*</text></text>
          <text v-if="errors.targetSystem" class="error-msg">{{ errors.targetSystem }}</text>
        </view>
        <view class="chip-grid two">
          <button
            v-for="option in systemOptions"
            :key="option"
            class="chip"
            :class="{ active: form.targetSystem === option }"
            @tap="setOption('targetSystem', option)"
          >
            {{ option }}
          </button>
        </view>
      </view>

      <view class="field" :class="{ error: errors.englishLevel }">
        <view class="field-head">
          <text class="field-label">英语基础 <text class="required">*</text></text>
          <text v-if="errors.englishLevel" class="error-msg">{{ errors.englishLevel }}</text>
        </view>
        <view class="chip-grid two">
          <button
            v-for="option in levelOptions"
            :key="option"
            class="chip"
            :class="{ active: form.englishLevel === option }"
            @tap="setOption('englishLevel', option)"
          >
            {{ option }}
          </button>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-index">03</text>
        <view>
          <text class="section-title">上课时间</text>
          <text class="section-subtitle">可多选，补充时间用逗号或换行分隔</text>
        </view>
      </view>

      <view class="field" :class="{ error: errors.preferredTimes }">
        <view class="field-head">
          <text class="field-label">常用时间 <text class="required">*</text></text>
          <text v-if="errors.preferredTimes" class="error-msg">{{ errors.preferredTimes }}</text>
        </view>
        <view class="chip-grid two">
          <button
            v-for="slot in timeSlotOptions"
            :key="slot"
            class="chip"
            :class="{ active: selectedTimeSlots.includes(slot) }"
            @tap="toggleTimeSlot(slot)"
          >
            {{ slot }}
          </button>
        </view>
        <textarea
          v-model="form.preferredTimes"
          class="textarea compact"
          placeholder="补充具体时间，例如：周六 9:00-11:00"
          @input="errors.preferredTimes = ''"
        />
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-index">04</text>
        <view>
          <text class="section-title">联系与备注</text>
          <text class="section-subtitle">选填，方便老师快速沟通</text>
        </view>
      </view>

      <view class="field">
        <text class="field-label">联系电话</text>
        <input
          v-model="form.contactPhone"
          class="text-input"
          type="number"
          maxlength="11"
          :placeholder="phonePlaceholder"
        />
      </view>

      <view class="field">
        <text class="field-label">补充说明</text>
        <textarea
          v-model="form.remark"
          class="textarea"
          placeholder="例如学习目标、希望人数、是否需要试听提醒"
        />
      </view>
    </view>

    <view class="submit-space" />
    <view class="submit-bar">
      <view class="submit-meta">
        <text class="submit-title">发起后进入拼班详情</text>
        <text class="submit-subtitle">{{ submitSubtitle }}</text>
      </view>
      <button
        class="submit-button"
        :class="{ disabled: !isFormValid || submitting }"
        :disabled="!isFormValid || submitting"
        @tap="submit"
      >
        {{ submitting ? '提交中' : '发起' }}
      </button>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 28rpx 28rpx 0;
  background: #f4efe6;
  color: #17211d;
}

button {
  margin: 0;
  padding: 0;
  border: 0;
  line-height: 1;
}

button::after {
  border: 0;
}

.hero {
  padding: 28rpx 8rpx 18rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24rpx;
}

.hero-copy {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  display: block;
  color: #2d6f83;
  font-size: 21rpx;
  font-weight: 900;
  letter-spacing: 2rpx;
}

.hero-title {
  display: block;
  margin-top: 8rpx;
  color: #17211d;
  font-size: 52rpx;
  font-weight: 900;
  line-height: 1.12;
}

.hero-subtitle {
  display: block;
  max-width: 500rpx;
  margin-top: 14rpx;
  color: #747a74;
  font-size: 25rpx;
  line-height: 1.55;
}

.progress-card {
  width: 136rpx;
  height: 136rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 30rpx;
  background: #18231e;
  color: #fff;
  box-shadow: 0 18rpx 36rpx rgba(23, 33, 29, 0.14);
}

.progress-number {
  color: #f0b84d;
  font-size: 38rpx;
  font-weight: 900;
}

.progress-label {
  margin-top: 2rpx;
  font-size: 19rpx;
  font-weight: 800;
}

.notice {
  margin: 14rpx 4rpx 28rpx;
  padding: 22rpx 24rpx;
  border-radius: 22rpx;
  background: #e7f0ed;
  border-left: 8rpx solid #2d6f83;
}

.notice-title {
  display: block;
  color: #225c49;
  font-size: 25rpx;
  font-weight: 900;
}

.notice-text {
  display: block;
  margin-top: 6rpx;
  color: #597065;
  font-size: 23rpx;
  line-height: 1.45;
}

.section {
  margin-top: 26rpx;
}

.section-head {
  padding: 0 6rpx 16rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.section-index {
  width: 54rpx;
  height: 54rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 22rpx;
  font-weight: 900;
}

.section-title {
  display: block;
  color: #17211d;
  font-size: 32rpx;
  font-weight: 900;
}

.section-subtitle {
  display: block;
  margin-top: 4rpx;
  color: #8b8d87;
  font-size: 23rpx;
  line-height: 1.45;
}

.field {
  margin-top: 14rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: #fffcf5;
  border: 2rpx solid transparent;
  box-shadow: 0 10rpx 24rpx rgba(54, 43, 30, 0.04);
}

.field.error {
  border-color: rgba(232, 93, 76, 0.7);
}

.field-head {
  min-height: 38rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.field-label {
  display: block;
  color: #17211d;
  font-size: 28rpx;
  font-weight: 900;
}

.required,
.error-msg {
  color: #e85d4c;
}

.error-msg {
  flex-shrink: 0;
  font-size: 21rpx;
  font-weight: 800;
}

.age-field {
  padding-bottom: 18rpx;
}

.age-control {
  margin-top: 20rpx;
  height: 82rpx;
  display: flex;
  align-items: center;
  border-radius: 20rpx;
  background: #f5f0e8;
  overflow: hidden;
}

.age-button {
  width: 92rpx;
  height: 82rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
  background: #e7f0ed;
  color: #225c49;
  font-size: 36rpx;
  font-weight: 900;
}

.age-input {
  flex: 1;
  height: 82rpx;
  padding: 0 18rpx;
  text-align: center;
  color: #17211d;
  font-size: 40rpx;
  font-weight: 900;
}

.age-unit {
  padding-right: 20rpx;
  color: #777b75;
  font-size: 24rpx;
  font-weight: 800;
}

.chip-grid {
  margin-top: 18rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.chip {
  min-width: 150rpx;
  height: 66rpx;
  padding: 0 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  background: #f5f0e8;
  color: #575b56;
  font-size: 24rpx;
  font-weight: 800;
  border: 2rpx solid transparent;
}

.chip-grid.two .chip {
  width: calc((100% - 12rpx) / 2);
  min-width: 0;
  box-sizing: border-box;
}

.chip.active {
  background: #225c49;
  color: #fff;
  border-color: #225c49;
  box-shadow: 0 10rpx 24rpx rgba(34, 92, 73, 0.18);
}

.text-input,
.textarea {
  width: 100%;
  margin-top: 18rpx;
  box-sizing: border-box;
  border-radius: 20rpx;
  background: #f5f0e8;
  color: #17211d;
  font-size: 27rpx;
  font-weight: 700;
  line-height: 1.55;
}

.text-input {
  height: 82rpx;
  padding: 0 22rpx;
}

.textarea {
  min-height: 156rpx;
  padding: 22rpx;
}

.textarea.compact {
  min-height: 104rpx;
}

.submit-space {
  height: 160rpx;
}

.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: rgba(255, 252, 245, 0.96);
  border-top: 1rpx solid #e6ded0;
  box-shadow: 0 -12rpx 30rpx rgba(23, 33, 29, 0.08);
}

.submit-meta {
  flex: 1;
  min-width: 0;
}

.submit-title {
  display: block;
  color: #17211d;
  font-size: 25rpx;
  font-weight: 900;
}

.submit-subtitle {
  display: block;
  margin-top: 4rpx;
  color: #858982;
  font-size: 21rpx;
  line-height: 1.35;
}

.submit-button {
  width: 166rpx;
  height: 82rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  background: #17211d;
  color: #fff;
  font-size: 28rpx;
  font-weight: 900;
  box-shadow: 0 14rpx 26rpx rgba(23, 33, 29, 0.18);
}

.submit-button.disabled {
  opacity: 0.42;
  box-shadow: none;
}
</style>
