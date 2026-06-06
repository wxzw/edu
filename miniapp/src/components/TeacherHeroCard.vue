<script setup lang="ts">
withDefaults(defineProps<{
  eyebrow: string;
  title?: string;
  subtitle?: string;
}>(), {
  title: '',
  subtitle: '',
});
</script>

<template>
  <view class="teacher-hero">
    <view class="hero-main">
      <view class="hero-copy">
        <text class="eyebrow">{{ eyebrow }}</text>
        <text class="title">{{ title || '--' }}</text>
        <text v-if="subtitle" class="subtitle">{{ subtitle }}</text>
      </view>
      <slot name="action" />
    </view>
    <slot />
  </view>
</template>

<style scoped>
.teacher-hero {
  padding: 34rpx;
  border-radius: 30rpx;
  background:
    linear-gradient(135deg, #193a2c 0%, #1f5a44 62%, #2b6c53 100%);
  color: #fff;
  box-shadow: 0 18rpx 42rpx rgba(31, 90, 68, 0.18);
}

.hero-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
}

.hero-copy {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  display: block;
  color: #f0b84d;
  font-size: 21rpx;
  font-weight: 900;
  letter-spacing: 3rpx;
}

.title {
  display: block;
  margin-top: 14rpx;
  color: #fff;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.16;
  word-break: break-all;
}

.subtitle {
  display: block;
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.78);
  font-size: 24rpx;
  font-weight: 800;
  line-height: 1.4;
}
</style>
