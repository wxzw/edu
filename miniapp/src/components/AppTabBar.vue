<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const identityType = computed(() => auth.selectedIdentity?.identityType);

interface TabItem {
  pagePath: string;
  text: string;
}

const studentTabs: TabItem[] = [
  { pagePath: '/pages/student/home', text: '首页' },
  { pagePath: '/pages/student/learning', text: '学习' },
  { pagePath: '/pages/student/course', text: '课程' },
  { pagePath: '/pages/mine/index', text: '我的' },
];

const teacherTabs: TabItem[] = [
  { pagePath: '/pages/teacher/home', text: '首页' },
  { pagePath: '/pages/teacher/schedule/index', text: '课表' },
  { pagePath: '/pages/teacher/class/list', text: '班级' },
  { pagePath: '/pages/mine/index', text: '我的' },
];

const tabs = computed(() => {
  if (identityType.value === 'TEACHER') return teacherTabs;
  return studentTabs;
});

function isActive(tab: TabItem): boolean {
  const pages = getCurrentPages();
  if (!pages.length) return false;
  const current = pages[pages.length - 1];
  const currentPath = '/' + current.route;
  return currentPath === tab.pagePath;
}

function switchTab(tab: TabItem) {
  if (isActive(tab)) return;
  const pages = getCurrentPages();
  if (!pages.length) return;

  // 如果目标tab已经在页面栈中，返回到它
  const targetIndex = pages.findIndex((p) => '/' + p.route === tab.pagePath);
  if (targetIndex >= 0) {
    uni.navigateBack({ delta: pages.length - 1 - targetIndex });
    return;
  }

  // 否则用 reLaunch 跳转
  uni.reLaunch({ url: tab.pagePath });
}

// 图标 SVG path 数据
const iconPaths: Record<string, { default: string; active: string }> = {
  '首页': {
    default: 'M12 3l9 8h-3v7h-4v-5h-4v5H6v-7H3l9-8z',
    active: 'M12 2L2 12h3v8h6v-6h2v6h6v-8h3L12 2z',
  },
  '学习': {
    default: 'M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z',
    active: 'M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3L1 9l11 6 9-4.91V17h2V9L12 3z',
  },
  '课程': {
    default: 'M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM9 10H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2z',
    active: 'M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM9 10H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2z',
  },
  '课表': {
    default: 'M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM9 10H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2z',
    active: 'M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM9 10H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2z',
  },
  '班级': {
    default: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
    active: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  },
  '作业': {
    default: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z',
    active: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z',
  },
  '我的': {
    default: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
    active: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
  },
};
</script>

<template>
  <view class="custom-tabbar">
    <view
      v-for="tab in tabs"
      :key="tab.pagePath"
      class="tab-item"
      :class="{ active: isActive(tab) }"
      @tap="switchTab(tab)"
    >
      <view class="tab-icon-wrap">
        <svg
          class="tab-icon"
          viewBox="0 0 24 24"
          fill="currentColor"
        >
          <path :d="isActive(tab) ? iconPaths[tab.text]?.active : iconPaths[tab.text]?.default" />
        </svg>
      </view>
      <text class="tab-text">{{ tab.text }}</text>
    </view>
  </view>
</template>

<style scoped>
.custom-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  height: 100rpx;
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #fffcf5;
  border-top: 1rpx solid #e8e0d0;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 2rpx;
}

.tab-icon-wrap {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-icon {
  width: 44rpx;
  height: 44rpx;
  color: #999;
  transition: color 0.2s;
}

.tab-item.active .tab-icon {
  color: #22624c;
}

.tab-text {
  font-size: 20rpx;
  color: #999;
  font-weight: 600;
  transition: color 0.2s;
}

.tab-item.active .tab-text {
  color: #22624c;
  font-weight: 800;
}
</style>
