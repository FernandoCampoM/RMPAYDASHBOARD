package com.retailmanager.rmpaydashboard.services.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDTO {
    private Long businessId;

    private String merchantId;
    @NotBlank(message = "{business.name.empty}")
    @Size(max = 255, message = "{business.name.max}")
    private String name;

    @NotBlank(message = "{business.businessPhoneNumber.empty}")
    @Size(max = 20, message = "{business.businessPhoneNumber.max}")
    private String businessPhoneNumber;

    @NotNull(message = "{business.additionalTerminals.null}")
    @Min(value = 0, message = "{business.additionalTerminals.min}")
    private Integer additionalTerminals;

    private Long serviceId;
    private double discount = 0.0;
    private boolean enable = false;
    private boolean terms = false;
    private Instant lastPayment;
    private String comment;
    private Long logo;
    private Long logoAth;
    private Instant registerDate;
    private Instant priorNotification;
    private Instant lastDayNotification;
    private Instant afterNotification;
    public Float percentageProfit;
    @Valid
    private AddressDTO address;
    private UserDTO user;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant createdAt;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant updatedAt;
    @NotNull(message = "{business.userId.null}")
    private Long userId;
    private List<CategoryDTO> categories;

    private List<TerminalDTO> terminals;
}
