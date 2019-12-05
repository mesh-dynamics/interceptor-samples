package com.cube.examples.dao;

import org.springframework.stereotype.Repository;

import com.cube.examples.model.EnhancedOrder;
import com.cube.examples.model.Order;
import com.cube.examples.model.Product;
import com.cube.examples.model.Products;

@Repository
public class OrdersDAO
{
    private static Products products = new Products();

    static
    {
        products.addProduct(new Product(1, "Phone", 1000));
        products.addProduct(new Product(1, "Laptop", 2000));
        products.addProduct(new Product(1, "Watch", 500));
    }
    public EnhancedOrder enhanceOrder(Order order) {
        return new EnhancedOrder(order.getId(), products.getProductById(order.getProductId()), order.getCustomer());
    }
}
