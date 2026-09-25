package com.ridelink.farepayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.Clock;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FarePaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarePaymentServiceApplication.class, args);
    }

    /** Injectable Clock keeps fare/time-dependent logic deterministic in tests. */
    @Bean
    public Clock clock() {
        return Clock.SYSTEM;
    }
}