package com.studyroom.service;

import com.studyroom.dto.BookingRequest;
import com.studyroom.model.Booking;
import com.studyroom.model.Room;
import com.studyroom.model.Seat;
import com.studyroom.model.Student;
import com.studyroom.repository.BookingRepository;
import com.studyroom.repository.RoomRepository;
import com.studyroom.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final RoomService roomService;
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * 返回当前座位今天的所有预定情况
     * @param seatId
     * @return
     */
    public List<Booking> getAllBookingsBySeat(Long seatId) {
        Instant dayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant dayEnd = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        return bookingRepository.findTodayBookingsBySeatId(seatId, dayStart, dayEnd);
    }

    public List<Booking> getAllBookingsByStudentId(Long studentId) {
        return bookingRepository.findByStudentIdOrderByStartTimeDesc(studentId);
    }

    public void bookSeat(Student student, BookingRequest bookingRequest) {
        Seat seat = seatRepository.findByRoomIdSeatId(bookingRequest.getRoomId(),bookingRequest.getSeatId())
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new RuntimeException("Seat is not available");
        }
        long durationInMinutes = Duration.between(Instant.ofEpochMilli(bookingRequest.getStartTime()), Instant.ofEpochMilli(bookingRequest.getEndTime())).toMinutes();
        if (durationInMinutes > seat.getMaxBookingTime()) {
            throw new RuntimeException("Booking Duration should less than max booking time");
        }

        Booking booking = new Booking();
        booking.setStudent(student);
        booking.setSeat(seat);
//        Instant now=Instant.ofEpochMilli(bookingRequest.getStartTime());
        booking.setStartTime(Instant.ofEpochMilli(bookingRequest.getStartTime()).plusSeconds(8 * 60 * 60));
        booking.setEndTime(Instant.ofEpochMilli(bookingRequest.getEndTime()).plusSeconds(8 * 60 * 60));
        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        booking.setRoom(room);

        bookingRepository.save(booking);

        seat.setStatus(Seat.SeatStatus.OCCUPIED);
        seatRepository.save(seat);
    }

//    public void checkIn(Long bookingId, Long studentId) {
//        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
//        if (!booking.getStudent().getId().equals(studentId)) {
//            throw new RuntimeException("Unauthorized");
//        }
//        if (booking.getStatus()==Booking.BookingStatus.COMPLETED) {
//            throw new RuntimeException("Already checked in");
//        }
//        booking.setStatus(Booking.BookingStatus.COMPLETED);
////        booking.setCheckInTime(LocalDateTime.now());
//        bookingRepository.save(booking);
//    }
    @Transactional
    public void checkIn(Student student,Long seatId) {
        Long studentId = student.getId();
//        Instant now = ZonedDateTime.now().toInstant()
        Instant now = ZonedDateTime.now().toInstant().plusSeconds(8 * 60 * 60).plusSeconds(15*60);
//        Instant local_now=ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant();
//        System.out.println("Local (CST): " + ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
//        System.out.println("UTC Instant: " + ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).toInstant());
//        System.out.println("Local_now (CST): " + local_now);
        List<Booking> bookings = bookingRepository.findCurrentBookingByStudentId(studentId, now);

        Booking activeBooking = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("当前没有可签到的预约记录"));

        if (activeBooking.getStatus()==Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("已经签到");
        }

//        roomService.checkInSeat(student, seatId);
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
//        roomService.validateStudentBooking(student, seatId);
        System.out.println("旧状态: " + seat.getStatus());
        seat.setStatus(Seat.SeatStatus.OCCUPIED);
        seatRepository.save(seat);
        seatRepository.flush(); // 强制同步到数据库
        Seat seat1 = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        System.out.println("新状态: " + seat1.getStatus());
        activeBooking.setStatus(Booking.BookingStatus.COMPLETED);
//        booking.setCheckInTime(LocalDateTime.now());
        bookingRepository.save(activeBooking);
    }

    public void notify(Booking booking, String message) {
        Student student = booking.getStudent();
        String info = String.format("【通知】学生: %s, 座位: %s, 时间: %s - %s\n消息: %s",
                student.getName(),
                booking.getSeat().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                message);
        System.out.println(info);
    }

    //提醒签到以及记录违约 但是还没有实现邮箱或者短信通知
    @Scheduled(fixedRate = 60000)
    public void monitorBookings() {
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(30 * 60);
        Instant windowEnd = now.plusSeconds(15 * 60);
        List<Booking> bookings = bookingRepository.findPendingCheckIn(windowStart, windowEnd);
        System.out.println("正在持续检查是否有临近预约记录");
        for (Booking booking : bookings) {
            long minutes = ChronoUnit.MINUTES.between(booking.getStartTime(), now);
            if (minutes >= -15 && minutes < 0) {
                notify(booking, "预约即将开始，请及时签到");
            } else if (minutes >= 0&& minutes < 15 && booking.getStatus()!=Booking.BookingStatus.COMPLETED) {
                notify(booking, "你还未签到，请尽快签到");
            } else if (minutes >= 15 && booking.getStatus()!=Booking.BookingStatus.COMPLETED && !booking.isViolationRecorded()) {
                booking.setViolationRecorded(true);
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                Seat seat = seatRepository.findById(booking.getSeat().getId())
                        .orElseThrow(() -> new RuntimeException("Seat not found"));
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seatRepository.save(seat);// 释放座位
                notify(booking, "预约已取消并记录违约");
            }
        }
    }

    //自动把一些已经超出结束时间的预约的座位设置为空闲
    @Scheduled(fixedRate = 1000)
    public void handlePassedBookings(){
        List<Booking> bookings = bookingRepository.findAll();
        Instant now = Instant.now();
        for (Booking booking : bookings) {
            long minutes = ChronoUnit.MINUTES.between(booking.getEndTime(), now);
            if (minutes>=0){
                Seat seat = seatRepository.findById(booking.getSeat().getId())
                        .orElseThrow(() -> new RuntimeException("Seat not found"));
                if (seat.getStatus()==Seat.SeatStatus.OCCUPIED) {
                    seat.setStatus(Seat.SeatStatus.AVAILABLE);
                    seatRepository.save(seat);
                }
            }
        }
    }



}