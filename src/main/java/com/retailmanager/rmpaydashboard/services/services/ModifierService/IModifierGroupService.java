package com.retailmanager.rmpaydashboard.services.services.ModifierService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.ModifierGroupDTO;

public interface IModifierGroupService {
    ResponseEntity<?> save(ModifierGroupDTO modifierGroupDTO);
    ResponseEntity<?> update(String modifierGroupId, ModifierGroupDTO modifierGroupDTO);
    boolean delete(String modifierGroupId);
    ResponseEntity<?> findById(String modifierGroupId);
    ResponseEntity<?> findByBusinessId(Long businessId);
}
