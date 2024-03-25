package com.example.complaintsystemmanagement;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username= "admin";
    private String password="admin";

    public  String getPassword() {
        return password;
    }
    // Other fields and methods
}

