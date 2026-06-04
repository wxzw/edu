<script setup lang="ts">
interface Props {
  title?: string;
  subtitle?: string;
  icon?: string;
}
defineProps<Props>();
</script>

<template>
  <view class="form-section">
    <view v-if="title || subtitle" class="section-header">
      <view class="title-row">
        <text v-if="icon" class="icon">{{ icon }}</text>
        <text v-if="title" class="title">{{ title }}</text>
      </view>
      <text v-if="subtitle" class="subtitle">{{ subtitle }}</text>
    </view>
    <view class="section-body">
      <slot />
    </view>
  </view>
</template>

<style scoped>
.form-section {
  margin-bottom: 24rpx;
}

.section-header {
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.icon {
  font-size: 32rpx;
}

.title {
  font-size: 30rpx;
  font-weight: 800;
  color: #17211d;
  letter-spacing: 0.5rpx;
}

.subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #8a8a8a;
  font-weight: 500;
  line-height: 1.5;
}

.section-body {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
</style>
