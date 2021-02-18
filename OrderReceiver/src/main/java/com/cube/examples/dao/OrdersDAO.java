package com.cube.examples.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.cube.examples.model.Order;
import com.cube.examples.model.Orders;

@Repository
public class OrdersDAO
{
    private static Orders list = new Orders();

    private static final Logger LOGGER = LogManager.getLogger(OrdersDAO.class);
    static 
    {
        init();
    }
    
    public Orders getAllOrders()
    {
        LOGGER.info("getAllOrders DAO call received and the size of the orders to be returned is: " + list.getOrderList().size());
        return list;
    }
    
    public void placeOrder(Order order) {
        list.getOrderList().add(order);
    }

    public Order getOrderByIndex(int index) {
        return list.getOrderList().get(index);
    }

    public Optional<Order> getOrderById(int id) {
        return list.getOrderList().stream().filter(value -> value.getId() == id).findFirst();
    }

    public void flushOrders() {
        init();
    }

    private static void init() {
        list = new Orders();
        list.getOrderList().add(new Order(1, 1, new Order.Customer("Lokesh", "Gupta", "xyz@gmail.com")));
        list.getOrderList().add(new Order(2, 2, new Order.Customer("Alex", "Kolenchiskey", "abc@gmail.com")));
        list.getOrderList().add(new Order(3, 3, new Order.Customer("David", "Kameron", "test@gmail.com")));
    }
}
