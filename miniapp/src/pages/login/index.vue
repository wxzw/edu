<script setup lang="ts">
import { ref } from 'vue';
import { miniappLogin } from '@/api/miniapp';
import { useAuthStore } from '@/stores/auth';
import type { IdentityType } from '@/types/api';
import { switchToHome } from '@/utils/routes';

const auth = useAuthStore();
const loading = ref(false);
const selectedRole = ref<IdentityType>('GUARDIAN');
const mockPhone = ref('13900000101');
const mockOpenId = ref('');

const roleOptions: Array<{ label: string; value: IdentityType; samplePhone: string; emoji: string; activeColor: string }> = [
  { label: '老师', value: 'TEACHER', samplePhone: '13900000011', emoji: 'T', activeColor: '#2D6A4F' },
  { label: '学生', value: 'STUDENT', samplePhone: '13900000201', emoji: 'S', activeColor: '#5B8DEE' },
  { label: '家长', value: 'GUARDIAN', samplePhone: '13900000101', emoji: 'P', activeColor: '#E07B54' },
];

function selectRole(value: IdentityType, samplePhone: string) {
  selectedRole.value = value;
  mockPhone.value = samplePhone;
}

async function submit() {
  if (!mockPhone.value.trim()) {
    uni.showToast({ title: '请输入手机号', icon: 'none' });
    return;
  }
  loading.value = true;
  try {
    const response = await miniappLogin({
      loginCode: 'mock-code',
      roleHint: selectedRole.value,
      mockPhone: mockPhone.value.trim(),
      mockOpenId: mockOpenId.value.trim() || undefined,
    });
    auth.applyLogin(response);
    if (response.availableIdentities.length > 1) {
      uni.reLaunch({ url: '/pages/identity/index' });
      return;
    }
    switchToHome(response.selectedIdentity.identityType);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '登录失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <view class="login-page">
    <!-- Header Area -->
    <view class="header">
      <view class="logo-wrap">
        <view class="logo">ABC</view>
      </view>
      <text class="brand">小区英语</text>
      <text class="slogan">Community English</text>
    </view>

    <!-- Login Form -->
    <view class="form-card">
      <!-- Role Selector -->
      <view class="form-section">
        <text class="label">选择身份</text>
        <view class="role-tabs">
          <view
            v-for="role in roleOptions"
            :key="role.value"
            class="role-tab"
            :class="{ active: selectedRole === role.value }"
            :style="selectedRole === role.value ? { borderColor: role.activeColor, color: role.activeColor } : {}"
            @click="selectRole(role.value, role.samplePhone)"
          >
            <text class="role-emoji">{{ role.emoji }}</text>
            <text class="role-name">{{ role.label }}</text>
          </view>
        </view>
      </view>

      <!-- Phone Input -->
      <view class="form-section">
        <text class="label">手机号</text>
        <view class="input-wrap">
          <text class="input-icon">📱</text>
          <input
            v-model="mockPhone"
            class="input-field"
            type="number"
            maxlength="20"
            placeholder="请输入手机号"
          />
        </view>
        <text class="hint">后台预建手机号</text>
      </view>

      <!-- OpenID Input -->
      <view class="form-section">
        <text class="label">OpenID</text>
        <view class="input-wrap">
          <text class="input-icon">🔑</text>
          <input
            v-model="mockOpenId"
            class="input-field"
            placeholder="可选"
          />
        </view>
        <text class="hint">为空则按手机号自动生成</text>
      </view>

      <!-- Login Button -->
      <view 
        class="login-btn"
        :class="{ loading: loading }"
        @click="submit"
      >
        <text class="login-btn-text">微信登录</text>
      </view>

      <!-- Agreement -->
      <view class="agreement">
        <text class="agreement-text">登录即表示同意</text>
        <text class="agreement-link">《用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #F5F0E6 0%, #EBE4D6 100%);
  padding: 80rpx 40rpx 40rpx;
  box-sizing: border-box;
}

/* Header */
.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.logo-wrap {
  width: 140rpx;
  height: 140rpx;
  background: linear-gradient(135deg, #F4A261 0%, #E9C46A 100%);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(244, 162, 97, 0.3);
}

.logo {
  font-size: 42rpx;
  font-weight: 900;
  color: #1B1B1B;
}

.brand {
  font-size: 52rpx;
  font-weight: 900;
  color: #1B1B1B;
  letter-spacing: 4rpx;
  margin-bottom: 10rpx;
}

.slogan {
  font-size: 24rpx;
  color: #2D6A4F;
  font-weight: 600;
  letter-spacing: 2rpx;
}

/* Form Card */
.form-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.form-section {
  margin-bottom: 32rpx;
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #1B1B1B;
  margin-bottom: 16rpx;
}

/* Role Tabs - 使用简单可靠的布局 */
.role-tabs {
  display: flex;
  background: #F5F5F5;
  border-radius: 16rpx;
  padding: 6rpx;
  gap: 6rpx;
}

.role-tab {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border-radius: 12rpx;
  border: 4rpx solid transparent;
  background: transparent;
  transition: all 0.2s ease;
}

.role-emoji {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E5E5E5;
  border-radius: 10rpx;
  font-size: 22rpx;
  font-weight: 800;
  color: #666;
  transition: all 0.2s ease;
}

.role-tab.active .role-emoji {
  background: currentColor;
  color: #FFF;
}

.role-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #666;
  transition: color 0.2s ease;
}

.role-tab.active .role-name {
  color: inherit;
}

/* Input */
.input-wrap {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 24rpx;
  background: #F8F8F8;
  border-radius: 16rpx;
  border: 2rpx solid #E8E8E8;
}

.input-icon {
  font-size: 28rpx;
  margin-right: 16rpx;
}

.input-field {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1B1B1B;
}

.hint {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-top: 12rpx;
  padding-left: 8rpx;
}

/* Login Button */
.login-btn {
  height: 96rpx;
  background: linear-gradient(135deg, #2D6A4F 0%, #40916C 100%);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(45, 106, 79, 0.3);
}

.login-btn:active {
  opacity: 0.9;
  transform: scale(0.98);
}

.login-btn.loading {
  background: #CCC;
  box-shadow: none;
}

.login-btn-text {
  font-size: 32rpx;
  font-weight: 800;
  color: #FFFFFF;
  letter-spacing: 2rpx;
}

/* Agreement */
.agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4rpx;
  margin-top: 32rpx;
}

.agreement-text {
  font-size: 22rpx;
  color: #999;
}

.agreement-link {
  font-size: 22rpx;
  color: #2D6A4F;
  font-weight: 600;
}
</style>
