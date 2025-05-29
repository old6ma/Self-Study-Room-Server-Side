<template>
  <div class="admin-room-detail-container">
    <h2>{{ room?.name }} - 座位管理</h2>
    <div>位置：{{ room?.location }}</div>
    <div>容量：{{ room?.capacity }}</div>
    <div>状态：{{ room?.status }}</div>
    <div class="search-bar">
      <input v-model="searchText" placeholder="搜索座位名称" />
      <button @click="searchSeats">搜索</button>
      <button @click="resetSearch">重置</button>
      <button @click="exportSeats">导出座位列表</button>
    </div>
    <button @click="showAddSeat = true">添加座位</button>
    <ul class="seat-list">
      <li v-for="seat in pagedSeats" :key="seat.seat_id">
        <div class="seat-title">{{ seat.name }}</div>
        <div>插座：{{ seat.has_socket ? '有' : '无' }}</div>
        <div>状态：{{ seat.status }}</div>
        <div>最大预约时长：{{ seat.max_booking_time }}分钟</div>
        <button @click="editSeat(seat.seat_id)">编辑</button>
        <button @click="deleteSeat(seat.seat_id)">删除</button>
      </li>
    </ul>
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1">上一页</button>
      <span>第{{ page }}/{{ totalPages }}页</span>
      <button @click="nextPage" :disabled="page === totalPages">下一页</button>
    </div>
    <div v-if="showAddSeat" class="modal">
      <div class="modal-content">
        <h4>添加座位</h4>
        <input v-model="newSeat.name" placeholder="座位名称" />
        <label><input type="checkbox" v-model="newSeat.has_socket" />有插座</label>
        <select v-model="newSeat.status">
          <option value="AVAILABLE">可用</option>
          <option value="OCCUPIED">占用</option>
          <option value="UNAVAILABLE">不可用</option>
        </select>
        <input v-model="newSeat.max_booking_time" type="number" placeholder="最大预约时长(分钟)" />
        <button @click="addSeat">确定</button>
        <button @click="showAddSeat = false">取消</button>
      </div>
    </div>
    <div v-if="showEditSeat" class="modal">
      <div class="modal-content">
        <h4>编辑座位</h4>
        <input v-model="editSeatData.name" placeholder="座位名称" />
        <label><input type="checkbox" v-model="editSeatData.has_socket" />有插座</label>
        <select v-model="editSeatData.status">
          <option value="AVAILABLE">可用</option>
          <option value="OCCUPIED">占用</option>
          <option value="UNAVAILABLE">不可用</option>
        </select>
        <input v-model="editSeatData.max_booking_time" type="number" placeholder="最大预约时长(分钟)" />
        <button @click="saveEditSeat">保存</button>
        <button @click="showEditSeat = false">取消</button>
      </div>
    </div>
    <button @click="goBack">返回</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const showAddSeat = ref(false);
const showEditSeat = ref(false);
const newSeat = ref({ name: '', has_socket: false, status: 'AVAILABLE', max_booking_time: 120 });
const editSeatData = ref({});
const searchText = ref('');
const page = ref(1);
const pageSize = 2;
const room = ref(null);
const seats = ref([
  { seat_id: 101, name: 'A1', has_socket: true, status: 'AVAILABLE', max_booking_time: 120 },
  { seat_id: 102, name: 'A2', has_socket: false, status: 'OCCUPIED', max_booking_time: 120 },
  { seat_id: 103, name: 'B1', has_socket: true, status: 'AVAILABLE', max_booking_time: 120 },
  { seat_id: 104, name: 'B2', has_socket: false, status: 'AVAILABLE', max_booking_time: 120 }
]);
const filteredSeats = ref([...seats.value]);

const pagedSeats = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredSeats.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(filteredSeats.value.length / pageSize));

const fetchRoom = () => {
  // 硬编码演示数据
  const allRooms = [
    { room_id: 1, name: '一号自习室', location: '教学楼A-201', capacity: 30, status: 'AVAILABLE' },
    { room_id: 2, name: '二号自习室', location: '教学楼B-101', capacity: 50, status: 'AVAILABLE' },
    { room_id: 3, name: '三号自习室', location: '教学楼C-301', capacity: 20, status: 'UNAVAILABLE' }
  ];
  room.value = allRooms.find(r => r.room_id == route.params.roomId);
};
const addSeat = () => {
  const id = Date.now();
  seats.value.push({ ...newSeat.value, seat_id: id });
  resetSearch();
  showAddSeat.value = false;
  newSeat.value = { name: '', has_socket: false, status: 'AVAILABLE', max_booking_time: 120 };
};
const editSeat = (seatId) => {
  const seat = seats.value.find(s => s.seat_id === seatId);
  editSeatData.value = { ...seat };
  showEditSeat.value = true;
};
const saveEditSeat = () => {
  const idx = seats.value.findIndex(s => s.seat_id === editSeatData.value.seat_id);
  if (idx !== -1) seats.value[idx] = { ...editSeatData.value };
  resetSearch();
  showEditSeat.value = false;
};
const deleteSeat = (seatId) => {
  seats.value = seats.value.filter(s => s.seat_id !== seatId);
  resetSearch();
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
  const csv = ['座位名称,有插座,状态,最大预约时长(分钟)'];
  filteredSeats.value.forEach(s => {
    csv.push(`${s.name},${s.has_socket ? '有' : '无'},${s.status},${s.max_booking_time}`);
  });
  const blob = new Blob([csv.join('\n')], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '座位列表.csv';
  a.click();
  URL.revokeObjectURL(url);
};
const goBack = () => router.push('/admin');
onMounted(() => {
  fetchRoom();
  resetSearch();
});
</script>

<style scoped>
.admin-room-detail-container { max-width: 700px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 2em; }
.search-bar { margin-bottom: 1em; }
.seat-list { list-style: none; padding: 0; }
.seat-list li { border-bottom: 1px solid #eee; padding: 1em 0; }
.seat-title { font-weight: bold; font-size: 1.1em; }
button { margin-right: 0.5em; }
.pagination { margin-top: 1em; }
.modal { position: fixed; left: 0; top: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.2); display: flex; align-items: center; justify-content: center; }
.modal-content { background: #fff; padding: 2em; border-radius: 8px; min-width: 300px; }
</style>
