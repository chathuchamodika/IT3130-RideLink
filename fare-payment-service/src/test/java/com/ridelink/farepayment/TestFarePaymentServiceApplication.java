package com.ridelink.farepayment;

import org.springframework.boot.SpringApplication;

public class TestFarePaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(FarePaymentServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
