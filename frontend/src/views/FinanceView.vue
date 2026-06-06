<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { orderApi, paymentApi } from '@/api/admin';
import type { OrderRecord, PaymentRecord } from '@/types/admin';
import SearchFilterBar from '@/components/SearchFilterBar.vue';

const activeTab = ref<'orders' | 'payments'>('orders');
const loading = ref(false);
const orders = ref<OrderRecord[]>([]);
const payments = ref<PaymentRecord[]>([]);
const orderTotal = ref(0);
const paymentTotal = ref(0);
const orderQuery = reactive({ pageNo: 1, pageSize: 10, orderType: '', payStatus: '', dateRange: [] as string[] });
const paymentQuery = reactive({ pageNo: 1, pageSize: 10, status: '', dateRange: [] as string[] });

const loadOrders = async () => {
  loading.value = true;
  try {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const params: any = { ...orderQuery };
    if (orderQuery.dateRange && orderQuery.dateRange.length === 2) {
      params.startDate = orderQuery.dateRange[0];
      params.endDate = orderQuery.dateRange[1];
    }
    delete params.dateRange;
    const page = await orderApi.page(params);
    orders.value = page.records;
    orderTotal.value = page.total;
  } finally {
    loading.value = false;
  }
};

const loadPayments = async () => {
  loading.value = true;
  try {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const params: any = { ...paymentQuery };
    if (paymentQuery.dateRange && paymentQuery.dateRange.length === 2) {
      params.startDate = paymentQuery.dateRange[0];
      params.endDate = paymentQuery.dateRange[1];
    }
    delete params.dateRange;
    const page = await paymentApi.page(params);
    payments.value = page.records;
    paymentTotal.value = page.total;
  } finally {
    loading.value = false;
  }
};

const resetOrderFilters = () => {
  orderQuery.orderType = '';
  orderQuery.payStatus = '';
  orderQuery.dateRange = [];
  orderQuery.pageNo = 1;
  loadOrders();
};

const resetPaymentFilters = () => {
  paymentQuery.status = '';
  paymentQuery.dateRange = [];
  paymentQuery.pageNo = 1;
  loadPayments();
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
      <SearchFilterBar :loading="loading" show-reset @search="loadOrders" @reset="resetOrderFilters">
        <template #filters>
          <el-select v-model="orderQuery.orderType" clearable placeholder="订单类型">
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="课程" value="COURSE" />
          </el-select>
          <el-select v-model="orderQuery.payStatus" clearable placeholder="支付状态">
            <el-option label="未支付" value="UNPAID" />
            <el-option label="已支付" value="PAID" />
          </el-select>
          <el-date-picker
            v-model="orderQuery.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="创建开始"
            end-placeholder="创建结束"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </template>
      </SearchFilterBar>
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
      <SearchFilterBar :loading="loading" show-reset @search="loadPayments" @reset="resetPaymentFilters">
        <template #filters>
          <el-select v-model="paymentQuery.status" clearable placeholder="流水状态">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
          <el-date-picker
            v-model="paymentQuery.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="支付开始"
            end-placeholder="支付结束"
            value-format="YYYY-MM-DD"
            unlink-panels
          />
        </template>
      </SearchFilterBar>
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
