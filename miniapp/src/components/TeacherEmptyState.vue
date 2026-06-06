<script setup lang="ts">
withDefaults(defineProps<{
  title: string;
  description?: string;
  actionText?: string;
}>(), {
  description: '',
  actionText: '',
});

const emit = defineEmits<{
  (event: 'action'): void;
}>();
</script>

<template>
  <view class="empty-state">
    <view class="mark">
      <text class="mark-line short" />
      <text class="mark-line" />
      <text class="mark-dot" />
    </view>
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-desc">{{ description }}</text>
    <button v-if="actionText" class="empty-action" @tap="emit('action')">
      {{ actionText }}
    </button>
  </view>
</template>

<style scoped>
.empty-state {
  min-height: 250rpx;
  padding: 38rpx 24rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 26rpx;
  background:
    linear-gradient(135deg, rgba(31, 90, 68, 0.06), rgba(240, 184, 77, 0.08)),
    #fffcf5;
  color: #17211d;
}

.mark {
  width: 92rpx;
  height: 92rpx;
  margin-bottom: 18rpx;
  position: relative;
  border-radius: 30rpx;
  background: #f4efe6;
}

.mark-line,
.mark-dot {
  position: absolute;
  left: 22rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: #1f5a44;
}

.mark-line {
  top: 30rpx;
  width: 48rpx;
}

.mark-line.short {
  top: 48rpx;
  width: 34rpx;
  background: #f0b84d;
}

.mark-dot {
  left: 62rpx;
  top: 48rpx;
  width: 8rpx;
}

.empty-title {
  display: block;
  color: #17211d;
  font-size: 29rpx;
  font-weight: 900;
}

.empty-desc {
  display: block;
  max-width: 440rpx;
  margin-top: 10rpx;
  color: #858982;
  font-size: 23rpx;
  line-height: 1.5;
  text-align: center;
}

.empty-action {
  min-width: 180rpx;
  height: 68rpx;
  margin-top: 22rpx;
  padding: 0 28rpx;
  border-radius: 999rpx;
  background: #1f5a44;
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 68rpx;
}

.empty-action::after {
  border: 0;
}
</style>
