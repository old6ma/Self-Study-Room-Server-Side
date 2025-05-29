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
        <div>座位ID：{{ seat.seat_id }}</div>
        <div class="seat-title">{{ seat.name }}</div>
        <div>插座：{{ seat.has_socket ? '有插座' : '无插座' }}</div>
        <div>状态：{{ seatStatusMap[seat.status] || seat.status }}</div>
        <div>
          最大预约时长：
          <span v-if="!seat._editingMax">{{ seat.maxBookingTime }} 分钟</span>
          <template v-else>
            <input type="number" v-model.number="seat._newMaxBookingTime" min="1" style="width:80px;" /> 分钟
            <button @click="saveMaxBookingTime(seat)">保存</button>
            <button @click="seat._editingMax=false">取消</button>
          </template>
          <button v-if="!seat._editingMax" @click="editMaxBookingTime(seat)">修改</button>
        </div>
        <div v-if="seat.ordering_list && seat.ordering_list.length">预约队列：{{ seat.ordering_list.length }}人</div>
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
        <input v-model="newSeat.seat_number" placeholder="座位编号" />
        <input v-model="newSeat.seat_name" placeholder="座位名称" />
        <label><input type="checkbox" v-model="newSeat.has_socket" />有插座</label>
        <select v-model="newSeat.status">
          <option value="AVAILABLE">可用</option>
          <option value="OCCUPIED">占用</option>
          <option value="UNAVAILABLE">不可用</option>
        </select>
        <input v-model.number="newSeat.max_booking_time" type="number" placeholder="最大预约时长(分钟)" />
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
        <input v-model.number="editSeatData.maxBookingTime" type="number" placeholder="最大预约时长(分钟)" />
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
import { adminApi } from '../api';

const route = useRoute();
const router = useRouter();
const showAddSeat = ref(false);
const showEditSeat = ref(false);
const seatStatusMap = {
  'AVAILABLE': '可用',
  'OCCUPIED': '占用',
  'UNAVAILABLE': '不可用',
  'BOOKED': '已预约',
  'LEAVE': '暂离',
};
const newSeat = ref({
  seat_number: '',
  seat_name: '',
  has_socket: false,
  status: 'AVAILABLE',
  max_booking_time: 120
});
const editSeatData = ref({});
const searchText = ref('');
const page = ref(1);
const pageSize = 10;
const room = ref(null);
const seats = ref([]);
const filteredSeats = ref([]);

const fetchRoomAndSeats = async () => {
  try {
    const roomId = route.params.roomId;
    const allRooms = await adminApi.getRooms();
    room.value = (allRooms.rooms || allRooms.data || allRooms).find(r => r.room_id == roomId);
    const seatRes = await adminApi.getSeats(roomId);
    // 统一字段名为后端格式
    seats.value = (seatRes.seats || seatRes.data || seatRes).map(s => ({
      ...s,
      maxBookingTime: s.maxBookingTime ?? s.max_booking_time,
      ordering_list: s.ordering_list ?? [],
    }));
    resetSearch();
  } catch (e) {
    room.value = null;
    seats.value = [];
    filteredSeats.value = [];
  }
};

const pagedSeats = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredSeats.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.max(1, Math.ceil(filteredSeats.value.length / pageSize)));

const addSeat = async () => {
  try {
    const payload = {
      room_id: String(room.value.room_id),
      seat_number: newSeat.value.seat_number,
      seat_name: newSeat.value.seat_name,
      has_socket: !!newSeat.value.has_socket,
      status: newSeat.value.status,
      max_booking_time: Number(newSeat.value.max_booking_time)
    };
    await adminApi.addSeat(payload);
    showAddSeat.value = false;
    newSeat.value = { seat_number: '', seat_name: '', has_socket: false, status: 'AVAILABLE', max_booking_time: 120 };
    fetchRoomAndSeats();
  } catch (e) { console.error(e); }
};
const editSeat = (seatId) => {
  const seat = seats.value.find(s => s.seat_id === seatId);
  editSeatData.value = { ...seat };
  showEditSeat.value = true;
};
const saveEditSeat = async () => {
  try {
    await adminApi.updateSeat(editSeatData.value.seat_id, {
      name: editSeatData.value.name,
      has_socket: !!editSeatData.value.has_socket,
      status: editSeatData.value.status,
      maxBookingTime: Number(editSeatData.value.maxBookingTime)
    });
    showEditSeat.value = false;
    fetchRoomAndSeats();
  } catch (e) { console.error(e); }
};
const deleteSeat = async (seatId) => {
  if (!confirm('确定要删除该座位吗？')) return;
  try {
    await adminApi.deleteSeat(seatId);
    fetchRoomAndSeats();
  } catch (e) { console.error(e); }
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
  const csv = ['seat_id,座位名称,maxBookingTime,有插座,状态,预约队列人数'];
  filteredSeats.value.forEach(s => {
    csv.push(`${s.seat_id},${s.name},${s.maxBookingTime},${s.has_socket ? '有插座' : '无插座'},${seatStatusMap[s.status] || s.status},${s.ordering_list ? s.ordering_list.length : 0}`);
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
const editMaxBookingTime = (seat) => {
  seat._editingMax = true;
  seat._newMaxBookingTime = seat.maxBookingTime;
};
const saveMaxBookingTime = async (seat) => {
  try {
    await adminApi.setMaxBookingTime(seat.seat_id, seat._newMaxBookingTime);
    seat.maxBookingTime = seat._newMaxBookingTime;
    seat._editingMax = false;
  } catch (e) {
    alert(e.message || '修改失败');
    seat._editingMax = false;
  }
};
onMounted(fetchRoomAndSeats);
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
