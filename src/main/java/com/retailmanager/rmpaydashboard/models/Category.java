package com.retailmanager.rmpaydashboard.models;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private boolean enable;

    @ManyToOne(cascade=CascadeType.PERSIST,optional = false)
    @JoinColumn( name="businessId",nullable = false)
    private Business business;
    private String color;
    private String position;
    private Instant createdAt;
    private Instant updatedAt;
}
