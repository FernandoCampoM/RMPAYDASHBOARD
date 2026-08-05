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
public class ResetPasswordRequestDTO {
    @NotBlank(message = "token.notBlank")
    private String token;

    @NotBlank(message = "password.notBlank")
    @Size(min = 8, message = "password.min")
    private String newPassword;
}
