<script setup lang="ts">
import { computed } from 'vue';
import type { CalendarScheduleItem } from '@/types/api';
import { compareScheduleTime, formatDateTitle, formatTime, todayKey } from '@/utils/calendar';

const props = withDefaults(defineProps<{
  date: string;
  schedules: CalendarScheduleItem[];
  loading?: boolean;
  role?: 'student' | 'teacher';
}>(), {
  loading: false,
  role: 'student',
});

const emit = defineEmits<{
  (event: 'itemTap', item: CalendarScheduleItem): void;
}>();

const safeSchedules = computed(() => Array.isArray(props.schedules) ? props.schedules : []);
const title = computed(() => formatDateTitle(props.date || todayKey()));
const sortedSchedules = computed(() => [...safeSchedules.value].sort((a, b) => compareScheduleTime(a.startTime, b.startTime)));

function statusLabel(status?: string) {
  const map: Record<string, string> = {
    SCHEDULED: '待上课',
    FINISHED: '已完成',
    CANCELLED: '已取消',
    IN_PROGRESS: '进行中',
  };
  return status ? map[status] || status : '待确认';
}

function metaLine(item: CalendarScheduleItem) {
  const main = props.role === 'teacher' ? item.className : item.teacherName;
  return [main, item.classroom].filter(Boolean).join(' · ');
}
</script>

<template>
  <view class="day-list">
    <view class="list-head">
      <text class="list-title">{{ title }}</text>
      <text class="list-count">{{ sortedSchedules.length }} 节课</text>
    </view>

    <view v-if="sortedSchedules.length" class="course-list">
      <view
        v-for="item in sortedSchedules"
        :key="item.id"
        class="course-card"
        @tap="emit('itemTap', item)"
      >
        <view class="time-rail">
          <text class="start-time">{{ formatTime(item.startTime) || '--:--' }}</text>
          <text class="end-time">{{ formatTime(item.endTime) }}</text>
        </view>
        <view class="course-info">
          <view class="course-head">
            <text class="course-title">{{ item.topic }}</text>
            <text class="status-pill">{{ statusLabel(item.status) }}</text>
          </view>
          <text class="course-line">{{ item.courseName || item.className }}</text>
          <text class="course-line">{{ metaLine(item) }}</text>
          <text v-if="role === 'teacher'" class="course-line">
            {{ item.studentCount || 0 }} 人 · 已考勤 {{ item.attendanceCount || 0 }}
          </text>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text>{{ loading ? '加载中...' : '当天暂无课程' }}</text>
    </view>
  </view>
</template>

<style scoped>
.day-list {
  margin-top: 22rpx;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.list-title {
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.list-count {
  color: #888;
  font-size: 22rpx;
  font-weight: 700;
}

.course-list {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.course-card {
  padding: 24rpx;
  display: flex;
  gap: 22rpx;
  border-radius: 24rpx;
  background: #fffcf5;
}

.course-card:active {
  opacity: 0.76;
}

.time-rail {
  width: 96rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.time-rail::after {
  content: '';
  width: 2rpx;
  flex: 1;
  margin-top: 10rpx;
  background: #e7dfd2;
}

.start-time {
  color: #1f5a44;
  font-size: 28rpx;
  font-weight: 900;
}

.end-time {
  margin-top: 4rpx;
  color: #aaa;
  font-size: 20rpx;
  font-weight: 700;
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
}

.course-title {
  flex: 1;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.25;
}

.status-pill {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #e8f0ea;
  color: #1f5a44;
  font-size: 20rpx;
  font-weight: 900;
  white-space: nowrap;
}

.course-line {
  display: block;
  margin-top: 10rpx;
  color: #8b8c87;
  font-size: 23rpx;
  line-height: 1.45;
}

.empty {
  margin-top: 18rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  background: #fffcf5;
  color: #aaa;
  font-size: 25rpx;
}
</style>
