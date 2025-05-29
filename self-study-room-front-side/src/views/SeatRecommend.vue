<template>
  <div class="recommend-container">
    <h2>个性化座位推荐</h2>
    <ul v-if="seats.length">
      <li v-for="seat in seats" :key="seat.seat_id">
        <div>座位：{{ seat.seat_name }}（自习室：{{ seat.room_name }}）</div>
        <div>插座：{{ seat.has_socket ? '有' : '无' }}，状态：{{ seat.status }}</div>
        <button @click="goRoom(seat.room_id)">前往预约</button>
      </li>
    </ul>
    <div v-else>暂无推荐</div>
    <button @click="goBack">返回</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { studentApi } from '../api';

const seats = ref([]);
const router = useRouter();

const fetchRecommend = async () => {
  try {
    const res = await studentApi.recommendSeats();
    seats.value = res.recommended_seats || [];
  } catch (e) {
    seats.value = [];
  }
};

const goRoom = (roomId) => router.push(`/room/${roomId}`);
const goBack = () => router.push('/');
onMounted(fetchRecommend);
</script>

<style scoped>
.recommend-container { max-width: 600px; margin: 30px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #eee; padding: 2em; }
ul { list-style: none; padding: 0; }
li { border-bottom: 1px solid #eee; padding: 1em 0; }
button { margin-top: 0.5em; margin-right: 1em; }
</style>
