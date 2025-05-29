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
        <div>状态：{{ seatStatusText(seat.status) }}</div>
        <div>预约情况：
          <ul>
            <li v-if="!seat.ordering_list || seat.ordering_list.length === 0">无</li>
            <li v-for="order in seat.ordering_list" :key="order.start_time">
              {{ formatTime(order.start_time) }} - {{ formatTime(order.end_time) }}
            </li>
          </ul>
        </div>
        <div class="seat-actions">
          <button @click="openBookDialog(seat)" :disabled="seat.status !== 'AVAILABLE'">预约</button>
          <button @click="occupyNow(seat)" :disabled="seat.status !== 'AVAILABLE'">临时抢占</button>
        </div>
      </li>
    </ul>
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1">上一页</button>
      <span>第{{ page }}/{{ totalPages }}页</span>
      <button @click="nextPage" :disabled="page === totalPages">下一页</button>
    </div>
    <button @click="goBack">返回</button>

    <template v-if="showBookDialog">
      <div class="dialog-mask">
        <div class="dialog">
          <h3>预约座位：{{ bookSeatData.name }}</h3>
          <div>起始时间：<input type="datetime-local" v-model="bookStart" /></div>
          <div>结束时间：<input type="datetime-local" v-model="bookEnd" /></div>
          <div style="margin-top:1em;">
            <button @click="submitBook">确认预约</button>
            <button @click="closeBookDialog">取消</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { studentApi } from '../api';

const route = useRoute();
const router = useRouter();
const room = ref(null);
const seats = ref([]);
const searchText = ref('');
const page = ref(1);
const pageSize = 10;
const filteredSeats = ref([]);
const pagedSeats = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredSeats.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(filteredSeats.value.length / pageSize));

const fetchRoom = async () => {
  try {
    // 获取所有自习室，找到当前room
    const res = await studentApi.getRooms();
    room.value = (res.rooms || []).find(r => r.room_id == route.params.roomId);
  } catch (e) {
    room.value = null;
  }
};
const fetchSeats = async () => {
  try {
    const res = await studentApi.getSeats(route.params.roomId);
    seats.value = res.seats || [];
    resetSearch();
  } catch (e) {
    seats.value = [];
    filteredSeats.value = [];
  }
};
const searchSeats = () => {
  page.value = 1;
  filteredSeats.value = seats.value.filter(s => s.name?.includes(searchText.value));
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
const seatStatusText = (status) => {
  if (status === 'AVAILABLE') return '可用';
  if (status === 'OCCUPIED') return '已占用';
  if (status === 'TEMP_LEAVE') return '暂离';
  if (status === 'DISABLED') return '禁用';
  return status;
};
const exportSeats = () => {
  const csv = ['座位名称,有插座,状态'];
  filteredSeats.value.forEach(s => {
    csv.push(`${s.name},${s.has_socket ? '有' : '无'},${seatStatusText(s.status)}`);
  });
  const blob = new Blob([csv.join('\n')], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '座位列表.csv';
  a.click();
  URL.revokeObjectURL(url);
};
const showBookDialog = ref(false);
const bookSeatData = ref({});
const bookStart = ref('');
const bookEnd = ref('');

const openBookDialog = (seat) => {
  bookSeatData.value = seat;
  // 默认起止时间为当前本地时间和+1小时，适配datetime-local控件
  const now = new Date();
  const pad = n => n.toString().padStart(2, '0');
  const toLocalInput = d => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  bookStart.value = toLocalInput(now);
  const end = new Date(now.getTime() + 60*60*1000);
  bookEnd.value = toLocalInput(end);
  showBookDialog.value = true;
};
const closeBookDialog = () => {
  showBookDialog.value = false;
};
const submitBook = async () => {
  try {
    // 直接用本地时间戳，确保与后端东八区一致
    const start = new Date(bookStart.value);
    const end = new Date(bookEnd.value);
    const startTime = start.getTime();
    const endTime = end.getTime();
    await studentApi.book({
      seat_id: bookSeatData.value.seat_id,
      room_id: room.value.room_id,
      start_time: startTime,
      end_time: endTime
    });
    alert('预约成功');
    showBookDialog.value = false;
    fetchSeats();
  } catch (e) {
    alert(e.message || '预约失败');
  }
};
const occupyNow = async (seat) => {
  try {
    await studentApi.occupyNow(seat.seat_id);
    alert('临时抢占成功');
    fetchSeats();
  } catch (e) {
    alert(e.message || '临时抢占失败');
  }
};
const goBack = () => router.push('/');
// 全局时间格式化函数，强制东八区
const formatTime = (t) => {
  if (!t) return '';
  const d = new Date(Number(t));
  return d.toLocaleString('zh-CN', { hour12: false, timeZone: 'Asia/Shanghai' });
};
onMounted(() => {
  fetchRoom();
  fetchSeats();
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
.dialog-mask { position: fixed; left:0; top:0; right:0; bottom:0; background: rgba(0,0,0,0.2); z-index: 1000; }
.dialog { background: #fff; border-radius: 8px; padding: 2em; max-width: 350px; margin: 10% auto; box-shadow: 0 2px 8px #aaa; }
</style>
