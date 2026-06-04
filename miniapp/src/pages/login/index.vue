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

const roleOptions: Array<{ label: string; value: IdentityType; samplePhone: string; color: string }> = [
  { label: '老师', value: 'TEACHER', samplePhone: '13900000011', color: '#2D6A4F' },
  { label: '学生', value: 'STUDENT', samplePhone: '13900000201', color: '#5B8DEE' },
  { label: '家长', value: 'GUARDIAN', samplePhone: '13900000101', color: '#E07B54' },
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
  <view class="login-page page-fade-enter">
    <view class="brand">
      <view class="logo">ABC</view>
      <text class="title">小区英语</text>
      <text class="subtitle">老师·学生·家长共用一个入口</text>
    </view>

    <view class="card">
      <text class="label">选择身份</text>
      <view class="roles">
        <view
          v-for="role in roleOptions"
          :key="role.value"
          class="role"
          :class="{ active: selectedRole === role.value }"
          :style="selectedRole === role.value ? { borderColor: role.color, color: role.color } : {}"
          @click="selectRole(role.value, role.samplePhone)"
        >
          {{ role.label }}
        </view>
      </view>

      <text class="label">手机号</text>
      <view class="input">
        <input v-model="mockPhone" type="number" maxlength="20" placeholder="请输入手机号" />
      </view>
      <text class="hint">后台预建手机号</text>

      <text class="label">OpenID</text>
      <view class="input">
        <input v-model="mockOpenId" placeholder="可选，为空按手机号生成" />
      </view>

      <button class="submit" :disabled="loading" @tap="submit">
        {{ loading ? '登录中...' : '微信登录' }}
      </button>

      <view class="terms">
        <text class="terms-text">登录即表示同意</text>
        <text class="terms-link">《用户协议》</text>
        <text class="terms-text">和</text>
        <text class="terms-link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 100rpx 40rpx 40rpx;
  background: linear-gradient(180deg, #F5F0E6 0%, #EBE4D6 100%);
}

.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 56rpx;
}

.logo {
  width: 128rpx;
  height: 128rpx;
  background: linear-gradient(135deg, #F4A261 0%, #E9C46A 100%);
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 900;
  color: #1B1B1B;
  margin-bottom: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(244, 162, 97, 0.3);
}

.title {
  font-size: 52rpx;
  font-weight: 900;
  color: #1B1B1B;
  letter-spacing: 4rpx;
  margin-bottom: 10rpx;
}

.subtitle {
  font-size: 24rpx;
  color: #909090;
}

.card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 36rpx 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.label {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #1B1B1B;
  margin-bottom: 14rpx;
  margin-top: 24rpx;
}

.label:first-child {
  margin-top: 0;
}

.roles {
  display: flex;
  background: #F5F5F5;
  border-radius: 14rpx;
  padding: 5rpx;
  gap: 5rpx;
}

.role {
  flex: 1;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11rpx;
  border: 3rpx solid transparent;
  font-size: 26rpx;
  font-weight: 700;
  color: #999;
  transition: all 0.2s;
}

.role.active {
  background: #FFFFFF;
  font-size: 28rpx;
}

.input {
  height: 84rpx;
  padding: 0 20rpx;
  background: #F8F8F8;
  border-radius: 14rpx;
  border: 2rpx solid #E8E8E8;
  display: flex;
  align-items: center;
}

.input input {
  flex: 1;
  font-size: 28rpx;
  line-height: 1.2;
}

.hint {
  display: block;
  font-size: 22rpx;
  color: #BBB;
  margin-top: 8rpx;
  padding-left: 6rpx;
}

.submit {
  height: 92rpx;
  margin-top: 32rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #2D6A4F 0%, #40916C 100%);
  color: #FFF;
  font-size: 30rpx;
  font-weight: 800;
}

.submit[disabled] {
  background: #CCC;
}

.terms {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 3rpx;
  margin-top: 28rpx;
}

.terms-text {
  font-size: 22rpx;
  color: #BBB;
}

.terms-link {
  font-size: 22rpx;
  color: #2D6A4F;
  font-weight: 600;
}
</style>
