<script setup lang="ts">
import { computed } from 'vue';
import type { CalendarScheduleItem } from '@/types/api';
import { WEEKDAY_LABELS, compareScheduleTime, formatTime, pad2, timeToHour, todayKey, weekDaysFor } from '@/utils/calendar';

const props = withDefaults(defineProps<{
  selectedDate: string;
  schedules: CalendarScheduleItem[];
  startHour?: number;
  endHour?: number;
}>(), {
  selectedDate: '',
  schedules: () => [],
  startHour: 8,
  endHour: 21,
});

const emit = defineEmits<{
  (event: 'itemTap', item: CalendarScheduleItem): void;
  (event: 'selectDate', date: string): void;
}>();

const safeSchedules = computed(() => Array.isArray(props.schedules) ? props.schedules : []);
const weekDays = computed(() => weekDaysFor(props.selectedDate || todayKey()));
const hours = computed(() => {
  const list: number[] = [];
  for (let hour = props.startHour; hour <= props.endHour; hour += 1) {
    list.push(hour);
  }
  return list;
});

function schedulesAt(date: string, hour: number) {
  return safeSchedules.value
    .filter((item) => item.lessonDate === date && timeToHour(item.startTime) === hour)
    .sort((a, b) => compareScheduleTime(a.startTime, b.startTime));
}
</script>

<template>
  <view class="timeline-card">
    <view class="timeline-head">
      <view class="time-spacer" />
      <view
        v-for="day in weekDays"
        :key="day.date"
        class="day-head"
        :class="{ selected: day.isSelected, today: day.isToday }"
        @tap="emit('selectDate', day.date)"
      >
        <text class="weekday">{{ WEEKDAY_LABELS[day.weekday] }}</text>
        <text class="day-number">{{ day.day }}</text>
      </view>
    </view>

    <scroll-view scroll-y class="timeline-scroll">
      <view v-for="hour in hours" :key="hour" class="hour-row">
        <view class="hour-label">{{ pad2(hour) }}:00</view>
        <view
          v-for="day in weekDays"
          :key="`${day.date}-${hour}`"
          class="hour-cell"
          :class="{ selected: day.isSelected }"
        >
          <view
            v-for="item in schedulesAt(day.date, hour)"
            :key="item.id"
            class="timeline-course"
            @tap="emit('itemTap', item)"
          >
            <text class="timeline-title">{{ item.topic }}</text>
            <text class="timeline-time">{{ formatTime(item.startTime) }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.timeline-card {
  margin-top: 22rpx;
  border-radius: 24rpx;
  background: #fffcf5;
  overflow: hidden;
}

.timeline-head {
  display: flex;
  border-bottom: 1rpx solid #eee5d8;
}

.time-spacer {
  width: 84rpx;
  min-height: 92rpx;
  flex-shrink: 0;
}

.day-head {
  flex: 1;
  min-width: 0;
  min-height: 92rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  color: #7c7f78;
}

.day-head.today {
  color: #22624c;
}

.day-head.selected {
  background: #22624c;
  color: #fff;
}

.weekday {
  font-size: 18rpx;
  font-weight: 800;
}

.day-number {
  font-size: 28rpx;
  font-weight: 900;
}

.timeline-scroll {
  max-height: 760rpx;
}

.hour-row {
  display: flex;
  min-height: 110rpx;
}

.hour-label {
  width: 84rpx;
  flex-shrink: 0;
  padding-top: 14rpx;
  color: #91938e;
  font-size: 20rpx;
  text-align: center;
  border-right: 1rpx solid #f0eadf;
}

.hour-cell {
  flex: 1;
  min-width: 0;
  padding: 8rpx 4rpx;
  border-right: 1rpx solid #f0eadf;
  border-bottom: 1rpx solid #f0eadf;
}

.hour-cell.selected {
  background: rgba(34, 98, 76, 0.04);
}

.timeline-course {
  min-height: 72rpx;
  padding: 8rpx;
  border-radius: 12rpx;
  background: #e8f0ea;
  color: #22624c;
  overflow: hidden;
}

.timeline-course + .timeline-course {
  margin-top: 6rpx;
}

.timeline-title {
  display: block;
  font-size: 18rpx;
  font-weight: 900;
  line-height: 1.25;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.timeline-time {
  display: block;
  margin-top: 4rpx;
  font-size: 16rpx;
  font-weight: 700;
  opacity: 0.8;
}
</style>
