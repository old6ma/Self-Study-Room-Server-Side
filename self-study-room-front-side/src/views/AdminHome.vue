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
        <div class="room-title">{{ room.name }}</div>
        <div>位置：{{ room.location }}</div>
        <div>容量：{{ room.capacity }}</div>
        <div>状态：{{ room.status }}</div>
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
        <input v-model="newRoom.capacity" type="number" placeholder="容量" />
        <select v-model="newRoom.status">
          <option value="AVAILABLE">开放</option>
          <option value="UNAVAILABLE">关闭</option>
        </select>
        <button @click="addRoom">确定</button>
        <button @click="showAddRoom = false">取消</button>
      </div>
    </div>
    <div v-if="showEditRoom" class="modal">
      <div class="modal-content">
        <h4>编辑自习室</h4>
        <input v-model="editRoomData.name" placeholder="名称" />
        <input v-model="editRoomData.location" placeholder="位置" />
        <input v-model="editRoomData.capacity" type="number" placeholder="容量" />
        <select v-model="editRoomData.status">
          <option value="AVAILABLE">开放</option>
          <option value="UNAVAILABLE">关闭</option>
        </select>
        <button @click="saveEditRoom">保存</button>
        <button @click="showEditRoom = false">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const adminName = localStorage.getItem('adminName') || '管理员';
const router = useRouter();
const showAddRoom = ref(false);
const showEditRoom = ref(false);
const newRoom = ref({ name: '', location: '', capacity: 10, status: 'AVAILABLE' });
const editRoomData = ref({});
const searchText = ref('');
const page = ref(1);
const pageSize = 2;
const rooms = ref([
  { room_id: 1, name: '一号自习室', location: '教学楼A-201', capacity: 30, status: 'AVAILABLE' },
  { room_id: 2, name: '二号自习室', location: '教学楼B-101', capacity: 50, status: 'AVAILABLE' },
  { room_id: 3, name: '三号自习室', location: '教学楼C-301', capacity: 20, status: 'UNAVAILABLE' },
  { room_id: 4, name: '四号自习室', location: '教学楼D-401', capacity: 40, status: 'AVAILABLE' },
  { room_id: 5, name: '五号自习室', location: '教学楼E-501', capacity: 25, status: 'AVAILABLE' }
]);
const filteredRooms = ref([...rooms.value]);

const pagedRooms = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredRooms.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(filteredRooms.value.length / pageSize));

const logout = () => {
  localStorage.removeItem('adminToken');
  localStorage.removeItem('adminName');
  router.push('/admin/login');
};
const addRoom = () => {
  const id = Date.now();
  rooms.value.push({ ...newRoom.value, room_id: id });
  resetSearch();
  showAddRoom.value = false;
  newRoom.value = { name: '', location: '', capacity: 10, status: 'AVAILABLE' };
};
const editRoom = (roomId) => {
  const room = rooms.value.find(r => r.room_id === roomId);
  editRoomData.value = { ...room };
  showEditRoom.value = true;
};
const saveEditRoom = () => {
  const idx = rooms.value.findIndex(r => r.room_id === editRoomData.value.room_id);
  if (idx !== -1) rooms.value[idx] = { ...editRoomData.value };
  resetSearch();
  showEditRoom.value = false;
};
const deleteRoom = (roomId) => {
  rooms.value = rooms.value.filter(r => r.room_id !== roomId);
  resetSearch();
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
