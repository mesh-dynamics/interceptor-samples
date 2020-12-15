package com.cube.examples.dao;

import org.springframework.stereotype.Repository;

import com.cube.examples.model.EnhancedOrder;
import com.cube.examples.model.Order;
import com.cube.examples.model.Product;
import com.cube.examples.model.Products;

@Repository
public class OrdersDAO
{
    public EnhancedOrder enhanceOrder(Order order) {
        return new EnhancedOrder(order.getId(), Products.getProductById(order.getProductId()), order.getCustomer());
    }


}
