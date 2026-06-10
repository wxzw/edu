<script setup lang="ts">
import { computed, ref } from 'vue';
import { miniappLogin } from '@/api/miniapp';
import { useAuthStore } from '@/stores/auth';
import type { IdentityType, MiniappLoginResponse } from '@/types/api';
import { switchToHome } from '@/utils/routes';

const auth = useAuthStore();
const loading = ref(false);
const selectedRole = ref<IdentityType>('GUARDIAN');
const mockPhone = ref('13900000101');
const debugExpanded = ref(false);
const errorMessage = ref('');
const mockLoginVisible = import.meta.env.DEV || import.meta.env.VITE_MINIAPP_MOCK_LOGIN === 'true';

const roleOptions: Array<{
  label: string;
  value: IdentityType;
  samplePhone: string;
  accent: string;
  copy: string;
}> = [
  { label: '老师', value: 'TEACHER', samplePhone: '13900000011', accent: '#285c7f', copy: '课表、考勤、作业点评' },
  { label: '学生', value: 'STUDENT', samplePhone: '13900000201', accent: '#22624c', copy: '课程、作业、学习资料' },
  { label: '家长', value: 'GUARDIAN', samplePhone: '13900000101', accent: '#b77624', copy: '孩子学习进度与活动报名' },
];
const selectedRoleLabel = computed(() => roleOptions.find((role) => role.value === selectedRole.value)?.label || '');

interface PhoneNumberEvent {
  detail?: {
    code?: string;
    errMsg?: string;
  };
}

function selectRole(value: IdentityType, samplePhone: string) {
  selectedRole.value = value;
  mockPhone.value = samplePhone;
}

function toggleDebug() {
  debugExpanded.value = !debugExpanded.value;
}

function getLoginCode() {
  return new Promise<string>((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (result) => {
        if (result.code) {
          resolve(result.code);
          return;
        }
        reject(new Error('未获取到微信登录凭证'));
      },
      fail: () => {
        reject(new Error('微信登录凭证获取失败'));
      },
    });
  });
}

function finishLogin(response: MiniappLoginResponse) {
  auth.applyLogin(response);
  if (response.availableIdentities.length > 1) {
    uni.reLaunch({ url: '/pages/identity/index' });
    return;
  }
  switchToHome(response.selectedIdentity.identityType);
}

function reportError(error: unknown, fallback: string) {
  const message = error instanceof Error ? error.message : fallback;
  errorMessage.value = message;
  uni.showToast({ title: message, icon: 'none' });
}

async function submitWechat(event: PhoneNumberEvent) {
  if (loading.value) {
    return;
  }

  const detail = event.detail || {};
  if (detail.errMsg && !detail.errMsg.includes(':ok')) {
    errorMessage.value = '需要授权手机号后才能登录';
    return;
  }
  if (!detail.code) {
    errorMessage.value = '未获取到手机号授权码，请升级微信后重试';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    const loginCode = await getLoginCode();
    const response = await miniappLogin({
      loginCode,
      phoneCode: detail.code,
      roleHint: selectedRole.value,
    });
    finishLogin(response);
  } catch (error) {
    reportError(error, '微信登录失败');
  } finally {
    loading.value = false;
  }
}

