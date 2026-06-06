<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getLessonAccounts, getLessonRecords, getSchedules } from '@/api/student';
import type { CalendarScheduleItem, LessonAccount, LessonRecord, ScheduleItem } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';
import {
  endOfMonthKey,
  formatFullDateTitle,
  formatTimeRange,
  startOfMonthKey,
  todayKey,
} from '@/utils/calendar';
import AppTabBar from '@/components/AppTabBar.vue';
import ScheduleCalendar from '@/components/ScheduleCalendar.vue';
import WeekScheduleStrip from '@/components/WeekScheduleStrip.vue';
import DayScheduleList from '@/components/DayScheduleList.vue';

const activeTab = ref<'schedule' | 'hours'>('schedule');
const schedules = ref<ScheduleItem[]>([]);
const accounts = ref<LessonAccount[]>([]);
const records = ref<LessonRecord[]>([]);
const loading = ref(false);
const scheduleLoading = ref(false);
const selectedDate = ref(todayKey());
const currentMonth = ref(startOfMonthKey(selectedDate.value));
const focusedSchedule = ref<ScheduleItem | null>(null);

const totalRemaining = computed(() => accounts.value.reduce((sum, item) => sum + Number(item.remainingHours || 0), 0));
const calendarSchedules = computed<CalendarScheduleItem[]>(() => schedules.value.map((item) => ({ ...item })));
const selectedSchedules = computed<CalendarScheduleItem[]>(() => calendarSchedules.value.filter((item) => item.lessonDate === selectedDate.value));

onShow(() => {
  if (requireStudentAccess()) {
    loadCourseData();
  }
});

async function loadCourseData() {
  loading.value = true;
  try {
    const [scheduleData, accountData, recordData] = await Promise.all([
      getSchedules(startOfMonthKey(currentMonth.value), endOfMonthKey(currentMonth.value)),
      getLessonAccounts(),
      getLessonRecords(),
    ]);
    schedules.value = scheduleData;
    accounts.value = accountData;
    records.value = recordData;
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

async function loadSchedulesForMonth(month: string) {
  scheduleLoading.value = true;
  try {
    schedules.value = await getSchedules(startOfMonthKey(month), endOfMonthKey(month));
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '课程加载失败', icon: 'none' });
  } finally {
    scheduleLoading.value = false;
  }
}

function changeMonth(month: string) {
  currentMonth.value = startOfMonthKey(month);
  if (selectedDate.value.slice(0, 7) !== currentMonth.value.slice(0, 7)) {
    selectedDate.value = currentMonth.value;
    focusedSchedule.value = null;
  }
  loadSchedulesForMonth(currentMonth.value);
}

function selectDate(date: string) {
  selectedDate.value = date;
  focusedSchedule.value = null;
  if (date.slice(0, 7) !== currentMonth.value.slice(0, 7)) {
    changeMonth(startOfMonthKey(date));
  }
}

function openSchedule(item: CalendarScheduleItem) {
  focusedSchedule.value = schedules.value.find((schedule) => schedule.id === item.id) || null;
}

function attendanceLabel(status?: string) {
  return {
    PRESENT: '正常',
    LATE: '迟到',
    ABSENT: '缺勤',
    LEAVE: '请假',
  }[status || ''] || status || '-';
}

function changeTypeLabel(type?: string) {
  return {
    PURCHASE: '购买',
    CONSUME: '课消',
    ADJUST: '调整',
    REFUND: '退款',
  }[type || ''] || type || '-';
}

function scheduleStatusLabel(status?: string) {
  return {
    SCHEDULED: '待上课',
    FINISHED: '已完成',
    CANCELLED: '已取消',
    IN_PROGRESS: '进行中',
  }[status || ''] || status || '待确认';
}
</script>

