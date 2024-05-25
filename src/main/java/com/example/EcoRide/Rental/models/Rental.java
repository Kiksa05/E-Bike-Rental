package com.example.EcoRide.Rental.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity

public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int customerId;
    private int ebikeId;
    private LocalDateTime rentalStartTime;
    private LocalDateTime rentalEndTime;
    private String rentalStatus;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getEbikeId() { return ebikeId; }
    public void setEbikeId(int ebikeId) { this.ebikeId = ebikeId; }

    public LocalDateTime getRentalStartTime() { return rentalStartTime; }
    public void setRentalStartTime(LocalDateTime rentalStartTime) { this.rentalStartTime = rentalStartTime; }

    public LocalDateTime getRentalEndTime() { return rentalEndTime; }
    public void setRentalEndTime(LocalDateTime rentalEndTime) { this.rentalEndTime = rentalEndTime; }

    public String getRentalStatus() { return rentalStatus; }
    public void setRentalStatus(String rentalStatus) { this.rentalStatus = rentalStatus; }
}
