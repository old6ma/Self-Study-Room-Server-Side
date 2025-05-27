<template>
  <div class="room-detail-container">
    <h2>{{ room?.name }}</h2>
    <div>位置：{{ room?.location }}</div>
    <div>容量：{{ room?.capacity }}</div>
    <div>开放时间：{{ formatTime(room?.open_time) }} - {{ formatTime(room?.close_time) }}</div>
    <div>状态：{{ room?.status }}</div>
    <div class="search-bar">
      <input v-model="searchText" placeholder="搜索座位名称" />
      <button @click="searchSeats">搜索</button>
      <button @click="resetSearch">重置</button>
      <button @click="exportSeats">导出座位列表</button>
    </div>
    <h3>座位列表</h3>
    <div v-if="pagedSeats.length === 0">暂无座位</div>
    <ul class="seat-list">
      <li v-for="seat in pagedSeats" :key="seat.seat_id">
        <div class="seat-title">{{ seat.name }}</div>
        <div>插座：{{ seat.has_socket ? '有' : '无' }}</div>
        <div>状态：{{ seat.status }}</div>
        <div>预约情况：
          <ul>
            <li v-for="order in seat.ordering_list" :key="order.start_time">
              {{ formatTime(order.start_time) }} - {{ formatTime(order.end_time) }}
            </li>
          </ul>
        </div>
        <div class="seat-actions">
          <button @click="bookSeat()" :disabled="seat.status !== 'AVAILABLE'">预约</button>
          <button @click="occupyNow()" :disabled="seat.status !== 'AVAILABLE'">临时抢占</button>
          <button @click="checkIn()" :disabled="seat.status !== 'OCCUPIED'">签到</button>
          <button @click="leaveSeat()" :disabled="seat.status !== 'OCCUPIED'">暂离</button>
          <button @click="releaseSeat()" :disabled="seat.status !== 'OCCUPIED'">释放</button>
        </div>
      </li>
    </ul>
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
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const room = ref(null);
const seats = ref([
  { seat_id: 101, name: 'A1', has_socket: true, status: 'AVAILABLE', ordering_list: [] },
  { seat_id: 102, name: 'A2', has_socket: false, status: 'OCCUPIED', ordering_list: [{ start_time: '2025-05-27T09:00:00Z', end_time: '2025-05-27T11:00:00Z' }] },
  { seat_id: 103, name: 'B1', has_socket: true, status: 'AVAILABLE', ordering_list: [] },
  { seat_id: 104, name: 'B2', has_socket: false, status: 'AVAILABLE', ordering_list: [] }
]);
const searchText = ref('');
const page = ref(1);
const pageSize = 2;
const filteredSeats = ref([...seats.value]);
const pagedSeats = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredSeats.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(filteredSeats.value.length / pageSize));

const fetchRoom = async () => {
  // 硬编码演示数据
  const allRooms = [
    { room_id: 1, name: '一号自习室', location: '教学楼A-201', capacity: 30, open_time: '2025-05-07T08:00:00Z', close_time: '2025-05-07T22:00:00Z', status: 'AVAILABLE' },
    { room_id: 2, name: '二号自习室', location: '教学楼B-101', capacity: 50, open_time: '2025-05-07T07:00:00Z', close_time: '2025-05-07T23:00:00Z', status: 'AVAILABLE' },
    { room_id: 3, name: '三号自习室', location: '教学楼C-301', capacity: 20, open_time: '2025-05-07T09:00:00Z', close_time: '2025-05-07T20:00:00Z', status: 'AVAILABLE' }
  ];
  room.value = allRooms.find(r => r.room_id == route.params.roomId);
};
const searchSeats = () => {
  page.value = 1;
  filteredSeats.value = seats.value.filter(s => s.name.includes(searchText.value));
};
const resetSearch = () => {
  searchText.value = '';
  filteredSeats.value = [...seats.value];
  page.value = 1;
};
const prevPage = () => {
  if (page.value > 1) page.value--;
};
const nextPage = () => {
  if (page.value < totalPages.value) page.value++;
};
const exportSeats = () => {
  const csv = ['座位名称,有插座,状态'];
  filteredSeats.value.forEach(s => {
    csv.push(`${s.name},${s.has_socket ? '有' : '无'},${s.status}`);
  });
  const blob = new Blob([csv.join('\n')], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '座位列表.csv';
  a.click();
  URL.revokeObjectURL(url);
};
const bookSeat = () => {
  alert('预约成功（演示）');
  resetSearch();
};
const occupyNow = () => {
  alert('临时抢占成功（演示）');
  resetSearch();
};
const checkIn = () => {
  alert('签到成功（演示）');
  resetSearch();
};
const leaveSeat = () => {
  alert('暂离成功（演示）');
  resetSearch();
};
const releaseSeat = () => {
  alert('释放成功（演示）');
  resetSearch();
};
const goBack = () => router.push('/');
const formatTime = (t) => {
  if (!t) return '';
  const d = new Date(t);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};
onMounted(() => {
  fetchRoom();
  resetSearch();
});
</script>

<style scoped>
.room-detail-container { max-width: 700px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 2em; }
.search-bar { margin-bottom: 1em; }
.seat-list { list-style: none; padding: 0; }
.seat-list li { border-bottom: 1px solid #eee; padding: 1em 0; }
.seat-title { font-weight: bold; font-size: 1.1em; }
.seat-actions button { margin-right: 0.5em; margin-top: 0.5em; }
button { margin-right: 1em; margin-top: 0.5em; }
.pagination { margin-top: 1em; text-align: center; }
</style>
