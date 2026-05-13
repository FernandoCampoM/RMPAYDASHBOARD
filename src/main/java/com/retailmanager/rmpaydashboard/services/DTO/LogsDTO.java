package com.retailmanager.rmpaydashboard.services.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogsDTO {
    private String level;
    private String tag;
    private String message;
    private String stacktrace;
    private String serial;
    private String terminalId;
}
