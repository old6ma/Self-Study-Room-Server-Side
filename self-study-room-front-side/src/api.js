// 后端API基础地址
export const API_BASE = 'http://60.204.232.89:8080';

// 通用请求方法，自动加token（不加Bearer前缀）
function request(url, options = {}) {
  const headers = options.headers || {};
  // 登录接口不加token
  if (!url.includes('/api/v1.0/admin/login')) {
    const token = localStorage.getItem('adminToken');
    if (token) headers['Authorization'] = token;
  }
  return fetch(url, { ...options, headers })
    .then(async res => {
      const data = await res.json().catch(() => ({}));
      if (!res.ok) throw data;
      return data;
    });
}

// 学生相关API
export const studentApi = {
  async login(username, password) {
    const res = await fetch(`${API_BASE}/api/v1.0/student/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!res.ok) throw new Error('用户名或密码错误');
    return await res.json();
  },
  async register(data) {
    const res = await fetch(`${API_BASE}/api/v1.0/student/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    const result = await res.json();
    if (!res.ok) throw result;
    return result;
  },
  async getRooms() {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/rooms`, {
      headers: { Authorization: token ? token : '' }
    });
    return await res.json();
  },
  async getSeats(roomId) {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/rooms/${roomId}/seats`, {
      headers: { Authorization: token ? token : '' }
    });
    return await res.json();
  },
  async book(data) {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/seats/book`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: token ? token : '' },
      body: JSON.stringify(data)
    });
    if (!res.ok) throw await res.json();
    return await res.json();
  },
  async occupyNow(seatId) {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/seats/${seatId}/occupy-now`, {
      method: 'POST',
      headers: { Authorization: token ? token : '' }
    });
    if (!res.ok) throw await res.json();
    return await res.json();
  },
  async checkin(seatId) {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/seats/${seatId}/checkin`, {
      method: 'POST',
      headers: { Authorization: token ? token : '' }
    });
    if (!res.ok) throw await res.json();
    return await res.json();
  },
  async leaveSeat(seatId) {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/seats/${seatId}/leave`, {
      method: 'POST',
      headers: { Authorization: token ? token : '' }
    });
    if (!res.ok) throw await res.json();
    return await res.json();
  },
  async releaseSeat(seatId) {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/seats/${seatId}/release`, {
      method: 'POST',
      headers: { Authorization: token ? token : '' }
    });
    if (!res.ok) throw await res.json();
    return await res.json();
  },
  async bookingHistory() {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/bookings/history`, {
      headers: { Authorization: token ? token : '' }
    });
    return await res.json();
  },
  async cancelBooking(bookingId) {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/bookings/${bookingId}`, {
      method: 'DELETE',
      headers: { Authorization: token ? token : '' }
    });
    if (!res.ok) throw await res.json();
    return await res.json();
  },
  async recommendSeats() {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_BASE}/api/v1.0/student/seats/recommend`, {
      headers: { Authorization: token ? token : '' }
    });
    return await res.json();
  }
};

// 管理员相关API
export const adminApi = {
  login: (username, password) => request(`${API_BASE}/api/v1.0/admin/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  }),
  getRooms: () => request(`${API_BASE}/api/v1.0/admin/rooms`),
  createRoom: (payload) => request(`${API_BASE}/api/v1.0/admin/rooms`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  updateRoom: (roomId, payload) => request(`${API_BASE}/api/v1.0/admin/rooms/${roomId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json'},
    body: JSON.stringify(payload)
  }),
  deleteRoom: (roomId) => request(`${API_BASE}/api/v1.0/admin/rooms/${roomId}`, { method: 'DELETE' }),
  getSeats: (roomId) => request(`${API_BASE}/api/v1.0/admin/rooms/${roomId}/seats`),
  addSeat: (payload) => request(`${API_BASE}/api/v1.0/admin/seats`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  updateSeat: (seatId, payload) => request(`${API_BASE}/api/v1.0/admin/seats/${seatId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  deleteSeat: (seatId) => request(`${API_BASE}/api/v1.0/admin/seats/${seatId}`, { method: 'DELETE' }),
  setMaxBookingTime: (seatId, minutes) => request(`${API_BASE}/api/v1.0/admin/seats/${seatId}/max-booking-time?minutes=${minutes}`, { method: 'PUT' }),
  getAllBookings: () => request(`${API_BASE}/api/v1.0/admin/bookings`),
};