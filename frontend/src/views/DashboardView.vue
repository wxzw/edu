<script setup lang="ts">
import { BookOpenCheck, Building2, GraduationCap, Layers3, ShieldCheck, UsersRound } from 'lucide-vue-next';
import type { Component } from 'vue';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { campusApi, classApi, courseApi, roleApi, teacherApi, userApi } from '@/api/admin';
import { useAuthStore } from '@/stores/auth';

interface MetricItem {
  label: string;
  value: number | string;
  accent: string;
  icon: Component;
}

const authStore = useAuthStore();
const loading = ref(false);
const counts = ref({
  campuses: 0,
  users: 0,
  roles: 0,
  teachers: 0,
  courses: 0,
  classes: 0,
});

const metrics = computed<MetricItem[]>(() => [
  { label: '校区', value: counts.value.campuses, accent: 'ink', icon: Building2 },
  { label: '账号', value: counts.value.users, accent: 'teal', icon: UsersRound },
  { label: '角色', value: counts.value.roles, accent: 'amber', icon: ShieldCheck },
  { label: '老师', value: counts.value.teachers, accent: 'green', icon: GraduationCap },
  { label: '课程', value: counts.value.courses, accent: 'blue', icon: BookOpenCheck },
  { label: '班级', value: counts.value.classes, accent: 'red', icon: Layers3 },
]);

const selectedCampusName = computed(() =>
  authStore.campusList.find((item) => item.id === authStore.selectedCampusId)?.name || '当前校区',
);

const loadMetrics = async () => {
  loading.value = true;
  try {
    const [campuses, users, roles, teachers, courses, classes] = await Promise.all([
      authStore.hasPermission('system:campus') ? campusApi.page({ pageNo: 1, pageSize: 1 }) : Promise.resolve({ total: 0 }),
      authStore.hasPermission('system:user') ? userApi.page({ pageNo: 1, pageSize: 1 }) : Promise.resolve({ total: 0 }),
      authStore.hasPermission('system:role') ? roleApi.page({ pageNo: 1, pageSize: 1 }) : Promise.resolve({ total: 0 }),
      authStore.hasPermission('edu:teacher') ? teacherApi.page({ pageNo: 1, pageSize: 1 }) : Promise.resolve({ total: 0 }),
      authStore.hasPermission('edu:course') ? courseApi.page({ pageNo: 1, pageSize: 1 }) : Promise.resolve({ total: 0 }),
      authStore.hasPermission('edu:class') ? classApi.page({ pageNo: 1, pageSize: 1 }) : Promise.resolve({ total: 0 }),
    ]);
    counts.value = {
      campuses: campuses.total,
      users: users.total,
      roles: roles.total,
      teachers: teachers.total,
      courses: courses.total,
      classes: classes.total,
    };
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadMetrics();
  window.addEventListener('campus-change', loadMetrics);
});

onUnmounted(() => {
  window.removeEventListener('campus-change', loadMetrics);
});
</script>

<template>
  <div class="page-stack">
    <section class="page-heading">
      <div>
        <p class="eyebrow">Operations Desk</p>
        <h2>工作台</h2>
      </div>
      <el-button :loading="loading" @click="loadMetrics">刷新</el-button>
    </section>

    <section class="metric-grid">
      <article v-for="item in metrics" :key="item.label" class="metric-card" :class="`accent-${item.accent}`">
        <div class="metric-icon">
          <component :is="item.icon" :size="22" />
        </div>
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </section>

    <section class="ops-board">
      <div class="board-panel wide">
        <div class="panel-title">今日关注</div>
        <div class="timeline-list">
          <div class="timeline-row">
            <span>09:00</span>
            <strong>自然拼读L1-A班</strong>
            <em>第2次课待考勤</em>
          </div>
          <div class="timeline-row">
            <span>15:00</span>
            <strong>KET拼班试听</strong>
            <em>试听安排待跟进</em>
          </div>
          <div class="timeline-row">
            <span>21:00</span>
            <strong>短元音a朗读打卡</strong>
            <em>截止前提醒</em>
          </div>
        </div>
      </div>
      <div class="board-panel">
        <div class="panel-title">{{ selectedCampusName }}</div>
        <p class="board-copy">
          本周重点：拼班试听、作业点评、课时核对、活动报名。
        </p>
      </div>
    </section>
  </div>
</template>
