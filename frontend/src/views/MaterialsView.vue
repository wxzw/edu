<script setup lang="ts">
import { Files, Plus, UploadCloud } from 'lucide-vue-next';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { classApi, fileApi, materialApi, materialCategoryApi, teacherApi } from '@/api/admin';
import type {
  ClassRecord,
  MaterialCategoryForm,
  MaterialCategoryRecord,
  MaterialForm,
  MaterialRecord,
  TeacherRecord,
} from '@/types/admin';
import { statusText, statusType } from '@/utils/status';
import SearchFilterBar from '@/components/SearchFilterBar.vue';

const loading = ref(false);
const dialogVisible = ref(false);
const categoryDialogVisible = ref(false);
const activePanel = ref<'materials' | 'categories'>('materials');
const editingId = ref<number>();
const editingCategoryId = ref<number>();
const formRef = ref<FormInstance>();
const categoryFormRef = ref<FormInstance>();
const records = ref<MaterialRecord[]>([]);
const categories = ref<MaterialCategoryRecord[]>([]);
const classes = ref<ClassRecord[]>([]);
const teachers = ref<TeacherRecord[]>([]);
const total = ref(0);

const query = reactive({ pageNo: 1, pageSize: 10, keyword: '', categoryId: undefined as number | undefined, status: '', dateRange: [] as string[] });
const form = reactive<MaterialForm>({
  title: '',
  description: '',
  resourceType: 'PDF',
  visibility: 'CAMPUS',
  studyType: 'OPTIONAL',
  allowDownload: false,
  status: 'DRAFT',
  classIds: [],
});
const categoryForm = reactive<MaterialCategoryForm>({ name: '', sortOrder: 0, status: 'ENABLED' });

const rules: FormRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  fileId: [{ required: true, message: '请上传资料文件', trigger: 'change' }],
};
const categoryRules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
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
    const [page, categoryList] = await Promise.all([
      materialApi.page(params),
      materialCategoryApi.list(),
    ]);
    records.value = page.records;
    total.value = page.total;
    categories.value = categoryList;
  } finally {
    loading.value = false;
  }
};

const loadOptions = async () => {
  const [classPage, teacherPage] = await Promise.all([
    classApi.page({ pageNo: 1, pageSize: 200 }),
    teacherApi.page({ pageNo: 1, pageSize: 200 }),
  ]);
  classes.value = classPage.records;
  teachers.value = teacherPage.records;
};

const resetForm = () => {
  Object.assign(form, {
    categoryId: undefined,
    title: '',
    description: '',
    resourceType: 'PDF',
    coverFileId: undefined,
    fileId: undefined,
    ownerTeacherId: undefined,
    visibility: 'CAMPUS',
    studyType: 'OPTIONAL',
    allowDownload: false,
    status: 'DRAFT',
    classIds: [],
  });
};

const openCreate = () => {
  editingId.value = undefined;
  resetForm();
  dialogVisible.value = true;
};

const openEdit = async (row: MaterialRecord) => {
  editingId.value = row.id;
  const detail = await materialApi.detail(row.id);
  Object.assign(form, detail);
  dialogVisible.value = true;
};

const submit = async () => {
  await formRef.value?.validate();
  if (form.visibility === 'CLASS' && !form.classIds.length) {
    ElMessage.warning('班级可见资料必须选择班级');
    return;
  }
  if (editingId.value) await materialApi.update(editingId.value, form);
  else await materialApi.create(form);
  ElMessage.success('资料已保存');
  dialogVisible.value = false;
  await loadData();
};

