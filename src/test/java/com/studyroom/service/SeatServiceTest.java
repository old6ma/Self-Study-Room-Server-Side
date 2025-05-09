package com.studyroom.service;

import com.studyroom.dto.SeatRequest;
import com.studyroom.model.Room;
import com.studyroom.model.Seat;
import com.studyroom.repository.RoomRepository;
import com.studyroom.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private SeatService seatService;

    private Room testRoom;
    private Seat testSeat;
    private SeatRequest seatRequest;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");

        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setSeatNumber("A1");
        testSeat.setRoom(testRoom);
        testSeat.setStatus(Seat.SeatStatus.AVAILABLE);

        seatRequest = new SeatRequest();
        seatRequest.setRoomId(1L);
        seatRequest.setSeatNumber("A1");
        seatRequest.setHasSocket(true);
    }

    @Test
    void addSeat_ShouldSuccess_WhenSeatNotExists() {
        // 模拟房间存在且座位不存在
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(seatRepository.findByRoomAndSeatNumber(any(), any())).thenReturn(Optional.empty());
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        // 执行测试
        Seat result = seatService.addSeat(seatRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals("A1", result.getSeatNumber());
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void addSeat_ShouldFail_WhenRoomNotExists() {
        // 模拟房间不存在
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // 验证抛出异常
        assertThrows(RuntimeException.class, () -> {
            seatService.addSeat(seatRequest);
        });
    }

    @Test
    void addSeat_ShouldFail_WhenSeatAlreadyExists() {
        // 模拟房间存在但座位已存在
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(seatRepository.findByRoomAndSeatNumber(any(), any())).thenReturn(Optional.of(testSeat));

        // 验证抛出异常
        assertThrows(RuntimeException.class, () -> {
            seatService.addSeat(seatRequest);
        });
    }

    @Test
    void deleteSeat_ShouldSuccess_WhenSeatExists() {
        // 模拟座位存在
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        // 执行测试
        seatService.deleteSeat(1L);

        // 验证删除操作被调用
        verify(seatRepository).delete(testSeat);
    }

    @Test
    void deleteSeat_ShouldFail_WhenSeatNotExists() {
        // 模拟座位不存在
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        // 验证抛出异常
        assertThrows(RuntimeException.class, () -> {
            seatService.deleteSeat(1L);
        });
    }

    @Test
    void updateSeat_ShouldUpdateStatus_WhenStatusProvided() {
        // 模拟座位存在
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        seatRequest.setStatus("occupied");

        // 执行测试
        Seat result = seatService.updateSeat(1L, seatRequest);

        // 验证结果
        assertEquals(Seat.SeatStatus.OCCUPIED, result.getStatus());
        verify(seatRepository).save(testSeat);
    }

    @Test
    void updateSeat_ShouldUpdateMaxBookingTime_WhenProvided() {
        // 模拟座位存在
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        seatRequest.setMaxBookingTime(120);

        // 执行测试
        Seat result = seatService.updateSeat(1L, seatRequest);

        // 验证结果
        assertEquals(120, result.getMaxBookingTime());
        verify(seatRepository).save(testSeat);
    }

    @Test
    void updateSeat_ShouldFail_WhenSeatNotExists() {
        // 模拟座位不存在
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        // 验证抛出异常
        assertThrows(RuntimeException.class, () -> {
            seatService.updateSeat(1L, seatRequest);
        });
    }
}