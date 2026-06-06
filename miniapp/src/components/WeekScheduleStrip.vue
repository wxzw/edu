<script setup lang="ts">
import { computed } from 'vue';
import type { CalendarScheduleItem } from '@/types/api';
import { WEEKDAY_LABELS, todayKey, weekDaysFor } from '@/utils/calendar';

const props = withDefaults(defineProps<{
  selectedDate: string;
  schedules: CalendarScheduleItem[];
}>(), {
  selectedDate: '',
  schedules: () => [],
});

const emit = defineEmits<{
  (event: 'select', date: string): void;
}>();

const countMap = computed(() => {
  const schedules = Array.isArray(props.schedules) ? props.schedules : [];
  return schedules.reduce<Record<string, number>>((map, item) => {
    map[item.lessonDate] = (map[item.lessonDate] || 0) + 1;
    return map;
  }, {});
});

const days = computed(() => weekDaysFor(props.selectedDate || todayKey()).map((day) => ({
  ...day,
  count: countMap.value[day.date] || 0,
})));
</script>

<template>
  <view class="week-strip">
    <view
      v-for="day in days"
      :key="day.date"
      class="week-day"
      :class="{ selected: day.isSelected, today: day.isToday }"
      @tap="emit('select', day.date)"
    >
      <text class="weekday">{{ WEEKDAY_LABELS[day.weekday] }}</text>
      <text class="date-number">{{ day.day }}</text>
      <text v-if="day.count" class="count-dot">{{ day.count }}</text>
    </view>
  </view>
</template>

<style scoped>
.week-strip {
  display: flex;
  gap: 8rpx;
}

.week-day {
  flex: 1;
  min-height: 104rpx;
  padding: 12rpx 4rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 22rpx;
  background: #fffcf5;
  border: 2rpx solid transparent;
  color: #73756f;
  position: relative;
}

.week-day.today {
  border-color: rgba(34, 98, 76, 0.35);
}

.week-day.selected {
  background: #22624c;
  color: #fff;
}

.weekday {
  font-size: 20rpx;
  font-weight: 800;
}

.date-number {
  margin-top: 6rpx;
  font-size: 30rpx;
  font-weight: 900;
}

.count-dot {
  margin-top: 8rpx;
  min-width: 28rpx;
  height: 24rpx;
  padding: 0 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 16rpx;
  font-weight: 900;
}
</style>
