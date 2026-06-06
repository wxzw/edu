<script setup lang="ts">
import { Plus, Trash2, Users, UserPlus } from 'lucide-vue-next';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { classApi, courseApi, teacherApi, studentApi } from '@/api/admin';
import type { ClassForm, ClassRecord, ClassStudentRecord, CourseRecord, TeacherRecord, StudentRecord } from '@/types/admin';
import { statusText, statusType } from '@/utils/status';
import SearchFilterBar from '@/components/SearchFilterBar.vue';

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<ClassRecord[]>([]);
const total = ref(0);
const courses = ref<CourseRecord[]>([]);
const teachers = ref<TeacherRecord[]>([]);

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', courseId: undefined as number | undefined, status: '', dateRange: [] as string[] });
const form = reactive<ClassForm>({
  courseId: undefined,
  classNo: '',
  name: '',
  headTeacherId: undefined,
  classroom: '',
  classWechatQrUrl: '',
  startDate: '',
  endDate: '',
  maxStudents: 8,
  status: 'PREPARING',
  remark: '',
});

const courseName = computed(() => new Map(courses.value.map((item) => [item.id, item.name])));
const teacherName = computed(() => new Map(teachers.value.map((item) => [item.id, item.name])));

const rules: FormRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  classNo: [{ required: true, message: '请输入班级编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
};

// ---- 学生 Drawer ----
const drawerVisible = ref(false);
const drawerLoading = ref(false);
const drawerClass = ref<ClassRecord>();
const studentList = ref<ClassStudentRecord[]>([]);

// 添加学生对话框
const addStudentDialogVisible = ref(false);
const addStudentFormRef = ref<FormInstance>();
const allStudents = ref<StudentRecord[]>([]);
const addStudentForm = reactive({
  studentId: undefined as number | undefined,
  joinDate: '',
});

const classStudentStatusText: Record<string, string> = {
  ACTIVE: '在读',
  LEFT: '已退出',
};

const classStudentStatusType: Record<string, string> = {
  ACTIVE: 'success',
  LEFT: 'info',
};

const openStudentDrawer = async (row: ClassRecord) => {
  drawerClass.value = row;
  drawerVisible.value = true;
  await loadStudentList();
};

const loadStudentList = async () => {
  if (!drawerClass.value) return;
  drawerLoading.value = true;
  try {
    studentList.value = await classApi.students(drawerClass.value.id);
  } finally {
    drawerLoading.value = false;
  }
};

const openAddStudentDialog = async () => {
  addStudentForm.studentId = undefined;
  addStudentForm.joinDate = new Date().toISOString().split('T')[0];
  
  // 加载所有学生供选择
  const page = await studentApi.page({ pageNo: 1, pageSize: 500, status: 'ACTIVE' });
  // 过滤掉已在班级中的学生
  const existingStudentIds = new Set(studentList.value.map(s => s.studentId));
  allStudents.value = page.records.filter(s => !existingStudentIds.has(s.id));
  
  addStudentDialogVisible.value = true;
};

const submitAddStudent = async () => {
  if (!addStudentForm.studentId) {
    ElMessage.warning('请选择学生');
    return;
  }
  if (!drawerClass.value) return;
  
  await classApi.addStudent(drawerClass.value.id, {
    studentId: addStudentForm.studentId,
    joinDate: addStudentForm.joinDate,
  });
  ElMessage.success('添加成功');
  addStudentDialogVisible.value = false;
  await loadStudentList();
  // 刷新班级列表以更新人数
  await loadData();
};

const removeStudent = async (row: ClassStudentRecord) => {
  if (!drawerClass.value) return;
  await ElMessageBox.confirm(`确认将 ${row.name} 从班级中移除？`, '移除学生', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await classApi.removeStudent(drawerClass.value.id, row.studentId);
  ElMessage.success('移除成功');
  await loadStudentList();
  // 刷新班级列表以更新人数
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
// ---- end 学生 Drawer ----

const loadOptions = async () => {
  const [coursePage, teacherPage] = await Promise.all([
    courseApi.page({ pageNo: 1, pageSize: 200, status: 'ENABLED' }),
    teacherApi.page({ pageNo: 1, pageSize: 200, status: 'ENABLED' }),
  ]);
  courses.value = coursePage.records;
  teachers.value = teacherPage.records;
};

const loadData = async () => {
  loading.value = true;
  try {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const params: any = { ...query };
    if (query.dateRange && query.dateRange.length === 2) {
      params.startDate = query.dateRange[0];
      params.endDate = query.dateRange[1];
    }
    delete params.dateRange;
    const page = await classApi.page(params);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const resetFilters = () => {
  query.keyword = '';
  query.courseId = undefined;
  query.status = '';
  query.dateRange = [];
  query.pageNo = 1;
  loadData();
};

const resetForm = () => {
  Object.assign(form, {
    courseId: undefined,
    classNo: '',
    name: '',
    headTeacherId: undefined,
    classroom: '',
    classWechatQrUrl: '',
    startDate: '',
    endDate: '',
    maxStudents: 8,
    status: 'PREPARING',
    remark: '',
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: ClassRecord) => {
  editingId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (editingId.value) await classApi.update(editingId.value, form);
  else await classApi.create(form);
  ElMessage.success('已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: ClassRecord) => {
  const nextStatus = row.status === 'OPEN' ? 'CLOSED' : 'OPEN';
  await ElMessageBox.confirm(`确认${statusText[nextStatus]} ${row.name}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await classApi.status(row.id, { status: nextStatus });
  ElMessage.success('状态已更新');
  await loadData();
};

const reloadAll = async () => {
  await Promise.all([loadOptions(), loadData()]);
};

onMounted(() => {
  reloadAll();
  window.addEventListener('campus-change', reloadAll);
});
onUnmounted(() => window.removeEventListener('campus-change', reloadAll));
</script>

<template>
  <div class="page-stack">
    <section class="page-heading">
      <div>
        <p class="eyebrow">Class</p>
        <h2>班级管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <Plus :size="17" />
        新增班级
      </el-button>
    </section>

    <section class="table-surface">
      <SearchFilterBar :loading="loading" show-reset @search="loadData" @reset="resetFilters">
        <template #filters>
          <el-input v-model="query.keyword" clearable placeholder="班级名称 / 编号" @keyup.enter="loadData" />
          <el-select v-model="query.courseId" clearable placeholder="课程">
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
          <el-select v-model="query.status" clearable placeholder="状态">
            <el-option label="筹备中" value="PREPARING" />
            <el-option label="开班中" value="OPEN" />
            <el-option label="已结班" value="CLOSED" />
          </el-select>
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开班开始"
            end-placeholder="开班结束"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </template>
      </SearchFilterBar>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="classNo" label="编号" min-width="140" />
        <el-table-column prop="name" label="班级" min-width="160" />
        <el-table-column label="课程" min-width="150">
          <template #default="{ row }">{{ courseName.get(row.courseId) || row.courseId }}</template>
        </el-table-column>
        <el-table-column label="班主任" width="120">
          <template #default="{ row }">{{ row.headTeacherId ? teacherName.get(row.headTeacherId) || row.headTeacherId : '-' }}</template>
        </el-table-column>
        <el-table-column label="人数" width="90">
          <template #default="{ row }">
            <span class="student-count" :class="{ 'has-students': row.currentStudents > 0 }">
              {{ row.currentStudents }}/{{ row.maxStudents }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="classroom" label="教室" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openStudentDrawer(row)">
              <Users :size="14" style="margin-right: 2px" />
              学生
            </el-button>
            <el-button link :type="row.status === 'OPEN' ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 'OPEN' ? '结班' : '开班' }}
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

    <!-- 新增/编辑班级对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑班级' : '新增班级'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid three">
          <el-form-item label="课程" prop="courseId">
            <el-select v-model="form.courseId" filterable>
              <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级编号" prop="classNo"><el-input v-model="form.classNo" /></el-form-item>
          <el-form-item label="班级名称" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="班主任">
            <el-select v-model="form.headTeacherId" clearable filterable>
              <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="最大人数"><el-input-number v-model="form.maxStudents" :min="1" :max="60" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="筹备中" value="PREPARING" />
              <el-option label="开班中" value="OPEN" />
              <el-option label="已结班" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始日期"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" type="date" /></el-form-item>
          <el-form-item label="结束日期"><el-date-picker v-model="form.endDate" value-format="YYYY-MM-DD" type="date" /></el-form-item>
          <el-form-item label="教室"><el-input v-model="form.classroom" /></el-form-item>
        </div>
        <el-form-item label="班级群二维码"><el-input v-model="form.classWechatQrUrl" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看班级学生 Drawer -->
    <el-drawer v-model="drawerVisible" :title="`${drawerClass?.name || '班级'} — 学生列表`" size="720px">
      <template #header>
        <div class="drawer-header">
          <div>
            <h3>{{ drawerClass?.name }}</h3>
            <p class="drawer-sub">
              {{ drawerClass?.classNo }}
              <el-tag v-if="drawerClass" size="small" :type="statusType(drawerClass.status)" style="margin-left: 8px">
                {{ statusText[drawerClass.status] }}
              </el-tag>
            </p>
          </div>
          <div class="drawer-actions">
            <div class="drawer-stat">
              <span class="stat-num">{{ studentList.length }}</span>
              <span class="stat-label">在册学生</span>
            </div>
            <el-button type="primary" @click="openAddStudentDialog">
              <UserPlus :size="14" style="margin-right: 4px" />
              添加学生
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="drawerLoading" class="student-drawer-body">
        <el-empty v-if="!drawerLoading && studentList.length === 0" description="暂无学生" />

        <el-table v-else :data="studentList" stripe>
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column prop="name" label="姓名" min-width="110">
            <template #default="{ row }">
              <div class="student-name">
                <span>{{ row.name }}</span>
                <span v-if="row.nickname" class="nickname">({{ row.nickname }})</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="gender" label="性别" width="60">
            <template #default="{ row }">
              {{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="年龄" width="60">
            <template #default="{ row }">{{ getAge(row.birthday) }}</template>
          </el-table-column>
          <el-table-column prop="grade" label="年级" min-width="100" />
          <el-table-column prop="school" label="学校" min-width="110" show-overflow-tooltip />
          <el-table-column prop="joinDate" label="入班日期" width="100" />
          <el-table-column prop="classStudentStatus" label="状态" width="70">
            <template #default="{ row }">
              <el-tag :type="classStudentStatusType[row.classStudentStatus] || 'info'" size="small" effect="plain">
                {{ classStudentStatusText[row.classStudentStatus] || row.classStudentStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeStudent(row)">
                <Trash2 :size="14" />
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 添加学生对话框 -->
    <el-dialog v-model="addStudentDialogVisible" title="添加学生到班级" width="480px">
      <el-form ref="addStudentFormRef" :model="addStudentForm" label-position="top">
        <el-form-item label="选择学生" required>
          <el-select
            v-model="addStudentForm.studentId"
            filterable
            remote
            placeholder="搜索学生姓名/学号"
            style="width: 100%"
          >
            <el-option
              v-for="student in allStudents"
              :key="student.id"
              :label="`${student.name} (${student.studentNo}) - ${student.grade || '无年级'}`"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入班日期">
          <el-date-picker
            v-model="addStudentForm.joinDate"
            value-format="YYYY-MM-DD"
            type="date"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addStudentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddStudent">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.student-count {
  font-variant-numeric: tabular-nums;
  color: var(--el-text-color-secondary);
}

.student-count.has-students {
  color: var(--el-color-primary);
  font-weight: 600;
}

.drawer-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
}

.drawer-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.drawer-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
}

.drawer-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.drawer-stat {
  text-align: center;
  padding: 8px 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--el-color-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.student-drawer-body {
  min-height: 200px;
}

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
</style>