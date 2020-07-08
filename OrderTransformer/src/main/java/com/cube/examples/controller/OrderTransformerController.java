package com.cube.examples.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.cube.examples.dao.OrdersDAO;
import com.cube.examples.model.EnhancedOrder;
import com.cube.examples.model.Order;

@RestController
@RequestMapping(path = "/enhanceAndSendForProcessing")
public class OrderTransformerController {

	private static final Logger LOGGER = LogManager.getLogger(OrderTransformerController.class);

	@Autowired
	private OrdersDAO ordersDao;

	@Autowired
	RestTemplate restTemplate;


//	@Autowired
//	private WebClient webClient;

	@PostMapping(path = "/", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> enhanceAndProcessOrder(@RequestBody Order order, HttpServletRequest request)
		throws Exception {
		//add resource
		EnhancedOrder enhancedOrder = ordersDao.enhanceOrder(order);

//		//send for processing
//		Mono<ResponseEntity<String>> result = webClient.post()
//			.uri("/processEnhancedOrders/")
//			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
//			.accept(org.springframework.http.MediaType.APPLICATION_JSON)
//			.headers(httpHeaders -> httpHeaders.addAll(serverHttpRequest.getHeaders()))
//			.body(Mono.just(enhancedOrder), EnhancedOrder.class)
//			.exchange()
//			.flatMap(response -> response.toEntity(String.class))
//			.flatMap(entity -> {
//				int code = entity.getStatusCodeValue();
//				if (code >= 200 && code <= 299) {
//					LOGGER.info("Response code Received :" + code);
//					//Create resource location
//					URI location = UriComponentsBuilder.fromHttpRequest(serverHttpRequest)
//						.path("/{id}")
//						.buildAndExpand(order.getId())
//						.toUri();
//					//Send location in response
//					return Mono.just(ResponseEntity.created(location).build());
//				} else {
//					LOGGER.info("Response Received :" + entity.toString());
//					throw new IllegalArgumentException(
//						"HTTP error response returned by Processor service " + code);
//				}
//			});
		URI location = UriComponentsBuilder.fromPath(request.getServletPath())
						.path("/{id}")
						.buildAndExpand(order.getId())
						.toUri();
		return ResponseEntity.created(location).build();
	}
}