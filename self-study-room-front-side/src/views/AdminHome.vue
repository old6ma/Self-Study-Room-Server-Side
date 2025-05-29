<template>
  <div class="admin-home-container">
    <header>
      <h2>自习室管理后台</h2>
      <div>
        <span>欢迎，{{ adminName }}</span>
        <button @click="logout">退出登录</button>
      </div>
    </header>
    <div class="actions">
      <router-link to="/admin">自习室管理</router-link>
      <router-link to="/admin/bookings">预约管理</router-link>
    </div>
    <div class="search-bar">
      <input v-model="searchText" placeholder="搜索自习室名称/位置" />
      <button @click="searchRooms">搜索</button>
      <button @click="resetSearch">重置</button>
      <button @click="exportRooms">导出自习室列表</button>
    </div>
    <h3>自习室列表</h3>
    <button @click="showAddRoom = true">添加自习室</button>
    <ul class="room-list">
      <li v-for="room in pagedRooms" :key="room.room_id">
        <div class="room-title">{{ room.name }}（ID: {{ room.room_id }}）</div>
        <div>位置：{{ room.location }}</div>
        <div>容量：{{ room.capacity }}</div>
        <div>座位数：{{ room.seat_number }}</div>
        <div>类型：{{ roomTypeText(room.type) }}</div>
        <div>开放时间：{{ formatTime(room.open_time) }}</div>
        <div>关闭时间：{{ formatTime(room.close_time) }}</div>
        <div>状态：{{ roomStatusText(room.status) }}</div>
        <button @click="editRoom(room.room_id)">编辑</button>
        <button @click="deleteRoom(room.room_id)">删除</button>
        <router-link :to="`/admin/rooms/${room.room_id}`">座位管理</router-link>
      </li>
    </ul>
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1">上一页</button>
      <span>第{{ page }}/{{ totalPages }}页</span>
      <button @click="nextPage" :disabled="page === totalPages">下一页</button>
    </div>
    <div v-if="showAddRoom" class="modal">
      <div class="modal-content">
        <h4>添加自习室</h4>
        <input v-model="newRoom.name" placeholder="名称" />
        <input v-model="newRoom.location" placeholder="位置" />
        <input v-model.number="newRoom.capacity" type="number" placeholder="容量" />
        <select v-model.number="newRoom.type">
          <option :value="1">自习室</option>
          <option :value="2">会议室</option>
        </select>
        <select v-model.number="newRoom.status">
          <option :value="0">开放</option>
          <option :value="1">关闭</option>
        </select>
        <label>开放时间：<input v-model="newRoom.open_time" type="datetime-local" /></label>
        <label>关闭时间：<input v-model="newRoom.close_time" type="datetime-local" /></label>
        <button @click="addRoom">确定</button>
        <button @click="showAddRoom = false">取消</button>
      </div>
    </div>
    <div v-if="showEditRoom" class="modal">
      <div class="modal-content">
        <h4>编辑自习室</h4>
        <input v-model="editRoomData.name" placeholder="名称" />
        <input v-model="editRoomData.location" placeholder="位置" />
        <input v-model.number="editRoomData.capacity" type="number" placeholder="容量" />
        <select v-model.number="editRoomData.type">
          <option :value="1">自习室</option>
          <option :value="2">会议室</option>
        </select>
        <select v-model="editRoomData.status">
          <option value="AVAILABLE">开放</option>
          <option value="UNAVAILABLE">关闭</option>
        </select>
        <label>开放时间：<input v-model="editRoomData.open_time" type="datetime-local" /></label>
        <label>关闭时间：<input v-model="editRoomData.close_time" type="datetime-local" /></label>
        <button @click="saveEditRoom">保存</button>
        <button @click="showEditRoom = false">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { adminApi } from '../api';

const adminName = localStorage.getItem('adminName') || '管理员';
const router = useRouter();
const showAddRoom = ref(false);
const showEditRoom = ref(false);
const newRoom = ref({
  name: '',
  location: '',
  capacity: 10,
  status: 0,
  type: 1,
  open_time: '',
  close_time: ''
});
const editRoomData = ref({});
const searchText = ref('');
const page = ref(1);
const pageSize = 10;
const rooms = ref([]);
const filteredRooms = ref([]);

