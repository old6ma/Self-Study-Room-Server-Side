package com.studyroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SeatRequest {
    private Long roomId;
    private String seatNumber;
    private String seatName;
    private Boolean hasSocket;
    private String Status;
    private int MaxBookingTime;

}