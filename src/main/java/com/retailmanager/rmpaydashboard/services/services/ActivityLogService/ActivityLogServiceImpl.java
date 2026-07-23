package com.retailmanager.rmpaydashboard.services.services.ActivityLogService;


import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpaydashboard.models.ActivityLog;
import com.retailmanager.rmpaydashboard.models.enums.ActivityType;
import com.retailmanager.rmpaydashboard.repositories.ActivityLogRepository;
import com.retailmanager.rmpaydashboard.services.DTO.ActivityLogCreateDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ActivityLogResponseDTO;
import com.retailmanager.rmpaydashboard.services.mapper.ActivityLogMapper;
import com.retailmanager.rmpaydashboard.services.services.ActivityLogService.IActivityLogService;

@Service
public class ActivityLogServiceImpl implements IActivityLogService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 100;

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    private final ObjectMapper objectMapper;

    public ActivityLogServiceImpl(
        ActivityLogRepository activityLogRepository,
        ActivityLogMapper activityLogMapper,
        ObjectMapper objectMapper
    ) {
        this.activityLogRepository = activityLogRepository;
        this.activityLogMapper = activityLogMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ActivityLogResponseDTO createActivity(ActivityLogCreateDTO dto) {
        ActivityLog activityLog = new ActivityLog();

        activityLog.setActivityType(dto.getActivityType());
        activityLog.setTitle(dto.getTitle());
        activityLog.setDetail(dto.getDetail());
        activityLog.setEntityType(dto.getEntityType());
        activityLog.setEntityId(dto.getEntityId());
        activityLog.setBusinessId(dto.getBusinessId());
        activityLog.setUserId(dto.getUserId());
        activityLog.setUserName(dto.getUserName());

        Instant now = Instant.now();

        activityLog.setOccurredAt(
            dto.getOccurredAt() != null
                ? dto.getOccurredAt()
                : now
        );

        activityLog.setCreatedAt(now);
        activityLog.setAdditionalData(
            serializeAdditionalData(dto.getAdditionalData())
        );

        ActivityLog savedActivity =
            activityLogRepository.save(activityLog);

        return activityLogMapper.toResponseDTO(savedActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponseDTO> getRecentActivities(int limit) {
        int safeLimit = normalizeLimit(limit);

        return activityLogRepository
            .findAllByOrderByOccurredAtDesc(
                PageRequest.of(0, safeLimit)
            )
            .stream()
            .map(activityLogMapper::toResponseDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponseDTO> getRecentActivitiesByBusiness(
        Long businessId,
        int limit
    ) {
        int safeLimit = normalizeLimit(limit);

        return activityLogRepository
            .findByBusinessIdOrderByOccurredAtDesc(
                businessId,
                PageRequest.of(0, safeLimit)
            )
            .stream()
            .map(activityLogMapper::toResponseDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponseDTO> getActivitiesByType(
        ActivityType activityType,
        int limit
    ) {
        int safeLimit = normalizeLimit(limit);

        return activityLogRepository
            .findByActivityTypeOrderByOccurredAtDesc(
                activityType,
                PageRequest.of(0, safeLimit)
            )
            .stream()
            .map(activityLogMapper::toResponseDTO)
            .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private String serializeAdditionalData(
        Object additionalData
    ) {
        if (additionalData == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(additionalData);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                "No fue posible serializar additionalData.",
                exception
            );
        }
    }
}