const fetchRooms = async () => {
  try {
    const res = await adminApi.getRooms();
    rooms.value = res.rooms || res.data || res; // 兼容不同返回结构
    resetSearch();
  } catch (e) {
    rooms.value = [];
    filteredRooms.value = [];
  }
};

const pagedRooms = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredRooms.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.max(1, Math.ceil(filteredRooms.value.length / pageSize)));

const logout = () => {
  localStorage.removeItem('adminToken');
  localStorage.removeItem('adminName');
  router.push('/admin/login');
};
const addRoom = async () => {
  try {
    // open_time/close_time转为时间戳
    const payload = {
      name: newRoom.value.name,
      location: newRoom.value.location,
      capacity: Number(newRoom.value.capacity),
      status: Number(newRoom.value.status),
      type: Number(newRoom.value.type),
      open_time: newRoom.value.open_time ? new Date(newRoom.value.open_time).getTime() : Date.now(),
      close_time: newRoom.value.close_time ? new Date(newRoom.value.close_time).getTime() : Date.now() + 2 * 3600 * 1000
    };
    await adminApi.createRoom(payload);
    showAddRoom.value = false;
    newRoom.value = { name: '', location: '', capacity: 10, status: 0, type: 1, open_time: '', close_time: '' };
    fetchRooms();
  } catch (e) { console.error(e); }
};
const editRoom = (roomId) => {
  const room = rooms.value.find(r => r.room_id === roomId);
  editRoomData.value = { ...room };
  showEditRoom.value = true;
};
const saveEditRoom = async () => {
  try {
    // open_time/close_time转为时间戳，type为数字，status为字符串
    const payload = {
      name: editRoomData.value.name,
      location: editRoomData.value.location,
      capacity: Number(editRoomData.value.capacity),
      status: editRoomData.value.status,
      type: Number(editRoomData.value.type),
      open_time: editRoomData.value.open_time ? new Date(editRoomData.value.open_time).getTime() : Date.now(),
      close_time: editRoomData.value.close_time ? new Date(editRoomData.value.close_time).getTime() : Date.now() + 2 * 3600 * 1000
    };
    await adminApi.updateRoom(editRoomData.value.room_id, payload);
    showEditRoom.value = false;
    fetchRooms();
  } catch (e) { console.error(e); }
};
const deleteRoom = async (roomId) => {
  if (!confirm('确定要删除该自习室吗？')) return;
  try {
    await adminApi.deleteRoom(roomId);
    fetchRooms();
  } catch (e) { console.error(e); }
};
const searchRooms = () => {
  page.value = 1;
  filteredRooms.value = rooms.value.filter(r => r.name.includes(searchText.value) || r.location.includes(searchText.value));
};
const resetSearch = () => {
  searchText.value = '';
  filteredRooms.value = [...rooms.value];
  page.value = 1;
};
const prevPage = () => {
  if (page.value > 1) page.value--;
};
const nextPage = () => {
  if (page.value < totalPages.value) page.value++;
};
const exportRooms = () => {
  const csv = ['名称,位置,容量,状态'];
  filteredRooms.value.forEach(r => {
    csv.push(`${r.name},${r.location},${r.capacity},${r.status}`);
  });
  const blob = new Blob([csv.join('\n')], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '自习室列表.csv';
  a.click();
  URL.revokeObjectURL(url);
};
function formatTime(ts) {
  if (!ts) return '';
  const d = new Date(Number(ts));
  return d.toLocaleString();
}
function roomStatusText(status) {
  if (status === 0) return '开放';
  if (status === 1) return '关闭';
  return status;
}
function roomTypeText(type) {
  if (type === 1) return '自习室';
  if (type === 2) return '会议室';
  return type;
}
onMounted(fetchRooms);
</script>

<style scoped>
.admin-home-container { max-width: 800px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 2em; }
header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1em; }
.actions { margin-bottom: 1em; }
.actions a { margin-right: 1em; color: #42b983; }
.search-bar { margin-bottom: 1em; }
.room-list { list-style: none; padding: 0; }
.room-list li { border-bottom: 1px solid #eee; padding: 1em 0; }
.room-title { font-weight: bold; font-size: 1.1em; }
button { margin-right: 0.5em; }
.pagination { margin-top: 1em; }
.modal { position: fixed; left: 0; top: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.2); display: flex; align-items: center; justify-content: center; }
.modal-content { background: #fff; padding: 2em; border-radius: 8px; min-width: 300px; }
</style>
