package com.studyroom.service;

import com.studyroom.dto.BookingRequest;
import com.studyroom.dto.SeatRequest;
import com.studyroom.model.Booking;
import com.studyroom.model.Room;
import com.studyroom.model.Seat;
import com.studyroom.model.Student;
import com.studyroom.repository.BookingRepository;
import com.studyroom.repository.RoomRepository;
import com.studyroom.repository.SeatRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;


    @PostConstruct
    public void init() {
        if (seatRepository.count() == 0) {
            Room room = roomRepository.findById(1L).orElseThrow(); // 确保 room 存在
            seatRepository.saveAll(List.of(
                    new Seat(null, "靠窗座位A1", room, "A1", true, Seat.SeatStatus.AVAILABLE, 120),
                    new Seat(null, "靠墙座位B2", room, "B2", false, Seat.SeatStatus.AVAILABLE, 90),
                    new Seat(null, "中间座位C3", room, "C3", true, Seat.SeatStatus.OCCUPIED, 60),
                    new Seat(null, "安静区D4", room, "D4", false, Seat.SeatStatus.UNAVAILABLE, 120),
                    new Seat(null, "插座位E5", room, "E5", true, Seat.SeatStatus.TEMPORARY_LEAVE, 120)
            ));
        }
    }

    public Seat addSeat(SeatRequest seatRequest) {
        Room room = roomRepository.findById(seatRequest.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 检查座位是否已存在
        if (seatRepository.findByRoomAndSeatNumber(room, seatRequest.getSeatName()).isPresent()) {
            throw new RuntimeException("Seat already exists in this room");
        }

        Seat seat = new Seat();
        seat.setRoom(room);
        seat.setSeatName(seatRequest.getSeatName());
        seat.setSeatNumber(seatRequest.getSeatName());
        seat.setHasSocket(seatRequest.getHasSocket());

        return seatRepository.save(seat);
    }

    public void bookSeat(Student student, BookingRequest bookingRequest) {
        Seat seat = seatRepository.findByRoomIdSeatId(bookingRequest.getRoomId(),bookingRequest.getSeatId())
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new RuntimeException("Seat is not available");
        }

        seat.setStatus(Seat.SeatStatus.OCCUPIED);
        seatRepository.save(seat);

        Booking booking = new Booking();
        booking.setStudent(student);
        booking.setSeat(seat);
        booking.setStartTime(Instant.ofEpochMilli(bookingRequest.getStartTime()));
        booking.setEndTime(Instant.ofEpochMilli(bookingRequest.getEndTime()));
        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        booking.setRoom(room);

        bookingRepository.save(booking);
    }

    public void deleteSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        seatRepository.delete(seat);
    }

    public Seat updateSeat(Long seatId, SeatRequest seatRequest) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        return seatRepository.save(seat);
    }

    public List<Seat> getSeats(Long roomId){
        return seatRepository.findByRoomId(roomId);
    }
}