const toggleStatus = async (row: MaterialRecord) => {
  const nextStatus = row.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED';
  await ElMessageBox.confirm(`确认${nextStatus === 'PUBLISHED' ? '发布' : '下架'} ${row.title}？`, '状态变更', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await materialApi.status(row.id, { status: nextStatus });
  ElMessage.success('状态已更新');
  await loadData();
};

const uploadMaterialFile = async (options: UploadRequestOptions) => {
  const file = await fileApi.uploadLocal(options.file, 'MATERIAL');
  form.fileId = file.id;
  ElMessage.success(`已上传 ${file.fileName}`);
  options.onSuccess(file);
};

const uploadCoverFile = async (options: UploadRequestOptions) => {
  const file = await fileApi.uploadLocal(options.file, 'MATERIAL_COVER');
  form.coverFileId = file.id;
  ElMessage.success(`已上传 ${file.fileName}`);
  options.onSuccess(file);
};

const openCategoryCreate = () => {
  editingCategoryId.value = undefined;
  Object.assign(categoryForm, { name: '', sortOrder: categories.value.length * 10 + 10, status: 'ENABLED' });
  categoryDialogVisible.value = true;
};

const openCategoryEdit = (row: MaterialCategoryRecord) => {
  editingCategoryId.value = row.id;
  Object.assign(categoryForm, row);
  categoryDialogVisible.value = true;
};

const submitCategory = async () => {
  await categoryFormRef.value?.validate();
  if (editingCategoryId.value) await materialCategoryApi.update(editingCategoryId.value, categoryForm);
  else await materialCategoryApi.create(categoryForm);
  ElMessage.success('分类已保存');
  categoryDialogVisible.value = false;
  await loadData();
};

const labelOf = (map: Record<string, string>, value: string) => map[value] || value;
const resourceText: Record<string, string> = { PDF: 'PDF', VIDEO: '视频', AUDIO: '音频', IMAGE: '图片', LINK: '链接' };
const studyText: Record<string, string> = { REQUIRED: '必学', OPTIONAL: '选学' };
const visibilityText: Record<string, string> = { CAMPUS: '全校区', CLASS: '指定班级' };

const resetFilters = () => {
  query.keyword = '';
  query.categoryId = undefined;
  query.status = '';
  query.dateRange = [];
  query.pageNo = 1;
  loadData();
};

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
      <div class="heading-main">
        <span class="heading-icon">
          <Files :size="22" />
        </span>
        <div>
          <p class="eyebrow">Resource</p>
          <h2>资料库</h2>
          <div class="heading-summary">
            <span class="summary-pill"><strong>{{ total }}</strong> 总量</span>
            <span class="summary-pill"><strong>{{ records.length }}</strong> 当前页</span>
            <span class="summary-pill"><strong>{{ categories.length }}</strong> 分类</span>
          </div>
        </div>
      </div>
      <div class="heading-actions">
        <el-segmented v-model="activePanel" :options="[{ label: '资料', value: 'materials' }, { label: '分类', value: 'categories' }]" />
        <el-button v-if="activePanel === 'materials'" type="primary" @click="openCreate">
          <Plus :size="17" /> 新增资料
        </el-button>
        <el-button v-else type="primary" @click="openCategoryCreate">
          <Plus :size="17" /> 新增分类
        </el-button>
      </div>
    </section>

    <section v-if="activePanel === 'materials'" class="table-surface">
      <div class="surface-header">
        <div>
          <h3>资料列表</h3>
          <p>共 {{ total }} 条资料，当前显示 {{ records.length }} 条</p>
        </div>
      </div>

      <SearchFilterBar :loading="loading" show-reset @search="loadData" @reset="resetFilters">
        <template #filters>
          <el-input v-model="query.keyword" clearable placeholder="资料标题 / 描述" @keyup.enter="loadData" />
          <el-select v-model="query.categoryId" clearable placeholder="分类">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
          <el-select v-model="query.status" clearable placeholder="状态">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
          </el-select>
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="上传开始"
            end-placeholder="上传结束"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </template>
      </SearchFilterBar>

      <div class="table-frame">
        <el-table v-loading="loading" :data="records" stripe>
          <el-table-column prop="title" label="资料" min-width="240">
            <template #default="{ row }">
              <div class="table-main-cell">
                <span class="cell-icon">
                  <Files :size="17" />
                </span>
                <div>
                  <span class="cell-title">{{ row.title }}</span>
                  <span class="cell-subtitle">{{ row.fileName || '未命名文件' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="130">
            <template #default="{ row }">
              <span class="text-pill">{{ row.categoryName || '未分类' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="resourceType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag class="status-tag" type="info" effect="plain">{{ labelOf(resourceText, row.resourceType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="studyType" label="学习属性" width="110">
            <template #default="{ row }">
              <el-tag class="status-tag" :type="row.studyType === 'REQUIRED' ? 'warning' : 'info'" effect="plain">
                {{ labelOf(studyText, row.studyType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="visibility" label="范围" width="110">
            <template #default="{ row }">
              <span class="text-pill">{{ labelOf(visibilityText, row.visibility) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="allowDownload" label="下载" width="90">
            <template #default="{ row }">
              <el-tag class="status-tag" :type="row.allowDownload ? 'success' : 'info'" effect="plain">{{ row.allowDownload ? '允许' : '禁止' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag class="status-tag" :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link :type="row.status === 'PUBLISHED' ? 'danger' : 'success'" @click="toggleStatus(row)">
                  {{ row.status === 'PUBLISHED' ? '下架' : '发布' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-row">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" layout="total, sizes, prev, pager, next" @change="loadData" />
      </div>
    </section>

    <section v-else class="table-surface">
      <div class="surface-header">
        <div>
          <h3>分类管理</h3>
          <p>共 {{ categories.length }} 个分类</p>
        </div>
      </div>
      <div class="table-frame">
        <el-table :data="categories" stripe>
          <el-table-column prop="name" label="分类名称" min-width="180">
            <template #default="{ row }">
              <span class="cell-title">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="120">
            <template #default="{ row }">
              <span class="code-pill">{{ row.sortOrder }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag class="status-tag" :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click="openCategoryEdit(row)">编辑</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑资料' : '新增资料'" width="860px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid three">
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="选择分类">
              <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型" prop="resourceType">
            <el-select v-model="form.resourceType">
              <el-option v-for="(label, value) in resourceText" :key="value" :label="label" :value="value" />
            </el-select>
          </el-form-item>
          <el-form-item label="老师">
            <el-select v-model="form.ownerTeacherId" clearable>
              <el-option v-for="teacher in teachers" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <div class="form-grid three">
          <el-form-item label="学习属性">
            <el-radio-group v-model="form.studyType">
              <el-radio-button label="REQUIRED">必学</el-radio-button>
              <el-radio-button label="OPTIONAL">选学</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="可见范围">
            <el-radio-group v-model="form.visibility">
              <el-radio-button label="CAMPUS">全校区</el-radio-button>
              <el-radio-button label="CLASS">指定班级</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已发布" value="PUBLISHED" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item v-if="form.visibility === 'CLASS'" label="可见班级">
          <el-select v-model="form.classIds" multiple collapse-tags collapse-tags-tooltip>
            <el-option v-for="item in classes" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <div class="form-grid three">
          <el-form-item label="资料文件" prop="fileId">
            <el-upload :http-request="uploadMaterialFile" :show-file-list="false">
              <el-button><UploadCloud :size="16" /> 上传文件</el-button>
            </el-upload>
            <span class="upload-hint">{{ form.fileId ? `文件ID ${form.fileId}` : '未上传' }}</span>
          </el-form-item>
          <el-form-item label="封面图">
            <el-upload :http-request="uploadCoverFile" :show-file-list="false">
              <el-button>上传封面</el-button>
            </el-upload>
            <span class="upload-hint">{{ form.coverFileId ? `文件ID ${form.coverFileId}` : '可选' }}</span>
          </el-form-item>
          <el-form-item label="下载权限">
            <el-switch v-model="form.allowDownload" active-text="允许下载" inactive-text="仅预览" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" :title="editingCategoryId ? '编辑分类' : '新增分类'" width="520px">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-position="top">
        <el-form-item label="分类名称" prop="name"><el-input v-model="categoryForm.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="categoryForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="categoryForm.status">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.heading-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-hint {
  color: var(--muted-soft);
  font-size: 12px;
  margin-left: 10px;
}

@media (max-width: 820px) {
  .heading-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
