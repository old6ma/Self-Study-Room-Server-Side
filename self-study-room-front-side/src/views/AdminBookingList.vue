<template>
  <div class="admin-booking-list-container">
    <h2>预约管理</h2>
    <div class="search-bar">
      <input v-model="searchText" placeholder="搜索用户ID/座位编号" />
      <button @click="searchBookings">搜索</button>
      <button @click="resetSearch">重置</button>
      <button @click="exportBookings">导出预约记录</button>
    </div>
    <ul v-if="pagedBookings.length">
      <li v-for="item in pagedBookings" :key="item.booking_id">
        <div>自习室ID：{{ item.room_id }}，座位编号：{{ item.seat_id }}</div>
        <div>用户ID：{{ item.user_id }}</div>
        <div>时间：{{ formatTime(item.start_time) }} - {{ formatTime(item.end_time) }}</div>
        <button @click="deleteBooking(item.booking_id)">删除预约</button>
      </li>
    </ul>
    <div v-else>暂无预约记录</div>
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1">上一页</button>
      <span>第{{ page }}/{{ totalPages }}页</span>
      <button @click="nextPage" :disabled="page === totalPages">下一页</button>
    </div>
    <button @click="goBack">返回</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { adminApi } from '../api';

const bookings = ref([]);
const searchText = ref('');
const page = ref(1);
const pageSize = 10;
const filteredBookings = ref([]);
const pagedBookings = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredBookings.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.max(1, Math.ceil(filteredBookings.value.length / pageSize)));
const router = useRouter();

const fetchBookings = async () => {
  try {
    const res = await adminApi.getAllBookings();
    bookings.value = res.bookings || res.data || res;
    resetSearch();
  } catch (e) { console.error(e); }
};

const deleteBooking = async (bookingId) => {
  if (!confirm('确定要删除该预约吗？')) return;
  try {
    await adminApi.deleteBooking(bookingId);
    fetchBookings();
  } catch (e) { console.error(e); }
};
const searchBookings = () => {
  page.value = 1;
  filteredBookings.value = bookings.value.filter(b => String(b.user_id).includes(searchText.value) || String(b.seat_id).includes(searchText.value));
};
const resetSearch = () => {
  searchText.value = '';
  filteredBookings.value = [...bookings.value];
  page.value = 1;
};
const prevPage = () => {
  if (page.value > 1) page.value--;
};
const nextPage = () => {
  if (page.value < totalPages.value) page.value++;
};
const exportBookings = () => {
  const csv = ['自习室ID,座位编号,用户ID,开始时间,结束时间'];
  filteredBookings.value.forEach(b => {
    csv.push(`${b.room_id},${b.seat_id},${b.user_id},${b.start_time},${b.end_time}`);
  });
  const blob = new Blob([csv.join('\n')], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '预约记录.csv';
  a.click();
  URL.revokeObjectURL(url);
};
const goBack = () => router.push('/admin');
const formatTime = (t) => {
  if (!t) return '';
  const d = new Date(Number(t));
  return d.toLocaleString('zh-CN', { hour12: false, timeZone: 'Asia/Shanghai' });
};
onMounted(fetchBookings);
</script>

<style scoped>
.admin-booking-list-container { max-width: 700px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 2em; }
.search-bar { margin-bottom: 1em; }
ul { list-style: none; padding: 0; }
li { border-bottom: 1px solid #eee; padding: 1em 0; }
button { margin-top: 0.5em; margin-right: 0.5em; }
.pagination { margin-top: 1em; }
</style>
