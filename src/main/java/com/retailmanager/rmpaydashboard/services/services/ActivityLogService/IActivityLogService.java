package com.retailmanager.rmpaydashboard.services.services.ActivityLogService;

import java.util.List;

import com.retailmanager.rmpaydashboard.models.enums.ActivityType;
import com.retailmanager.rmpaydashboard.services.DTO.ActivityLogCreateDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ActivityLogResponseDTO;

public interface IActivityLogService {

    ActivityLogResponseDTO createActivity(ActivityLogCreateDTO dto);

    List<ActivityLogResponseDTO> getRecentActivities(int limit);

    List<ActivityLogResponseDTO> getRecentActivitiesByBusiness(
        Long businessId,
        int limit
    );

    List<ActivityLogResponseDTO> getActivitiesByType(
        ActivityType activityType,
        int limit
    );
}