package com.cube.examples.controller;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientResponse.Headers;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

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
	private WebClient webClient;

	@PostMapping(path = "/", consumes = "application/json", produces = "application/json")
	public Mono<ResponseEntity<Object>> enhanceAndProcessOrder(ServerHttpRequest serverHttpRequest,
		@RequestBody Order order)
		throws Exception {
		//add resource
		EnhancedOrder enhancedOrder = ordersDao.enhanceOrder(order);

		//send for processing
		Mono<ClientResponse> clientResponse = webClient.post()
			.uri("/processEnhancedOrders/")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.accept(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(Mono.just(enhancedOrder), EnhancedOrder.class)
			.exchange();

		clientResponse.subscribe((response) -> {
			// here you can access headers and status code
			Headers headers = response.headers();
			HttpStatus stausCode = response.statusCode();

			Mono<String> bodyToMono = response.bodyToMono(String.class);
			// the second subscribe to access the body
			bodyToMono.subscribe((body) -> {

				// here you can access the body
				LOGGER.info("body:" + body);

				LOGGER.info("headers:" + headers.asHttpHeaders());
				LOGGER.info("stausCode:" + stausCode);

			}, (ex) -> {
				// handle error
				LOGGER.error(ex);
			});
		}, (ex) -> {
			// handle network error
			LOGGER.error(ex);
		});

		//on success
		URI location = UriComponentsBuilder.fromHttpRequest(serverHttpRequest)
			.path("/{id}")
			.buildAndExpand(order.getId())
			.toUri();
		//Send location in response
		return Mono.just(ResponseEntity.created(location).build());
	}
}