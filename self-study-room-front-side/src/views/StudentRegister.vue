<template>
  <div class="register-container">
    <h2>学生注册</h2>
    <form @submit.prevent="handleRegister">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="name" placeholder="姓名" required />
      <input v-model="studentId" placeholder="学号" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <input v-model="confirmPassword" type="password" placeholder="确认密码" required />
      <button type="submit">注册</button>
      <router-link to="/login">已有账号？登录</router-link>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">{{ success }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const username = ref('');
const name = ref('');
const studentId = ref('');
const password = ref('');
const confirmPassword = ref('');
const error = ref('');
const success = ref('');
const router = useRouter();

const handleRegister = async () => {
  error.value = '';
  success.value = '';
  if (password.value !== confirmPassword.value) {
    error.value = '两次密码不一致';
    return;
  }
  // 临时代码：本地模拟注册
  if (username.value && name.value && studentId.value && password.value) {
    success.value = '注册成功，请登录';
    setTimeout(() => router.push('/login'), 1200);
  } else {
    error.value = '请填写完整信息';
  }
};
</script>

<style scoped>
.register-container { max-width: 350px; margin: 60px auto; padding: 2em; border-radius: 8px; box-shadow: 0 2px 8px #eee; background: #fff; }
input { display: block; width: 100%; margin-bottom: 1em; padding: 0.7em; border-radius: 4px; border: 1px solid #ccc; }
button { width: 100%; padding: 0.7em; background: #42b983; color: #fff; border: none; border-radius: 4px; font-size: 1em; }
.error { color: #e74c3c; margin-top: 1em; }
.success { color: #2ecc71; margin-top: 1em; }
</style>
