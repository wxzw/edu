<script setup lang="ts">
import { Plus, Search } from 'lucide-vue-next';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { teacherApi } from '@/api/admin';
import type { TeacherForm, TeacherRecord } from '@/types/admin';
import { statusText, statusType } from '@/utils/status';

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<TeacherRecord[]>([]);
const total = ref(0);

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', status: '' });
const form = reactive<TeacherForm>({
  employeeNo: '',
  name: '',
  gender: '',
  phone: '',
  title: '',
  intro: '',
  status: 'ENABLED',
});

const rules: FormRules = {
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入老师姓名', trigger: 'blur' }],
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await teacherApi.page(query);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    userId: undefined,
    employeeNo: '',
    name: '',
    gender: '',
    phone: '',
    title: '',
    intro: '',
    hireDate: '',
    status: 'ENABLED',
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: TeacherRecord) => {
  editingId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (editingId.value) await teacherApi.update(editingId.value, form);
  else await teacherApi.create(form);
  ElMessage.success('已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: TeacherRecord) => {
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  await ElMessageBox.confirm(`确认${statusText[nextStatus]} ${row.name}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await teacherApi.status(row.id, { status: nextStatus });
  ElMessage.success('状态已更新');
  await loadData();
};

onMounted(() => {
  loadData();
  window.addEventListener('campus-change', loadData);
});
onUnmounted(() => window.removeEventListener('campus-change', loadData));
</script>

<template>
  <div class="page-stack">
    <section class="page-heading">
      <div>
        <p class="eyebrow">Teacher</p>
        <h2>老师管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <Plus :size="17" />
        新增老师
      </el-button>
    </section>

    <section class="table-surface">
      <div class="table-toolbar">
        <el-input v-model="query.keyword" clearable placeholder="姓名 / 工号 / 电话" @keyup.enter="loadData">
          <template #prefix><Search :size="16" /></template>
        </el-input>
        <el-select v-model="query.status" clearable placeholder="状态">
          <el-option label="启用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="employeeNo" label="工号" min-width="130" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="title" label="职务" min-width="140" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="hireDate" label="入职日期" width="120" />
        <el-table-column prop="intro" label="简介" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑老师' : '新增老师'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid two">
          <el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" /></el-form-item>
          <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="性别">
            <el-select v-model="form.gender" clearable>
              <el-option label="女" value="FEMALE" />
              <el-option label="男" value="MALE" />
            </el-select>
          </el-form-item>
          <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="职务"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="入职日期"><el-date-picker v-model="form.hireDate" value-format="YYYY-MM-DD" type="date" /></el-form-item>
        </div>
        <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
