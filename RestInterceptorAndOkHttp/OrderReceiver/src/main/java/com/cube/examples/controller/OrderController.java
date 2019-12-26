package com.cube.examples.controller;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientResponse.Headers;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import reactor.core.publisher.Mono;

import com.cube.examples.dao.OrdersDAO;
import com.cube.examples.model.Order;
import com.cube.examples.model.Orders;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

	@Autowired
	private OrdersDAO ordersDao;

	@Autowired
	private OkHttpClient httpClient;

	@Autowired
	private WebClient defaultWebClient;

	@Autowired
	private ObjectMapper jacksonObjectMapper;

	private static final Logger LOGGER = LogManager.getLogger(OrderController.class);

	@GetMapping(path = "/", produces = "application/json")
	public Orders getOrders() {
		LOGGER.info("getOrders call Received");
		return ordersDao.getAllOrders();
	}

	@PostMapping(path = "/", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> placeOrders(ServerHttpRequest serverHttpRequest,
		@RequestBody Order order)
		throws Exception {
		//Generate resource id
		Integer id = ordersDao.getAllOrders().getOrderList().size() + 1;
		order.setId(id);

		//add resource
		ordersDao.placeOrder(order);

		// send it to order transformer
		// send for processing
		Mono<ClientResponse> clientResponse = defaultWebClient.post()
			.uri("/enhanceAndSendForProcessing/")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.accept(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(Mono.just(order), Order.class)
			.exchange();

		clientResponse.subscribe((response) -> {
			// here you can access headers and status code
			Headers headers = response.headers();
			HttpStatus stausCode = response.statusCode();

			Mono<String> bodyToMono = response.bodyToMono(String.class);
			// the second subscribe to access the body
			bodyToMono.subscribe((body) -> {

				// here you can access the body
				System.out.println("body:" + body);

				// and you can also access headers and status code if you need
				System.out.println("headers:" + headers.asHttpHeaders());
				System.out.println("stausCode:" + stausCode);

			}, (ex) -> {
				// handle error
			});
		}, (ex) -> {
			// handle network error
		});

		//on success
		URI location = UriComponentsBuilder.fromHttpRequest(serverHttpRequest)
			.path("/{id}")
			.buildAndExpand(order.getId())
			.toUri();
		//Send location in response
		return ResponseEntity.created(location).build();

		//Below code uses okhttp3 client.
		//If need to use this, comment webflux dependency
		//and uncomment spring-boot-starter-web dependency in pom file.

//		Request.Builder requestBuilder = new Request.Builder()
//			.url("http://order-transformer:9080/enhanceAndSendForProcessing/");
//
//		requestBuilder.post(okhttp3.RequestBody.create(MediaType.parse("application/json"),
//			jacksonObjectMapper.writeValueAsString(order)));
//		try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
//			int code = response.code();
//			if (code >= 200 && code <= 299) {
//				LOGGER.info("Response code Received :" + response.code());
//				//Create resource location
//				URI location = ServletUriComponentsBuilder.fromCurrentRequest()
//					.path("/{id}")
//					.buildAndExpand(order.getId())
//					.toUri();
//				//Send location in response
//				return ResponseEntity.created(location).build();
//			} else {
//				LOGGER.info("Response Received :" + response.toString());
//				throw new IllegalArgumentException(
//					"HTTP error response returned by Transformer service " + code);
//			}
//		}
	}
}
