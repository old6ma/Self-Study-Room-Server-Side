<template>
  <div class="login-container">
    <h2>管理员登录</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <button type="submit">登录</button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { adminApi } from '../api';

const username = ref('');
const password = ref('');
const error = ref('');
const router = useRouter();

const handleLogin = async () => {
  error.value = '';
  try {
    const res = await adminApi.login(username.value, password.value);
    localStorage.setItem('adminToken', res.token);
    localStorage.setItem('adminName', res.name || '管理员');
    router.push('/admin');
  } catch (e) {
    error.value = e.message || (e.error || '用户名或密码错误');
  }
};
</script>

<style scoped>
.login-container { max-width: 350px; margin: 60px auto; padding: 2em; border-radius: 8px; box-shadow: 0 2px 8px #eee; background: #fff; }
input { display: block; width: 100%; margin-bottom: 1em; padding: 0.7em; border-radius: 4px; border: 1px solid #ccc; }
button { width: 100%; padding: 0.7em; background: #42b983; color: #fff; border: none; border-radius: 4px; font-size: 1em; }
.error { color: #e74c3c; margin-top: 1em; }
</style>
