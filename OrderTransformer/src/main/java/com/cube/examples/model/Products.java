package com.cube.examples.model;

import java.util.HashMap;

public class Products {

  private static Products products = new Products();

  private static HashMap<Integer, Product> productIdVsProduct = new HashMap();

  static
  {
    productIdVsProduct.put(1, new Product(1, "Phone", 1000, "Phone from Apple"));
    productIdVsProduct.put(2, new Product(2, "Laptop", 2000, "Macbook Pro"));
    productIdVsProduct.put(3, new Product(3, "Watch", 500, "Titan smart watch"));
    productIdVsProduct.put(4, new Product(4, "Car", 500000, "BMW x3"));
    productIdVsProduct.put(5, new Product(5, "Bike", 50000, "Harly Davidson "));
  }



  public static Product getProductById(Integer productId) {
    if (productIdVsProduct.containsKey(productId)) {
      return productIdVsProduct.get(productId);
    } else {
      throw new IllegalArgumentException("Product id not found");
    }
  }

  public void addProduct (Product product) {
    productIdVsProduct.put(product.getId(), product);
  }

  public static Product[] getProducts() {
    Product[] productsAry = new Product[productIdVsProduct.size()];
    return productIdVsProduct.values().toArray(productsAry);
  }
}
