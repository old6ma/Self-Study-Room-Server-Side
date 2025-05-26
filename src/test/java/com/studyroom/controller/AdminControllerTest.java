package com.studyroom.controller;

import com.studyroom.dto.*;
import com.studyroom.model.*;
import com.studyroom.service.*;
import com.studyroom.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private RoomService roomService;

    @Mock
    private SeatService seatService;

    @Mock
    private BookingService bookingService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminController adminController;

    private LoginRequest loginRequest;
    private RoomRequest roomRequest;
    private SeatRequest seatRequest;
    private Room testRoom;
    private Seat testSeat;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("password");
        
        roomRequest = new RoomRequest();
        roomRequest.setName("Test Room");
        roomRequest.setLocation("Building A");
        
        seatRequest = new SeatRequest();
        seatRequest.setRoomId(1L);
        seatRequest.setSeatNumber("A1");
        seatRequest.setHasSocket(true);
    
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setLocation("Building A");
    
        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setSeatNumber("A1");
        testSeat.setRoom(testRoom);
        testSeat.setStatus(Seat.SeatStatus.AVAILABLE);

        Student testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("student");
        testStudent.setStudentId("202500001");

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setStudent(testStudent);
        testBooking.setSeat(testSeat);
        testBooking.setRoom(testRoom);
        ZoneId zoneId = ZoneId.systemDefault();
        testBooking.setStartTime(LocalDateTime.now().plusHours(1).atZone(zoneId).toInstant());
        testBooking.setEndTime(LocalDateTime.now().plusHours(3).atZone(zoneId).toInstant());
        testBooking.setStatus(Booking.BookingStatus.ACTIVE);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        // 模拟认证成功
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtUtil.generateToken("admin","ROLE_ADMIN")).thenReturn("testToken");

        // 执行测试
        ResponseEntity<?> response = adminController.login(loginRequest);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof LoginResponse);
        assertEquals("testToken", ((LoginResponse) response.getBody()).getToken());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsInvalid() {
        // 模拟认证失败
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // 执行测试
        ResponseEntity<?> response = adminController.login(loginRequest);

        // 验证结果
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Invalid credentials", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void createRoom_ShouldReturnCreated_WhenRoomNotExists() {
        // 模拟创建成功 - 修改为返回 Room 对象
        when(roomService.createRoom(any(RoomRequest.class))).thenReturn(testRoom);
    
        // 执行测试
        ResponseEntity<?> response = adminController.createRoom(roomRequest);
    
        // 验证结果
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        assertEquals("Room created successfully", ((ApiResponse) response.getBody()).getMessage());
    }
    
    @Test
    void createRoom_ShouldReturnConflict_WhenRoomAlreadyExists() {
        // 模拟房间已存在 - 修改为抛出异常
        when(roomService.createRoom(any(RoomRequest.class)))
            .thenThrow(new RuntimeException("Room already exists"));
    
        // 执行测试
        ResponseEntity<?> response = adminController.createRoom(roomRequest);
    
        // 验证结果
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Room already exists", ((Map<?, ?>) response.getBody()).get("error"));
    }
    
    @Test
    void addSeat_ShouldReturnCreated_WhenSeatNotExists() {
        // 模拟添加成功 - 修改为返回 Seat 对象
        when(seatService.addSeat(any(SeatRequest.class))).thenReturn(testSeat);
    
        // 执行测试
        ResponseEntity<?> response = adminController.addSeat(seatRequest);
    
        // 验证结果
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        assertEquals("Seat added successfully", ((ApiResponse) response.getBody()).getMessage());
    }
    
    @Test
    void updateRoom_ShouldReturnOk_WhenRoomExists() {
        // 模拟更新成功 - 修改为返回 Room 对象
        when(roomService.updateRoom(anyLong(), any(RoomRequest.class))).thenReturn(testRoom);
    
        // 执行测试
        ResponseEntity<?> response = adminController.updateRoom(1L, roomRequest);
    
        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        assertEquals("Room updated successfully", ((ApiResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteRoom_ShouldReturnOk_WhenRoomExists() {
        // 模拟删除成功
        doNothing().when(roomService).deleteRoom(1L);

        // 执行测试
        ResponseEntity<?> response = adminController.deleteRoom(1L);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        assertEquals("Room deleted successfully", ((ApiResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteRoom_ShouldReturnNotFound_WhenRoomNotExists() {
        // 模拟房间不存在
        doThrow(new RuntimeException("Room not found")).when(roomService).deleteRoom(1L);

        // 执行测试
        ResponseEntity<?> response = adminController.deleteRoom(1L);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Room not found", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void getAllRooms_ShouldReturnRoomList() {
        // 模拟返回房间列表
        when(roomService.getAllRooms()).thenReturn(Collections.singletonList(testRoom));

        // 执行测试
        ResponseEntity<?> response = adminController.getAllRooms();

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        List<?> rooms = (List<?>) ((Map<?, ?>) response.getBody()).get("rooms");
        assertEquals(1, rooms.size());
    }

    @Test
    void getAllBookings_ShouldReturnBookingList() {
        // 模拟返回预订列表
        when(bookingService.getAllBookings()).thenReturn(Collections.singletonList(testBooking));

        // 执行测试
        ResponseEntity<?> response = adminController.getAllBookings();

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        List<?> bookings = (List<?>) ((Map<?, ?>) response.getBody()).get("bookings");
        assertEquals(1, bookings.size());
    }
}