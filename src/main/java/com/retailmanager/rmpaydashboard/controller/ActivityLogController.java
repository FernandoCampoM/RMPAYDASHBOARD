package com.retailmanager.rmpaydashboard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.models.enums.ActivityType;
import com.retailmanager.rmpaydashboard.services.DTO.ActivityLogCreateDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ActivityLogResponseDTO;
import com.retailmanager.rmpaydashboard.services.services.ActivityLogService.IActivityLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activity-logs")
@Validated
public class ActivityLogController {

    private final IActivityLogService activityLogService;

    public ActivityLogController(
        IActivityLogService activityLogService
    ) {
        this.activityLogService = activityLogService;
    }

    @PostMapping
    public ResponseEntity<ActivityLogResponseDTO> createActivity(
        @RequestBody @Valid ActivityLogCreateDTO dto
    ) {
        ActivityLogResponseDTO response =
            activityLogService.createActivity(dto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityLogResponseDTO>> getRecentActivities(
        @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(
            activityLogService.getRecentActivities(limit)
        );
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<ActivityLogResponseDTO>>
        getActivitiesByBusiness(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "20") int limit
        ) {
        return ResponseEntity.ok(
            activityLogService.getRecentActivitiesByBusiness(
                businessId,
                limit
            )
        );
    }

    @GetMapping("/type/{activityType}")
    public ResponseEntity<List<ActivityLogResponseDTO>>
        getActivitiesByType(
            @PathVariable ActivityType activityType,
            @RequestParam(defaultValue = "20") int limit
        ) {
        return ResponseEntity.ok(
            activityLogService.getActivitiesByType(
                activityType,
                limit
            )
        );
    }
}