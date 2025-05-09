package com.studyroom.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatName;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String seatNumber;

    private boolean hasSocket;//有无插座

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private Integer maxBookingTime = 120; // 默认最大预约时间（分钟）

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum SeatStatus {
        AVAILABLE, UNAVAILABLE, OCCUPIED, TEMPORARY_LEAVE
    }
}

//INSERT INTO seat (seat_name, room_id, seat_number, has_socket, status, max_booking_time)
//VALUES
//        ('靠窗座位A1', 1, 'A1', TRUE, 'AVAILABLE', 120),
//  ('靠墙座位B2', 1, 'B2', FALSE, 'AVAILABLE', 90),
//        ('中间座位C3', 1, 'C3', TRUE, 'OCCUPIED', 60),
//        ('安静区D4', 1, 'D4', FALSE, 'UNAVAILABLE', 120),
//        ('插座位E5', 1, 'E5', TRUE, 'TEMPORARY_LEAVE', 120);