package com.retailmanager.rmpaydashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.ModifierGroupDTO;
import com.retailmanager.rmpaydashboard.services.services.ModifierService.IModifierGroupService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api")
@Validated
public class ModifierGroupController {

    @Autowired
    private IModifierGroupService modifierGroupService;

    @PostMapping("/modifier-groups")
    public ResponseEntity<?> save(@Valid @RequestBody ModifierGroupDTO modifierGroupDTO) {
        return modifierGroupService.save(modifierGroupDTO);
    }

    @PutMapping("/modifier-groups/{modifierGroupId}")
    public ResponseEntity<?> update(@Valid @PathVariable @NotBlank String modifierGroupId,
            @Valid @RequestBody ModifierGroupDTO modifierGroupDTO) {
        return modifierGroupService.update(modifierGroupId, modifierGroupDTO);
    }

    @DeleteMapping("/modifier-groups/{modifierGroupId}")
    public boolean delete(@Valid @PathVariable @NotBlank String modifierGroupId) {
        return modifierGroupService.delete(modifierGroupId);
    }

    @GetMapping("/modifier-groups/{modifierGroupId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @NotBlank String modifierGroupId) {
        return modifierGroupService.findById(modifierGroupId);
    }

    @GetMapping("/modifier-groups/business/{businessId}")
    public ResponseEntity<?> findByBusinessId(@Valid @PathVariable @Positive Long businessId) {
        return modifierGroupService.findByBusinessId(businessId);
    }
}
