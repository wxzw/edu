<script setup lang="ts">
import { Plus } from 'lucide-vue-next';
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { campusApi } from '@/api/admin';
import type { CampusForm, CampusRecord } from '@/types/admin';
import { statusText, statusType } from '@/utils/status';
import SearchFilterBar from '@/components/SearchFilterBar.vue';

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<CampusRecord[]>([]);
const total = ref(0);

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  status: '',
});

const form = reactive<CampusForm>({
  code: '',
  name: '',
  shortName: '',
  contactName: '',
  contactPhone: '',
  address: '',
  businessHours: '',
  status: 'ENABLED',
});

const rules: FormRules = {
  code: [{ required: true, message: '请输入校区编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入校区名称', trigger: 'blur' }],
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await campusApi.page(query);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  editingId.value = undefined;
  Object.assign(form, {
    code: '',
    name: '',
    shortName: '',
    contactName: '',
    contactPhone: '',
    address: '',
    businessHours: '',
    status: 'ENABLED',
  });
  dialogVisible.value = true;
};

const openEdit = (row: CampusRecord) => {
  editingId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (editingId.value) {
    await campusApi.update(editingId.value, form);
  } else {
    await campusApi.create(form);
  }
  ElMessage.success('已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: CampusRecord) => {
  const nextStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  await ElMessageBox.confirm(`确认${statusText[nextStatus]} ${row.name}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await campusApi.status(row.id, { status: nextStatus });
  ElMessage.success('状态已更新');
  await loadData();
};

const resetFilters = () => {
  query.keyword = '';
  query.status = '';
  query.pageNo = 1;
  loadData();
};

onMounted(loadData);
</script>

<template>
  <div class="page-stack">
    <section class="page-heading">
      <div>
        <p class="eyebrow">Campus</p>
        <h2>校区管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <Plus :size="17" />
        新增校区
      </el-button>
    </section>

    <section class="table-surface">
      <SearchFilterBar :loading="loading" show-reset @search="loadData" @reset="resetFilters">
        <template #filters>
          <el-input v-model="query.keyword" clearable placeholder="校区名称 / 编码" @keyup.enter="loadData" />
          <el-select v-model="query.status" clearable placeholder="状态">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </template>
      </SearchFilterBar>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="code" label="编码" min-width="130" />
        <el-table-column prop="name" label="校区" min-width="180" />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="240" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑校区' : '新增校区'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid two">
          <el-form-item label="校区编码" prop="code"><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="校区名称" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="简称"><el-input v-model="form.shortName" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="form.contactPhone" /></el-form-item>
        </div>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
