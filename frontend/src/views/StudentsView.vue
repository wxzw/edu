<script setup lang="ts">
import { GraduationCap, Plus, Search } from 'lucide-vue-next';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { studentApi } from '@/api/admin';
import type { StudentForm, StudentRecord } from '@/types/admin';
import { statusText, statusType } from '@/utils/status';

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<StudentRecord[]>([]);
const total = ref(0);

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', status: '', grade: '' });
const form = reactive<StudentForm>({
  studentNo: '',
  name: '',
  nickname: '',
  gender: '',
  birthday: '',
  grade: '',
  school: '',
  englishLevel: '',
  learningGoal: '',
  status: 'ACTIVE',
});

const rules: FormRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
};

const studentStatusText: Record<string, string> = {
  ACTIVE: '在读',
  SUSPENDED: '休学',
  GRADUATED: '毕业',
  WITHDRAWN: '退学',
};

const studentStatusType: Record<string, string> = {
  ACTIVE: 'success',
  SUSPENDED: 'warning',
  GRADUATED: 'info',
  WITHDRAWN: 'danger',
};

const gradeOptions = [
  '幼儿园小班',
  '幼儿园中班',
  '幼儿园大班',
  '小学一年级',
  '小学二年级',
  '小学三年级',
  '小学四年级',
  '小学五年级',
  '小学六年级',
  '初中一年级',
  '初中二年级',
  '初中三年级',
  '高中一年级',
  '高中二年级',
  '高中三年级',
];

const loadData = async () => {
  loading.value = true;
  try {
    const page = await studentApi.page(query);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    userId: undefined,
    studentNo: '',
    name: '',
    nickname: '',
    avatarUrl: '',
    gender: '',
    birthday: '',
    grade: '',
    school: '',
    englishLevel: '',
    learningGoal: '',
    enrolledAt: '',
    status: 'ACTIVE',
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: StudentRecord) => {
  editingId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (editingId.value) await studentApi.update(editingId.value, form);
  else await studentApi.create(form);
  ElMessage.success('已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: StudentRecord) => {
  const nextStatus = row.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
  await ElMessageBox.confirm(`确认${studentStatusText[nextStatus]} ${row.name}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await studentApi.status(row.id, { status: nextStatus });
  ElMessage.success('状态已更新');
  await loadData();
};

const getAge = (birthday?: string) => {
  if (!birthday) return '-';
  const birth = new Date(birthday);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }
  return age + '岁';
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
        <p class="eyebrow">Student</p>
        <h2>学生管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <Plus :size="17" />
        新增学生
      </el-button>
    </section>

    <section class="table-surface">
      <div class="table-toolbar">
        <el-input v-model="query.keyword" clearable placeholder="姓名 / 学号 / 昵称 / 学校" @keyup.enter="loadData">
          <template #prefix><Search :size="16" /></template>
        </el-input>
        <el-select v-model="query.status" clearable placeholder="状态">
          <el-option label="在读" value="ACTIVE" />
          <el-option label="休学" value="SUSPENDED" />
          <el-option label="毕业" value="GRADUATED" />
          <el-option label="退学" value="WITHDRAWN" />
        </el-select>
        <el-select v-model="query.grade" clearable placeholder="年级" filterable>
          <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="studentNo" label="学号" min-width="130" />
        <el-table-column prop="name" label="姓名" min-width="120">
          <template #default="{ row }">
            <div class="student-name">
              <span>{{ row.name }}</span>
              <span v-if="row.nickname" class="nickname">({{ row.nickname }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="年龄" width="80">
          <template #default="{ row }">
            {{ getAge(row.birthday) }}
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" min-width="120" />
        <el-table-column prop="school" label="学校" min-width="150" show-overflow-tooltip />
        <el-table-column prop="englishLevel" label="英语水平" min-width="100" />
        <el-table-column prop="enrolledAt" label="入学日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="studentStatusType[row.status] || 'info'" effect="plain">
              {{ studentStatusText[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'ACTIVE' || row.status === 'SUSPENDED'"
              link
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '休学' : '复学' }}
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑学生' : '新增学生'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid three">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入学生姓名" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="英文名/昵称" />
          </el-form-item>
        </div>
        <div class="form-grid three">
          <el-form-item label="性别">
            <el-select v-model="form.gender" clearable placeholder="请选择">
              <el-option label="女" value="FEMALE" />
              <el-option label="男" value="MALE" />
            </el-select>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="form.birthday" value-format="YYYY-MM-DD" type="date" placeholder="选择日期" />
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="form.grade" clearable filterable placeholder="请选择年级">
              <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-grid two">
          <el-form-item label="就读学校">
            <el-input v-model="form.school" placeholder="当前就读学校" />
          </el-form-item>
          <el-form-item label="英语水平">
            <el-input v-model="form.englishLevel" placeholder="如：初级/中级/高级" />
          </el-form-item>
        </div>
        <div class="form-grid two">
          <el-form-item label="入学日期">
            <el-date-picker v-model="form.enrolledAt" value-format="YYYY-MM-DD" type="date" placeholder="选择入学日期" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" placeholder="请选择状态">
              <el-option label="在读" value="ACTIVE" />
              <el-option label="休学" value="SUSPENDED" />
              <el-option label="毕业" value="GRADUATED" />
              <el-option label="退学" value="WITHDRAWN" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="学习目标">
          <el-input v-model="form.learningGoal" type="textarea" :rows="3" placeholder="描述学生的学习目标和期望" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.student-name {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.nickname {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.form-grid.three {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.form-grid.two {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
</style>