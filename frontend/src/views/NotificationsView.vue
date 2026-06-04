<script setup lang="ts">
import { BellPlus, Search } from 'lucide-vue-next';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { classApi, notificationApi, studentApi } from '@/api/admin';
import type { ClassRecord, NotificationForm, NotificationRecord, StudentRecord } from '@/types/admin';

const loading = ref(false);
const dialogVisible = ref(false);
const formRef = ref<FormInstance>();
const records = ref<NotificationRecord[]>([]);
const classes = ref<ClassRecord[]>([]);
const students = ref<StudentRecord[]>([]);
const total = ref(0);

const query = reactive({ pageNo: 1, pageSize: 10, status: '', bizType: '', receiverStudentId: undefined as number | undefined });
const form = reactive<NotificationForm>({
  targetType: 'CAMPUS',
  title: '',
  content: '',
  bizType: 'ADMIN_NOTICE',
});

const rules: FormRules = {
  targetType: [{ required: true, message: '请选择发送范围', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await notificationApi.page(query);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const loadOptions = async () => {
  const [classPage, studentPage] = await Promise.all([
    classApi.page({ pageNo: 1, pageSize: 200 }),
    studentApi.page({ pageNo: 1, pageSize: 200 }),
  ]);
  classes.value = classPage.records;
  students.value = studentPage.records;
};

const openPublish = () => {
  Object.assign(form, { targetType: 'CAMPUS', classId: undefined, studentId: undefined, title: '', content: '', bizType: 'ADMIN_NOTICE', bizId: undefined });
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  const result = await notificationApi.publish(form);
  ElMessage.success(`已发送 ${result.sentCount} 条通知`);
  dialogVisible.value = false;
  await loadData();
};

const formatTime = (value?: string) => value?.slice(0, 16).replace('T', ' ') || '-';

onMounted(() => {
  loadData();
  loadOptions();
  window.addEventListener('campus-change', loadData);
});
onUnmounted(() => window.removeEventListener('campus-change', loadData));
</script>

<template>
  <div class="page-stack">
    <section class="page-heading">
      <div>
        <p class="eyebrow">Message</p>
        <h2>通知管理</h2>
      </div>
      <el-button type="primary" @click="openPublish">
        <BellPlus :size="17" /> 发布通知
      </el-button>
    </section>

    <section class="table-surface">
      <div class="table-toolbar">
        <el-input v-model="query.bizType" clearable placeholder="业务类型" @keyup.enter="loadData">
          <template #prefix><Search :size="16" /></template>
        </el-input>
        <el-select v-model="query.status" clearable placeholder="状态">
          <el-option label="未读" value="UNREAD" />
          <el-option label="已读" value="READ" />
        </el-select>
        <el-select v-model="query.receiverStudentId" clearable filterable placeholder="学生">
          <el-option v-for="student in students" :key="student.id" :label="student.name" :value="student.id" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="receiverStudentName" label="接收学生" width="130" />
        <el-table-column prop="bizType" label="业务类型" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'UNREAD' ? 'warning' : 'success'" effect="plain">{{ row.status === 'UNREAD' ? '未读' : '已读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发送时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="loadData" />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="发布通知" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid two">
          <el-form-item label="发送范围" prop="targetType">
            <el-select v-model="form.targetType">
              <el-option label="当前校区全部学生" value="CAMPUS" />
              <el-option label="指定班级" value="CLASS" />
              <el-option label="指定学生" value="STUDENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="业务类型">
            <el-input v-model="form.bizType" />
          </el-form-item>
        </div>
        <el-form-item v-if="form.targetType === 'CLASS'" label="班级">
          <el-select v-model="form.classId" filterable>
            <el-option v-for="item in classes" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.targetType === 'STUDENT'" label="学生">
          <el-select v-model="form.studentId" filterable>
            <el-option v-for="item in students" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>
