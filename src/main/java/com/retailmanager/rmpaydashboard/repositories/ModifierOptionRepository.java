package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.ModifierGroup;
import com.retailmanager.rmpaydashboard.models.ModifierOption;

public interface ModifierOptionRepository extends CrudRepository<ModifierOption, String> {
    List<ModifierOption> findByModifierGroupOrderBySortOrderAscNameAsc(ModifierGroup modifierGroup);
}
