package com.example.EcoRide.Rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.example.EcoRide.Rental")
@EnableScheduling
public class EcoRideRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcoRideRentalApplication.class, args);
	}

}
	