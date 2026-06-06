<script setup lang="ts">
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { requireIdentity } from '@/utils/auth-flow';
import TeacherEmptyState from '@/components/TeacherEmptyState.vue';
import TeacherHeroCard from '@/components/TeacherHeroCard.vue';

interface TeacherMaterialItem {
  id: number;
  title: string;
  description?: string;
}

const materials = ref<TeacherMaterialItem[]>([]);
const loading = ref(false);

async function fetchMaterials() {
  loading.value = true;
  try {
    materials.value = [];
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

onShow(() => {
  if (!requireIdentity('TEACHER')) return;
  fetchMaterials();
});

function toUpload() {
  uni.navigateTo({ url: '/pages/teacher/material/upload' });
}
</script>

<template>
  <view class="page">
    <TeacherHeroCard eyebrow="Materials" title="教学资料" subtitle="沉淀讲义、音频和课堂素材">
      <template #action>
        <button class="upload-button" @tap="toUpload">上传</button>
      </template>
    </TeacherHeroCard>

    <view class="list">
      <view
        v-for="item in materials"
        :key="item.id"
        class="material-card"
      >
        <text class="material-title">{{ item.title }}</text>
        <text class="material-desc">{{ item.description || '暂无说明' }}</text>
      </view>

      <TeacherEmptyState
        v-if="!materials.length"
        :title="loading ? '正在加载资料' : '资料库暂未开放'"
        description="首版老师端先展示入口，资料维护可以继续通过后台完成。"
        action-text="填写资料信息"
        @action="toUpload"
      />
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 34rpx 28rpx 70rpx;
  box-sizing: border-box;
  background: #f4efe6;
  color: #17211d;
}

button {
  margin: 0;
  padding: 0;
}

button::after {
  border: 0;
}

.upload-button {
  width: 116rpx;
  height: 62rpx;
  border-radius: 999rpx;
  background: rgba(255, 252, 245, 0.16);
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 62rpx;
}

.list {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.material-card {
  padding: 26rpx;
  border-radius: 30rpx;
  background: #fffcf5;
  box-shadow: 0 10rpx 28rpx rgba(54, 43, 30, 0.04);
}

.material-title,
.material-desc {
  display: block;
}

.material-title {
  color: #17211d;
  font-size: 31rpx;
  font-weight: 900;
}

.material-desc {
  margin-top: 8rpx;
  color: #858982;
  font-size: 23rpx;
  line-height: 1.45;
}
</style>
