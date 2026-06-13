package com.retailmanager.rmpaydashboard.models;

import java.time.Instant;
import java.util.List;

import com.retailmanager.rmpaydashboard.enums.Rol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;
    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private String phone;
    
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean enable=true;
    @Column(nullable = true)
    private Instant registerDate;
    @Column(nullable = true)
    private Instant lastLogin;
    @Column(nullable = true)
    private String tempAuthId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,columnDefinition = "VARCHAR(50) default 'ROLE_USER'")
    private Rol rol;
    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    private List<Business> business;
}
