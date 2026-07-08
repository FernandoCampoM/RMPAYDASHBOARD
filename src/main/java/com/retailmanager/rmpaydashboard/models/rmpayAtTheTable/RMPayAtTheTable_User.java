package com.retailmanager.rmpaydashboard.models.rmpayAtTheTable;

import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RMPayAtTheTable_User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column( nullable = false)
    private String businessName;

    @Column( nullable = false)
    private String phone;

    @Column( nullable = false)
    private String address;

    @Column( nullable = false)
    private String merchantId;
    @Column( nullable = true)
    private String name;

    @Column( nullable = false, unique = true)
    private String username;
     @Column( nullable = true, columnDefinition = "VARCHAR(MAX)")
    private String tokenATHMovil;

    @Column(nullable = false)
    private String password;
    @Column(nullable = true)
    private String unencryptedPassword;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RMPayAtTheTable_Terminal> terminals = new ArrayList<>();
}
