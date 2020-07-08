package com.cube.examples.controller;

import java.net.URI;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.cube.examples.dao.OrdersDAO;
import com.cube.examples.model.Order;
import com.cube.examples.model.Orders;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

	//public static final String URL = "http://order-transformer:9080/enhanceAndSendForProcessing/";
	public static final String URL = "http://localhost:8082/enhanceAndSendForProcessing/";
	//public static final String URL = "http://transformer:8081/enhanceAndSendForProcessing/";

	@Autowired
	private OrdersDAO ordersDao;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private ObjectMapper jacksonObjectMapper;

	private static final Logger LOGGER = LogManager.getLogger(OrderController.class);

	@GetMapping(path = "/getOrders", produces = "application/json")
	public Orders getOrders(Principal principal) {
		LOGGER.info("getOrders call Received from "+ principal.getName());
		return ordersDao.getAllOrders();
	}

	@GetMapping(path = "/getOrderByIndex", produces = "application/json")
	public Order getOrderByIndex(Principal principal, @RequestParam("index") String index) {
		LOGGER.info("getOrderByIndex call Received from "+ principal.getName());
		return ordersDao.getOrderByIndex(Integer.parseInt(index));
	}

	@GetMapping(path = "/getOrderByIndexQP", produces = "application/json")
	public Order getOrderByIndexQP(Principal principal, @QueryParam("index") String index) {
		LOGGER.info("getOrderByIndexQP call Received from "+ principal.getName());
		return ordersDao.getOrderByIndex(Integer.parseInt(index));
	}

	@PostMapping(path = "/postOrder", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> placeOrders(@RequestBody Order order,  HttpServletRequest request )
		throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity( URL, order, String.class);

		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			//Generate resource id
			Integer id = ordersDao.getAllOrders().getOrderList().size() + 1;
			order.setId(id);

			//add resource
			ordersDao.placeOrder(order);

			LOGGER.info("Response code Received :" + responseEntity.getStatusCode());
			URI location = UriComponentsBuilder.fromPath(request.getServletPath())
					.path("/{id}")
					.buildAndExpand(order.getId())
					.toUri();
				return ResponseEntity.created(location).build();
			} else {
				LOGGER.info("Response Received :" + responseEntity.toString());
				throw new IllegalArgumentException(
					"HTTP error response returned by Transformer service " + responseEntity.getStatusCode());
			}
		}
}
