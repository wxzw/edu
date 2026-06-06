<script setup lang="ts">
import { KeyRound, Plus } from 'lucide-vue-next';
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { campusApi, roleApi, userApi } from '@/api/admin';
import type { CampusRecord, RoleRecord, UserForm, UserRecord } from '@/types/admin';
import { accountTypeText, statusText, statusType } from '@/utils/status';
import SearchFilterBar from '@/components/SearchFilterBar.vue';

const loading = ref(false);
const dialogVisible = ref(false);
const passwordDialogVisible = ref(false);
const editingId = ref<number>();
const passwordUserId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<UserRecord[]>([]);
const total = ref(0);
const roles = ref<RoleRecord[]>([]);
const campuses = ref<CampusRecord[]>([]);
const newPassword = ref('123456');
let autoSearchTimer: number | undefined;
let suppressAutoSearch = false;

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', accountType: '', status: '', loginDateRange: [] as string[] });
const form = reactive<UserForm>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  accountType: 'CAMPUS_ADMIN',
  status: 'ENABLED',
  roleIds: [],
  campusIds: [],
  defaultCampusId: undefined,
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  accountType: [{ required: true, message: '请选择账号类型', trigger: 'change' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change' }],
};

const loadOptions = async () => {
  const [rolePage, campusPage] = await Promise.all([
    roleApi.page({ pageNo: 1, pageSize: 200, status: 'ENABLED' }),
    campusApi.page({ pageNo: 1, pageSize: 200, status: 'ENABLED' }),
  ]);
  roles.value = rolePage.records;
  campuses.value = campusPage.records;
};

const loadData = async () => {
  loading.value = true;
  try {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const params: any = { ...query };
    if (query.loginDateRange && query.loginDateRange.length === 2) {
      params.startDate = query.loginDateRange[0];
      params.endDate = query.loginDateRange[1];
    }
    delete params.loginDateRange;
    const page = await userApi.page(params);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const clearAutoSearchTimer = () => {
  if (autoSearchTimer) {
    window.clearTimeout(autoSearchTimer);
    autoSearchTimer = undefined;
  }
};

const searchNow = () => {
  clearAutoSearchTimer();
  query.pageNo = 1;
  loadData();
};

const scheduleAutoSearch = () => {
  if (suppressAutoSearch) return;
  if (query.loginDateRange.length === 1) return;
  clearAutoSearchTimer();
  autoSearchTimer = window.setTimeout(() => {
    query.pageNo = 1;
    loadData();
  }, 420);
};

const resetForm = () => {
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    accountType: 'CAMPUS_ADMIN',
    status: 'ENABLED',
    roleIds: [],
    campusIds: [],
    defaultCampusId: undefined,
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: UserRecord) => {
  editingId.value = row.id;
  Object.assign(form, {
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    email: row.email,
    accountType: row.accountType,
    status: row.status,
    roleIds: row.roleIds || [],
    campusIds: row.campusIds || [],
    defaultCampusId: row.campusIds?.[0],
  });
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (!form.defaultCampusId && form.campusIds.length > 0) {
    form.defaultCampusId = form.campusIds[0];
  }
  if (editingId.value) await userApi.update(editingId.value, form);
  else await userApi.create(form);
  ElMessage.success('已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: UserRecord) => {
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  await ElMessageBox.confirm(`确认${statusText[nextStatus]} ${row.realName}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await userApi.status(row.id, { status: nextStatus });
  ElMessage.success('状态已更新');
  await loadData();
};

const openPassword = (row: UserRecord) => {
  passwordUserId.value = row.id;
  newPassword.value = '123456';
  passwordDialogVisible.value = true;
};

const resetPassword = async () => {
  if (!passwordUserId.value) return;
  await userApi.resetPassword(passwordUserId.value, newPassword.value);
  ElMessage.success('密码已重置');
  passwordDialogVisible.value = false;
};

const roleLabels = (ids: number[]) => {
  const map = new Map(roles.value.map((item) => [item.id, item.name]));
  return ids.map((id) => map.get(id) || id).join('、');
};

const reloadAll = async () => {
  await Promise.all([loadOptions(), loadData()]);
};

const resetFilters = () => {
  suppressAutoSearch = true;
  clearAutoSearchTimer();
  query.keyword = '';
  query.accountType = '';
  query.status = '';
  query.loginDateRange = [];
  query.pageNo = 1;
  loadData();
  nextTick(() => {
    suppressAutoSearch = false;
  });
};

watch(
  () => [query.keyword, query.accountType, query.status, query.loginDateRange[0], query.loginDateRange[1]],
  scheduleAutoSearch,
);

onMounted(reloadAll);
onUnmounted(clearAutoSearchTimer);
</script>

<template>
  <div class="page-stack">
    <section class="page-heading">
      <div>
        <p class="eyebrow">Account</p>
        <h2>用户管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <Plus :size="17" />
        新增用户
      </el-button>
    </section>

    <section class="table-surface">
      <SearchFilterBar :loading="loading" show-reset @search="searchNow" @reset="resetFilters">
        <template #filters>
          <el-input v-model="query.keyword" clearable placeholder="用户名 / 姓名 / 电话" @keyup.enter="searchNow" />
          <el-select v-model="query.accountType" clearable placeholder="账号类型">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="校区管理员" value="CAMPUS_ADMIN" />
            <el-option label="老师" value="TEACHER" />
            <el-option label="家长" value="GUARDIAN" />
          </el-select>
          <el-select v-model="query.status" clearable placeholder="状态">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
          <el-date-picker
            v-model="query.loginDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="登录开始"
            end-placeholder="登录结束"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </template>
      </SearchFilterBar>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="username" label="账号" min-width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column label="类型" width="130">
          <template #default="{ row }">{{ accountTypeText[row.accountType] || row.accountType }}</template>
        </el-table-column>
        <el-table-column label="角色" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ roleLabels(row.roleIds || []) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最近登录" min-width="170" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openPassword(row)">密码</el-button>
            <el-button link :type="row.status === 'ENABLED' ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 'ENABLED' ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid three">
          <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
          <el-form-item v-if="!editingId" label="初始密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
          <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="账号类型" prop="accountType">
            <el-select v-model="form.accountType">
              <el-option label="校区管理员" value="CAMPUS_ADMIN" />
              <el-option label="老师" value="TEACHER" />
              <el-option label="家长" value="GUARDIAN" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple filterable>
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <div class="form-grid two">
          <el-form-item label="可访问校区">
            <el-select v-model="form.campusIds" multiple filterable>
              <el-option v-for="campus in campuses" :key="campus.id" :label="campus.name" :value="campus.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="默认校区">
            <el-select v-model="form.defaultCampusId" clearable>
              <el-option v-for="campus in campuses" :key="campus.id" :label="campus.name" :value="campus.id" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="420px">
      <el-input v-model="newPassword" type="password" show-password>
        <template #prefix><KeyRound :size="17" /></template>
      </el-input>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>
