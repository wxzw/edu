<script setup lang="ts">
import { CalendarPlus } from 'lucide-vue-next';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { activityApi, fileApi } from '@/api/admin';
import type { ActivityForm, ActivityRecord, ActivityRegistrationRecord } from '@/types/admin';
import { statusText, statusType } from '@/utils/status';
import SearchFilterBar from '@/components/SearchFilterBar.vue';

const loading = ref(false);
const dialogVisible = ref(false);
const registrationsVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<ActivityRecord[]>([]);
const registrations = ref<ActivityRegistrationRecord[]>([]);
const total = ref(0);
const registrationTotal = ref(0);
const activeActivity = ref<ActivityRecord>();

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', status: '', dateRange: [] as string[] });
const registrationQuery = reactive({ pageNo: 1, pageSize: 10, activityId: undefined as number | undefined, status: '' });
const form = reactive<ActivityForm>({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  fee: 0,
  quota: 30,
  status: 'DRAFT',
});

const rules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
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
    const page = await activityApi.page(params);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    title: '',
    description: '',
    coverFileId: undefined,
    startTime: '',
    endTime: '',
    location: '',
    fee: 0,
    quota: 30,
    status: 'DRAFT',
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: ActivityRecord) => {
  editingId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (editingId.value) await activityApi.update(editingId.value, form);
  else await activityApi.create(form);
  ElMessage.success('活动已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: ActivityRecord) => {
  const nextStatus = row.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED';
  await ElMessageBox.confirm(`确认${nextStatus === 'PUBLISHED' ? '发布' : '下架'} ${row.title}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await activityApi.status(row.id, { status: nextStatus });
  ElMessage.success('状态已更新');
  await loadData();
};

const uploadCover = async (options: UploadRequestOptions) => {
  const file = await fileApi.uploadLocal(options.file, 'ACTIVITY_COVER');
  form.coverFileId = file.id;
  ElMessage.success(`已上传 ${file.fileName}`);
  options.onSuccess(file);
};

const openRegistrations = async (row: ActivityRecord) => {
  activeActivity.value = row;
  registrationQuery.activityId = row.id;
  registrationsVisible.value = true;
  await loadRegistrations();
};

const loadRegistrations = async () => {
  const page = await activityApi.registrations(registrationQuery);
  registrations.value = page.records;
  registrationTotal.value = page.total;
};

const formatTime = (value?: string) => value?.slice(0, 16).replace('T', ' ') || '-';

const resetFilters = () => {
  query.keyword = '';
  query.status = '';
  query.dateRange = [];
  query.pageNo = 1;
  loadData();
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
        <p class="eyebrow">Operation</p>
        <h2>活动管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <CalendarPlus :size="17" /> 新增活动
      </el-button>
    </section>

    <section class="table-surface">
      <SearchFilterBar :loading="loading" show-reset @search="loadData" @reset="resetFilters">
        <template #filters>
          <el-input v-model="query.keyword" clearable placeholder="活动标题" @keyup.enter="loadData" />
          <el-select v-model="query.status" clearable placeholder="状态">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="活动开始"
            end-placeholder="活动结束"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </template>
      </SearchFilterBar>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="title" label="活动" min-width="220" />
        <el-table-column prop="startTime" label="时间" min-width="210">
          <template #default="{ row }">{{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="180" />
        <el-table-column prop="fee" label="费用" width="100">
          <template #default="{ row }">￥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column label="名额" width="110">
          <template #default="{ row }">{{ row.registeredCount }}/{{ row.quota || '不限' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="openRegistrations(row)">报名</el-button>
            <el-button link :type="row.status === 'PUBLISHED' ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 'PUBLISHED' ? '下架' : '发布' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="loadData" />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑活动' : '新增活动'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="活动标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <div class="form-grid two">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ssZ" />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ssZ" />
          </el-form-item>
        </div>
        <el-form-item label="地点" prop="location"><el-input v-model="form.location" /></el-form-item>
        <div class="form-grid three">
          <el-form-item label="费用"><el-input-number v-model="form.fee" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="名额"><el-input-number v-model="form.quota" :min="1" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="封面图">
          <el-upload :http-request="uploadCover" :show-file-list="false"><el-button>上传封面</el-button></el-upload>
          <span class="upload-hint">{{ form.coverFileId ? `文件ID ${form.coverFileId}` : '可选' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="registrationsVisible" :title="`报名记录 · ${activeActivity?.title || ''}`" size="680px">
      <el-table :data="registrations" stripe>
        <el-table-column prop="registrationNo" label="报名号" min-width="150" />
        <el-table-column prop="studentName" label="学生" width="110" />
        <el-table-column prop="amount" label="金额" width="90" />
        <el-table-column prop="payStatus" label="支付" width="100" />
        <el-table-column prop="registeredAt" label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.registeredAt) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination v-model:current-page="registrationQuery.pageNo" v-model:page-size="registrationQuery.pageSize" :total="registrationTotal" layout="total, prev, pager, next" @change="loadRegistrations" />
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.upload-hint {
  margin-left: 10px;
  color: var(--muted-text);
  font-size: 12px;
}
</style>
