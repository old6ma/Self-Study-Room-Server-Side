package com.studyroom.service;

import com.studyroom.dto.RoomRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    @PostConstruct
    public void init(){
        if (roomRepository.count() == 0) {
            List<Room> rooms = List.of(
                    new Room(null, "一号自习室", 1, 30, "靠近图书馆，有充电插座",
                            Instant.parse("2025-05-07T08:00:00Z"), Instant.parse("2025-05-07T22:00:00Z"),
                            "教学楼A-201", Room.RoomStatus.AVAILABLE),
                    new Room(null, "二号自习室", 2, 50, "安静区，适合长时间学习",
                            Instant.parse("2025-05-07T07:00:00Z"), Instant.parse("2025-05-07T23:00:00Z"),
                            "教学楼B-101", Room.RoomStatus.AVAILABLE),
                    new Room(null, "三号自习室", 1, 20, "靠窗小教室，采光好",
                            Instant.parse("2025-05-07T09:00:00Z"), Instant.parse("2025-05-07T20:00:00Z"),
                            "教学楼C-301", Room.RoomStatus.AVAILABLE),
                    new Room(null, "封闭讨论室", 3, 10, "支持小组讨论，配有白板",
                            Instant.parse("2025-05-07T10:00:00Z"), Instant.parse("2025-05-07T18:00:00Z"),
                            "教学楼D-109", Room.RoomStatus.UNAVAILABLE)
            );
            roomRepository.saveAll(rooms);
        }
    }

    public Room createRoom(RoomRequest roomRequest) {
        // 检查自习室是否已存在
        if (roomRepository.findByName(roomRequest.getName()).isPresent()) {
            throw new RuntimeException("Room already exists");
        }

        Room room = new Room();
        room.setName(roomRequest.getName());
        room.setType(roomRequest.getType());
        room.setCapacity(roomRequest.getCapacity());
        room.setOpenTime(Instant.ofEpochMilli(roomRequest.getOpenTime()));
        room.setCloseTime(Instant.ofEpochMilli(roomRequest.getCloseTime()));
        room.setLocation(roomRequest.getLocation());

        return roomRepository.save(room);
    }

    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        roomRepository.delete(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room updateRoom(Long roomId, RoomRequest roomRequest) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (roomRequest.getOpenTime() != null) {
            room.setOpenTime(Instant.ofEpochMilli(roomRequest.getOpenTime()));
        }

        if (roomRequest.getCloseTime() != null) {
            room.setCloseTime(Instant.ofEpochMilli(roomRequest.getCloseTime()));
        }

        if (roomRequest.getStatus() != null) {
            room.setStatus(Room.RoomStatus.valueOf(roomRequest.getStatus().toUpperCase()));
        }

        if (roomRequest.getCapacity() != null){
            room.setCapacity(roomRequest.getCapacity());
        }

        if (roomRequest.getLocation() != null) {
            room.setLocation(roomRequest.getLocation());
        }

        if (roomRequest.getType() != null) {
            room.setType(roomRequest.getType());
        }

        return roomRepository.save(room);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public Map<Room, Long> getRoomsWithAvailableSeats() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream().collect(Collectors.toMap(
                room -> room,
                room -> seatRepository.findByRoomId(room.getId()).stream()
                        .filter(seat -> seat.getStatus() == Seat.SeatStatus.AVAILABLE)
                        .count()
        ));
    }

    public List<Seat> searchSeats(String query) {
        return seatRepository.searchSeats(query);
    }

    @Transactional
    public Booking bookSeat(Student student, Long roomId, Long seatId,
                            Long startTime, Long endTime) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new RuntimeException("Seat is not available");
        }

        seat.setStatus(Seat.SeatStatus.OCCUPIED);
        seatRepository.save(seat);

        Booking booking = new Booking();
        booking.setStudent(student);
        booking.setSeat(seat);
        booking.setStartTime(Instant.ofEpochMilli(startTime));
        booking.setEndTime(Instant.ofEpochMilli(endTime));

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(Student student, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndStudent(bookingId, student)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Seat seat = booking.getSeat();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void temporaryLeaveSeat(Student student, Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        // 确认学生有预订此座位
        validateStudentBooking(student, seatId);

        seat.setStatus(Seat.SeatStatus.TEMPORARY_LEAVE);
        seatRepository.save(seat);
    }

    @Transactional
    public void checkInSeat(Student student, Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        validateStudentBooking(student, seatId);

        seat.setStatus(Seat.SeatStatus.OCCUPIED);
        seatRepository.save(seat);
    }

    @Transactional
    public void releaseSeat(Student student, Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        List<Booking> activeBookings = getActiveBookingForSeat(student, seatId);
        if (activeBookings.isEmpty()) {
            throw new RuntimeException("Student has not booked this seat");
        }

        Booking booking = activeBookings.get(0);
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }

    public List<Booking> getBookingHistory(Student student) {
        return bookingRepository.findByStudentOrderByStartTimeDesc(student);
    }

    private List<Booking> getActiveBookingForSeat(Student student, Long seatId) {
        return bookingRepository.findByStudentOrderByStartTimeDesc(student).stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.ACTIVE &&
                        booking.getSeat().getId().equals(seatId))
                .toList();
    }

    private void validateStudentBooking(Student student, Long seatId) {
        List<Booking> activeBookings = getActiveBookingForSeat(student, seatId);
        if (activeBookings.isEmpty()) {
            throw new RuntimeException("Student has not booked this seat");
        }
    }
}