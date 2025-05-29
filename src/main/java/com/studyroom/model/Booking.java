package com.studyroom.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private Instant startTime;

    private Instant endTime;

    private boolean violationRecorded; // 是否记录违约

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.ACTIVE;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum BookingStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED,

    }

}