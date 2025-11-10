package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

 @Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSchedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long asId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String duration; // Ejemplo: "01:30" (HH:mm)
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT '#0d6efd'")
    private String color = "#0d6efd";
    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "userBusinessId", nullable = false)
    private UsersBusiness employee;
}
