<template>
  <div class="history-container">
    <h2>我的预约历史</h2>
    <div class="search-container">
      <input v-model="searchKeyword" placeholder="搜索预约..." />
      <button @click="resetSearch">重置</button>
      <button @click="fetchHistory">搜索</button>
      <button @click="exportHistory">导出 CSV</button>
    </div>
    <ul v-if="history.length">
      <li v-for="item in history" :key="item.booking_id">
        <div>预约ID：{{ item.booking_id }}</div>
        <div>自习室ID：{{ item.room_id }}，座位ID：{{ item.seat_id }}</div>
        <div>时间：{{ formatTime(item.start_time) }} - {{ formatTime(item.end_time) }}</div>
        <div>状态：<span :style="{color: item.status==='CANCELLED' ? 'red' : 'green'}">{{ statusMap[item.status] || item.status }}</span></div>
        <button v-if="item.status==='ACTIVE'" @click="cancelBooking(item.booking_id)">取消预约</button>
        <button v-if="item.status==='ACTIVE'" @click="checkIn(item.seat_id)">签到</button>
        <button v-if="item.status==='ACTIVE'" @click="leaveSeat(item.seat_id)">暂离</button>
        <button v-if="item.status==='ACTIVE'" @click="releaseSeat(item.seat_id)">释放</button>
        <button @click="rebook(item.seat_id)">再次预约</button>
      </li>
    </ul>
    <div v-else>暂无预约记录</div>
    <div class="pagination-container">
      <button @click="handlePageChange(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
      <span>第 {{ currentPage }} 页</span>
      <button @click="handlePageChange(currentPage + 1)" :disabled="currentPage * pageSize >= total">下一页</button>
    </div>
    <button @click="goBack">返回</button>
    <div v-if="loading" class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { studentApi } from '../api';

const history = ref([]);
const router = useRouter();
const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

const statusMap = {
  'ACTIVE': '进行中',
  'CANCELLED': '已取消',
};

const fetchHistory = async () => {
  loading.value = true;
  try {
    const res = await studentApi.bookingHistory();
    history.value = res.history || [];
    total.value = history.value.length;
  } catch (e) {
    history.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const exportHistory = () => {
  if (!history.value.length) return;
  const header = '预约ID,自习室ID,座位ID,开始时间,结束时间\n';
  const rows = history.value.map(item => `${item.booking_id},${item.room_id},${item.seat_id},${formatTime(item.start_time)},${formatTime(item.end_time)}`).join('\n');
  const csv = header + rows;
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '预约历史.csv';
  a.click();
  URL.revokeObjectURL(url);
};

const resetSearch = () => {
  searchKeyword.value = '';
  currentPage.value = 1;
  fetchHistory();
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchHistory();
};

const cancelBooking = async (bookingId) => {
  if (!confirm('确定要取消该预约吗？')) return;
  try {
    await studentApi.cancelBooking(bookingId);
    fetchHistory();
    alert('取消成功');
  } catch (e) {
    alert(e.message || '取消失败');
  }
};

const rebook = (seatId) => {
  router.push({ path: '/room-detail', query: { seatId } });
};

const checkIn = async (seatId) => {
  try {
    await studentApi.checkin(seatId);
    alert('签到成功');
    fetchHistory();
  } catch (e) {
    alert(e.message || '签到失败');
  }
};
const leaveSeat = async (seatId) => {
  try {
    await studentApi.leaveSeat(seatId);
    alert('暂离成功');
    fetchHistory();
  } catch (e) {
    alert(e.message || '暂离失败');
  }
};
const releaseSeat = async (seatId) => {
  try {
    await studentApi.releaseSeat(seatId);
    alert('释放成功');
    fetchHistory();
  } catch (e) {
    alert(e.message || '释放失败');
  }
};

const goBack = () => router.push('/');
const formatTime = (t) => {
  if (!t) return '';
  // 注意：后端返回的时间戳已是东八区毫秒数，直接用本地Date显示，不要再加8小时！
  const d = new Date(Number(t));
  return d.toLocaleString('zh-CN', { hour12: false, timeZone: 'Asia/Shanghai' });
};

onMounted(fetchHistory);
</script>

<style scoped>
.history-container { max-width: 600px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 2em; }
ul { list-style: none; padding: 0; }
li { border-bottom: 1px solid #eee; padding: 1em 0; }
button { margin-top: 0.5em; margin-right: 0.5em; }
.search-container { margin-bottom: 1em; }
.pagination-container { margin-top: 1em; text-align: center; }
.loading { text-align: center; margin-top: 1em; }
</style>
