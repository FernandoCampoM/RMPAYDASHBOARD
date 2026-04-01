package com.retailmanager.rmpaydashboard.services.DTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retailmanager.rmpaydashboard.models.Product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;

    @Size(max = 255, message = "{product.barcode.max}")
    @NotBlank(message = "{product.barcode.empty}")
    private String barcode;

    @NotBlank(message = "{product.name.empty}")
    @Size(max = 255, message = "{product.name.max}")
    private String name;

    @Size(max = 1000, message = "{product.description.max}")
    private String description;

    @NotNull(message = "{product.cost.null}")
    @DecimalMin(value = "0.00", message = "{product.cost.min}")
    private BigDecimal cost;

    @NotNull(message = "{product.price.null}")
    @DecimalMin(value = "0.00", message = "{product.price.min}")
    private BigDecimal price;
    
    private Long idCategory;
    private String nameCategory;
    private Long position;
    private Long idBusiness;
    @Size(max = 255, message = "{product.code.max}")
    @NotBlank(message = "{product.code.empty}")
    private String code;

    // Inventory attributes

    @NotNull(message = "{product.estatal.null}")
    private boolean estatal;

    @NotNull(message = "{product.municipal.null}")
    private boolean municipal;
    @NotNull(message = "{product.reducedTax.null}")
    private boolean reducedTax;
    
    @NotNull(message = "{product.quantity.null}")
    private int quantity;


    @NotNull(message = "{product.minimumLevel.null}")
    @Min(value = 0, message = "{product.minimumLevel.min}")
    private int minimumLevel;

    @NotNull(message = "{product.maximumLevel.null}")
    @Min(value = 0, message = "{product.maximumLevel.min}")
    private int maximumLevel;
    @NotNull(message = "{product.enable.null}")
    private Boolean enable;

    private int suggestedPurchase;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant createdAt;
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant updatedAt;

    public static ProductDTO tOProduct(Product product) {
        ProductDTO objProduct = new ProductDTO();
        objProduct.setProductId(product.getProductId());
        objProduct.setIdCategory(product.getCategory().getCategoryId());
        objProduct.setBarcode(product.getBarcode());
        objProduct.setName(product.getName());
        objProduct.setDescription(  product.getDescription());
        objProduct.setCost(product.getCost());
        objProduct.setPrice(product.getPrice());
        objProduct.setCode( product.getCode());
        objProduct.setEstatal(product.isEstatal());
        objProduct.setMunicipal(product.isMunicipal());
        objProduct.setQuantity(product.getQuantity());
        objProduct.setMinimumLevel(product.getMinimumLevel());
        objProduct.setMaximumLevel(product.getMaximumLevel());
        objProduct.setEnable(product.isEnable());
        objProduct.setSuggestedPurchase(product.getMaximumLevel() - product.getQuantity());
        objProduct.setCreatedAt(product.getCreatedAt());
        objProduct.setUpdatedAt(product.getUpdatedAt());
        return objProduct;
    }
}