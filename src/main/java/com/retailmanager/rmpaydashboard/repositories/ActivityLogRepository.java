package com.retailmanager.rmpaydashboard.repositories;


import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.retailmanager.rmpaydashboard.models.ActivityLog;
import com.retailmanager.rmpaydashboard.models.enums.ActivityEntityType;
import com.retailmanager.rmpaydashboard.models.enums.ActivityType;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByOccurredAtDesc(Pageable pageable);

    List<ActivityLog> findByBusinessIdOrderByOccurredAtDesc(
        Long businessId,
        Pageable pageable
    );

    List<ActivityLog> findByActivityTypeOrderByOccurredAtDesc(
        ActivityType activityType,
        Pageable pageable
    );

    List<ActivityLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
       ActivityEntityType entityType,
        String entityId,
        Pageable pageable
    );
    List<ActivityLog> findByActivityTypeAndOccurredAtBetweenOrderByOccurredAtDesc(
        ActivityType activityType,
        Instant startDate,
        Instant endDate
);
}