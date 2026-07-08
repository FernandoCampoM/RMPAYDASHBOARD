package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScheduleCalendarDTO {
    Long id;
    @NotBlank(message = "{scheduleCalendar.title.empty}")
    String title;
    @NotNull(message = "{scheduleCalendar.start.empty}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateStart;
    @NotNull(message = "{scheduleCalendar.end.empty}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateEnd;
    @NotBlank(message = "{scheduleCalendar.color.empty}")
    String color;
   
    Long employeeId;

}
