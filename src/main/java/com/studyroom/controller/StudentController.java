package com.studyroom.controller;

import com.studyroom.dto.BookingRequest;
import com.studyroom.dto.LoginRequest;
import com.studyroom.model.Booking;
import com.studyroom.model.Room;
import com.studyroom.model.Seat;
import com.studyroom.model.Student;
import com.studyroom.repository.BookingRepository;
import com.studyroom.repository.RoomRepository;
import com.studyroom.repository.SeatRepository;
import com.studyroom.service.BookingService;
import com.studyroom.service.SeatService;
import com.studyroom.util.JwtUtil;
import com.studyroom.service.RoomService;
import com.studyroom.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1.0/student")
@RequiredArgsConstructor
public class StudentController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StudentService studentService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final SeatService seatService;
    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        String jwt = jwtUtil.generateToken(loginRequest.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        System.out.println(loginRequest.getUsername()+loginRequest.getPassword()+"successfully login");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/seats/{seatId}/leave")
    public ResponseEntity<?> leaveSeat(@PathVariable Long seatId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Student student = studentService.findByUsername(authentication.getName());

        roomService.temporaryLeaveSeat(student, seatId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Seat set to leave status");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/seats/book")
    public ResponseEntity<?> bookSeat(@RequestBody BookingRequest bookingRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Student student = studentService.findByUsername(authentication.getName());

        bookingService.bookSeat(
                student,
                bookingRequest
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "Seat booked successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/seats/{seatId}/release")
    public ResponseEntity<?> releaseSeat(@PathVariable Long seatId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Student student = studentService.findByUsername(authentication.getName());

        roomService.releaseSeat(student, seatId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Seat released successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings/history")
    public ResponseEntity<?> getBookingHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Student student = studentService.findByUsername(authentication.getName());

        List<Booking> bookings = bookingService.getAllBookingsByStudentId(student.getId());

        List<Map<String, Object>> history = bookings.stream().map(booking -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("booking_id", booking.getId());
            entry.put("room_id", booking.getSeat().getRoom().getId());
            entry.put("seat_id", booking.getSeat().getId());
            entry.put("start_time", booking.getStartTime());
            entry.put("end_time", booking.getEndTime());
            return entry;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("history", history);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取roomId下的所有座位信息
     * @param roomId
     * @return
     */
    @GetMapping("/rooms/{roomId}/seats")
    public ResponseEntity<?> getSeatsByRoom(@PathVariable Long roomId) {
        try {
            List<Seat> seats = seatService.getSeats(roomId);
            List<Map<String, Object>> seatsResponse = seats.stream()
                    .map(seat->{
                        Map<String, Object> map = new HashMap<>();
                        map.put("seat_id", seat.getId().toString());
                        map.put("name", seat.getSeatName());
                        map.put("status", seat.getStatus());
                        map.put("has_socket",seat.isHasSocket());
                        map.put("ordering_list",bookingService.getAllBookingsBySeat(seat.getId()).stream()
                                .map(booking -> {
                                    Map<String, Object> imap = new HashMap<>();
                                    imap.put("start_time", booking.getStartTime());
                                    imap.put("end_time", booking.getEndTime());
                                    return imap;
                                }).toList());
                        return map;
                    })
                    .toList();
            return ResponseEntity.ok(Map.of("seats", seatsResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/seats/{seatId}/checkin")
    public ResponseEntity<?> checkInSeat(@PathVariable Long seatId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Student student = studentService.findByUsername(authentication.getName());

        bookingService.checkIn(student,seatId);
//        roomService.checkInSeat(student, seatId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Checked in successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        List<Map<String, Object>> roomsResponse = rooms.stream()
                .map(room -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("room_id", room.getId().toString());
                    map.put("name", room.getName());
                    map.put("location", room.getLocation());
                    map.put("status", room.getStatus());
                    map.put("type",room.getType());
                    map.put("seat_number",seatService.getSeats(room.getId()).size());
                    map.put("capacity",room.getCapacity());
                    map.put("open_time",room.getOpenTime());
                    map.put("close_time",room.getCloseTime());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(Map.of("rooms", roomsResponse));
    }


    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Student student = studentService.findByUsername(authentication.getName());

        try {
            roomService.cancelBooking(student, bookingId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Booking cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Booking not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    //每个座位都有临时抢占功能（默认一小时，若所剩空余时间不足一小时则直接抢占所有时间）
    @PostMapping("/seats/{seatId}/occupy-now")
    public ResponseEntity<?> occupyNow(@PathVariable Long seatId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Student student = studentService.findByUsername(auth.getName());

        Instant now = Instant.now();
        Instant endTime=Instant.now();
        List<Booking> bookings = bookingService.getAllBookingsBySeat(seatId);

        // 检查当前是否空闲
        for (Booking booking : bookings) {
            Seat seat=booking.getSeat();
            if (!Objects.equals(seat.getId(), seatId)) {
                continue;
            }
            long minutes1 = ChronoUnit.MINUTES.between(booking.getStartTime(), now);
            long minutes2 = ChronoUnit.MINUTES.between(booking.getEndTime(), now);
            if (minutes1 >= 0&& minutes2 <= 0 ) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "该座位当前不可用"));
            }
            // 设定占用时间段，比如从 now 到下一个整点或配置默认时间（如1小时）
            endTime = now.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS);
            if (endTime.isBefore(booking.getStartTime())) {
                endTime = endTime.plus(1, ChronoUnit.HOURS);
            }else{
                endTime=booking.getStartTime();
            }
            seat.setStatus(Seat.SeatStatus.OCCUPIED);
            seatRepository.save(seat);

            Booking new_booking = new Booking();
            new_booking.setStudent(student);
            new_booking.setSeat(seat);
            new_booking.setStartTime(now);
            new_booking.setEndTime(endTime);
            Room room = roomRepository.findById(seat.getRoom().getId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            booking.setRoom(room);

            bookingRepository.save(booking);
            break;
        }
        return ResponseEntity.ok(Map.of("message", "座位抢占成功", "startTime", now, "endTime", endTime));
    }
}