<script setup lang="ts">
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { getTeacherSchedules } from '@/api/teacher';
import type { CalendarScheduleItem, TodaySchedule } from '@/types/api';
import { requireIdentity } from '@/utils/auth-flow';
import {
  endOfMonthKey,
  endOfWeekKey,
  formatMonthTitle,
  formatTimeRange,
  startOfMonthKey,
  startOfWeekKey,
  todayKey,
  weekNumberOfYear,
} from '@/utils/calendar';
import AppTabBar from '@/components/AppTabBar.vue';
import DayScheduleList from '@/components/DayScheduleList.vue';
import ScheduleCalendar from '@/components/ScheduleCalendar.vue';
import WeekScheduleStrip from '@/components/WeekScheduleStrip.vue';
import WeekTimeline from '@/components/WeekTimeline.vue';

type ViewMode = 'month' | 'week' | 'day';

const viewMode = ref<ViewMode>('month');
const schedules = ref<TodaySchedule[]>([]);
const loading = ref(false);
const selectedDate = ref(todayKey());
const currentMonth = ref(startOfMonthKey(selectedDate.value));

const modes: Array<{ label: string; value: ViewMode }> = [
  { label: '月', value: 'month' },
  { label: '周', value: 'week' },
  { label: '日', value: 'day' },
];

const calendarSchedules = computed<CalendarScheduleItem[]>(() => schedules.value.map((item) => ({ ...item })));
const selectedSchedules = computed<CalendarScheduleItem[]>(() => (
  calendarSchedules.value.filter((item) => item.lessonDate === selectedDate.value)
));
const weekSchedules = computed<CalendarScheduleItem[]>(() => {
  const start = startOfWeekKey(selectedDate.value);
  const end = endOfWeekKey(selectedDate.value);
  return calendarSchedules.value.filter((item) => item.lessonDate >= start && item.lessonDate <= end);
});
const pageTitle = computed(() => {
  if (viewMode.value === 'week') {
    return `${formatMonthTitle(selectedDate.value)} 第${weekNumberOfYear(selectedDate.value)}周`;
  }
  if (viewMode.value === 'day') {
    return selectedDate.value;
  }
  return formatMonthTitle(currentMonth.value);
});

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  loadSchedules();
});

onPullDownRefresh(() => {
  loadSchedules().finally(() => uni.stopPullDownRefresh());
});

async function loadSchedules() {
  loading.value = true;
  try {
    schedules.value = await getTeacherSchedules(startOfMonthKey(currentMonth.value), endOfMonthKey(currentMonth.value));
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '课表加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function changeMonth(month: string) {
  currentMonth.value = startOfMonthKey(month);
  if (selectedDate.value.slice(0, 7) !== currentMonth.value.slice(0, 7)) {
    selectedDate.value = currentMonth.value;
  }
  loadSchedules();
}

function selectDate(date: string) {
  selectedDate.value = date;
  if (date.slice(0, 7) !== currentMonth.value.slice(0, 7)) {
    changeMonth(startOfMonthKey(date));
  }
}

function openSchedule(item: CalendarScheduleItem) {
  uni.navigateTo({ url: `/pages/teacher/attendance/detail?scheduleId=${item.id}` });
}
</script>

<template>
  <view class="page">
    <view class="topbar">
      <view>
        <text class="caption">Teacher Calendar</text>
        <text class="title">{{ pageTitle }}</text>
      </view>
    </view>

    <view class="mode-tabs">
      <view
        v-for="mode in modes"
        :key="mode.value"
        class="mode-tab"
        :class="{ active: viewMode === mode.value }"
        @tap="viewMode = mode.value"
      >
        {{ mode.label }}
      </view>
    </view>

    <view v-if="viewMode === 'month'" class="section">
      <ScheduleCalendar
        :current-month="currentMonth"
        :selected-date="selectedDate"
        :schedules="calendarSchedules"
        @select="selectDate"
        @change-month="changeMonth"
      />
      <DayScheduleList
        :date="selectedDate"
        :schedules="selectedSchedules"
        :loading="loading"
        role="teacher"
        @item-tap="openSchedule"
      />
    </view>

    <view v-else-if="viewMode === 'week'" class="section">
      <WeekScheduleStrip
        :selected-date="selectedDate"
        :schedules="calendarSchedules"
        @select="selectDate"
      />
      <WeekTimeline
        :selected-date="selectedDate"
        :schedules="weekSchedules"
        @select-date="selectDate"
        @item-tap="openSchedule"
      />
    </view>

    <view v-else class="section">
      <WeekScheduleStrip
        :selected-date="selectedDate"
        :schedules="calendarSchedules"
        @select="selectDate"
      />
      <DayScheduleList
        :date="selectedDate"
        :schedules="selectedSchedules"
        :loading="loading"
        role="teacher"
        @item-tap="openSchedule"
      />
      <view v-if="selectedSchedules.length" class="day-note">
        <text>点击课程进入考勤详情。</text>
        <text class="note-time">
          {{ formatTimeRange(selectedSchedules[0].startTime, selectedSchedules[selectedSchedules.length - 1].endTime) }}
        </text>
      </view>
    </view>

    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 160rpx;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

.topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
}

.caption {
  display: block;
  color: #1f5a44;
  font-size: 21rpx;
  font-weight: 900;
  letter-spacing: 3rpx;
}

.title {
  display: block;
  margin-top: 8rpx;
  color: #17211d;
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1.18;
}

.mode-tabs {
  margin-top: 26rpx;
  padding: 6rpx;
  display: flex;
  gap: 6rpx;
  border-radius: 20rpx;
  background: #e8e2d8;
}

.mode-tab {
  flex: 1;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  color: #747970;
  font-size: 27rpx;
  font-weight: 900;
}

.mode-tab.active {
  background: #fffcf5;
  color: #17211d;
  box-shadow: 0 8rpx 18rpx rgba(54, 43, 30, 0.06);
}

.section {
  margin-top: 22rpx;
}

.day-note {
  margin-top: 18rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  border-radius: 24rpx;
  background: #fffcf5;
  color: #7d827c;
  font-size: 23rpx;
  line-height: 1.45;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.note-time {
  color: #1f5a44;
  font-weight: 900;
  white-space: nowrap;
}
</style>
