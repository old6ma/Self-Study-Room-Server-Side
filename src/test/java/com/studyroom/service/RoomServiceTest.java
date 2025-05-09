package com.studyroom.service;


import com.studyroom.model.Booking;
import com.studyroom.model.Room;
import com.studyroom.model.Seat;
import com.studyroom.model.Student;
import com.studyroom.repository.BookingRepository;
import com.studyroom.repository.RoomRepository;
import com.studyroom.repository.SeatRepository;
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
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomService roomService;

    private Student testStudent;
    private Room testRoom;
    private Seat testSeat;
    private Booking testBooking;
    private List<Booking> activeBookings;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("student");

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
        ZoneId zoneId = ZoneId.systemDefault();
        testBooking.setStartTime(LocalDateTime.now().plusHours(1).atZone(zoneId).toInstant());
        testBooking.setEndTime(LocalDateTime.now().plusHours(3).atZone(zoneId).toInstant());
        testBooking.setStatus(Booking.BookingStatus.ACTIVE);

        activeBookings = Collections.singletonList(testBooking);
    }

    @Test
    void getAllRooms_ShouldReturnAllRooms() {
        // 设置模拟行为
        List<Room> rooms = Arrays.asList(testRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        // 执行测试
        List<Room> result = roomService.getAllRooms();

        // 验证结果
        assertEquals(1, result.size());
        assertEquals(testRoom.getId(), result.get(0).getId());
    }

    @Test
    void getRoomById_ShouldReturnRoom() {
        // 设置模拟行为
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        // 执行测试
        Room result = roomService.getRoomById(1L);

        // 验证结果
        assertEquals(testRoom.getId(), result.getId());
    }

    @Test
    void getRoomById_NotFound_ShouldThrowException() {
        // 设置模拟行为
        when(roomRepository.findById(2L)).thenReturn(Optional.empty());

        // 执行测试并验证
        assertThrows(RuntimeException.class, () -> roomService.getRoomById(2L));
    }

    @Test
    void getRoomsWithAvailableSeats_ShouldReturnRoomsWithCounts() {
        // 设置模拟行为
        List<Room> rooms = Collections.singletonList(testRoom);
        when(roomRepository.findAll()).thenReturn(rooms);

        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Seat seat = new Seat();
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seats.add(seat);
        }
        when(seatRepository.findByRoomId(1L)).thenReturn(seats);

        // 执行测试
        Map<Room, Long> result = roomService.getRoomsWithAvailableSeats();

        // 验证结果
        assertEquals(1, result.size());
        assertEquals(5L, result.get(testRoom));
    }

    @Test
    void searchSeats_ShouldReturnMatchingSeats() {
        // 设置模拟行为
        when(seatRepository.searchSeats("test")).thenReturn(Collections.singletonList(testSeat));

        // 执行测试
        List<Seat> result = roomService.searchSeats("test");

        // 验证结果
        assertEquals(1, result.size());
        assertEquals(testSeat.getId(), result.get(0).getId());
    }

    @Test
    void bookSeat_ShouldCreateBooking() {
        // 设置模拟行为
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // 执行测试
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);
        Booking result = roomService.bookSeat(testStudent, 1L, 1L, start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        // 验证结果
        assertEquals(testBooking.getId(), result.getId());
        assertEquals(Seat.SeatStatus.OCCUPIED, testSeat.getStatus());
        verify(seatRepository).save(testSeat);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookSeat_SeatNotAvailable_ShouldThrowException() {
        // 设置模拟行为
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        testSeat.setStatus(Seat.SeatStatus.OCCUPIED);
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        // 执行测试并验证
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);
        assertThrows(RuntimeException.class, () ->
                roomService.bookSeat(testStudent, 1L, 1L, start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    @Test
    void cancelBooking_ShouldCancelAndReleaseSeat() {
        // 设置模拟行为
        when(bookingRepository.findByIdAndStudent(1L, testStudent))
                .thenReturn(Optional.of(testBooking));

        // 执行测试
        roomService.cancelBooking(testStudent, 1L);

        // 验证结果
        assertEquals(Seat.SeatStatus.AVAILABLE, testSeat.getStatus());
        assertEquals(Booking.BookingStatus.CANCELLED, testBooking.getStatus());
        verify(seatRepository).save(testSeat);
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void cancelBooking_BookingNotFound_ShouldThrowException() {
        // 设置模拟行为
        when(bookingRepository.findByIdAndStudent(2L, testStudent))
                .thenReturn(Optional.empty());

        // 执行测试并验证
        assertThrows(RuntimeException.class, () ->
                roomService.cancelBooking(testStudent, 2L));
    }

    @Test
    void temporaryLeaveSeat_ShouldUpdateSeatStatus() {
        // 设置模拟行为
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(bookingRepository.findByStudentOrderByStartTimeDesc(testStudent))
                .thenReturn(activeBookings);

        // 执行测试
        roomService.temporaryLeaveSeat(testStudent, 1L);

        // 验证结果
        assertEquals(Seat.SeatStatus.TEMPORARY_LEAVE, testSeat.getStatus());
        verify(seatRepository).save(testSeat);
    }

    @Test
    void checkInSeat_ShouldUpdateSeatStatus() {
        // 设置模拟行为
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(bookingRepository.findByStudentOrderByStartTimeDesc(testStudent))
                .thenReturn(activeBookings);

        // 执行测试
        roomService.checkInSeat(testStudent, 1L);

        // 验证结果
        assertEquals(Seat.SeatStatus.OCCUPIED, testSeat.getStatus());
        verify(seatRepository).save(testSeat);
    }

    @Test
    void releaseSeat_ShouldCompleteBookingAndReleaseSeat() {
        // 设置模拟行为
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(bookingRepository.findByStudentOrderByStartTimeDesc(testStudent))
                .thenReturn(activeBookings);

        // 执行测试
        roomService.releaseSeat(testStudent, 1L);

        // 验证结果
        assertEquals(Seat.SeatStatus.AVAILABLE, testSeat.getStatus());
        assertEquals(Booking.BookingStatus.COMPLETED, testBooking.getStatus());
        verify(seatRepository).save(testSeat);
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void getBookingHistory_ShouldReturnBookings() {
        // 设置模拟行为
        when(bookingRepository.findByStudentOrderByStartTimeDesc(testStudent))
                .thenReturn(Collections.singletonList(testBooking));

        // 执行测试
        List<Booking> result = roomService.getBookingHistory(testStudent);

        // 验证结果
        assertEquals(1, result.size());
        assertEquals(testBooking.getId(), result.get(0).getId());
    }
}