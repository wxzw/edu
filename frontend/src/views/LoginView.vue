<script setup lang="ts">
import { LockKeyhole, LogIn, MapPin, UserRound } from 'lucide-vue-next';
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import classroomImage from '@/assets/classroom-board.png';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  username: 'admin',
  password: '123456',
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const handleLogin = async () => {
  await formRef.value?.validate();
  loading.value = true;
  try {
    await authStore.login(form);
    await router.replace((route.query.redirect as string) || '/dashboard');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <main class="login-page">
    <section class="login-visual">
      <img :src="classroomImage" alt="社区教室运营台" />
      <div class="visual-caption">
        <MapPin :size="18" />
        <span>阳光花园 · 滨河家园 · 周末班档案</span>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-brand">
        <div class="brand-mark large">
          <LogIn :size="25" />
        </div>
        <div>
          <h1>社区英语组班管理台</h1>
          <p>Neighborhood English Operations</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @keyup.enter="handleLogin">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" size="large" autocomplete="username">
            <template #prefix><UserRound :size="18" /></template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" show-password autocomplete="current-password">
            <template #prefix><LockKeyhole :size="18" /></template>
          </el-input>
        </el-form-item>
        <el-button class="login-button" type="primary" size="large" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>
    </section>
  </main>
</template>
