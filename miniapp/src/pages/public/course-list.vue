<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { apiBaseUrl } from '@/api/http';
import { getPublicCampuses, getPublicCourses } from '@/api/course';
import type { PublicCampus, PublicCourseSummary } from '@/types/api';

const loading = ref(false);
const keyword = ref('');
const campuses = ref<PublicCampus[]>([]);
const selectedCampusId = ref<number>();
const courses = ref<PublicCourseSummary[]>([]);

onLoad(async () => {
  await loadCampuses();
});

onPullDownRefresh(async () => {
  await loadCourses();
  uni.stopPullDownRefresh();
});

async function loadCampuses() {
  loading.value = true;
  try {
    campuses.value = await getPublicCampuses();
    selectedCampusId.value = selectedCampusId.value || campuses.value[0]?.id;
    await loadCourses();
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

async function loadCourses() {
  if (!selectedCampusId.value) {
    courses.value = [];
    return;
  }
  loading.value = true;
  try {
    courses.value = await getPublicCourses({
      campusId: selectedCampusId.value,
      keyword: keyword.value.trim(),
    });
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function selectCampus(id: number) {
  selectedCampusId.value = id;
  loadCourses();
}

function openCourse(id: number) {
  uni.navigateTo({ url: `/pages/public/course-detail?id=${id}&campusId=${selectedCampusId.value}` });
}

function openRegistrations() {
  uni.navigateTo({ url: '/pages/student/registrations' });
}

function resolveMediaUrl(url?: string) {
  if (!url) return '';
  if (/^https?:\/\//.test(url)) return url;
  return `${apiBaseUrl()}${url.startsWith('/') ? url : `/${url}`}`;
}

function ageText(item: PublicCourseSummary) {
  if (item.targetAgeMin && item.targetAgeMax) return `${item.targetAgeMin}-${item.targetAgeMax}岁`;
  return item.gradeScope || '适龄可咨询';
}
</script>

<template>
  <view class="page">
    <view class="headline">
      <view>
        <text class="caption">COURSES</text>
        <text class="title">选一门想了解的课</text>
      </view>
      <button class="record-button" @tap="openRegistrations">我的报名</button>
    </view>

    <view class="search-row">
      <input v-model="keyword" confirm-type="search" placeholder="课程名称 / 体系" @confirm="loadCourses" />
      <button @tap="loadCourses">搜索</button>
    </view>

    <scroll-view v-if="campuses.length" scroll-x class="campus-scroll" show-scrollbar="false">
      <view class="campus-row">
        <view
          v-for="campus in campuses"
          :key="campus.id"
          class="campus-chip"
          :class="{ active: selectedCampusId === campus.id }"
          @tap="selectCampus(campus.id)"
        >
          <text>{{ campus.shortName || campus.name }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="courses.length" class="course-list">
      <view v-for="item in courses" :key="item.id" class="course-card" @tap="openCourse(item.id)">
        <image v-if="item.coverUrl" class="cover" :src="resolveMediaUrl(item.coverUrl)" mode="aspectFill" />
        <view v-else class="cover cover-fallback">
          <text>{{ item.courseSystem }}</text>
        </view>
        <view class="card-body">
          <view class="card-head">
            <text class="course-title">{{ item.name }}</text>
            <text class="price">￥{{ item.packagePrice }}</text>
          </view>
          <text class="summary">{{ item.publicSummary || item.description || '课程详情可查看班级和老师安排' }}</text>
          <view class="meta-row">
            <text>{{ ageText(item) }}</text>
            <text>{{ item.totalHours }}课时</text>
            <text>{{ item.openClassCount || 0 }}个可选班</text>
          </view>
          <text v-if="item.teacherNames" class="teacher-line">{{ item.teacherNames }}</text>
        </view>
      </view>
    </view>

    <view v-else class="empty">{{ loading ? '加载中...' : '当前校区暂无公开课程' }}</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx 32rpx 80rpx;
  background: #f4f5f1;
  color: #202823;
}

.headline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
}

.caption {
  display: block;
  color: #237967;
  font-size: 22rpx;
  font-weight: 900;
  letter-spacing: 2rpx;
}

.title {
  display: block;
  margin-top: 8rpx;
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1.14;
}

.record-button {
  width: 168rpx;
  height: 68rpx;
  flex-shrink: 0;
  border-radius: 16rpx;
  background: #202823;
  color: #fff;
  font-size: 23rpx;
  font-weight: 900;
}

.search-row {
  margin-top: 28rpx;
  display: flex;
  gap: 14rpx;
}

.search-row input {
  flex: 1;
  height: 78rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: #ffffff;
  color: #202823;
  font-size: 25rpx;
}

.search-row button {
  width: 118rpx;
  height: 78rpx;
  border-radius: 18rpx;
  background: #237967;
  color: #fff;
  font-size: 25rpx;
  font-weight: 900;
}

.campus-scroll {
  margin-top: 22rpx;
  white-space: nowrap;
}

.campus-row {
  display: flex;
  gap: 14rpx;
}

.campus-chip {
  display: inline-flex;
  align-items: center;
  height: 62rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #ffffff;
  color: #6c746e;
  font-size: 24rpx;
  font-weight: 900;
}

.campus-chip.active {
  background: #f2eadc;
  color: #202823;
}

.course-list {
  margin-top: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.course-card {
  overflow: hidden;
  border: 1rpx solid rgba(222, 223, 216, 0.78);
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(32, 40, 35, 0.045);
}

.course-card:active {
  transform: translateY(2rpx) scale(0.992);
}

.cover {
  width: 100%;
  height: 220rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-fallback {
  background:
    linear-gradient(135deg, rgba(35, 121, 103, 0.92), rgba(60, 102, 142, 0.88)),
    #237967;
  color: #fff;
  font-size: 44rpx;
  font-weight: 900;
}

.card-body {
  padding: 24rpx;
}

.card-head,
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.course-title {
  flex: 1;
  min-width: 0;
  color: #202823;
  font-size: 31rpx;
  font-weight: 900;
}

.price {
  color: #b77624;
  font-size: 28rpx;
  font-weight: 900;
}

.summary,
.teacher-line,
.meta-row,
.empty {
  margin-top: 14rpx;
  color: #6c746e;
  font-size: 24rpx;
  line-height: 1.5;
}

.meta-row {
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 14rpx 22rpx;
  font-weight: 800;
}

.teacher-line {
  display: block;
  color: #3c668e;
  font-weight: 900;
}

.empty {
  margin-top: 90rpx;
  text-align: center;
}
</style>
