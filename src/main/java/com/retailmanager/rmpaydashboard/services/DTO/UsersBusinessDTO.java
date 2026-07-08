package com.retailmanager.rmpaydashboard.services.DTO;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersBusinessDTO {

    private Long userBusinessId;

    @NotBlank(message = "{usersbusiness.username.empty}")
    @Size(min = 2, message = "{usersbusiness.username.min}")
    @Size(max = 255, message = "{usersbusiness.username.max}")
    private String username;

    @NotBlank(message = "{usersbusiness.password.empty}")
   @Size(min = 4, max = 4, message = "{usersbusiness.password.size}")
    @Pattern(regexp = "\\d{4}", message = "{usersbusiness.password.digits}")
    private String password;

    @NotNull(message = "{usersbusiness.enable.null}")
    private Boolean enable;
    @NotNull(message = "{usersbusiness.idBusiness.null}")
    @Positive(message = "{usersbusiness.idBusiness.positive}")
    private Long idBusines;
    List<Long> activesPermissions;
    private Double costHour;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private int roleId;
    //List<UserPermissionDTO> userPermissions;
}
