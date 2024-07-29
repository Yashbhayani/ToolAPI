package com.bookstore.bookstore.Repository;

import com.bookstore.bookstore.EntityModels.ICategoryModel;
import com.bookstore.bookstore.EntityModels.ISubCategoryModel;

import java.util.Map;

public interface SubCategoryRepository {
    Map<String, Object> getSubCategory(String Token, String report);
    Map<String, Object> getSubCategoryCode(String Token, String Code);
    Map<String, Object> getSubCategoryPath(String Token, String Code);
    Map<String, Object> save(String Token, ISubCategoryModel iSubCategoryModel);
    Map<String, Object> update(String Token, ISubCategoryModel iSubCategoryModel);
    Map<String, Object> delete(String Token, String scid);
}
