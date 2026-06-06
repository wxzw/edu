<script setup lang="ts">
import {
  BookOpen,
  Building2,
  CalendarClock,
  ChevronLeft,
  ChevronRight,
  CreditCard,
  Files,
  GraduationCap,
  LayoutDashboard,
  LogOut,
  Menu,
  PartyPopper,
  Bell,
  ShieldCheck,
  User,
  UserCog,
  UsersRound,
} from 'lucide-vue-next';
import type { Component } from 'vue';
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

interface NavItem {
  path: string;
  label: string;
  icon: Component;
  permission?: string;
}

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const collapsed = ref(false);

const navItems: NavItem[] = [
  { path: '/dashboard', label: '工作台', icon: LayoutDashboard },
  { path: '/campuses', label: '校区', icon: Building2, permission: 'system:campus' },
  { path: '/users', label: '用户', icon: UserCog, permission: 'system:user' },
  { path: '/roles', label: '角色', icon: ShieldCheck, permission: 'system:role' },
  { path: '/teachers', label: '老师', icon: GraduationCap, permission: 'edu:teacher' },
  { path: '/courses', label: '课程', icon: BookOpen, permission: 'edu:course' },
  { path: '/classes', label: '班级', icon: UsersRound, permission: 'edu:class' },
  { path: '/students', label: '学生', icon: User, permission: 'edu:student' },
  { path: '/materials', label: '资料', icon: Files, permission: 'resource:material' },
  { path: '/activities', label: '活动', icon: PartyPopper, permission: 'operation:activity' },
  { path: '/finance', label: '财务', icon: CreditCard, permission: 'finance:order' },
  { path: '/notifications', label: '通知', icon: Bell, permission: 'system:notification' },
];

const visibleNavItems = computed(() =>
  navItems.filter((item) => !item.permission || authStore.hasPermission(item.permission)),
);

const activePath = computed(() => route.path);
const campusName = computed(() => {
  return authStore.campusList.find((item) => item.id === authStore.selectedCampusId)?.shortName || '选择校区';
});

const handleCampusChange = (campusId: number) => {
  authStore.switchCampus(campusId);
  window.dispatchEvent(new CustomEvent('campus-change'));
};

const handleLogout = async () => {
  await ElMessageBox.confirm('确认退出当前账号？', '退出登录', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await authStore.logout();
  await router.replace({ name: 'login' });
};
</script>

<template>
  <div class="admin-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="side-rail">
      <div class="brand-lockup">
        <div class="brand-mark">
          <CalendarClock :size="22" />
        </div>
        <div v-if="!collapsed" class="brand-text">
          <strong>邻里英语</strong>
          <span>Class Ops</span>
        </div>
      </div>

      <nav class="nav-list">
        <el-tooltip
          v-for="item in visibleNavItems"
          :key="item.path"
          :content="item.label"
          :disabled="!collapsed"
          placement="right"
          :show-after="400"
        >
          <button
            class="nav-item"
            :class="{ active: activePath === item.path }"
            @click="router.push(item.path)"
          >
            <component :is="item.icon" :size="19" />
            <span v-if="!collapsed">{{ item.label }}</span>
          </button>
        </el-tooltip>
      </nav>

      <button class="collapse-button" :title="collapsed ? '展开' : '收起'" @click="collapsed = !collapsed">
        <ChevronRight v-if="collapsed" :size="18" />
        <ChevronLeft v-else :size="18" />
      </button>
    </aside>

    <main class="main-stage">
      <header class="top-bar">
        <button class="icon-button mobile-menu" title="菜单">
          <Menu :size="18" />
        </button>
        <div class="campus-switcher">
          <span>{{ campusName }}</span>
          <el-select
            :model-value="authStore.selectedCampusId"
            size="large"
            class="campus-select"
            @change="handleCampusChange"
          >
            <el-option
              v-for="campus in authStore.campusList"
              :key="campus.id"
              :label="campus.name"
              :value="campus.id"
            />
          </el-select>
        </div>
        <div class="user-cluster">
          <div class="user-chip">
            <span class="avatar-dot">{{ authStore.userInfo?.realName?.slice(0, 1) || 'U' }}</span>
            <span>{{ authStore.userInfo?.realName }}</span>
          </div>
          <button class="icon-button" title="退出登录" @click="handleLogout">
            <LogOut :size="18" />
          </button>
        </div>
      </header>
      <section class="content-stage">
        <router-view />
      </section>
    </main>
  </div>
</template>
