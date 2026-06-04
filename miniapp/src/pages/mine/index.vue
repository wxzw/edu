<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { selectIdentity } from '@/api/miniapp';
import { getStudentDashboard } from '@/api/student';
import { useAuthStore } from '@/stores/auth';
import type { ChildStudent, MiniappIdentity, StudentDashboard } from '@/types/api';
import { requireLogin } from '@/utils/auth-flow';
import { switchToHome } from '@/utils/routes';
import AppTabBar from '@/components/AppTabBar.vue';

const auth = useAuthStore();
const current = computed(() => auth.selectedIdentity);
const dashboard = ref<StudentDashboard>();
const children = computed(() => dashboard.value?.children || current.value?.children || []);
const activeChildIndex = computed(() => {
  const index = children.value.findIndex((child) => child.studentId === auth.currentStudentId);
  return index >= 0 ? index : 0;
});

onShow(() => {
  if (requireLogin()) {
    loadStudentSummary();
  }
});

function label(identity?: MiniappIdentity) {
  if (!identity) {
    return '';
  }
  return {
    TEACHER: '老师',
    STUDENT: '学生',
    GUARDIAN: '家长',
  }[identity.identityType];
}

async function switchIdentity(identity: MiniappIdentity) {
  try {
    const response = await selectIdentity(identity.identityType, identity.identityId);
    auth.applyMe(response);
    switchToHome(response.selectedIdentity.identityType);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '切换失败', icon: 'none' });
  }
}

async function loadStudentSummary() {
  if (current.value?.identityType !== 'STUDENT' && current.value?.identityType !== 'GUARDIAN') {
    dashboard.value = undefined;
    return;
  }
  try {
    dashboard.value = await getStudentDashboard();
    auth.setCurrentStudent(dashboard.value.currentStudentId);
  } catch {
    dashboard.value = undefined;
  }
}

function changeChild(event: { detail: { value: number } }) {
  const child = children.value[event.detail.value] as ChildStudent | undefined;
  if (!child) {
    return;
  }
  auth.setCurrentStudent(child.studentId);
  loadStudentSummary();
}

function goGroupList() {
  uni.navigateTo({ url: '/pages/student/group/detail?id=' + (dashboard.value?.activeGroupRequest?.id || '') });
}

function goRegistrations() {
  uni.navigateTo({ url: '/pages/student/registrations' });
}

function goNotifications() {
  uni.navigateTo({ url: '/pages/mine/notifications' });
}

function logout() {
  auth.clear();
  uni.reLaunch({ url: '/pages/login/index' });
}
</script>

<template>
  <view class="page">
    <view class="profile">
      <view class="avatar">{{ current?.displayName?.slice(0, 1) || '我' }}</view>
      <view class="profile-main">
        <text class="name">{{ current?.displayName }}</text>
        <text class="meta">{{ label(current) }} · 校区 {{ current?.campusId }}</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">身份</text>
      <button
        v-for="identity in auth.identities"
        :key="`${identity.identityType}-${identity.identityId}`"
        class="identity-row"
        :class="{ active: current?.identityType === identity.identityType && current?.identityId === identity.identityId }"
        @tap="switchIdentity(identity)"
      >
        <text class="identity-name">{{ identity.displayName }}</text>
        <text class="identity-type">{{ label(identity) }}</text>
      </button>
    </view>

    <view class="section">
      <text class="section-title">学习服务</text>
      <picker v-if="children.length > 1" :range="children" range-key="name" :value="activeChildIndex" @change="changeChild">
        <view class="info-row">
          <text class="info-label">当前学生</text>
          <text class="info-value">{{ children[activeChildIndex]?.name }}</text>
        </view>
      </picker>
      <view v-if="dashboard" class="info-row">
        <text class="info-label">剩余课时</text>
        <text class="info-value">{{ dashboard.lessonSummary.totalRemainingHours }}</text>
      </view>
      <view v-if="dashboard" class="info-row">
        <text class="info-label">所在班级</text>
        <text class="info-value">{{ dashboard.profile.classNames || '-' }}</text>
      </view>
      <button v-if="dashboard?.activeGroupRequest" class="service-row" @tap="goGroupList">
        我的拼班：{{ dashboard.activeGroupRequest.targetSystem }}
      </button>
      <button v-if="dashboard" class="service-row" @tap="goRegistrations">我的报名</button>
      <button v-if="dashboard" class="service-row" @tap="goNotifications">通知中心</button>
      <view class="info-row">
        <text class="info-label">联系客服</text>
        <text class="info-value">请联系所在校区</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">账号</text>
      <view class="info-row">
        <text class="info-label">用户名</text>
        <text class="info-value">{{ auth.userInfo?.username }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">手机号</text>
        <text class="info-value">{{ auth.userInfo?.phone || '-' }}</text>
      </view>
    </view>

    <button class="logout" @tap="logout">退出登录</button>
    <AppTabBar />
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36rpx 30rpx 160rpx;
  background: #f6f1e8;
}

.profile {
  padding: 32rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  border-radius: 24rpx;
  background: #17211d;
  color: #fff;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 22rpx;
  background: #f0b84d;
  color: #17211d;
  font-size: 36rpx;
  font-weight: 900;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.name {
  display: block;
  font-size: 34rpx;
  font-weight: 900;
}

.meta {
  display: block;
  margin-top: 10rpx;
  color: #d8e0d9;
  font-size: 24rpx;
}

.section {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 22rpx;
  background: #fffcf5;
}

.section-title {
  display: block;
  margin-bottom: 18rpx;
  color: #17211d;
  font-size: 30rpx;
  font-weight: 900;
}

.identity-row,
.info-row,
.service-row {
  min-height: 82rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 2rpx solid #eee4d4;
  text-align: left;
}

.service-row {
  width: 100%;
  padding: 0;
  background: transparent;
  color: #22624c;
  font-size: 26rpx;
  font-weight: 900;
}

.identity-row.active {
  color: #22624c;
}

.identity-name,
.info-label {
  color: #17211d;
  font-size: 27rpx;
  font-weight: 800;
}

.identity-type,
.info-value {
  color: #6f756f;
  font-size: 24rpx;
}

.logout {
  height: 90rpx;
  margin-top: 34rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background: #b64334;
  color: #fff;
  font-size: 29rpx;
  font-weight: 900;
}
</style>
