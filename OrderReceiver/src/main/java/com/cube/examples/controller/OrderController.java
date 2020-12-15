package com.cube.examples.controller;

import java.net.URI;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.cube.examples.dao.OrdersDAO;
import com.cube.examples.model.EnhancedOrder;
import com.cube.examples.model.Order;
import com.cube.examples.model.Orders;
import com.cube.examples.model.Product;

@RestController
@RequestMapping(path = "/api")
public class OrderController {

	//When capturing the egress through local proxy, need to use this
	public static final String URL = "http://localhost:9000/transformer:8081";

	//In the kubernetes or any other cluster, the service is hit directly
	//public static final String URL = "http://transformer:8081";

	@Autowired
	private OrdersDAO ordersDao;

	@Autowired
	RestTemplate restTemplate;

	private static final Logger LOGGER = LogManager.getLogger(OrderController.class);

	@GetMapping(path = "/orders", produces = "application/json")
	public Orders getOrders(Principal principal) {
		LOGGER.info("getOrders call Received from "+ principal.getName());
		return ordersDao.getAllOrders();
	}

	@GetMapping(path = "/products", produces = "application/json")
	public Product[] getProducts() {
		LOGGER.info("getProducts call Received ");
		String getProductsUrl = URL.concat("/products");
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent", "Mozilla/5.0 (platform; rv:geckoversion) Gecko/geckotrail Firefox/firefoxversion");
		headers.add("Accept", "application/json");
		HttpEntity entity = new HttpEntity(headers);
		ResponseEntity<Product[]> responseEntity = restTemplate.exchange(getProductsUrl, HttpMethod.GET, entity, Product[].class);
		return responseEntity.getBody();
	}

	@PostMapping(path = "/orders", consumes = "application/json", produces = "application/json")
	public ResponseEntity<EnhancedOrder> placeOrders(@RequestBody Order order,  HttpServletRequest request )
		throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String postOrdersURL = URL.concat("/transformorder");
		ResponseEntity<EnhancedOrder> responseEntity = restTemplate.postForEntity( postOrdersURL, order, EnhancedOrder.class);

		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			//Generate resource id
			Integer id = ordersDao.getAllOrders().getOrderList().size() + 1;
			order.setId(id);

			//add resource
			ordersDao.placeOrder(order);

			//responseEntity.getBody().setId(id);

			LOGGER.info("Response code Received :" + responseEntity.getStatusCode());
			URI location = UriComponentsBuilder.fromPath(request.getServletPath())
					.path("/{id}")
					.buildAndExpand(order.getId())
					.toUri();
				return ResponseEntity.created(location).header("content-type", "application/json").build();
			//return  ResponseEntity.of(Optional.of(responseEntity.getBody()));
			} else {
				LOGGER.info("Response Received :" + responseEntity.toString());
				throw new IllegalArgumentException(
					"HTTP error response returned by Transformer service " + responseEntity.toString());
			}
		}
}
