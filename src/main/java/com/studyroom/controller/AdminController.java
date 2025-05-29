package com.studyroom.controller;

import com.studyroom.model.Seat;
import com.studyroom.repository.SeatRepository;
import com.studyroom.util.JwtUtil;
import com.studyroom.dto.*;
import com.studyroom.model.Booking;
import com.studyroom.model.Room;
import com.studyroom.service.AdminService;
import com.studyroom.service.BookingService;
import com.studyroom.service.RoomService;
import com.studyroom.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1.0/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RoomService roomService;
    private final SeatService seatService;
    private final BookingService bookingService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SeatRepository seatRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println(loginRequest.getUsername()+' '+loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            UserDetails userDetails = adminService.loadUserByUsername(loginRequest.getUsername());
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE"); // 默认角色
            if (authentication.isAuthenticated()) {
                String token = jwtUtil.generateToken(loginRequest.getUsername(),role);
                System.out.println(loginRequest.getUsername()+loginRequest.getPassword()+"successfully login!!!"+role);
                return ResponseEntity.ok(new LoginResponse(token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest roomRequest) {
        try {
            roomService.createRoom(roomRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Room created successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Room already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.ok(new ApiResponse("Room deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    //对每个座位都可以更新最大可预约时长
    @PutMapping("/seats/{seatId}/max-booking-time")
    public ResponseEntity<?> setMaxBookingTime(@PathVariable Long seatId, @RequestParam Integer minutes) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        seat.setMaxBookingTime(minutes);
        seatRepository.save(seat);
        return ResponseEntity.ok(Map.of("message", "Update maxBookingTime", "maxBookingTime", minutes));
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        List<Map<String, Object>> roomsResponse = rooms.stream()
                .map(room -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("room_id", room.getId());
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

    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable Long roomId, @RequestBody RoomRequest roomRequest) {
        try {
            roomService.updateRoom(roomId, roomRequest);
            return ResponseEntity.ok(new ApiResponse("Room updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
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
                        map.put("seat_id", seat.getId());
                        map.put("name", seat.getSeatName());
                        map.put("status", seat.getStatus());
                        map.put("has_socket",seat.isHasSocket());
                        map.put("maxBookingTime",seat.getMaxBookingTime());
                        map.put("ordering_list",bookingService.getAllBookingsBySeat(seat.getId()).stream()
                                .map(booking -> {
                                    Map<String, Object> imap = new HashMap<>();
                                    imap.put("student_id", booking.getStudent().getStudentId());
                                    imap.put("student_name", booking.getStudent().getName());
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

    @PostMapping("/seats")
    public ResponseEntity<?> addSeat(@RequestBody SeatRequest seatRequest) {
        try {
            seatService.addSeat(seatRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Seat added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/seats/{seatId}")
    public ResponseEntity<?> deleteSeat(@PathVariable Long seatId) {
        try {
            seatService.deleteSeat(seatId);
            System.out.println("deleteSeat by seatId");
            return ResponseEntity.ok(new ApiResponse("Seat deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/seats/{seatId}")
    public ResponseEntity<?> updateSeat(@PathVariable Long seatId, @RequestBody SeatRequest seatRequest) {
        try {
            seatService.updateSeat(seatId, seatRequest);
            return ResponseEntity.ok(new ApiResponse("Seat updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        List<Map<String, Object>> bookingsResponse = bookings.stream()
                .map(booking -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("room_id", booking.getRoom().getId());
                    map.put("seat_id", booking.getSeat().getSeatNumber());
                    map.put("user_id", booking.getStudent().getStudentId());
                    map.put("start_time", booking.getStartTime());
                    map.put("end_time", booking.getEndTime());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(Map.of("bookings", bookingsResponse));
    }
}