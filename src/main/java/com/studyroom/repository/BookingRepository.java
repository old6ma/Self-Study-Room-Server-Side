package com.studyroom.repository;

import com.studyroom.model.Booking;
import com.studyroom.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStudentOrderByStartTimeDesc(Student student);

    Optional<Booking> findByIdAndStudent(Long id, Student student);

    @Query("SELECT b FROM Booking b WHERE b.seat.id = :seatId AND " +
            "((b.startTime >= :dayStart AND b.startTime < :dayEnd) OR " +
            "(b.endTime > :dayStart AND b.endTime <= :dayEnd) OR " +
            "(b.startTime <= :dayStart AND b.endTime >= :dayEnd))")
    List<Booking> findTodayBookingsBySeatId(
            @Param("seatId") Long seatId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd);

    List<Booking> findByStudentIdOrderByStartTimeDesc(Long id);

    @Query("SELECT b FROM Booking b WHERE b.student.id = :studentId AND :now BETWEEN b.startTime AND b.endTime")
    Optional<Booking> findCurrentBookingByStudentId(@Param("studentId") Long studentId, @Param("now") Instant now);

    @Query("SELECT b FROM Booking b WHERE " +
            "b.status = 'ACTIVE' AND b.violationRecorded = false AND " +
            "b.startTime BETWEEN :startWindow AND :endWindow")
    List<Booking> findPendingCheckIn(@Param("startWindow") Instant startWindow,
                                     @Param("endWindow") Instant endWindow);



}