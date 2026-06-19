<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { apiBaseUrl } from '@/api/http';
import { createCourseRegistration, getPublicCourseDetail, payCourseOrder } from '@/api/course';
import { miniappLogin } from '@/api/miniapp';
import { useAuthStore } from '@/stores/auth';
import type { CourseRegistrationResult, MiniappLoginResponse, PublicCourseDetail } from '@/types/api';

interface PhoneNumberEvent {
  detail?: {
    code?: string;
    errMsg?: string;
  };
}

const auth = useAuthStore();
const loading = ref(false);
const submitting = ref(false);
const courseId = ref<number>();
const campusId = ref<number>();
const detail = ref<PublicCourseDetail>();
const contactPhone = ref('13900000999');
const mockLoginVisible = import.meta.env.DEV || import.meta.env.VITE_MINIAPP_MOCK_LOGIN === 'true';

const form = reactive({
  preferredClassId: undefined as number | undefined,
  applicantName: '',
  childName: '',
  childAge: undefined as number | undefined,
  childGrade: '',
  note: '',
});

const selectedClass = computed(() => detail.value?.classes.find((item) => item.id === form.preferredClassId));

onLoad((query) => {
  courseId.value = Number(query?.id);
  campusId.value = Number(query?.campusId);
  auth.hydrate();
  loadDetail();
});

