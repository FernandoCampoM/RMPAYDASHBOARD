package com.retailmanager.rmpaydashboard.services.mapper;


import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpaydashboard.models.ActivityLog;
import com.retailmanager.rmpaydashboard.services.DTO.ActivityLogResponseDTO;

@Component
public class ActivityLogMapper {

    private final ObjectMapper objectMapper;

    public ActivityLogMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ActivityLogResponseDTO toResponseDTO(ActivityLog activityLog) {
        ActivityLogResponseDTO dto = new ActivityLogResponseDTO();

        dto.setActivityId(activityLog.getActivityLogId());
        dto.setType(activityLog.getActivityType());
        dto.setTitle(activityLog.getTitle());
        dto.setDetail(activityLog.getDetail());
        dto.setOccurredAt(activityLog.getOccurredAt());
        dto.setEntityType(activityLog.getEntityType());
        dto.setEntityId(activityLog.getEntityId());
        dto.setBusinessId(activityLog.getBusinessId());
        dto.setUserId(activityLog.getUserId());
        dto.setUserName(activityLog.getUserName());
        dto.setAdditionalData(
            deserializeAdditionalData(activityLog.getAdditionalData())
        );

        return dto;
    }

    private Map<String, Object> deserializeAdditionalData(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(
                json,
                new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception exception) {
            return Collections.emptyMap();
        }
    }
}