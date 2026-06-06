<script setup lang="ts">
import { computed } from 'vue';
import type { CalendarScheduleItem } from '@/types/api';
import {
  WEEKDAY_LABELS,
  addMonthsKey,
  buildMonthDays,
  compareScheduleTime,
  formatMonthTitle,
  pad2,
  startOfMonthKey,
  todayKey,
} from '@/utils/calendar';

const props = withDefaults(defineProps<{
  currentMonth: string;
  selectedDate: string;
  schedules: CalendarScheduleItem[];
}>(), {
  currentMonth: '',
  selectedDate: '',
  schedules: () => [],
});

const emit = defineEmits<{
  (event: 'select', date: string): void;
  (event: 'changeMonth', month: string): void;
}>();

const safeSelectedDate = computed(() => props.selectedDate || todayKey());
const safeCurrentMonth = computed(() => props.currentMonth || startOfMonthKey(safeSelectedDate.value));
const safeSchedules = computed(() => Array.isArray(props.schedules) ? props.schedules : []);
const monthTitle = computed(() => formatMonthTitle(safeCurrentMonth.value));

const scheduleMap = computed(() => {
  return safeSchedules.value.reduce<Record<string, CalendarScheduleItem[]>>((map, item) => {
    const key = item.lessonDate;
    if (!key) return map;
    if (!map[key]) map[key] = [];
    map[key].push(item);
    return map;
  }, {});
});

const days = computed(() => {
  return buildMonthDays(safeCurrentMonth.value, safeSelectedDate.value).map((day) => {
    const items = (scheduleMap.value[day.date] || []).sort((a, b) => compareScheduleTime(a.startTime, b.startTime));
    return {
      ...day,
      items,
      dotIndexes: Array.from({ length: Math.min(items.length, 2) }, (_, index) => index),
      extraCount: Math.max(items.length - 2, 0),
      firstTopic: items[0]?.topic || '',
    };
  });
});

function goMonth(offset: number) {
  emit('changeMonth', addMonthsKey(safeCurrentMonth.value, offset));
}

function goToday() {
  const today = new Date();
  const month = `${today.getFullYear()}-${pad2(today.getMonth() + 1)}-01`;
  emit('changeMonth', month);
  emit('select', `${month.slice(0, 8)}${pad2(today.getDate())}`);
}
</script>

<template>
  <view class="calendar">
    <view class="calendar-head">
      <view>
        <text class="eyebrow">Calendar</text>
        <text class="month-title">{{ monthTitle }}</text>
      </view>
      <view class="month-actions">
        <view class="nav-button" @tap="goMonth(-1)">‹</view>
        <view class="today-button" @tap="goToday">今</view>
        <view class="nav-button" @tap="goMonth(1)">›</view>
      </view>
    </view>

    <view class="week-labels">
      <text v-for="label in WEEKDAY_LABELS" :key="label" class="week-label">{{ label }}</text>
    </view>

    <view class="month-grid">
      <view
        v-for="day in days"
        :key="day.date"
        class="day-cell"
        :class="{
          muted: !day.inCurrentMonth,
          today: day.isToday,
          selected: day.isSelected,
          hasCourse: day.items.length
        }"
        @tap="emit('select', day.date)"
      >
        <text class="day-number">{{ day.day }}</text>
        <view v-if="day.items.length" class="course-markers">
          <text v-for="dot in day.dotIndexes" :key="dot" class="course-dot" />
          <text v-if="day.extraCount" class="course-extra">+{{ day.extraCount }}</text>
        </view>
        <text v-if="day.firstTopic" class="day-topic">{{ day.firstTopic }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.calendar {
  padding: 28rpx;
  border-radius: 26rpx;
  background: #fffcf5;
}

.calendar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.eyebrow {
  display: block;
  color: #1f5a44;
  font-size: 20rpx;
  font-weight: 900;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.month-title {
  display: block;
  margin-top: 6rpx;
  color: #17211d;
  font-size: 42rpx;
  font-weight: 900;
}

.month-actions {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.nav-button,
.today-button {
  height: 56rpx;
  min-width: 56rpx;
  padding: 0 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #f0ece3;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.today-button {
  color: #1f5a44;
  font-size: 24rpx;
}

.week-labels {
  margin-top: 28rpx;
  display: flex;
}

.week-label {
  width: 14.285%;
  text-align: center;
  color: #8f908b;
  font-size: 22rpx;
  font-weight: 800;
}

.month-grid {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
}

.day-cell {
  width: 14.285%;
  min-height: 104rpx;
  margin-bottom: 8rpx;
  padding: 10rpx 4rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 2rpx solid transparent;
  border-radius: 20rpx;
  box-sizing: border-box;
  background: #f8f3ea;
  overflow: hidden;
}

.day-cell.muted {
  opacity: 0.38;
}

.day-cell.today {
  border-color: rgba(31, 90, 68, 0.4);
}

.day-cell.selected {
  background: #1f5a44;
  color: #fff;
  box-shadow: 0 12rpx 28rpx rgba(31, 90, 68, 0.22);
}

.day-number {
  font-size: 26rpx;
  font-weight: 900;
}

.course-markers {
  height: 14rpx;
  margin-top: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
}

.course-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #f0b84d;
}

.selected .course-dot {
  background: #fff;
}

.course-extra {
  color: #1f5a44;
  font-size: 16rpx;
  font-weight: 900;
}

.selected .course-extra {
  color: #fff;
}

.day-topic {
  max-width: 86rpx;
  margin-top: 4rpx;
  color: #7b7d78;
  font-size: 16rpx;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected .day-topic {
  color: rgba(255, 255, 255, 0.9);
}
</style>