async function loadDetail() {
  if (!courseId.value || !campusId.value) return;
  loading.value = true;
  try {
    detail.value = await getPublicCourseDetail(courseId.value, campusId.value);
    form.preferredClassId = detail.value.classes[0]?.id;
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function resolveMediaUrl(url?: string) {
  if (!url) return '';
  if (/^https?:\/\//.test(url)) return url;
  return `${apiBaseUrl()}${url.startsWith('/') ? url : `/${url}`}`;
}

function getLoginCode() {
  return new Promise<string>((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (result) => {
        if (result.code) {
          resolve(result.code);
          return;
        }
        reject(new Error('未获取到微信登录凭证'));
      },
      fail: () => reject(new Error('微信登录凭证获取失败')),
    });
  });
}

function applyGuardianLogin(response: MiniappLoginResponse) {
  auth.applyLogin(response);
}

async function ensureGuardianLogin(event?: PhoneNumberEvent) {
  auth.hydrate();
  if (auth.isLoggedIn) {
    if (auth.selectedIdentity?.identityType === 'GUARDIAN') return true;
    const guardian = auth.identities.find((item) => item.identityType === 'GUARDIAN');
    if (guardian) {
      auth.setSelectedIdentity(guardian);
      return true;
    }
    uni.showToast({ title: '请使用家长身份报名', icon: 'none' });
    return false;
  }

  if (mockLoginVisible) {
    const phone = contactPhone.value.trim();
    if (!phone) {
      uni.showToast({ title: '请填写联系手机号', icon: 'none' });
      return false;
    }
    const response = await miniappLogin({
      loginCode: 'mock-code',
      loginIntent: 'COURSE_REGISTRATION',
      campusId: campusId.value,
      mockPhone: phone,
    });
    applyGuardianLogin(response);
    return true;
  }

  const detail = event?.detail || {};
  if (detail.errMsg && !detail.errMsg.includes(':ok')) {
    uni.showToast({ title: '需要授权手机号后才能报名', icon: 'none' });
    return false;
  }
  if (!detail.code) {
    uni.showToast({ title: '未获取到手机号授权码', icon: 'none' });
    return false;
  }
  const loginCode = await getLoginCode();
  const response = await miniappLogin({
    loginCode,
    phoneCode: detail.code,
    loginIntent: 'COURSE_REGISTRATION',
    campusId: campusId.value,
  });
  applyGuardianLogin(response);
  return true;
}

function confirmPay() {
  return new Promise<boolean>((resolve) => {
    uni.showModal({
      title: '确认支付',
      content: '本地环境将模拟完成支付，支付后校区可审核分班。',
      confirmText: '支付',
      cancelText: '稍后',
      success: (result) => resolve(result.confirm),
      fail: () => resolve(false),
    });
  });
}

async function submitRegistration(event?: PhoneNumberEvent) {
  if (submitting.value || !detail.value) return;
  if (!form.childName.trim()) {
    uni.showToast({ title: '请填写孩子姓名', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    const ready = await ensureGuardianLogin(event);
    if (!ready) return;
    const result = await createCourseRegistration({
      courseId: detail.value.id,
      preferredClassId: form.preferredClassId,
      applicantName: form.applicantName.trim(),
      childName: form.childName.trim(),
      childAge: form.childAge,
      childGrade: form.childGrade.trim(),
      note: form.note.trim(),
    });
    await handleRegistrationResult(result);
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '报名失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

async function handleRegistrationResult(result: CourseRegistrationResult) {
  if (result.payRequired && result.orderId) {
    const confirmed = await confirmPay();
    if (confirmed) {
      await payCourseOrder(result.orderId);
      uni.showToast({ title: '已支付，等待审核', icon: 'success' });
    } else {
      uni.showToast({ title: '报名已提交，待支付', icon: 'none' });
    }
  } else {
    uni.showToast({ title: '报名已提交', icon: 'success' });
  }
  setTimeout(() => {
    uni.navigateTo({ url: '/pages/student/registrations' });
  }, 650);
}
</script>

<template>
  <view class="page">
    <view v-if="detail" class="content">
      <image v-if="detail.coverUrl" class="hero" :src="resolveMediaUrl(detail.coverUrl)" mode="aspectFill" />
      <view v-else class="hero hero-fallback">
        <text>{{ detail.courseSystem }}</text>
      </view>

      <view class="intro">
        <text class="system">{{ detail.courseSystem }}</text>
        <text class="title">{{ detail.name }}</text>
        <text class="summary">{{ detail.publicSummary || detail.description || '适合长期系统学习的校区课程' }}</text>
        <view class="meta-row">
          <text>{{ detail.gradeScope || '适龄可咨询' }}</text>
          <text>{{ detail.totalHours }}课时</text>
          <text>￥{{ detail.packagePrice }}</text>
        </view>
      </view>

      <view v-if="detail.publicDetail" class="section">
        <text class="section-title">课程介绍</text>
        <text class="paragraph">{{ detail.publicDetail }}</text>
      </view>

      <view v-if="detail.teachers.length" class="section">
        <text class="section-title">授课老师</text>
        <view class="teacher-list">
          <view v-for="teacher in detail.teachers" :key="teacher.id" class="teacher-card">
            <image v-if="teacher.avatarUrl" class="teacher-avatar" :src="resolveMediaUrl(teacher.avatarUrl)" mode="aspectFill" />
            <view v-else class="teacher-avatar teacher-fallback">{{ teacher.name.slice(0, 1) }}</view>
            <view class="teacher-copy">
              <text class="teacher-name">{{ teacher.name }}</text>
              <text class="teacher-title">{{ teacher.title || teacher.roleName || '授课老师' }}</text>
              <text v-if="teacher.intro" class="teacher-intro">{{ teacher.intro }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-title">选择班级</text>
        <view v-if="detail.classes.length" class="class-list">
          <view
            v-for="klass in detail.classes"
            :key="klass.id"
            class="class-chip"
            :class="{ active: form.preferredClassId === klass.id }"
            @tap="form.preferredClassId = klass.id"
          >
            <text class="class-name">{{ klass.name }}</text>
            <text class="class-meta">{{ klass.currentStudents }}/{{ klass.maxStudents }} · {{ klass.classroom || '待定教室' }}</text>
          </view>
        </view>
        <text v-else class="paragraph">当前课程暂无可选班级，报名后校区会联系安排。</text>
      </view>

      <view class="section form-section">
        <text class="section-title">报名信息</text>
        <view v-if="mockLoginVisible && !auth.isLoggedIn" class="field">
          <text class="field-label">联系手机号</text>
          <input v-model="contactPhone" type="number" maxlength="20" placeholder="用于创建家长报名身份" />
        </view>
        <view class="field">
          <text class="field-label">家长称呼</text>
          <input v-model="form.applicantName" placeholder="可选" />
        </view>
        <view class="field">
          <text class="field-label">孩子姓名</text>
          <input v-model="form.childName" placeholder="请输入孩子姓名" />
        </view>
        <view class="form-grid">
          <view class="field">
            <text class="field-label">年龄</text>
            <input v-model.number="form.childAge" type="digit" placeholder="可选" />
          </view>
          <view class="field">
            <text class="field-label">年级</text>
            <input v-model="form.childGrade" placeholder="如三年级" />
          </view>
        </view>
        <view class="field">
          <text class="field-label">备注</text>
          <textarea v-model="form.note" auto-height placeholder="学习基础、可上课时间等" />
        </view>
      </view>

      <view class="bottom-spacer" />
      <view class="bottom-bar">
        <view class="chosen">
          <text>{{ selectedClass?.name || '报名后安排班级' }}</text>
          <text>￥{{ detail.packagePrice }}</text>
        </view>
        <button
          v-if="!auth.isLoggedIn && !mockLoginVisible"
          class="submit-button"
          open-type="getPhoneNumber"
          :disabled="submitting"
          @getphonenumber="submitRegistration"
        >
          {{ submitting ? '提交中' : '授权并报名' }}
        </button>
        <button v-else class="submit-button" :disabled="submitting" @tap="submitRegistration">
          {{ submitting ? '提交中' : '立即报名' }}
        </button>
      </view>
    </view>

    <view v-else class="empty">{{ loading ? '加载中...' : '课程不存在' }}</view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #f4f5f1;
  color: #202823;
}

.content {
  padding-bottom: 24rpx;
}

.hero {
  width: 100%;
  height: 360rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-fallback {
  background:
    linear-gradient(135deg, rgba(35, 121, 103, 0.92), rgba(60, 102, 142, 0.88)),
    #237967;
  color: #fff;
  font-size: 54rpx;
  font-weight: 900;
}

.intro,
.section {
  margin: 24rpx 32rpx 0;
  padding: 28rpx;
  border: 1rpx solid rgba(222, 223, 216, 0.78);
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(32, 40, 35, 0.045);
}

.system,
.section-title,
.field-label {
  display: block;
  color: #237967;
  font-size: 22rpx;
  font-weight: 900;
}

.title {
  display: block;
  margin-top: 10rpx;
  color: #202823;
  font-size: 46rpx;
  font-weight: 900;
  line-height: 1.12;
}

.summary,
.paragraph,
.teacher-title,
.teacher-intro,
.class-meta,
.empty {
  margin-top: 14rpx;
  color: #6c746e;
  font-size: 24rpx;
  line-height: 1.55;
}

.meta-row {
  margin-top: 18rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx 22rpx;
  color: #202823;
  font-size: 24rpx;
  font-weight: 900;
}

.section-title {
  color: #202823;
  font-size: 30rpx;
}

.teacher-list,
.class-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.teacher-card {
  display: flex;
  gap: 18rpx;
  padding: 20rpx;
  border-radius: 20rpx;
  background: #f8f8f4;
}

.teacher-avatar {
  width: 88rpx;
  height: 88rpx;
  flex-shrink: 0;
  border-radius: 18rpx;
}

.teacher-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #202823;
  color: #c99a45;
  font-size: 34rpx;
  font-weight: 900;
}

.teacher-copy {
  flex: 1;
  min-width: 0;
}

.teacher-name {
  display: block;
  color: #202823;
  font-size: 27rpx;
  font-weight: 900;
}

.teacher-title,
.teacher-intro {
  display: block;
}

.class-chip {
  padding: 22rpx;
  border: 1rpx solid #dedfd8;
  border-radius: 20rpx;
  background: #f8f8f4;
}

.class-chip.active {
  border-color: #237967;
  background: #eef6f2;
}

.class-name {
  display: block;
  color: #202823;
  font-size: 27rpx;
  font-weight: 900;
}

.form-section {
  margin-bottom: 28rpx;
}

.field {
  margin-top: 20rpx;
}

.field input,
.field textarea {
  width: 100%;
  min-height: 78rpx;
  box-sizing: border-box;
  margin-top: 12rpx;
  padding: 0 20rpx;
  border-radius: 18rpx;
  background: #f8f8f4;
  color: #202823;
  font-size: 26rpx;
}

.field textarea {
  min-height: 120rpx;
  padding-top: 18rpx;
  padding-bottom: 18rpx;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.bottom-spacer {
  height: 150rpx;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  padding: 18rpx 32rpx calc(18rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  border-top: 1rpx solid rgba(222, 223, 216, 0.86);
  background: rgba(255, 255, 255, 0.96);
}

.chosen {
  flex: 1;
  min-width: 0;
}

.chosen text {
  display: block;
  color: #202823;
  font-size: 23rpx;
  font-weight: 900;
}

.chosen text + text {
  margin-top: 6rpx;
  color: #b77624;
}

.submit-button {
  width: 244rpx;
  height: 82rpx;
  border-radius: 18rpx;
  background: #237967;
  color: #fff;
  font-size: 27rpx;
  font-weight: 900;
}

.submit-button[disabled] {
  opacity: 0.62;
}

.empty {
  padding-top: 120rpx;
  text-align: center;
}
</style>
