package com.cube.examples;


import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import io.cube.spring.egress.RestTemplateMockInterceptor;
import io.cube.spring.egress.RestTemplateTracingInterceptor;

@SpringBootApplication(scanBasePackages = {"com.cube.examples", "io.cube"})
public class OrderReceiverApplication {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RestTemplateMockInterceptor());
        interceptors.add(new RestTemplateTracingInterceptor());
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
    public static void main(String[] args) {
        SpringApplication.run(OrderReceiverApplication.class, args);
    }
}
