package com.studyroom.service;

import com.studyroom.model.*;
import com.studyroom.repository.BookingRepository;
import com.studyroom.repository.SeatRepository;
import com.studyroom.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private BookingService bookingService;

    private Student testStudent;
    private Seat testSeat;
    private Booking testBooking;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("testuser");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");

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
        testBooking.setStartTime(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        testBooking.setEndTime(LocalDateTime.now().plusHours(3).atZone(ZoneId.systemDefault()).toInstant());
        testBooking.setStatus(Booking.BookingStatus.ACTIVE);
    }

    @Test
    void getAllBookings_ShouldReturnAllBookings() {
        // 模拟返回预订列表
        when(bookingRepository.findAll()).thenReturn(Collections.singletonList(testBooking));

        // 执行测试
        List<Booking> result = bookingService.getAllBookings();

        // 验证结果
        assertEquals(1, result.size());
        assertEquals(testBooking.getId(), result.get(0).getId());
    }
}