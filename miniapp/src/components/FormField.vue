<script setup lang="ts">
interface Props {
  label: string;
  required?: boolean;
  error?: string;
}
withDefaults(defineProps<Props>(), { required: false });
</script>

<template>
  <view class="form-field" :class="{ 'has-error': error }">
    <view class="label-row">
      <text class="label">{{ label }}</text>
      <text v-if="required" class="required">*</text>
    </view>
    <view class="input-wrap">
      <slot />
    </view>
    <text v-if="error" class="error-msg">{{ error }}</text>
  </view>
</template>

<style scoped>
.form-field {
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fffcf5;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
}
.form-field.has-error {
  border-color: #e85d4c;
}

.label-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 16rpx;
}

.label {
  font-size: 28rpx;
  font-weight: 700;
  color: #17211d;
  letter-spacing: 0.5rpx;
}

.required {
  color: #e85d4c;
  font-size: 24rpx;
  font-weight: 700;
}

.input-wrap {
  width: 100%;
}

.error-msg {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #e85d4c;
  font-weight: 500;
}
</style>
