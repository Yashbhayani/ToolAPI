package com.bookstore.bookstore.Repository;

import java.util.Map;

public interface SubCategoryRepository {
    Map<String, Object> getSubCategory(String Token, String report);
}
