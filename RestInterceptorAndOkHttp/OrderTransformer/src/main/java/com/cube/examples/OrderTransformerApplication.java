package com.cube.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.cube.examples", "io.cube"})
public class OrderTransformerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderTransformerApplication.class, args);
    }
}
