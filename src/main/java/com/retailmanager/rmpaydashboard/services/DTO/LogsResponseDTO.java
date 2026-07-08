package com.retailmanager.rmpaydashboard.services.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogsResponseDTO {

    private Long id;
    private String level;
    private String tag;
    private String message;
    private String stacktrace;
    private String serial;
    private Instant createdAt;
}