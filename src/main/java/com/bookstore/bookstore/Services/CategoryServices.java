package com.bookstore.bookstore.Services;

import com.bookstore.bookstore.CustomModel.ListModel.Category.CategoryTypesModel;
import com.bookstore.bookstore.CustomModel.Model.CategoryTypeModel;
import com.bookstore.bookstore.CustomModel.Model.ProductModel;
import com.bookstore.bookstore.Enum.ProjectCodes;
import com.bookstore.bookstore.Repository.AuthJwtRepository;
import com.bookstore.bookstore.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServices implements CategoryRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public String SP = "SELECT Sp_Name FROM sp_table WHERE Sp_Code = ?";
    public String SpResult = null;
    private final AuthJwtRepository authjwtrepository;
    public CategoryServices(AuthJwtRepository authjwtrepository) {
        this.authjwtrepository = authjwtrepository;
    }

    public Map<String, Object> getCategory(String Token, String report) {
        Map<String, Object> response = new HashMap<>();
        try{
            if (authjwtrepository.isTokenValid(Token)) {
                String username = authjwtrepository.getUsernameFromToken(Token);
                SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.CHECKUSERROLE.name()}, String.class);
                Map<String, Object> result = jdbcTemplate.queryForMap(SpResult, new Object[]{username});
                String userRoleResult = (String) result.get("Result");
                if (userRoleResult != null) {
                    boolean isAdmin = Boolean.parseBoolean(userRoleResult);
                    if (isAdmin) {
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ReportCods.ALLCATEGORYTYPES.name()}, String.class);
                        var categoryList = jdbcTemplate.execute(SpResult, (CallableStatementCallback<CategoryTypesModel>) callableStatement -> {
                            CategoryTypesModel categoryTypesModel = new CategoryTypesModel();
                            callableStatement.setString(1, report);
                            boolean hasResults = callableStatement.execute();

                            if (hasResults) {
                                try (ResultSet rs = callableStatement.getResultSet()) {
                                    /* ResultSetMetaData metaData = rs.getMetaData();
                                    int totalColumn = metaData.getColumnCount();
                                    System.out.println(totalColumn);*/
                                    if (rs != null && rs.next()) {
                                        categoryTypesModel.setCategoryCount(rs.getInt("total_Categorys"));
                                    }
                                }

                                // Second result set: product details
                                if (callableStatement.getMoreResults()) {
                                    try (ResultSet rs = callableStatement.getResultSet()) {
                                        List<CategoryTypeModel> categoryTypeModels = new ArrayList<>();
                                        while (rs != null && rs.next()) {
                                            CategoryTypeModel categoryTypeModel = new CategoryTypeModel();
                                            try {
                                                categoryTypeModel.setId(authjwtrepository.BookidEncrypt(rs.getInt("CategoryID")));
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                            categoryTypeModel.setProductName(rs.getString("ProductName"));
                                            categoryTypeModel.setCategoryCode(rs.getString("CategoryCode"));
                                            categoryTypeModel.setCategoryValue(rs.getString("CategoryValue"));
                                            categoryTypeModel.setCategoryPath(rs.getString("CategoryPath"));
                                            categoryTypeModel.setIsActive(rs.getBoolean("IsActive"));
                                            categoryTypeModels.add(categoryTypeModel);
                                        }
                                        categoryTypesModel.setCategoryTypeModels(categoryTypeModels);
                                    }
                                }
                            }
                            return categoryTypesModel;
                        });
                        if (categoryList != null ) {
                            response.put("data", categoryList);
                            response.put("Success", true);
                            response.put("Code", 200);
                        } else {
                            response.put("Message", "CategoryList List not found for the specified language.");
                            response.put("Success", false);
                        }
                    }
                    else {
                        response.put("Message", "User is Not valid");
                        response.put("Success", false);
                    }
                }else {
                    response.put("Message", "User is Not valid");
                    response.put("Success", false);
                }
            }
            else {
                response.put("Message", "User is Not valid");
                response.put("Success", false);
            }
        }
        catch (Exception e){
            response.put("Message", e.getMessage());
            response.put("Success", false);
        }
        return response;
    }
}
