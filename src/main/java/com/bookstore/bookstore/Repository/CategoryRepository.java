package com.bookstore.bookstore.Repository;

import java.util.Map;

public interface CategoryRepository {
    Map<String, Object> getCategory(String Token, String report);

}
