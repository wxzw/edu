<script setup lang="ts">
interface Props {
  loading?: boolean;
  disabled?: boolean;
  text?: string;
  type?: 'primary' | 'secondary' | 'danger';
}
withDefaults(defineProps<Props>(), {
  loading: false,
  disabled: false,
  text: '提交',
  type: 'primary',
});

const emit = defineEmits<{
  (e: 'tap'): void;
}>();
</script>

<template>
  <button
    class="submit-btn"
    :class="[type, { loading, disabled }]"
    :disabled="disabled || loading"
    @tap="emit('tap')"
  >
    <view v-if="loading" class="spinner" />
    <text>{{ loading ? '提交中...' : text }}</text>
  </button>
</template>

<style scoped>
.submit-btn {
  height: 96rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 30rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
  transition: all 0.2s ease;
  border: none;
}

.submit-btn.primary {
  background: linear-gradient(135deg, #22624c 0%, #1a4d3a 100%);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(34, 98, 76, 0.25);
}
.submit-btn.primary:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(34, 98, 76, 0.2);
}

.submit-btn.secondary {
  background: #fffcf5;
  color: #22624c;
  border: 2rpx solid #22624c;
}

.submit-btn.danger {
  background: linear-gradient(135deg, #e85d4c 0%, #c94a3d 100%);
  color: #fff;
}

.submit-btn.disabled {
  opacity: 0.5;
}

.spinner {
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
