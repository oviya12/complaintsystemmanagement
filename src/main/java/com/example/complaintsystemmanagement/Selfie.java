package com.example.complaintsystemmanagement;



import jakarta.persistence.*;

@Entity
public class Selfie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Lob
    private byte[] encryptedData;

    // Default constructor
    public Selfie() {
    }

    // Parameterized constructor
    public Selfie(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public  byte[] getSelfieData() {
        return encryptedData;
    }


}

