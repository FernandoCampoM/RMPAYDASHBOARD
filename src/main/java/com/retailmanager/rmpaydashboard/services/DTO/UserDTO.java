package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retailmanager.rmpaydashboard.enums.Rol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UserDTO {
    @NonNull
    private Long userID=0L;
    @NotEmpty(message = "{user.username.empty}")
    @Size(min = 5, message = "{user.username.min}")
    private String username;
    @NotEmpty(message = "{user.name.empty}")
    private String name;
    //@NotEmpty(message = "{user.email.empty}")
    @Email(message = "{user.email.format}")
    private String email;
    @NotEmpty(message = "{user.password.empty}")
    private String password;
    private Rol rol;
    private boolean enable;
    //@NotEmpty(message = "{user.phone.empty}")
    private String phone;
    private Instant registerDate;
    private Instant lastLogin;
    private List<BusinessDTO> business;

}
