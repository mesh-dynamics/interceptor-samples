package com.cube.examples.dao;

import org.springframework.stereotype.Repository;

import com.cube.examples.model.Order;
import com.cube.examples.model.Orders;

@Repository
public class OrdersDAO
{
    private static Orders list = new Orders();
    
    static 
    {
        list.getOrderList().add(new Order(1, 1, new Order.Customer("Lokesh", "Gupta", "xyz@gmail.com")));
        list.getOrderList().add(new Order(2, 2, new Order.Customer("Alex", "Kolenchiskey", "abc@gmail.com")));
        list.getOrderList().add(new Order(3, 3, new Order.Customer("David", "Kameron", "test@gmail.com")));
    }
    
    public Orders getAllOrders()
    {
        return list;
    }
    
    public void placeOrder(Order order) {
        list.getOrderList().add(order);
    }
}
