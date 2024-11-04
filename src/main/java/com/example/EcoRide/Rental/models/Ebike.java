package com.example.EcoRide.Rental.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
public class Ebike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String model;
    private double batteryLevel;
    private String status;
    private double price;


    @ManyToOne
    @JoinColumn(name = "station_id")
    @JsonBackReference
    private Station station;

    public Ebike(){}

    public Ebike(int id, String model, double batteryLevel, String status, Station station) {
        this.id = id;
        this.model = model;
        this.batteryLevel = batteryLevel;
        this.status = status;
        this.station = station;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(double batteryLevel) { this.batteryLevel = batteryLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
