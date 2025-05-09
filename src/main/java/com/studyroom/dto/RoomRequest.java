package com.studyroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomRequest {
    private String name;
    private Integer type;
    private String location;

    @JsonProperty("open_time")
    private Long openTime;

    @JsonProperty("close_time")
    private Long closeTime;
    private Integer capacity;
    private String status;
//    private String campus;
}