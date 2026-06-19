<script setup lang="ts">
import { ClipboardCheck, Upload } from 'lucide-vue-next';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { admissionApi, classApi, courseApi, studentApi } from '@/api/admin';
import type {
  AdmissionConfirmForm,
  AdmissionRegistrationRecord,
  AdmissionRejectForm,
  BatchEnrollmentResult,
  BatchEnrollmentRow,
  ClassRecord,
  CourseRecord,
  StudentRecord,
} from '@/types/admin';
import { statusText, statusType } from '@/utils/status';
import SearchFilterBar from '@/components/SearchFilterBar.vue';

const router = useRouter();
const loading = ref(false);
const records = ref<AdmissionRegistrationRecord[]>([]);
const total = ref(0);
const courses = ref<CourseRecord[]>([]);
const classes = ref<ClassRecord[]>([]);
const students = ref<StudentRecord[]>([]);

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  courseId: undefined as number | undefined,
  status: '',
});

const selectedRegistration = ref<AdmissionRegistrationRecord>();
const confirmDialogVisible = ref(false);
const confirmMode = ref<'create' | 'link'>('create');
const confirmForm = reactive<AdmissionConfirmForm>({
  studentId: undefined,
  studentNo: '',
  studentName: '',
  nickname: '',
  gender: '',
  birthday: '',
  grade: '',
  school: '',
  englishLevel: '',
  learningGoal: '',
  relation: '家长',
  classId: undefined,
  joinDate: '',
  remark: '',
});

const rejectDialogVisible = ref(false);
const rejectForm = reactive<AdmissionRejectForm>({
  reviewRemark: '',
});

const batchDialogVisible = ref(false);
const batchText = ref('');
const batchResult = ref<BatchEnrollmentResult>();

const courseName = computed(() => new Map(courses.value.map((item) => [item.id, item.name])));
const className = computed(() => new Map(classes.value.map((item) => [item.id, item.name])));
const classOptions = computed(() => {
  const courseId = selectedRegistration.value?.courseId;
  return courseId ? classes.value.filter((item) => item.courseId === courseId) : classes.value;
});
const linkedStudent = computed(() => students.value.find((item) => item.id === confirmForm.studentId));
const selectedCourseName = computed(() => {
  const row = selectedRegistration.value;
  if (!row) return '';
  return row.courseName || courseName.value.get(row.courseId) || `课程 ${row.courseId}`;
});

const today = () => new Date().toISOString().split('T')[0];

