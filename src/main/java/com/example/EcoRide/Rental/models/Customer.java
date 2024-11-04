package com.example.EcoRide.Rental.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String email;
    private String phone;
    private double accountBalance;
    private String password;
    private String role;

    public Customer(){}
    public Customer(String name, String phone){
        this.name = name;
        this.phone = phone;
    }
    public Customer(int id, String name, String email, String phone, double accountBalance, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.accountBalance = accountBalance;
        this.password = password;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getAccountBalance() { return accountBalance; }
    public void setAccountBalance(double accountBalance) { this.accountBalance = accountBalance; }

    public boolean sameMail(Customer c) {
        if ( c == null || this.getEmail() == null ||  c.getEmail() == null) {
            return false;
        }
        String mail1 = this.getEmail();
        String mail2 = c.getEmail();
        return  mail1.equalsIgnoreCase(mail2);
    }
}
