package com.example.EcoRide.Rental.models;

import jakarta.persistence.*;

@Entity
public class Ebike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String model;
    private double batteryLevel;
    private String status;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

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
}
