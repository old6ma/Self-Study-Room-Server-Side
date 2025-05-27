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
        <div>自习室ID：{{ item.room_id }}，座位ID：{{ item.seat_id }}</div>
        <div>时间：{{ formatTime(item.start_time) }} - {{ formatTime(item.end_time) }}</div>
        <button @click="cancelBooking(item.booking_id)">取消预约</button>
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
import { API_BASE } from '../api';

const history = ref([]);
const router = useRouter();

// 新增：搜索、分页、导出相关状态
const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

// 获取预约历史（真实接口）
const fetchHistory = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/booking/history?search=${encodeURIComponent(searchKeyword.value)}&page=${currentPage.value}&size=${pageSize.value}`, {
      headers: { Authorization: token ? `Bearer ${token}` : '' }
    });
    const data = await res.json();
    if (data && data.data) {
      history.value = data.data.records || [];
      total.value = data.data.total || 0;
    }
  } catch (e) {
    // eslint-disable-next-line
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// 导出预约历史为 CSV
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

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = '';
  currentPage.value = 1;
  fetchHistory();
};

// 分页切换
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchHistory();
};

// 取消预约（真实接口）
const cancelBooking = async (bookingId) => {
  if (!confirm('确定要取消该预约吗？')) return;
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/booking/${bookingId}`, {
      method: 'DELETE',
      headers: { Authorization: token ? `Bearer ${token}` : '' }
    });
    const data = await res.json();
    if (data.code === 0) {
      fetchHistory();
      alert('取消成功');
    } else {
      alert(data.msg || '取消失败');
    }
  } catch (e) {
    // eslint-disable-next-line
    console.error(e);
    alert('取消失败');
  }
};

// 再次预约
const rebook = (seatId) => {
  router.push({ path: '/room-detail', query: { seatId } });
};

const goBack = () => router.push('/');
const formatTime = (t) => {
  if (!t) return '';
  const d = new Date(t);
  return d.toLocaleString();
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
