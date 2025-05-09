package com.studyroom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyroom.dto.BookingRequest;
import com.studyroom.dto.LoginRequest;
import com.studyroom.model.*;
import com.studyroom.service.CompositeUserDetailsService;
import com.studyroom.util.JwtUtil;
import com.studyroom.service.RoomService;
import com.studyroom.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;  // Add this import
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 添加这两个import
import org.springframework.context.annotation.Import;
import com.studyroom.config.SecurityConfig;

// 修改类注解
@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CompositeUserDetailsService compositeUserDetailsService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private RoomService roomService;

    private Student testStudent;
    private Room testRoom;
    private Seat testSeat;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("student");
        testStudent.setPassword("password");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setCapacity(20);

        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setSeatNumber("A1");
        testSeat.setRoom(testRoom);
        testSeat.setStatus(Seat.SeatStatus.AVAILABLE);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setStudent(testStudent);
        testBooking.setSeat(testSeat);
        testBooking.setRoom(testRoom);
        ZoneId zoneId = ZoneId.systemDefault();
        testBooking.setStartTime(LocalDateTime.now().plusHours(1).atZone(zoneId).toInstant());
        testBooking.setEndTime(LocalDateTime.now().plusHours(3).atZone(zoneId).toInstant());
        testBooking.setStatus(Booking.BookingStatus.ACTIVE);

        // 简化安全配置
        UserDetails userDetails = User.withUsername("student")
            .password("password")
            .roles("STUDENT")
            .build();
            
        when(compositeUserDetailsService.loadUserByUsername(anyString()))
            .thenReturn(userDetails);
            
        // 确保返回完整的Authentication对象
        when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()));
    }
}