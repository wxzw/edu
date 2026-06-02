<script setup lang="ts">
import { Plus, Search } from 'lucide-vue-next';
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { campusApi, roleApi } from '@/api/admin';
import type { CampusRecord, PermissionNode, RoleForm, RoleRecord } from '@/types/admin';
import { dataScopeText, statusText, statusType } from '@/utils/status';

const loading = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number>();
const formRef = ref<FormInstance>();
const records = ref<RoleRecord[]>([]);
const total = ref(0);
const permissionTree = ref<PermissionNode[]>([]);
const campuses = ref<CampusRecord[]>([]);

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', status: '' });
const form = reactive<RoleForm>({
  campusId: undefined,
  code: '',
  name: '',
  scopeType: 'SYSTEM',
  dataScope: 'CAMPUS',
  status: 'ENABLED',
  remark: '',
  permissionIds: [],
});

const rules: FormRules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
};

const loadOptions = async () => {
  const [tree, campusPage] = await Promise.all([
    roleApi.permissionTree(),
    campusApi.page({ pageNo: 1, pageSize: 200, status: 'ENABLED' }),
  ]);
  permissionTree.value = tree;
  campuses.value = campusPage.records;
};

const loadData = async () => {
  loading.value = true;
  try {
    const page = await roleApi.page(query);
    records.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  Object.assign(form, {
    campusId: undefined,
    code: '',
    name: '',
    scopeType: 'SYSTEM',
    dataScope: 'CAMPUS',
    status: 'ENABLED',
    remark: '',
    permissionIds: [],
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row: RoleRecord) => {
  editingId.value = row.id;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (editingId.value) await roleApi.update(editingId.value, form);
  else await roleApi.create(form);
  ElMessage.success('已保存');
  dialogVisible.value = false;
  await loadData();
};

const campusLabel = (campusId?: number) => campuses.value.find((item) => item.id === campusId)?.shortName || '全局';

const reloadAll = async () => {
  await Promise.all([loadOptions(), loadData()]);
};

onMounted(reloadAll);
</script>

<template>
  <div class="page-stack">
    <section class="page-heading">
      <div>
        <p class="eyebrow">RBAC</p>
        <h2>角色权限</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <Plus :size="17" />
        新增角色
      </el-button>
    </section>

    <section class="table-surface">
      <div class="table-toolbar">
        <el-input v-model="query.keyword" clearable placeholder="角色名称 / 编码" @keyup.enter="loadData">
          <template #prefix><Search :size="16" /></template>
        </el-input>
        <el-select v-model="query.status" clearable placeholder="状态">
          <el-option label="启用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
        <el-button @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="code" label="编码" min-width="150" />
        <el-table-column prop="name" label="角色" min-width="140" />
        <el-table-column label="校区" width="120">
          <template #default="{ row }">{{ campusLabel(row.campusId) }}</template>
        </el-table-column>
        <el-table-column label="数据范围" width="130">
          <template #default="{ row }">{{ dataScopeText[row.dataScope] || row.dataScope }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid three">
          <el-form-item label="角色编码" prop="code"><el-input v-model="form.code" /></el-form-item>
          <el-form-item label="角色名称" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="校区">
            <el-select v-model="form.campusId" clearable>
              <el-option v-for="campus in campuses" :key="campus.id" :label="campus.name" :value="campus.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="角色域">
            <el-select v-model="form.scopeType">
              <el-option label="系统" value="SYSTEM" />
              <el-option label="校区" value="CAMPUS" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据范围">
            <el-select v-model="form.dataScope">
              <el-option label="全部校区" value="ALL" />
              <el-option label="当前校区" value="CAMPUS" />
              <el-option label="本人数据" value="SELF" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <el-form-item label="权限">
          <el-tree-select
            v-model="form.permissionIds"
            :data="permissionTree"
            multiple
            show-checkbox
            check-strictly
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
