package com.retailmanager.rmpaydashboard.services.services.ModifierService;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.ModifierGroup;
import com.retailmanager.rmpaydashboard.models.ModifierOption;
import com.retailmanager.rmpaydashboard.models.Product;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.repositories.ModifierGroupRepository;
import com.retailmanager.rmpaydashboard.repositories.ProductRepository;
import com.retailmanager.rmpaydashboard.services.DTO.ModifierGroupDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ModifierOptionDTO;

@Service
public class ModifierGroupService implements IModifierGroupService {

    @Autowired
    private ModifierGroupRepository modifierGroupRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public ResponseEntity<?> save(ModifierGroupDTO modifierGroupDTO) {
        if (modifierGroupDTO.getModifierGroupId() != null
                && modifierGroupRepository.existsById(modifierGroupDTO.getModifierGroupId())) {
            throw new EntidadYaExisteException("El grupo de modificadores con id "
                    + modifierGroupDTO.getModifierGroupId() + " ya existe en la Base de datos");
        }

        Optional<ModifierGroup> existsByName = modifierGroupRepository.findByNameAndBusinessId(
                modifierGroupDTO.getName(), modifierGroupDTO.getBusinesId());
        if (existsByName.isPresent()) {
            throw new EntidadYaExisteException("El negocio con Id " + modifierGroupDTO.getBusinesId()
                    + " ya tiene un grupo de modificadores con nombre " + modifierGroupDTO.getName());
        }

        Business business = findBusiness(modifierGroupDTO.getBusinesId());
        ModifierGroup modifierGroup = toEntity(modifierGroupDTO, business, null);
        modifierGroup = modifierGroupRepository.save(modifierGroup);
        return new ResponseEntity<>(toDto(modifierGroup), HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(String modifierGroupId, ModifierGroupDTO modifierGroupDTO) {
        ModifierGroup modifierGroup = modifierGroupRepository.findById(modifierGroupId)
                .orElseThrow(() -> new EntidadNoExisteException("El grupo de modificadores con id "
                        + modifierGroupId + " no existe en la Base de datos"));

        if (!modifierGroup.getName().equals(modifierGroupDTO.getName())) {
            Optional<ModifierGroup> existsByName = modifierGroupRepository.findByNameAndBusinessId(
                    modifierGroupDTO.getName(), modifierGroup.getBusiness().getBusinessId());
            if (existsByName.isPresent()) {
                throw new EntidadYaExisteException("El negocio con Id "
                        + modifierGroup.getBusiness().getBusinessId()
                        + " ya tiene un grupo de modificadores con nombre " + modifierGroupDTO.getName());
            }
        }

        ModifierGroup updated = toEntity(modifierGroupDTO, modifierGroup.getBusiness(), modifierGroup);
        updated = modifierGroupRepository.save(updated);
        return new ResponseEntity<>(toDto(updated), HttpStatus.OK);
    }

    @Override
    @Transactional
    public boolean delete(String modifierGroupId) {
        Optional<ModifierGroup> optional = modifierGroupRepository.findById(modifierGroupId);
        if (optional.isPresent()) {
            ModifierGroup modifierGroup = optional.get();
            for (Product product : productRepository.findProductsByModifierGroupId(modifierGroupId)) {
                product.getModifierGroups().removeIf(group -> modifierGroupId.equals(group.getModifierGroupId()));
                productRepository.save(product);
            }
            modifierGroupRepository.delete(modifierGroup);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(String modifierGroupId) {
        ModifierGroup modifierGroup = modifierGroupRepository.findById(modifierGroupId)
                .orElseThrow(() -> new EntidadNoExisteException("El grupo de modificadores con id "
                        + modifierGroupId + " no existe en la Base de datos"));
        return new ResponseEntity<>(toDto(modifierGroup), HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByBusinessId(Long businessId) {
        Business business = findBusiness(businessId);
        List<ModifierGroupDTO> modifiers = modifierGroupRepository.findByBusinessOrderBySortOrderAscNameAsc(business)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(modifiers, HttpStatus.OK);
    }

    private Business findBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new EntidadNoExisteException("El business con businessId "
                        + businessId + " no existe en la Base de datos"));
    }

    private ModifierGroup toEntity(ModifierGroupDTO dto, Business business, ModifierGroup current) {
        ModifierGroup modifierGroup = current != null ? current : new ModifierGroup();
        modifierGroup.setModifierGroupId(resolveId(dto.getModifierGroupId(), modifierGroup.getModifierGroupId()));
        modifierGroup.setBusiness(business);
        modifierGroup.setProductId(dto.getProductId() != null ? dto.getProductId() : 0);
        modifierGroup.setName(dto.getName());
        modifierGroup.setRequired(Boolean.TRUE.equals(dto.getRequired()));
        modifierGroup.setMultiSelect(Boolean.TRUE.equals(dto.getMultiSelect()));
        modifierGroup.setMaxSelections(dto.getMaxSelections() != null && dto.getMaxSelections() > 0 ? dto.getMaxSelections() : 1);
        modifierGroup.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        modifierGroup.setEnable(dto.getEnable() == null || dto.getEnable());

        List<ModifierOption> currentOptions = new ArrayList<>(modifierGroup.getOptions());
        List<ModifierOption> options = dto.getOptions() == null ? List.of() : dto.getOptions().stream()
                .map(optionDto -> toEntity(optionDto, business, modifierGroup, currentOptions))
                .collect(Collectors.toList());
        modifierGroup.setOptions(options);
        return modifierGroup;
    }

    private ModifierOption toEntity(ModifierOptionDTO dto, Business business, ModifierGroup modifierGroup,
            List<ModifierOption> currentOptions) {
        ModifierOption option = findCurrentOption(dto.getModifierOptionId(), currentOptions).orElse(new ModifierOption());
        option.setModifierOptionId(resolveId(dto.getModifierOptionId(), null));
        option.setModifierGroup(modifierGroup);
        option.setBusiness(business);
        option.setName(dto.getName());
        option.setAdditionalPrice(dto.getAdditionalPrice() != null ? dto.getAdditionalPrice() : 0.0);
        option.setEnabled(dto.getEnabled() == null || dto.getEnabled());
        option.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        return option;
    }

    private Optional<ModifierOption> findCurrentOption(String modifierOptionId, List<ModifierOption> currentOptions) {
        if (modifierOptionId == null || modifierOptionId.isBlank()) return Optional.empty();
        return currentOptions.stream()
                .filter(option -> modifierOptionId.equals(option.getModifierOptionId()))
                .findFirst();
    }

    private String resolveId(String dtoId, String currentId) {
        if (currentId != null && !currentId.isBlank()) return currentId;
        if (dtoId != null && !dtoId.isBlank()) return dtoId;
        return UUID.randomUUID().toString();
    }

    private ModifierGroupDTO toDto(ModifierGroup modifierGroup) {
        ModifierGroupDTO dto = new ModifierGroupDTO();
        dto.setModifierGroupId(modifierGroup.getModifierGroupId());
        dto.setBusinesId(modifierGroup.getBusiness().getBusinessId());
        dto.setProductId(modifierGroup.getProductId());
        dto.setName(modifierGroup.getName());
        dto.setRequired(modifierGroup.isRequired());
        dto.setMultiSelect(modifierGroup.isMultiSelect());
        dto.setMaxSelections(modifierGroup.getMaxSelections());
        dto.setSortOrder(modifierGroup.getSortOrder());
        dto.setEnable(modifierGroup.isEnable());
        dto.setCreatedAt(modifierGroup.getCreatedAt());
        dto.setUpdatedAt(modifierGroup.getUpdatedAt());
        dto.setOptions(modifierGroup.getOptions().stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private ModifierOptionDTO toDto(ModifierOption option) {
        ModifierOptionDTO dto = new ModifierOptionDTO();
        dto.setModifierOptionId(option.getModifierOptionId());
        dto.setModifierGroupId(option.getModifierGroup().getModifierGroupId());
        dto.setBusinesId(option.getBusiness().getBusinessId());
        dto.setName(option.getName());
        dto.setAdditionalPrice(option.getAdditionalPrice());
        dto.setEnabled(option.isEnabled());
        dto.setSortOrder(option.getSortOrder());
        dto.setCreatedAt(option.getCreatedAt());
        dto.setUpdatedAt(option.getUpdatedAt());
        return dto;
    }
}
