<template>
  <div class="home-container">
    <header>
      <h2>自习室预约系统</h2>
      <div>
        <span>欢迎，{{ studentName }}</span>
        <button @click="logout">退出登录</button>
      </div>
    </header>
    <div class="actions">
      <router-link to="/bookings">我的预约</router-link>
      <router-link to="/recommend">座位推荐</router-link>
    </div>
    <div class="search-bar">
      <input v-model="searchText" placeholder="搜索自习室名称/位置" />
      <button @click="searchRooms">搜索</button>
      <button @click="resetSearch">重置</button>
      <button @click="exportRooms">导出自习室列表</button>
    </div>
    <h3>自习室列表</h3>
    <div v-if="pagedRooms.length === 0">暂无自习室</div>
    <ul class="room-list">
      <li v-for="room in pagedRooms" :key="room.room_id" @click="goRoom(room.room_id)">
        <div class="room-title">{{ room.name }}</div>
        <div>位置：{{ room.location }}</div>
        <div>容量：{{ room.capacity }}，可用座位：{{ room.seat_number }}</div>
        <div>开放时间：{{ formatTime(room.open_time) }} - {{ formatTime(room.close_time) }}</div>
        <div>状态：{{ room.status }}</div>
      </li>
    </ul>
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1">上一页</button>
      <span>第{{ page }}/{{ totalPages }}页</span>
      <button @click="nextPage" :disabled="page === totalPages">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const rooms = ref([
  { room_id: 1, name: '一号自习室', location: '教学楼A-201', capacity: 30, seat_number: 10, open_time: '2025-05-07T08:00:00Z', close_time: '2025-05-07T22:00:00Z', status: 'AVAILABLE' },
  { room_id: 2, name: '二号自习室', location: '教学楼B-101', capacity: 50, seat_number: 20, open_time: '2025-05-07T07:00:00Z', close_time: '2025-05-07T23:00:00Z', status: 'AVAILABLE' },
  { room_id: 3, name: '三号自习室', location: '教学楼C-301', capacity: 20, seat_number: 5, open_time: '2025-05-07T09:00:00Z', close_time: '2025-05-07T20:00:00Z', status: 'AVAILABLE' },
  { room_id: 4, name: '四号自习室', location: '教学楼D-401', capacity: 40, seat_number: 15, open_time: '2025-05-07T08:00:00Z', close_time: '2025-05-07T22:00:00Z', status: 'AVAILABLE' }
]);
const searchText = ref('');
const page = ref(1);
const pageSize = 2;
const filteredRooms = ref([...rooms.value]);
const pagedRooms = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredRooms.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(filteredRooms.value.length / pageSize));
const studentName = localStorage.getItem('studentName') || '演示用户';
const router = useRouter();

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
  const csv = ['名称,位置,容量,可用座位,开放时间,关闭时间,状态'];
  filteredRooms.value.forEach(r => {
    csv.push(`${r.name},${r.location},${r.capacity},${r.seat_number},${formatTime(r.open_time)},${formatTime(r.close_time)},${r.status}`);
  });
  const blob = new Blob([csv.join('\n')], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = '自习室列表.csv';
  a.click();
  URL.revokeObjectURL(url);
};
const goRoom = (roomId) => {
  router.push(`/room/${roomId}`);
};
const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('studentName');
  router.push('/login');
};
const formatTime = (t) => {
  if (!t) return '';
  const d = new Date(t);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};
onMounted(resetSearch);
</script>

<style scoped>
.home-container { max-width: 700px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 2em; }
header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1em; }
.actions { margin-bottom: 1em; }
.actions a { margin-right: 1em; color: #42b983; }
.search-bar { margin-bottom: 1em; }
.room-list { list-style: none; padding: 0; }
.room-list li { border-bottom: 1px solid #eee; padding: 1em 0; cursor: pointer; }
.room-title { font-weight: bold; font-size: 1.1em; }
.pagination { margin-top: 1em; text-align: center; }
</style>
