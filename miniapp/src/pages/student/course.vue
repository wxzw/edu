<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getLessonAccounts, getLessonRecords, getSchedules } from '@/api/student';
import type { LessonAccount, LessonRecord, ScheduleItem } from '@/types/api';
import { requireStudentAccess } from '@/utils/auth-flow';
import AppTabBar from '@/components/AppTabBar.vue';

const activeTab = ref<'schedule' | 'hours'>('schedule');
const schedules = ref<ScheduleItem[]>([]);
const accounts = ref<LessonAccount[]>([]);
const records = ref<LessonRecord[]>([]);
const loading = ref(false);

const totalRemaining = computed(() => accounts.value.reduce((sum, item) => sum + Number(item.remainingHours || 0), 0));

onShow(() => {
  if (requireStudentAccess()) {
    loadCourseData();
  }
});

async function loadCourseData() {
  loading.value = true;
  try {
    const [scheduleData, accountData, recordData] = await Promise.all([
      getSchedules(),
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

    <view v-if="activeTab === 'schedule'" class="list">
      <view v-for="item in schedules" :key="item.id" class="card">
        <view class="card-head">
          <text class="card-title">{{ item.topic }}</text>
          <text class="pill">{{ item.status }}</text>
        </view>
        <text class="line">{{ item.lessonDate }} {{ item.startTime?.slice(0, 5) }}-{{ item.endTime?.slice(0, 5) }}</text>
        <text class="line">{{ item.courseName }} · {{ item.teacherName }}</text>
        <text class="line">{{ item.classroom || item.className }}</text>
      </view>
      <view v-if="!schedules.length" class="empty">{{ loading ? '加载中...' : '暂无课程安排' }}</view>
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

.list {
  margin-top: 22rpx;
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

.pill {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #E8F0EA;
  color: #22624c;
  font-size: 22rpx;
  font-weight: 800;
}

.line {
  display: block;
  margin-top: 10rpx;
  color: #909090;
  font-size: 23rpx;
  line-height: 1.5;
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
