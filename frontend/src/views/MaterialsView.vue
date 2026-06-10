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

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
  status: '',
  auditStatus: '',
  dateRange: [] as string[],
});
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

const dispositionFileName = (disposition?: string) => {
  if (!disposition) return '';
  const encodedMatch = /filename\*=UTF-8''([^;]+)/i.exec(disposition);
  if (encodedMatch?.[1]) {
    try {
      return decodeURIComponent(encodedMatch[1]);
    } catch {
      return encodedMatch[1];
    }
  }
  const plainMatch = /filename="?([^";]+)"?/i.exec(disposition);
  return plainMatch?.[1] || '';
};

const previewMaterial = async (row: MaterialRecord) => {
  try {
    const response = await materialApi.previewBlob(row.id);
    const url = URL.createObjectURL(response.data);
    const opened = window.open(url, '_blank');
    if (!opened) {
      URL.revokeObjectURL(url);
      ElMessage.warning('浏览器阻止了新窗口，请允许弹窗后重试');
      return;
    }
    opened.opener = null;
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
  } catch {
    ElMessage.warning('无法预览，可下载查看');
  }
};

const downloadMaterial = async (row: MaterialRecord) => {
  const response = await materialApi.downloadBlob(row.id);
  const disposition = String(response.headers['content-disposition'] || '');
  const fileName = dispositionFileName(disposition) || row.fileName || `${row.title || 'material'}`;
  const url = URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
};

const approveMaterial = async (row: MaterialRecord) => {
  await ElMessageBox.confirm(`确认通过「${row.title}」的资料审核？`, '审核通过', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success',
  });
  await materialApi.audit(row.id, { auditStatus: 'APPROVED' });
  ElMessage.success('资料已通过审核');
  await loadData();
};

const rejectMaterial = async (row: MaterialRecord) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', `驳回「${row.title}」`, {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '填写给老师看的驳回原因',
      inputValidator: (value) => Boolean(value?.trim()) || '请填写驳回原因',
    });
    await materialApi.audit(row.id, { auditStatus: 'REJECTED', rejectedReason: String(value || '').trim() });
    ElMessage.success('资料已驳回');
    await loadData();
  } catch {
    // User cancelled the prompt.
  }
};

const handleMaterialCommand = async (command: unknown, row: MaterialRecord) => {
  const action = String(command);
  if (action === 'edit') {
    await openEdit(row);
    return;
  }
  if (action === 'toggle-status') {
    await toggleStatus(row);
  }
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
const auditText: Record<string, string> = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' };
const auditType = (value: string) => {
  if (value === 'APPROVED') return 'success';
  if (value === 'REJECTED') return 'danger';
  return 'warning';
};
const formatFileSize = (size?: number) => {
  if (!size) return '';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
};

const resetFilters = () => {
  query.keyword = '';
  query.categoryId = undefined;
  query.status = '';
  query.auditStatus = '';
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
          <el-select v-model="query.status" clearable placeholder="发布状态">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
          </el-select>
          <el-select v-model="query.auditStatus" clearable placeholder="审核状态">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
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
                  <span class="cell-subtitle file-meta">
                    <button type="button" class="file-preview-link" @click.stop="previewMaterial(row)">
                      {{ row.fileName || '未命名文件' }}
                    </button>
                    <span v-if="row.fileSize"> · {{ formatFileSize(row.fileSize) }}</span>
                  </span>
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
          <el-table-column prop="allowDownload" label="学生下载" width="100">
            <template #default="{ row }">
              <el-tag class="status-tag" :type="row.allowDownload ? 'success' : 'info'" effect="plain">{{ row.allowDownload ? '允许' : '禁止' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditStatus" label="审核" width="100">
            <template #default="{ row }">
              <el-tag class="status-tag" :type="auditType(row.auditStatus)" effect="plain">{{ auditText[row.auditStatus] || row.auditStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="发布状态" width="105">
            <template #default="{ row }">
              <el-tag class="status-tag" :type="statusType(row.status)" effect="plain">{{ statusText[row.status] || row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" class="table-action" @click="downloadMaterial(row)">下载</el-button>
                <template v-if="row.auditStatus === 'PENDING'">
                  <el-button link type="success" class="table-action" @click="approveMaterial(row)">通过</el-button>
                  <el-button link type="danger" class="table-action" @click="rejectMaterial(row)">驳回</el-button>
                </template>
                <el-dropdown trigger="click" @command="handleMaterialCommand($event, row)">
                  <el-button link type="primary" class="table-action table-action-muted">更多</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="toggle-status">
                        {{ row.status === 'PUBLISHED' ? '下架' : '发布' }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
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

.file-meta {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
}

.file-preview-link {
  appearance: none;
  border: 0;
  background: transparent;
  color: var(--teal-hover);
  cursor: pointer;
  font: inherit;
  font-weight: 500;
  padding: 0;
  text-align: left;
}

.file-preview-link:hover {
  color: var(--primary);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.table-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  line-height: 1;
}

.table-actions :deep(.el-button) {
  margin-left: 0;
}

.table-action {
  font-size: 13px;
  font-weight: 500;
  min-height: 24px;
  padding: 0;
}

.table-action-muted {
  color: var(--muted);
}

@media (max-width: 820px) {
  .heading-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
