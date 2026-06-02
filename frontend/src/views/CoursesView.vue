<script setup lang="ts">
import { Plus, Search } from 'lucide-vue-next';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { courseApi } from '@/api/admin';
import type { CourseForm, CourseRecord } from '@/types/admin';
import { statusText, statusType } from '@/utils/status';

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<CourseRecord[]>([]);
const total = ref(0);

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', courseSystem: '', status: '' });
const form = reactive<CourseForm>({
  courseCode: '',
  courseSystem: '',
  name: '',
  levelName: '',
  gradeScope: '',
  totalHours: 48,
  unitPrice: 200,
  packagePrice: 9600,
  description: '',
  status: 'ENABLED',
});

const rules: FormRules = {
  courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }],
  courseSystem: [{ required: true, message: '请输入课程体系', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  totalHours: [{ required: true, message: '请输入课时', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  packagePrice: [{ required: true, message: '请输入套餐价', trigger: 'blur' }],
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await courseApi.page(query);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    courseCode: '',
    courseSystem: '',
    name: '',
    levelName: '',
    targetAgeMin: undefined,
    targetAgeMax: undefined,
    gradeScope: '',
    totalHours: 48,
    unitPrice: 200,
    packagePrice: 9600,
    description: '',
    status: 'ENABLED',
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: CourseRecord) => {
  editingId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (editingId.value) await courseApi.update(editingId.value, form);
  else await courseApi.create(form);
  ElMessage.success('已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: CourseRecord) => {
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  await ElMessageBox.confirm(`确认${statusText[nextStatus]} ${row.name}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await courseApi.status(row.id, { status: nextStatus });
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
        <p class="eyebrow">Course</p>
        <h2>课程管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <Plus :size="17" />
        新增课程
      </el-button>
    </section>

    <section class="table-surface">
      <div class="table-toolbar">
        <el-input v-model="query.keyword" clearable placeholder="课程名称 / 编码" @keyup.enter="loadData">
          <template #prefix><Search :size="16" /></template>
        </el-input>
        <el-input v-model="query.courseSystem" clearable placeholder="课程体系" />
        <el-select v-model="query.status" clearable placeholder="状态">
          <el-option label="启用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="courseCode" label="编码" min-width="140" />
        <el-table-column prop="name" label="课程" min-width="160" />
        <el-table-column prop="courseSystem" label="体系" min-width="150" />
        <el-table-column prop="levelName" label="级别" width="120" />
        <el-table-column prop="totalHours" label="课时" width="90" />
        <el-table-column prop="unitPrice" label="单价" width="100" />
        <el-table-column prop="packagePrice" label="套餐价" width="110" />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新增课程'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid three">
          <el-form-item label="课程编码" prop="courseCode"><el-input v-model="form.courseCode" /></el-form-item>
          <el-form-item label="课程体系" prop="courseSystem"><el-input v-model="form.courseSystem" /></el-form-item>
          <el-form-item label="课程名称" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="级别"><el-input v-model="form.levelName" /></el-form-item>
          <el-form-item label="最小年龄"><el-input-number v-model="form.targetAgeMin" :min="0" :precision="1" /></el-form-item>
          <el-form-item label="最大年龄"><el-input-number v-model="form.targetAgeMax" :min="0" :precision="1" /></el-form-item>
          <el-form-item label="课时" prop="totalHours"><el-input-number v-model="form.totalHours" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="套餐价" prop="packagePrice"><el-input-number v-model="form.packagePrice" :min="0" :precision="2" /></el-form-item>
        </div>
        <el-form-item label="年级范围"><el-input v-model="form.gradeScope" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
