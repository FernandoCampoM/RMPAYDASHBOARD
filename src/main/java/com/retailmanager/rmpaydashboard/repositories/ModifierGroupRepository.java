package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.ModifierGroup;

public interface ModifierGroupRepository extends CrudRepository<ModifierGroup, String> {
    List<ModifierGroup> findByBusinessOrderBySortOrderAscNameAsc(Business business);

    @Query("SELECT m FROM ModifierGroup m WHERE m.business.businessId = :businessId AND m.name = :name")
    Optional<ModifierGroup> findByNameAndBusinessId(String name, Long businessId);
}
