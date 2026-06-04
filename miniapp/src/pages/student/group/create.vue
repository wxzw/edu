<script setup lang="ts">
import { reactive, ref, computed } from 'vue';
import { createGroupRequest } from '@/api/student';
import { useAuthStore } from '@/stores/auth';
import { requireStudentAccess } from '@/utils/auth-flow';
import FormField from '@/components/FormField.vue';
import FormSection from '@/components/FormSection.vue';
import SubmitButton from '@/components/SubmitButton.vue';

const auth = useAuthStore();
const submitting = ref(false);
const errors = reactive<Record<string, string>>({});

const form = reactive({
  childAge: '',
  grade: '',
  targetSystem: '',
  englishLevel: '',
  preferredTimes: '',
  remark: '',
  contactPhone: '',
});

const isFormValid = computed(() => {
  return form.childAge && form.grade && form.targetSystem && form.englishLevel && form.preferredTimes;
});

requireStudentAccess();

function validate(): boolean {
  let valid = true;
  errors.childAge = '';
  errors.grade = '';
  errors.targetSystem = '';
  errors.englishLevel = '';
  errors.preferredTimes = '';

  if (!form.childAge) {
    errors.childAge = '请填写孩子年龄';
    valid = false;
  }
  if (!form.grade) {
    errors.grade = '请填写年级';
    valid = false;
  }
  if (!form.targetSystem) {
    errors.targetSystem = '请填写目标体系';
    valid = false;
  }
  if (!form.englishLevel) {
    errors.englishLevel = '请填写英语基础';
    valid = false;
  }
  if (!form.preferredTimes) {
    errors.preferredTimes = '请填写可上课时间';
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
      preferredTimes: form.preferredTimes.split(/[，,\n]/).map((item) => item.trim()).filter(Boolean),
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
  <view class="page page-fade-enter">
    <!-- 顶部标题区 -->
    <view class="hero">
      <view class="hero-badge">
        <view class="badge-dot" />
        <text class="badge-text">GROUP STARTER</text>
      </view>
      <text class="hero-title">我要拼班</text>
      <text class="hero-subtitle">满 4 人后，校区会安排试听课</text>
      <view class="hero-divider" />
    </view>

    <!-- 表单区 -->
    <FormSection title="基本信息" subtitle="请填写孩子的基本学习情况，方便老师安排合适的班级">
      <FormField label="孩子年龄" required :error="errors.childAge">
        <input
          v-model="form.childAge"
          type="digit"
          placeholder="例如 8.5"
          :class="{ 'input-error': errors.childAge }"
          @input="errors.childAge = ''"
        />
      </FormField>

      <FormField label="年级" required :error="errors.grade">
        <input
          v-model="form.grade"
          placeholder="例如 三年级"
          :class="{ 'input-error': errors.grade }"
          @input="errors.grade = ''"
        />
      </FormField>

      <FormField label="目标体系" required :error="errors.targetSystem">
        <input
          v-model="form.targetSystem"
          placeholder="例如 剑桥 KET 体系"
          :class="{ 'input-error': errors.targetSystem }"
          @input="errors.targetSystem = ''"
        />
      </FormField>

      <FormField label="英语基础" required :error="errors.englishLevel">
        <input
          v-model="form.englishLevel"
          placeholder="例如 校内同步"
          :class="{ 'input-error': errors.englishLevel }"
          @input="errors.englishLevel = ''"
        />
      </FormField>
    </FormSection>

    <FormSection title="上课时间" subtitle="填写孩子方便上课的时间段，用逗号分隔">
      <FormField label="可上课时间" required :error="errors.preferredTimes">
        <textarea
          v-model="form.preferredTimes"
          placeholder="用逗号分隔，例如：&#10;周六上午 9:00-11:00&#10;周日下午 14:00-16:00"
          :class="{ 'input-error': errors.preferredTimes }"
          @input="errors.preferredTimes = ''"
        />
      </FormField>
    </FormSection>

    <FormSection title="联系方式" subtitle="方便校区老师与您取得联系">
      <FormField label="联系电话">
        <input
          v-model="form.contactPhone"
          type="number"
          maxlength="11"
          :placeholder="auth.userInfo?.phone || '请输入手机号'"
        />
      </FormField>
    </FormSection>

    <FormSection title="其他" subtitle="可选填，帮助老师更好地了解孩子">
      <FormField label="备注">
        <textarea
          v-model="form.remark"
          placeholder="可填写学习目标、特殊需求或其他补充信息"
        />
      </FormField>
    </FormSection>

    <!-- 提交按钮 -->
    <view class="submit-wrap">
      <SubmitButton
        :loading="submitting"
        :disabled="!isFormValid"
        text="发起拼班"
        @tap="submit"
      />
      <text class="submit-hint">提交后，系统会自动为您匹配同需求的家长</text>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 32rpx 32rpx 80rpx;
  background: #f6f1e8;
  color: #17211d;
}

/* ===== Hero 标题区 ===== */
.hero {
  padding: 24rpx 8rpx 32rpx;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  padding: 8rpx 18rpx;
  border-radius: 100rpx;
  background: rgba(34, 98, 76, 0.08);
  margin-bottom: 20rpx;
}

.badge-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #22624c;
}

.badge-text {
  font-size: 22rpx;
  font-weight: 800;
  color: #22624c;
  letter-spacing: 2rpx;
}

.hero-title {
  display: block;
  font-size: 52rpx;
  font-weight: 900;
  color: #17211d;
  letter-spacing: 1rpx;
  line-height: 1.2;
}

.hero-subtitle {
  display: block;
  margin-top: 14rpx;
  font-size: 26rpx;
  color: #8a8a8a;
  font-weight: 500;
  line-height: 1.5;
}

.hero-divider {
  margin-top: 32rpx;
  height: 2rpx;
  background: linear-gradient(90deg, rgba(34, 98, 76, 0.15) 0%, transparent 100%);
  border-radius: 2rpx;
}

/* ===== 输入框样式 ===== */
input,
textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 20rpx 24rpx;
  border-radius: 14rpx;
  background: #f8f5ee;
  color: #17211d;
  font-size: 28rpx;
  font-weight: 500;
  line-height: 1.5;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
}

input::placeholder,
textarea::placeholder {
  color: #b8b4ad;
  font-weight: 400;
}

input:focus,
textarea:focus {
  background: #fff;
  border-color: rgba(34, 98, 76, 0.3);
  box-shadow: 0 0 0 4rpx rgba(34, 98, 76, 0.06);
}

input.input-error,
textarea.input-error {
  border-color: #e85d4c;
  background: rgba(232, 93, 76, 0.04);
}

textarea {
  min-height: 160rpx;
  line-height: 1.6;
}

/* ===== 提交区 ===== */
.submit-wrap {
  margin-top: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.submit-hint {
  font-size: 22rpx;
  color: #a0a0a0;
  font-weight: 400;
  text-align: center;
}
</style>