async function submitMock() {
  const phone = mockPhone.value.trim();
  if (!phone) {
    errorMessage.value = '请输入预置手机号';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await miniappLogin({
      loginCode: 'mock-code',
      roleHint: selectedRole.value,
      mockPhone: phone,
    });
    finishLogin(response);
  } catch (error) {
    reportError(error, 'mock 登录失败');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <view class="login-page page-fade-enter">
    <view class="grain" />

    <view class="brand-panel">
      <view class="brand-mark">
        <text class="brand-letter">ABC</text>
        <view class="mark-line" />
      </view>
      <view class="brand-copy">
        <text class="eyebrow">COMMUNITY ENGLISH</text>
        <text class="title">小区英语组班</text>
        <text class="subtitle">老师、学生、家长使用同一个微信入口</text>
      </view>
    </view>

    <view class="login-panel">
      <view class="panel-head">
        <text class="panel-title">选择登录身份</text>
        <text class="panel-note">用于匹配校区预置账号</text>
      </view>

      <view class="role-list">
        <view
          v-for="role in roleOptions"
          :key="role.value"
          class="role-card"
          :class="{ active: selectedRole === role.value }"
          :style="selectedRole === role.value ? { borderColor: role.accent } : {}"
          @tap="selectRole(role.value, role.samplePhone)"
        >
          <view class="role-main">
            <text class="role-label">{{ role.label }}</text>
            <text class="role-copy">{{ role.copy }}</text>
          </view>
          <view class="role-check" :style="{ background: selectedRole === role.value ? role.accent : '#ede4d6' }" />
        </view>
      </view>

      <view class="wechat-card">
        <view>
          <text class="wechat-title">微信手机号授权登录</text>
          <text class="wechat-copy">系统将通过微信获取登录凭证和手机号授权码，不需要手动填写开发字段。</text>
        </view>
        <view class="wechat-symbol">微</view>
      </view>

      <button
        class="wechat-button"
        open-type="getPhoneNumber"
        :disabled="loading"
        @getphonenumber="submitWechat"
      >
        <view v-if="loading" class="button-loader" />
        <text>{{ loading ? '授权登录中' : '微信授权登录' }}</text>
      </button>

      <text v-if="errorMessage" class="form-error">{{ errorMessage }}</text>

      <view class="terms">
        <text class="terms-text">登录即表示同意</text>
        <text class="terms-link">《用户协议》</text>
        <text class="terms-text">和</text>
        <text class="terms-link">《隐私政策》</text>
      </view>
    </view>

    <view v-if="mockLoginVisible" class="debug-panel" :class="{ expanded: debugExpanded }">
      <view class="debug-head" @tap="toggleDebug">
        <view>
          <text class="debug-title">开发调试</text>
          <text class="debug-copy">本地 mock 登录，不提交开发标识</text>
        </view>
        <text class="debug-toggle">{{ debugExpanded ? '收起' : '展开' }}</text>
      </view>

      <view v-if="debugExpanded" class="debug-body">
        <view class="mock-summary">
          <text>当前身份</text>
          <text>{{ selectedRoleLabel }}</text>
        </view>
        <view class="mock-input">
          <text class="field-label">预置手机号</text>
          <input v-model="mockPhone" type="number" maxlength="20" placeholder="请输入预置手机号" />
        </view>
        <button class="mock-button" :disabled="loading" @tap="submitMock">
          {{ loading ? '登录中' : '使用 mock 登录' }}
        </button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  padding: 72rpx 32rpx 44rpx;
  overflow: hidden;
  background:
    radial-gradient(circle at 16% 8%, rgba(240, 184, 77, 0.2), transparent 34%),
    linear-gradient(180deg, #f8f2e7 0%, #efe5d5 100%);
  color: #17211d;
}

.grain {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.32;
  background-image:
    linear-gradient(90deg, rgba(23, 33, 29, 0.025) 1rpx, transparent 1rpx),
    linear-gradient(180deg, rgba(23, 33, 29, 0.02) 1rpx, transparent 1rpx);
  background-size: 42rpx 42rpx;
}

.brand-panel,
.login-panel,
.debug-panel {
  position: relative;
}

.brand-panel {
  display: flex;
  align-items: center;
  gap: 28rpx;
  padding: 20rpx 8rpx 42rpx;
}

.brand-mark {
  width: 126rpx;
  height: 126rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border-radius: 34rpx;
  background: #17211d;
  box-shadow: 0 18rpx 42rpx rgba(44, 54, 42, 0.16);
}

.brand-letter {
  color: #f3c568;
  font-size: 34rpx;
  font-weight: 900;
  letter-spacing: 1rpx;
}

.mark-line {
  width: 54rpx;
  height: 5rpx;
  border-radius: 999rpx;
  background: #fffcf5;
}

.brand-copy {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  display: block;
  color: #22624c;
  font-size: 21rpx;
  font-weight: 900;
  letter-spacing: 3rpx;
}

.title {
  display: block;
  margin-top: 12rpx;
  color: #17211d;
  font-size: 50rpx;
  font-weight: 900;
  line-height: 1.08;
}

.subtitle {
  display: block;
  margin-top: 12rpx;
  color: #6f756f;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.45;
}

.login-panel {
  padding: 30rpx;
  border: 1rpx solid rgba(110, 94, 69, 0.14);
  border-radius: 30rpx;
  background: rgba(255, 252, 245, 0.92);
  box-shadow: 0 18rpx 46rpx rgba(78, 60, 36, 0.08);
}

.panel-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 18rpx;
}

.panel-title {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.panel-note {
  color: #8b8d86;
  font-size: 22rpx;
  font-weight: 700;
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.role-card {
  min-height: 106rpx;
  padding: 22rpx 22rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  border: 2rpx solid transparent;
  border-radius: 24rpx;
  background: #f7f0e5;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.role-card:active {
  transform: scale(0.985);
}

.role-card.active {
  background: #fffcf5;
}

.role-main {
  flex: 1;
  min-width: 0;
}

.role-label {
  display: block;
  color: #17211d;
  font-size: 29rpx;
  font-weight: 900;
}

.role-copy {
  display: block;
  margin-top: 8rpx;
  color: #747970;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.35;
}

.role-check {
  width: 26rpx;
  height: 26rpx;
  flex-shrink: 0;
  border-radius: 999rpx;
  box-shadow: inset 0 0 0 5rpx #fffcf5;
}

.wechat-card {
  margin-top: 26rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  border-radius: 26rpx;
  background: #18241f;
}

.wechat-title,
.wechat-copy {
  display: block;
}

.wechat-title {
  color: #fffcf5;
  font-size: 28rpx;
  font-weight: 900;
}

.wechat-copy {
  margin-top: 9rpx;
  color: rgba(255, 252, 245, 0.68);
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.5;
}

.wechat-symbol {
  width: 74rpx;
  height: 74rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 32rpx;
  font-weight: 900;
}

.wechat-button {
  height: 94rpx;
  margin-top: 24rpx;
  gap: 12rpx;
  border-radius: 22rpx;
  background: #22624c;
  color: #fff;
  font-size: 30rpx;
  font-weight: 900;
  box-shadow: 0 16rpx 34rpx rgba(34, 98, 76, 0.2);
  transition: transform 0.2s ease, opacity 0.2s ease, box-shadow 0.2s ease;
}

.wechat-button:active {
  transform: translateY(2rpx) scale(0.99);
  box-shadow: 0 10rpx 24rpx rgba(34, 98, 76, 0.17);
}

.wechat-button[disabled] {
  opacity: 0.62;
  box-shadow: none;
}

.button-loader {
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.34);
  border-top-color: #fff;
  border-radius: 999rpx;
  animation: spin 0.8s linear infinite;
}

.form-error {
  display: block;
  margin-top: 18rpx;
  padding: 16rpx 18rpx;
  border-radius: 18rpx;
  background: #fff0e9;
  color: #b84f35;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 1.4;
}

.terms {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4rpx;
  margin-top: 24rpx;
}

.terms-text {
  color: #9a9c94;
  font-size: 22rpx;
  font-weight: 700;
}

.terms-link {
  color: #22624c;
  font-size: 22rpx;
  font-weight: 900;
}

.debug-panel {
  margin-top: 22rpx;
  border: 1rpx solid rgba(110, 94, 69, 0.14);
  border-radius: 24rpx;
  background: rgba(255, 252, 245, 0.58);
  overflow: hidden;
}

.debug-head {
  min-height: 92rpx;
  padding: 18rpx 22rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.debug-title,
.debug-copy,
.debug-toggle {
  display: block;
}

.debug-title {
  color: #17211d;
  font-size: 25rpx;
  font-weight: 900;
}

.debug-copy {
  margin-top: 6rpx;
  color: #777d74;
  font-size: 21rpx;
  font-weight: 700;
}

.debug-toggle {
  flex-shrink: 0;
  color: #22624c;
  font-size: 23rpx;
  font-weight: 900;
}

.debug-body {
  padding: 0 22rpx 22rpx;
}

.mock-summary {
  height: 66rpx;
  padding: 0 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 18rpx;
  background: #f2eadc;
  color: #5f655f;
  font-size: 23rpx;
  font-weight: 800;
}

.mock-input {
  margin-top: 16rpx;
  padding: 20rpx;
  border-radius: 20rpx;
  background: #fffcf5;
}

.field-label {
  display: block;
  margin-bottom: 14rpx;
  color: #17211d;
  font-size: 24rpx;
  font-weight: 900;
}

.mock-input input {
  height: 70rpx;
  padding: 0 18rpx;
  border-radius: 16rpx;
  background: #f7f0e5;
  color: #17211d;
  font-size: 27rpx;
}

.mock-button {
  height: 82rpx;
  margin-top: 16rpx;
  border-radius: 18rpx;
  background: #17211d;
  color: #fffcf5;
  font-size: 27rpx;
  font-weight: 900;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.mock-button:active {
  transform: translateY(2rpx) scale(0.99);
}

.mock-button[disabled] {
  opacity: 0.58;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
