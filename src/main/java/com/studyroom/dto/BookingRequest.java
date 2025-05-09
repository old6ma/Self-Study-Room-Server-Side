package com.studyroom.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private Long seatId;
    private Long roomId;
    private Long startTime;
    private Long endTime;
}