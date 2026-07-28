package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifierOptionDTO {
    private String modifierOptionId;
    private String modifierGroupId;
    private Long businesId;

    @NotBlank(message = "modifier.option.name.notBlank")
    private String name;

    private Double additionalPrice = 0.0;
    private Boolean enabled = true;
    private Integer sortOrder = 0;
    private Instant createdAt;
    private Instant updatedAt;
}
