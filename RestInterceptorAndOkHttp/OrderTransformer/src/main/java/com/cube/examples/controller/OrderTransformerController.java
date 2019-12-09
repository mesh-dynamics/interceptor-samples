package com.cube.examples.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.cube.examples.model.EnhancedOrder;
import com.cube.examples.model.Order;

@RestController
@RequestMapping(path = "/enhanceAndSendForProcessing")
public class OrderTransformerController
{
    @Autowired
    private OrdersDAO ordersDao;

    @Autowired
    private OkHttpClient httpClient;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @PostMapping(path= "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> enhanceAndProcessOrder(
                        @RequestBody Order order)
                 throws Exception 
    {
        //add resource
        EnhancedOrder enhancedOrder = ordersDao.enhanceOrder(order);

        //send for processing
        Request.Builder requestBuilder = new Request.Builder().url("http://order-processor:9080/processEnhancedOrders/");

        requestBuilder.post( okhttp3.RequestBody.create(MediaType.parse("application/json"), jacksonObjectMapper.writeValueAsString(enhancedOrder)));
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            int code = response.code();
            if (code >= 200 && code <= 299) {
                //Create resource location
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(order.getId())
                    .toUri();
                //Send location in response
                return ResponseEntity.created(location).build();
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
