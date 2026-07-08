package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryReceiptDTO {
    private Long receiptId;
    private String comments;
    private String supplier;
    private String inventoryEntered;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerDate;
    private Long businessId;

    private List<InventoryItemDTO> inventoryItems;
}
