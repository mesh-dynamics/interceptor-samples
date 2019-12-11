package com.cube.examples.controller;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.cube.examples.dao.OrdersDAO;
import com.cube.examples.model.Order;
import com.cube.examples.model.Orders;

@RestController
@RequestMapping(path = "/orders")
public class OrderController
{
    @Autowired
    private OrdersDAO ordersDao;

    @Autowired
    private OkHttpClient httpClient;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    private static final Logger LOGGER = LogManager.getLogger(OrderController.class);

    @GetMapping(path="/", produces = "application/json")
    public Orders getOrders()
    {
        LOGGER.info("getOrders call Received");
        return ordersDao.getAllOrders();
    }
    
    @PostMapping(path= "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> placeOrders(
                        @RequestBody Order order)
                 throws Exception 
    {       
        //Generate resource id
        Integer id = ordersDao.getAllOrders().getOrderList().size() + 1;
        order.setId(id);
        
        //add resource
        ordersDao.placeOrder(order);

        // send it to order transformer
        //send for processing
        Request.Builder requestBuilder = new Request.Builder().url("http://order-transformer:9080/enhanceAndSendForProcessing/");

        requestBuilder.post( okhttp3.RequestBody.create(MediaType.parse("application/json"), jacksonObjectMapper.writeValueAsString(order)));
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            int code = response.code();
            if (code >= 200 && code <= 299) {
                LOGGER.info("Response code Received :" + response.code());
                //Create resource location
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(order.getId())
                    .toUri();
                //Send location in response
                return ResponseEntity.created(location).build();
            } else {
                LOGGER.info("Response Received :" + response.toString());
                throw new IllegalArgumentException("HTTP error response returned by Transformer service "+ code);
            }
        }
    }
}
