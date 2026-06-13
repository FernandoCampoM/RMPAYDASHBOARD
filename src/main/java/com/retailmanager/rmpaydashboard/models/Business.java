package com.retailmanager.rmpaydashboard.models;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @NoArgsConstructor  @AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;
    @Column(nullable = true)
    private String merchantId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String businessPhoneNumber;
    @Column(nullable = false)
    private Integer additionalTerminals;
    @Column(nullable = false)
    private boolean enable=false;
    @Column(nullable = true)
    private boolean terms=false;
    @Column(nullable = true)
    private Long serviceId;
    @Column(nullable = false)
    private double discount=0.0;
    @Column(nullable = true)
    private Instant lastPayment;
    @Column(nullable = true)
    private String comment;

    private Long logo;
    private Long logoAth;
    
    @Column(nullable = true)
    private Instant priorNotification;
    @Column(nullable = true)
    private Instant lastDayNotification;
    @Column(nullable = true)
    private Instant afterNotification;
    @Column(nullable = true)
    private Instant registerDate;
    @Column(nullable = true)
    public Float percentageProfit;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
    /**Objeto que encapsula la información de la dirección */
    @OneToOne(cascade=CascadeType.ALL,optional = true)
    @JoinColumn( name="addressId",nullable = true)
    private Address address;

    @OneToOne(cascade=CascadeType.ALL,optional = true)
    @JoinColumn( name="paymentId",nullable = true)
    private PaymentData paymentData;

    

    @ManyToOne(optional = false)
    @JoinColumn( name="userId",nullable = false)
    private User user;

    @OneToMany(mappedBy = "business",fetch = FetchType.LAZY)
    private List<Category> categories;
    
    @OneToMany(mappedBy = "business",fetch =FetchType.LAZY)
    private List<Terminal> terminals;

    @OneToMany(mappedBy = "business",fetch =FetchType.LAZY)
    private List<UsersBusiness> usersBusiness;
    
    @PrePersist
    private void prePersist() {
        
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = Instant.now();
    }
}