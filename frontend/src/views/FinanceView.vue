<script setup lang="ts">
import { Search } from 'lucide-vue-next';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { orderApi, paymentApi } from '@/api/admin';
import type { OrderRecord, PaymentRecord } from '@/types/admin';

const activeTab = ref<'orders' | 'payments'>('orders');
const loading = ref(false);
const orders = ref<OrderRecord[]>([]);
const payments = ref<PaymentRecord[]>([]);
const orderTotal = ref(0);
const paymentTotal = ref(0);
const orderQuery = reactive({ pageNo: 1, pageSize: 10, orderType: '', payStatus: '' });
const paymentQuery = reactive({ pageNo: 1, pageSize: 10, status: '' });

const loadOrders = async () => {
  loading.value = true;
  try {
    const page = await orderApi.page(orderQuery);
    orders.value = page.records;
    orderTotal.value = page.total;
  } finally {
    loading.value = false;
  }
};

const loadPayments = async () => {
  loading.value = true;
  try {
    const page = await paymentApi.page(paymentQuery);
    payments.value = page.records;
    paymentTotal.value = page.total;
  } finally {
    loading.value = false;
  }
};

const loadData = () => activeTab.value === 'orders' ? loadOrders() : loadPayments();
const formatTime = (value?: string) => value?.slice(0, 16).replace('T', ' ') || '-';

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
        <p class="eyebrow">Finance</p>
        <h2>订单与支付</h2>
      </div>
      <el-segmented v-model="activeTab" :options="[{ label: '订单', value: 'orders' }, { label: '支付流水', value: 'payments' }]" @change="loadData" />
    </section>

    <section v-if="activeTab === 'orders'" class="table-surface">
      <div class="table-toolbar">
        <el-select v-model="orderQuery.orderType" clearable placeholder="订单类型">
          <el-option label="活动" value="ACTIVITY" />
          <el-option label="课程" value="COURSE" />
        </el-select>
        <el-select v-model="orderQuery.payStatus" clearable placeholder="支付状态">
          <el-option label="未支付" value="UNPAID" />
          <el-option label="已支付" value="PAID" />
        </el-select>
        <el-button @click="loadOrders"><Search :size="16" /> 查询</el-button>
      </div>
      <el-table v-loading="loading" :data="orders" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="studentName" label="学生" width="110" />
        <el-table-column prop="activityTitle" label="活动/课程" min-width="180" />
        <el-table-column prop="totalAmount" label="金额" width="100" />
        <el-table-column prop="payStatus" label="支付" width="100" />
        <el-table-column prop="payChannel" label="渠道" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination v-model:current-page="orderQuery.pageNo" v-model:page-size="orderQuery.pageSize" :total="orderTotal" layout="total, sizes, prev, pager, next" @change="loadOrders" />
      </div>
    </section>

    <section v-else class="table-surface">
      <div class="table-toolbar">
        <el-select v-model="paymentQuery.status" clearable placeholder="流水状态">
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
        </el-select>
        <el-button @click="loadPayments"><Search :size="16" /> 查询</el-button>
      </div>
      <el-table v-loading="loading" :data="payments" stripe>
        <el-table-column prop="paymentNo" label="支付流水号" min-width="170" />
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="payChannel" label="渠道" width="100" />
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column prop="transactionNo" label="交易号" min-width="170" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="paidAt" label="支付时间" width="170">
          <template #default="{ row }">{{ formatTime(row.paidAt) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination v-model:current-page="paymentQuery.pageNo" v-model:page-size="paymentQuery.pageSize" :total="paymentTotal" layout="total, sizes, prev, pager, next" @change="loadPayments" />
      </div>
    </section>
  </div>
</template>
