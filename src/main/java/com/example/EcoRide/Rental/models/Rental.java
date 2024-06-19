package com.example.EcoRide.Rental.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity

public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;
    @ManyToOne
    @JoinColumn(name = "ebike_id", referencedColumnName = "id")
    private Ebike ebike;
    private LocalDateTime rentalStartTime;
    private LocalDateTime rentalEndTime;
    private String rentalStatus;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Ebike getEbike() {
        return ebike;
    }

    public void setEbike(Ebike ebike) {
        this.ebike = ebike;
    }

    public LocalDateTime getRentalStartTime() { return rentalStartTime; }
    public void setRentalStartTime(LocalDateTime rentalStartTime) { this.rentalStartTime = rentalStartTime; }

    public LocalDateTime getRentalEndTime() { return rentalEndTime; }
    public void setRentalEndTime(LocalDateTime rentalEndTime) { this.rentalEndTime = rentalEndTime; }

    public String getRentalStatus() { return rentalStatus; }
    public void setRentalStatus(String rentalStatus) { this.rentalStatus = rentalStatus; }
}
