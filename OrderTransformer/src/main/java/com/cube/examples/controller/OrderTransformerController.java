package com.cube.examples.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.security.Principal;

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
import com.cube.examples.model.Product;
import com.cube.examples.model.Products;

@RestController
@RequestMapping(path = "/")
public class OrderTransformerController {

	private static final Logger LOGGER = LogManager.getLogger(OrderTransformerController.class);

	@Autowired
	private OrdersDAO ordersDao;

//	@Autowired
//	private WebClient webClient;

	@PostMapping(path = "/transformorder", consumes = "application/json", produces = "application/json")
	public EnhancedOrder enhanceAndProcessOrder(@RequestBody Order order, HttpServletRequest request)
		throws Exception {
		LOGGER.info("Call Received :" + order.toString());
		//add resource
		EnhancedOrder enhancedOrder = ordersDao.enhanceOrder(order);
		return enhancedOrder;
	}

	@GetMapping(path = "/products", produces = "application/json")
	public Product[] getProducts() {
		LOGGER.info("getProducts call Received ");
		return Products.getProducts();
	}
}