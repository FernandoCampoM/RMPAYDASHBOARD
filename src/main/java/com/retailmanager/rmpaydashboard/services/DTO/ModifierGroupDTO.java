package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifierGroupDTO {
    private String modifierGroupId;

    @NotNull(message = "modifier.group.business.null")
    private Long businesId;

    private Integer productId = 0;

    @NotBlank(message = "modifier.group.name.notBlank")
    private String name;

    private Boolean required = false;
    private Boolean multiSelect = false;
    private Integer maxSelections = 1;
    private Integer sortOrder = 0;
    private Boolean enable = true;
    private Instant createdAt;
    private Instant updatedAt;

    @Valid
    private List<ModifierOptionDTO> options = new ArrayList<>();
}
