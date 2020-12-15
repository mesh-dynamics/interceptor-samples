package com.cube.examples;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.cube.spring.egress.RestTemplateDataInterceptor;
import io.cube.spring.egress.RestTemplateMockInterceptor;
import io.cube.spring.egress.RestTemplateTracingInterceptor;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderReceiverApplicationTests {

	@Test
	public void contextLoads() {
	}
}
