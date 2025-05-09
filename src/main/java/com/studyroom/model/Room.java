package com.studyroom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int type;

    private int capacity;

    private String description;

    private Instant openTime ;

    private Instant closeTime ;

    private String location;

    @Enumerated(EnumType.ORDINAL)
    private RoomStatus status = RoomStatus.AVAILABLE;

    public enum RoomStatus {
        AVAILABLE, UNAVAILABLE
    }
}

//
//INSERT INTO room (name, type, capacity, description, open_time, close_time, location, status)
//VALUES
//        ('一号自习室', 1, 30, '靠近图书馆，有充电插座', '2025-05-07T08:00:00Z', '2025-05-07T22:00:00Z', '教学楼A-201', 0),
//  ('二号自习室', 2, 50, '安静区，适合长时间学习', '2025-05-07T07:00:00Z', '2025-05-07T23:00:00Z', '教学楼B-101', 0),
//          ('三号自习室', 1, 20, '靠窗小教室，采光好', '2025-05-07T09:00:00Z', '2025-05-07T20:00:00Z', '教学楼C-301', 0),
//          ('封闭讨论室', 3, 10, '支持小组讨论，配有白板', '2025-05-07T10:00:00Z', '2025-05-07T18:00:00Z', '教学楼D-109', 1);