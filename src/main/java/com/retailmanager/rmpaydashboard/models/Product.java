package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @Column(nullable = false, unique = false)
    private String barcode;
    @Column(nullable = false, unique = false)
    private String name;
    @Column(nullable = true, unique = false)
    private String description;
    @Column(nullable = false, unique = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false, unique = false, precision = 10, scale = 2)
    private BigDecimal price;

    
    @Column(nullable = false, unique = false)
    private String code;
    @Column(nullable = false, columnDefinition = "int DEFAULT 0")
    private int quantity;
    // Inventory attributes
    @Column(nullable = false, unique = false)
    private boolean estatal;
    @Column(nullable = false, unique = false) 
    private boolean municipal;
    @Column(nullable = false, unique = false,columnDefinition = "bit DEFAULT 0") 
    private boolean reducedTax;
    
    @Column(nullable = false, unique = false)
    private int minimumLevel;
    @Column(nullable = false, unique = false) 
    private int maximumLevel;
    @Column(nullable = false)
    private boolean enable=false;
    @ManyToOne(cascade=CascadeType.PERSIST,optional = true)
    @JoinColumn( name="categoryId",nullable = false)
    private Category category;
    @ManyToMany
    @JoinTable(
        name = "Product_ModifierGroup",
        joinColumns = @JoinColumn(name = "productId"),
        inverseJoinColumns = @JoinColumn(name = "modifierGroupId")
    )
    private Set<ModifierGroup> modifierGroups = new HashSet<>();
    private Instant createdAt;
    private Instant updatedAt;
}
