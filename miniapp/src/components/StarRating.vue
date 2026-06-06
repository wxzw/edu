<script setup lang="ts">
const props = withDefaults(defineProps<{
  modelValue?: number;
  readonly?: boolean;
  size?: 'normal' | 'large';
  label?: string;
}>(), {
  modelValue: 0,
  readonly: false,
  size: 'normal',
  label: '',
});

const emit = defineEmits<{
  (event: 'update:modelValue', value: number): void;
  (event: 'change', value: number): void;
}>();

const stars = [1, 2, 3, 4, 5];

function select(value: number) {
  if (props.readonly) return;
  emit('update:modelValue', value);
  emit('change', value);
}
</script>

<template>
  <view class="rating" :class="[size, { readonly }]">
    <view class="stars">
      <text
        v-for="star in stars"
        :key="star"
        class="star"
        :class="{ active: star <= (modelValue || 0) }"
        @tap="select(star)"
      >
        ★
      </text>
    </view>
    <text v-if="label || modelValue" class="rating-label">
      {{ label || `${modelValue || 0} 星` }}
    </text>
  </view>
</template>

<style scoped>
.rating {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.stars {
  display: flex;
  align-items: center;
  gap: 7rpx;
}

.star {
  width: 42rpx;
  height: 42rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ddd4c7;
  font-size: 38rpx;
  line-height: 1;
}

.star.active {
  color: #f0b84d;
  text-shadow: 0 4rpx 12rpx rgba(240, 184, 77, 0.22);
}

.large .star {
  width: 58rpx;
  height: 58rpx;
  font-size: 54rpx;
}

.rating-label {
  color: #17211d;
  font-size: 23rpx;
  font-weight: 900;
  white-space: nowrap;
}

.readonly .star {
  pointer-events: none;
}
</style>
