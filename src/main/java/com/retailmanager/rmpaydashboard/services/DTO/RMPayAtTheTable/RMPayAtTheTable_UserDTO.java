package com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable;

import java.util.ArrayList;
import java.util.List;

import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_Terminal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RMPayAtTheTable_UserDTO {
    private Long userId;
    @NotBlank(message = "{userPayAtTheTable.businessName.notBlank}")
    
    private String businessName;
    
    @NotBlank(message = "{userPayAtTheTable.phone.notBlank}")
    @Pattern(regexp = "^[0-9+\\-\\s()]{8,20}$", message = "{userPayAtTheTable.phone.pattern}")
    private String phone;
    
    @NotBlank(message = "{userPayAtTheTable.address.notBlank}")
   
    private String address;
    
    @NotBlank(message = "{userPayAtTheTable.merchantId.notBlank}")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]{4,50}$", message = "{userPayAtTheTable.merchantId.pattern}")
    private String merchantId;
    @NotBlank(message = "{userPayAtTheTable.name.notBlank}")
    private String name;
    
    @NotBlank(message = "{userPayAtTheTable.username.notBlank}")
    
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{userPayAtTheTable.username.pattern}")
    private String username;
    private String tokenATHMovil;
    @NotBlank(message = "{userPayAtTheTable.password.notBlank}")
    //@Size(min = 8, max = 20, message = "{userPayAtTheTable.password.size}")
    //@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$", 
     //        message = "{userPayAtTheTable.password.pattern}")
    private String password;
    private String unencryptedPassword;
    private List<RMPayAtTheTable_TerminalDTO> terminals = new ArrayList<>();
}
