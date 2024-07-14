package com.bookstore.bookstore.Repository;

import java.util.Map;

public interface ProductRepository {
    Map<String, Object> getproduct(String Token, String report);
    Map<String, Object> getproductcode(String Token, String Code);

}
