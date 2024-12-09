package com.example.EcoRide.Rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.example.EcoRide.Rental")
public class EcoRideRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcoRideRentalApplication.class, args);
	}

}
