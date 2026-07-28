package com.retailmanager.rmpaydashboard.models;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifierOption {

    @Id
    private String modifierOptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modifierGroupId", nullable = false)
    private ModifierGroup modifierGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "businessId", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double additionalPrice = 0.0;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        if (modifierOptionId == null || modifierOptionId.isBlank()) {
            modifierOptionId = UUID.randomUUID().toString();
        }
        if (additionalPrice == null) additionalPrice = 0.0;
        if (sortOrder == null) sortOrder = 0;
        createdAt = createdAt != null ? createdAt : Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        if (additionalPrice == null) additionalPrice = 0.0;
        if (sortOrder == null) sortOrder = 0;
        updatedAt = Instant.now();
    }
}
