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
        <div>类型：{{ roomTypeText(room.type) }}</div>
        <div>开放时间：{{ formatDate(room.open_time) }} - {{ formatDate(room.close_time) }}</div>
        <div>状态：{{ roomStatusText(room.status) }}</div>
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
import { studentApi } from '../api';

const rooms = ref([]);
const searchText = ref('');
const page = ref(1);
const pageSize = 10;
const filteredRooms = ref([]);
const pagedRooms = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredRooms.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(filteredRooms.value.length / pageSize));
const studentName = localStorage.getItem('studentName') || '演示用户';
const router = useRouter();

const fetchRooms = async () => {
  try {
    const res = await studentApi.getRooms();
    rooms.value = res.rooms || [];
    resetSearch();
  } catch (e) {
    rooms.value = [];
    filteredRooms.value = [];
  }
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
  const csv = ['名称,位置,容量,可用座位,类型,开放时间,关闭时间,状态'];
  filteredRooms.value.forEach(r => {
    csv.push(`${r.name},${r.location},${r.capacity},${r.seat_number},${roomTypeText(r.type)},${formatDate(r.open_time)},${formatDate(r.close_time)},${roomStatusText(r.status)}`);
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
const roomTypeText = (type) => {
  if (type === 1) return '普通自习室';
  if (type === 2) return '研讨室';
  return '未知类型';
};
const roomStatusText = (status) => {
  if (status === 0) return '开放';
  if (status === 1) return '关闭';
  return '未知';
};
const formatDate = (t) => {
  if (!t) return '';
  const d = new Date(t);
  return d.toLocaleString('zh-CN', { hour12: false });
};
onMounted(fetchRooms);
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
