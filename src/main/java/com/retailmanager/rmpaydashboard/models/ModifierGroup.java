package com.retailmanager.rmpaydashboard.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class ModifierGroup {

    @Id
    private String modifierGroupId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "businessId", nullable = false)
    private Business business;

    @Column(nullable = false)
    private Integer productId = 0;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean required = false;

    @Column(nullable = false)
    private boolean multiSelect = false;

    @Column(nullable = false)
    private Integer maxSelections = 1;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private boolean enable = true;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "modifierGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModifierOption> options = new ArrayList<>();

    public void setOptions(List<ModifierOption> options) {
        this.options.clear();
        if (options != null) {
            options.forEach(this::addOption);
        }
    }

    public void addOption(ModifierOption option) {
        option.setModifierGroup(this);
        option.setBusiness(this.business);
        this.options.add(option);
    }

    @PrePersist
    private void prePersist() {
        if (modifierGroupId == null || modifierGroupId.isBlank()) {
            modifierGroupId = UUID.randomUUID().toString();
        }
        if (productId == null) productId = 0;
        if (maxSelections == null || maxSelections < 1) maxSelections = 1;
        if (sortOrder == null) sortOrder = 0;
        createdAt = createdAt != null ? createdAt : Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        if (productId == null) productId = 0;
        if (maxSelections == null || maxSelections < 1) maxSelections = 1;
        if (sortOrder == null) sortOrder = 0;
        updatedAt = Instant.now();
    }
}
