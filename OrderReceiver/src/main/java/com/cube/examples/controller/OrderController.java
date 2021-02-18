package com.cube.examples.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.md.apache.commons.io.IOUtils;

import com.cube.examples.dao.OrdersDAO;
import com.cube.examples.model.Order;
import com.cube.examples.model.Orders;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

	//public static final String URL = "http://order-transformer:9080/enhanceAndSendForProcessing/";
	//public static final String URL = "http://localhost:8082/enhanceAndSendForProcessing/";
	public static final String URL = "http://transformer:8081/enhanceAndSendForProcessing/";

	@Autowired
	private OrdersDAO ordersDao;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private ObjectMapper jacksonObjectMapper;

	@Autowired
	private ResourceLoader resourceLoader;

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

	@GetMapping(path = "/getOrderByIndexPV/{index}", produces = "application/json")
	public Order getOrderByIndexPV(Principal principal, @PathVariable("index") String index) {
		LOGGER.info("getOrderByIndexPV call Received from "+ principal.getName());
		return ordersDao.getOrderByIndex(Integer.parseInt(index));
	}

	@PostMapping(path = "/postFormParams", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<Object> postFormParams(@RequestParam MultiValueMap<String,String> paramMap ) {
		return ResponseEntity.ok(paramMap);
	}

	@PostMapping(path = "/postOrder", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> placeOrders(@RequestBody Order order,  HttpServletRequest request )
		throws Exception {

		if (order != null && ordersDao.getOrderById(order.getId()).isPresent()) {
			LOGGER.info("Order with order id already present :");
			return new ResponseEntity<>(null, HttpStatus.CONFLICT);
		}

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

	@GetMapping(path = "/largePayload", produces = "application/json")
	public ResponseEntity<Object> randomLargePayload() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String largePayloadURL = URL.concat("/largePayload");

		Resource resource= resourceLoader.getResource("classpath:/large_payload.json");

		InputStream inputStream= resource.getInputStream();

		Assert.notNull(inputStream,"Could not load template resource!");

		String fileData = null;

		try {
			byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
			fileData = new String(bdata, StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.warn("IOException", e);
		}finally {
			if ( inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
		}

		HttpEntity<String> entity = new HttpEntity<String>(fileData, headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(largePayloadURL, entity, String.class);

		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			LOGGER.info("Response code Received :" + responseEntity.getStatusCode());
			return ResponseEntity.ok(responseEntity.getBody());
		} else {
			LOGGER.info("Response Received :" + responseEntity.toString());
			throw new IllegalArgumentException(
				"HTTP error response returned by Transformer service " + responseEntity.getStatusCode());
		}

	}

	@GetMapping(path = "/testChunkedResponse", produces = "text/html")
	public ResponseEntity<String> testChunkedResponse() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent", "Mozilla/5.0 (platform; rv:geckoversion) Gecko/geckotrail Firefox/firefoxversion");
		headers.add("Accept", "*/*");
		HttpEntity entity = new HttpEntity(headers);

		String chunkedRespURL = "http://jsonplaceholder.typicode.com/todos";
		ResponseEntity<String> responseEntity = restTemplate.exchange(chunkedRespURL, HttpMethod.GET, entity, String.class);

		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			LOGGER.info("Response code Received :" + responseEntity.getStatusCode());
			return responseEntity;
		} else {
			LOGGER.info("Response Received :" + responseEntity.toString());
			throw new IllegalArgumentException(
				"HTTP error response returned by Transformer service " + responseEntity.getStatusCode());
		}

	}

	@PostMapping(path = "/flushAll")
	public ResponseEntity<String> flushOrders(Principal principal) {
		ordersDao.flushOrders();
		return ResponseEntity.ok("Cleared");
	}

}