const loadOptions = async () => {
  const [coursePage, classPage, studentPage] = await Promise.all([
    courseApi.page({ pageNo: 1, pageSize: 200, status: 'ENABLED' }),
    classApi.page({ pageNo: 1, pageSize: 200 }),
    studentApi.page({ pageNo: 1, pageSize: 200, status: 'ACTIVE' }),
  ]);
  courses.value = coursePage.records;
  classes.value = classPage.records;
  students.value = studentPage.records;
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await admissionApi.page(query);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const reloadAll = async () => {
  await Promise.all([loadOptions(), loadData()]);
};

const resetFilters = () => {
  query.keyword = '';
  query.courseId = undefined;
  query.status = '';
  query.pageNo = 1;
  loadData();
};

const resetConfirmForm = (row: AdmissionRegistrationRecord) => {
  confirmMode.value = 'create';
  Object.assign(confirmForm, {
    studentId: undefined,
    studentNo: undefined,
    studentName: row.studentName || row.childName,
    nickname: '',
    gender: '',
    birthday: '',
    grade: row.childGrade || '',
    school: '',
    englishLevel: '',
    learningGoal: '',
    relation: '家长',
    classId: row.preferredClassId,
    joinDate: today(),
    remark: '',
  });
};

const openConfirm = (row: AdmissionRegistrationRecord) => {
  selectedRegistration.value = row;
  resetConfirmForm(row);
  confirmDialogVisible.value = true;
};

const submitConfirm = async () => {
  if (!selectedRegistration.value) return;
  if (!confirmForm.classId) {
    ElMessage.warning('请选择班级');
    return;
  }
  if (confirmMode.value === 'link' && !confirmForm.studentId) {
    ElMessage.warning('请选择已有学生');
    return;
  }
  if (confirmMode.value === 'create' && !confirmForm.studentName) {
    ElMessage.warning('请填写学生姓名');
    return;
  }
  const payload: AdmissionConfirmForm = {
    ...confirmForm,
    studentId: confirmMode.value === 'link' ? confirmForm.studentId : undefined,
    studentNo: undefined,
  };
  await admissionApi.confirm(selectedRegistration.value.id, payload);
  ElMessage.success('已确认报名并分班');
  confirmDialogVisible.value = false;
  await reloadAll();
};

const openReject = (row: AdmissionRegistrationRecord) => {
  selectedRegistration.value = row;
  rejectForm.reviewRemark = '';
  rejectDialogVisible.value = true;
};

const submitReject = async () => {
  if (!selectedRegistration.value) return;
  if (!rejectForm.reviewRemark.trim()) {
    ElMessage.warning('请填写拒绝原因');
    return;
  }
  await admissionApi.reject(selectedRegistration.value.id, rejectForm);
  ElMessage.success('报名已拒绝');
  rejectDialogVisible.value = false;
  await loadData();
};

const openBatchDialog = () => {
  batchText.value = '';
  batchResult.value = undefined;
  batchDialogVisible.value = true;
};

const splitBatchLine = (line: string) => line.split(/[\t,，]/).map((item) => item.trim());

const parseBatchRows = (): BatchEnrollmentRow[] => {
  return batchText.value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .filter((line, index) => index !== 0 || !/studentName|学生|guardianPhone|家长/.test(line))
    .map((line) => {
      const [studentName, guardianPhone, courseId, classId, grade, school, purchasedHours, guardianName] = splitBatchLine(line);
      return {
        studentName,
        guardianPhone,
        courseId: Number(courseId),
        classId: Number(classId),
        grade,
        school,
        purchasedHours: purchasedHours ? Number(purchasedHours) : undefined,
        guardianName,
        joinDate: today(),
      };
    });
};

const submitBatch = async () => {
  const rows = parseBatchRows();
  const invalid = rows.find((row) => !row.studentName || !row.guardianPhone || !row.courseId || !row.classId);
  if (!rows.length || invalid) {
    ElMessage.warning('请补全学生姓名、家长手机、课程ID、班级ID');
    return;
  }
  batchResult.value = await admissionApi.batchEnroll({ rows });
  ElMessage.success(`导入完成：成功 ${batchResult.value.successCount}，失败 ${batchResult.value.failedCount}`);
  await reloadAll();
};

const confirmPayBadge = (row: AdmissionRegistrationRecord) => {
  return row.orderId ? (statusText[row.payStatus || ''] || row.payStatus || '-') : '无需支付';
};

const formatTime = (value?: string) => value?.slice(0, 16).replace('T', ' ') || '-';

const canReview = (row: AdmissionRegistrationRecord) => row.status === 'PENDING_REVIEW';

const goCreateClass = () => {
  if (!selectedRegistration.value) return;
  router.push({
    name: 'classes',
    query: {
      courseId: String(selectedRegistration.value.courseId),
      create: '1',
    },
  });
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
        <p class="eyebrow">Admissions</p>
        <h2>招生管理</h2>
      </div>
      <el-button type="primary" @click="openBatchDialog">
        <Upload :size="17" />
        批量入班
      </el-button>
    </section>

    <section class="table-surface">
      <SearchFilterBar :loading="loading" show-reset @search="loadData" @reset="resetFilters">
        <template #filters>
          <el-input v-model="query.keyword" clearable placeholder="报名号 / 学生 / 手机" @keyup.enter="loadData" />
          <el-select v-model="query.courseId" clearable filterable placeholder="课程">
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
          <el-select v-model="query.status" clearable placeholder="报名状态">
            <el-option label="待支付" value="WAITING_PAY" />
            <el-option label="待审核" value="PENDING_REVIEW" />
            <el-option label="已排班" value="CLASS_ASSIGNED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </template>
      </SearchFilterBar>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="registrationNo" label="报名号" min-width="150" />
        <el-table-column prop="courseName" label="课程" min-width="150" />
        <el-table-column prop="childName" label="孩子" min-width="110">
          <template #default="{ row }">
            <div class="student-name">
              <span>{{ row.childName }}</span>
              <span v-if="row.childGrade" class="muted">({{ row.childGrade }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applicantPhone" label="手机" width="140" />
        <el-table-column label="意向班级" min-width="140">
          <template #default="{ row }">
            {{ row.preferredClassName || (row.preferredClassId ? className.get(row.preferredClassId) : '-') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="已排班级" min-width="140">
          <template #default="{ row }">
            {{ row.assignedClassName || (row.assignedClassId ? className.get(row.assignedClassId) : '-') || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column label="支付" width="100">
          <template #default="{ row }">{{ confirmPayBadge(row) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="160">
          <template #default="{ row }">{{ formatTime(row.registeredAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!canReview(row)" @click="openConfirm(row)">
              <ClipboardCheck :size="14" style="margin-right: 2px" />
              确认
            </el-button>
            <el-button link type="danger" :disabled="!canReview(row)" @click="openReject(row)">拒绝</el-button>
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

    <el-dialog v-model="confirmDialogVisible" title="确认报名并分班" width="760px">
      <el-form :model="confirmForm" label-position="top">
        <div class="summary-line">
          <strong>{{ selectedRegistration?.childName }}</strong>
          <span>{{ selectedRegistration?.courseName }}</span>
          <span>{{ selectedRegistration?.applicantPhone }}</span>
        </div>

        <el-radio-group v-model="confirmMode" class="student-mode">
          <el-radio-button label="create">新建学生档案</el-radio-button>
          <el-radio-button label="link">关联已有学生</el-radio-button>
        </el-radio-group>

        <el-alert
          v-if="confirmMode === 'create'"
          class="mode-tip"
          title="学号由系统自动生成，规则：S-校区码-年份后两位+4位流水，例如 S-SUN-260001。"
          type="info"
          :closable="false"
          show-icon
        />

        <div v-if="confirmMode === 'link'" class="link-existing">
          <el-form-item label="选择已有学生" required>
            <el-select v-model="confirmForm.studentId" clearable filterable style="width: 100%">
              <el-option
                v-for="student in students"
                :key="student.id"
                :label="`${student.name} (${student.studentNo})`"
                :value="student.id"
              />
            </el-select>
          </el-form-item>
          <div v-if="linkedStudent" class="linked-student-card">
            <strong>{{ linkedStudent.name }}</strong>
            <span>{{ linkedStudent.studentNo }} · {{ linkedStudent.grade || '无年级' }} · {{ linkedStudent.school || '未填写学校' }}</span>
          </div>
        </div>

        <div v-if="confirmMode === 'create'" class="form-grid three">
          <el-form-item label="学生姓名">
            <el-input v-model="confirmForm.studentName" />
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="confirmForm.gender" clearable>
              <el-option label="女" value="FEMALE" />
              <el-option label="男" value="MALE" />
            </el-select>
          </el-form-item>
          <el-form-item label="生日">
            <el-date-picker v-model="confirmForm.birthday" value-format="YYYY-MM-DD" type="date" />
          </el-form-item>
          <el-form-item label="年级">
            <el-input v-model="confirmForm.grade" />
          </el-form-item>
          <el-form-item label="学校">
            <el-input v-model="confirmForm.school" />
          </el-form-item>
        </div>

        <div class="form-grid three">
          <el-form-item label="分配班级" required>
            <el-select v-model="confirmForm.classId" filterable style="width: 100%">
              <el-option
                v-for="klass in classOptions"
                :key="klass.id"
                :label="`${klass.name} (${klass.currentStudents}/${klass.maxStudents})`"
                :value="klass.id"
              />
              <template #empty>
                <div class="class-empty">
                  <strong>{{ selectedCourseName }}</strong>
                  <span>当前课程还没有可分配班级，请先创建班级再确认入班。</span>
                  <el-button link type="primary" @click="goCreateClass">去创建班级</el-button>
                </div>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item label="入班日期">
            <el-date-picker v-model="confirmForm.joinDate" value-format="YYYY-MM-DD" type="date" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="confirmForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfirm">确认入班</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝报名" width="460px">
      <el-form :model="rejectForm" label-position="top">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.reviewRemark" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批量入班" width="760px">
      <el-input
        v-model="batchText"
        type="textarea"
        :rows="8"
        placeholder="张小小,13900000999,1001,4001,三年级,实验小学,48"
      />
      <div class="batch-toolbar">
        <span>列：学生姓名、家长手机、课程ID、班级ID、年级、学校、购买课时、家长姓名</span>
        <el-button type="primary" @click="submitBatch">提交</el-button>
      </div>
      <el-table v-if="batchResult" :data="batchResult.results" stripe class="batch-result">
        <el-table-column prop="rowIndex" label="行" width="70" />
        <el-table-column prop="studentName" label="学生" min-width="120" />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" effect="plain">
              {{ row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="说明" min-width="220" />
      </el-table>
      <template #footer>
        <el-button @click="batchDialogVisible = false">关闭</el-button>
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

.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.summary-line {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  color: var(--el-text-color-secondary);
}

.summary-line strong {
  color: var(--el-text-color-primary);
}

.student-mode {
  margin-bottom: 14px;
}

.mode-tip {
  margin-bottom: 16px;
}

.link-existing {
  margin-bottom: 16px;
}

.linked-student-card {
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.linked-student-card strong,
.linked-student-card span {
  display: block;
}

.linked-student-card strong {
  margin-bottom: 4px;
  color: var(--el-text-color-primary);
}

.class-empty {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: left;
  color: var(--el-text-color-secondary);
}

.class-empty strong {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.batch-result {
  margin-top: 16px;
}
</style>
