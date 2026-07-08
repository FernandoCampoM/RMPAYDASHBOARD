package com.retailmanager.rmpaydashboard.enums;

public enum EmployeeRole {
    // Cada constante del enum es un "rol de empleado"
    ADMIN(1, "ADMIN"),
    USER(2, "USER");

    private final int id;
    private final String name;

    // Constructor del enum
    EmployeeRole(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Métodos para acceder a los atributos del enum
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Opcional: un método estático para obtener un rol por su ID
    public static EmployeeRole fromId(int id) {
        for (EmployeeRole role : EmployeeRole.values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        return null; // O lanzar una excepción si prefieres
    }

    // Opcional: un método estático para obtener un rol por su nombre
    public static EmployeeRole fromName(String name) {
        for (EmployeeRole role : EmployeeRole.values()) {
            if (role.getName().equalsIgnoreCase(name)) { // Ignora mayúsculas/minúsculas
                return role;
            }
        }
        return null; // O lanzar una excepción si prefieres
    }
}