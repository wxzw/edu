<script setup lang="ts">
import { RotateCcw, Search } from 'lucide-vue-next';

defineProps<{
  loading?: boolean;
  showReset?: boolean;
}>();

const emit = defineEmits<{
  search: [];
  reset: [];
}>();
</script>

<template>
  <div class="filter-bar">
    <div class="filter-fields">
      <slot name="filters" />
    </div>
    <div class="filter-actions">
      <el-button v-if="showReset" plain :disabled="loading" @click="emit('reset')">
        <RotateCcw :size="14" />
        <span>重置</span>
      </el-button>
      <el-button type="primary" :loading="loading" @click="emit('search')">
        <Search :size="15" />
        <span>查询</span>
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  margin-bottom: 12px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(249, 251, 249, 0.94)),
    var(--surface-2);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82);
}

.filter-fields {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  min-width: 0;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  align-items: center;
}

.filter-actions .el-button {
  min-width: 82px;
}

@media (max-width: 820px) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-actions {
    width: 100%;
  }
  .filter-actions .el-button {
    flex: 1;
  }
}
</style>
