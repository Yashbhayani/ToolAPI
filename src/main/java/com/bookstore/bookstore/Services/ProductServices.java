package com.bookstore.bookstore.Services;

import com.bookstore.bookstore.Enum.ProjectCodes;
import com.bookstore.bookstore.CustomModel.ListModel.Product.ProductsModel;
import com.bookstore.bookstore.CustomModel.Model.ProductModel;
import com.bookstore.bookstore.Repository.AuthJwtRepository;
import com.bookstore.bookstore.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServices implements ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public String SP = "SELECT Sp_Name FROM sp_table WHERE Sp_Code = ?";
    public String SpResult = null;
    private final AuthJwtRepository authjwtrepository;
    public ProductServices(AuthJwtRepository authjwtrepository) {
        this.authjwtrepository = authjwtrepository;
    }

    public Map<String, Object> getproduct(String Token, String report) {
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
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ReportCods.ALLPRODUCTTYPES.name()}, String.class);
                        var productList = jdbcTemplate.execute(SpResult, (CallableStatementCallback<ProductsModel>) callableStatement -> {
                            ProductsModel productsModel = new ProductsModel();
                            callableStatement.setString(1, report);
                            boolean hasResults = callableStatement.execute();

                            if (hasResults) {
                                try (ResultSet rs = callableStatement.getResultSet()) {
                                    if (rs != null && rs.next()) {
                                        productsModel.setProductCount(rs.getInt("total_Products"));
                                    }
                                }

                                // Second result set: product details
                                if (callableStatement.getMoreResults()) {
                                    try (ResultSet rs = callableStatement.getResultSet()) {
                                        List<ProductModel> productModels = new ArrayList<>();
                                        while (rs != null && rs.next()) {
                                            ProductModel productModel = new ProductModel();
                                            try {
                                                productModel.setPid(authjwtrepository.BookidEncrypt(rs.getInt("id")));
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                            productModel.setCode(rs.getString("CategoryCode"));
                                            productModel.setName(rs.getString("CategoryName"));
                                            productModel.setActive(rs.getBoolean("IsActive"));
                                            productModels.add(productModel);
                                        }
                                        productsModel.setProductModels(productModels);
                                    }
                                }
                            }
                            return productsModel;
                        });
                        if (productList != null ) {
                            response.put("data", productList);
                            response.put("Success", true);
                            response.put("Code", 200);
                        } else {
                            response.put("Message", "Product List not found for the specified language.");
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

    @Override
    public Map<String, Object> getproductcode(String Token, String Code) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authjwtrepository.isTokenValid(Token)) {
                String username = authjwtrepository.getUsernameFromToken(Token);
                SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.CHECKUSERROLE.name()}, String.class);
                Map<String, Object> result = jdbcTemplate.queryForMap(SpResult, new Object[]{username});
                String userRoleResult = (String) result.get("Result");
                if (userRoleResult != null) {
                    boolean isAdmin = Boolean.parseBoolean(userRoleResult);
                    if (isAdmin) {
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.VERIFYPRODUCTCODE.name()}, String.class);
                        boolean productCode = Boolean.TRUE.equals(jdbcTemplate.execute(SpResult, (CallableStatementCallback<Boolean>) callableStatement -> {
                            callableStatement.setString(1, Code);
                            boolean hasResults = callableStatement.execute();
                            if (hasResults) {
                                try (ResultSet rs = callableStatement.getResultSet()) {
                                    if (rs != null && rs.next()) {
                                        return rs.getBoolean("VerifyCode");
                                    }
                                }
                            }
                            return false;
                        }));

                        response.put("data", productCode);
                        response.put("Success", true);
                        response.put("Code", 200);
                    } else {
                        response.put("Message", "User is Not valid");
                        response.put("Success", false);
                    }
                } else {
                    response.put("Message", "User is Not valid");
                    response.put("Success", false);
                }
            }
        }
        catch (Exception e){
            response.put("Message", e.getMessage());
            response.put("Success", false);
        }
        return response;
    }

}