<template>
  <view class="page">
    <view class="headline">
      <text class="caption">COURSE LEDGER</text>
      <text class="title">我的课程</text>
    </view>

    <view class="summary">
      <text>总剩余课时</text>
      <text class="hours">{{ totalRemaining }}</text>
    </view>

    <view class="tabs">
      <button class="tab" :class="{ active: activeTab === 'schedule' }" @tap="activeTab = 'schedule'">课程表</button>
      <button class="tab" :class="{ active: activeTab === 'hours' }" @tap="activeTab = 'hours'">课时记录</button>
    </view>

    <view v-if="activeTab === 'schedule'" class="schedule-board">
      <ScheduleCalendar
        :current-month="currentMonth"
        :selected-date="selectedDate"
        :schedules="calendarSchedules"
        @select="selectDate"
        @change-month="changeMonth"
      />

      <view class="week-wrap">
        <WeekScheduleStrip
          :selected-date="selectedDate"
          :schedules="calendarSchedules"
          @select="selectDate"
        />
      </view>

      <DayScheduleList
        :date="selectedDate"
        :schedules="selectedSchedules"
        :loading="loading || scheduleLoading"
        role="student"
        @item-tap="openSchedule"
      />

      <view v-if="focusedSchedule" class="detail-panel">
        <view class="detail-head">
          <text class="detail-title">{{ focusedSchedule.topic }}</text>
          <text class="detail-pill">{{ scheduleStatusLabel(focusedSchedule.status) }}</text>
        </view>
        <text class="detail-line">{{ formatFullDateTitle(focusedSchedule.lessonDate) }} {{ formatTimeRange(focusedSchedule.startTime, focusedSchedule.endTime) }}</text>
        <text class="detail-line">{{ focusedSchedule.courseName }} · {{ focusedSchedule.teacherName }}</text>
        <text class="detail-line">{{ focusedSchedule.classroom || focusedSchedule.className }}</text>
        <text v-if="focusedSchedule.content" class="detail-content">{{ focusedSchedule.content }}</text>
      </view>
    </view>

    <view v-else class="list">
      <view v-for="account in accounts" :key="account.id" class="account">
        <view>
          <text class="card-title">{{ account.courseName }}</text>
          <text class="line">{{ account.courseSystem || '课程' }}</text>
        </view>
        <text class="account-hours">{{ account.remainingHours }}</text>
      </view>

      <view v-for="record in records" :key="record.id" class="card">
        <view class="card-head">
          <text class="card-title">{{ record.lessonTopic || record.courseName }}</text>
          <text class="pill">{{ changeTypeLabel(record.changeType) }}</text>
        </view>
        <text class="line">{{ record.occurredAt?.slice(0, 16).replace('T', ' ') }} · {{ record.hoursDelta }}课时</text>
        <text class="line">考勤：{{ attendanceLabel(record.attendanceStatus) }} · 余额 {{ record.balanceAfter }}</text>
        <text v-if="record.remark" class="line">{{ record.remark }}</text>
      </view>
      <view v-if="!accounts.length && !records.length" class="empty">{{ loading ? '加载中...' : '暂无课时记录' }}</view>
    </view>
    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 160rpx;
  background: #f6f1e8;
  color: #17211d;
}

.caption {
  display: block;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
}

.title {
  display: block;
  margin-top: 8rpx;
  font-size: 44rpx;
  font-weight: 900;
}

.summary {
  margin-top: 24rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #1B3A2D 0%, #2D6A4F 100%);
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}

.hours {
  color: #f0b84d;
  font-size: 46rpx;
  font-weight: 900;
}

.tabs {
  margin-top: 24rpx;
  display: flex;
  background: #E8E8E8;
  border-radius: 14rpx;
  padding: 5rpx;
  gap: 5rpx;
}

.tab {
  flex: 1;
  height: 70rpx;
  border-radius: 11rpx;
  background: transparent;
  color: #888;
  font-size: 26rpx;
  font-weight: 700;
}

.tab.active {
  background: #FFFFFF;
  color: #17211d;
}

.schedule-board,
.list {
  margin-top: 22rpx;
}

.week-wrap {
  margin-top: 18rpx;
}

.detail-panel {
  margin-top: 18rpx;
  padding: 28rpx;
  border-radius: 22rpx;
  background: #fffcf5;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.detail-title {
  flex: 1;
  color: #17211d;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.25;
}

.detail-pill,
.pill {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #E8F0EA;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 800;
  white-space: nowrap;
}

.detail-line,
.detail-content,
.line {
  display: block;
  margin-top: 10rpx;
  color: #909090;
  font-size: 23rpx;
  line-height: 1.5;
}

.detail-content {
  color: #565a54;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.card,
.account {
  padding: 26rpx;
  border-radius: 18rpx;
  background: #fffcf5;
}

.account,
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.card-title {
  display: block;
  font-size: 29rpx;
  font-weight: 900;
}

.account-hours {
  color: #22624c;
  font-size: 44rpx;
  font-weight: 900;
}

.empty {
  margin-top: 60rpx;
  color: #AAA;
  font-size: 26rpx;
  text-align: center;
}
</style>
