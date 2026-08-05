package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientPasswordResetRequestDTO {
    @NotBlank(message = "newPassword.notBlank")
    @Size(min = 8, message = "newPassword.size")
    private String newPassword;
}
