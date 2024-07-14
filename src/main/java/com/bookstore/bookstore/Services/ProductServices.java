package com.bookstore.bookstore.Services;

import com.bookstore.bookstore.EntityModels.IProductModel;
import com.bookstore.bookstore.Enum.ProjectCodes;
import com.bookstore.bookstore.CustomModel.ListModel.Product.ProductsModel;
import com.bookstore.bookstore.CustomModel.Model.ProductModel;
import com.bookstore.bookstore.Repository.AuthJwtRepository;
import com.bookstore.bookstore.Repository.ProductRepository;
import com.bookstore.bookstore.SelectModel.ISelectModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@Service
public class ProductServices implements ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public String SP = "SELECT Sp_Name FROM sp_table WHERE Sp_Code = ?";
    public String SpResult;
    private final AuthJwtRepository authjwtrepository;

    public ProductServices(AuthJwtRepository authjwtrepository) {
        this.authjwtrepository = authjwtrepository;
    }

    @Override
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
    public Map<String, Object> getselectproductlist(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authjwtrepository.isTokenValid(token)) {
                String username = authjwtrepository.getUsernameFromToken(token);
                SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.CHECKUSERROLE.name()}, String.class);
                Map<String, Object> result = jdbcTemplate.queryForMap(SpResult, new Object[]{username});
                String userRoleResult = (String) result.get("Result");
                if (userRoleResult != null) {
                    boolean isAdmin = Boolean.parseBoolean(userRoleResult);

                    if (isAdmin) {
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.SelectCodes.SELECTPRODUCT.name()}, String.class);
                        // Execute stored procedure and fetch product list
                        var I_Select_Product_Model = jdbcTemplate.execute(
                                SpResult,
                                (CallableStatementCallback<List<ISelectModel>>) callableStatement -> {
                                    List<ISelectModel> resultList = new ArrayList<>();
                                    boolean hasResults = callableStatement.execute();

                                    if (hasResults) {
                                        try (ResultSet rs = callableStatement.getResultSet()) {
                                            while (rs != null && rs.next()) {
                                                ISelectModel iSelectModel = new ISelectModel();
                                                iSelectModel.setId(rs.getInt("id"));
                                                iSelectModel.setName(rs.getString("CategoryName"));
                                                resultList.add(iSelectModel);
                                            }
                                        }
                                    }
                                    return resultList;
                                });
                        if (I_Select_Product_Model != null ) {
                            response.put("data", I_Select_Product_Model);
                            response.put("Success", true);
                        }else {
                            response.put("Message", "Product List not found for the specified language.");
                            response.put("Success", false);
                        }
                    } else {
                        response.put("Message", "User is not an admin.");
                        response.put("Success", false);
                    }
                } else {
                    response.put("Message", "User role not found.");
                    response.put("Success", false);
                }
            } else {
                response.put("Message", "Invalid token.");
                response.put("Success", false);
            }
        } catch (Exception e) {
            response.put("Message", e.getMessage());
            response.put("Success", false);
        }
        return response;
    }

    @Override
    public Map<String, Object> save(String Token, IProductModel iProductModel) {
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
                        String newQuery= "INSERT INTO producttypetable (CategoryCode, CategoryName) VALUES (?, ?);";
                        KeyHolder keyHolder = new GeneratedKeyHolder();
                        jdbcTemplate.update(connection -> {
                            PreparedStatement ps = connection.prepareStatement(newQuery, Statement.RETURN_GENERATED_KEYS);
                            ps.setString(1, iProductModel.Code);
                            ps.setString(2, iProductModel.Name);
                            return ps;
                        }, keyHolder);
                        Long generatedId = keyHolder.getKey().longValue();

                        if(generatedId >= 1) {
                            response.put("Message", "Data Added!");
                            response.put("Success", true);
                        }else{
                            response.put("Message", "Data Not Added!");
                            response.put("Success", false);
                        }
                    } else {
                        response.put("Message", "User is Not valid");
                        response.put("Success", false);
                    }
                } else {
                    response.put("Message", "User is Not valid");
                    response.put("Success", false);
                }
            }else {
                    response.put("Message", "User is Not valid");
                    response.put("Success", false);
                }
        }catch (Exception e){
            response.put("Message", e.getMessage());
            response.put("Success", false);
        }
        return response;
    }

    @Override
    public Map<String, Object> update(String Token, IProductModel iProductModel) {
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
                        int pid = Integer.parseInt(authjwtrepository.BookidDecrypt(iProductModel.Pid));
                        String newQuery= "UPDATE producttypetable SET CategoryCode = ?, CategoryName = ?, IsActive = ? WHERE id = ?;";
                        KeyHolder keyHolder = new GeneratedKeyHolder();
                        long rowsAffected = jdbcTemplate.update(connection -> {
                            PreparedStatement ps = connection.prepareStatement(newQuery, Statement.RETURN_GENERATED_KEYS);
                            ps.setString(1, iProductModel.Code);
                            ps.setString(2, iProductModel.Name);
                            ps.setInt(3, iProductModel.isActive ? 1 : 0 );
                            ps.setInt(4, pid);
                            return ps;
                        }, keyHolder);

                        if(rowsAffected > 1) {
                            response.put("Message", "Data Added!");
                            response.put("Success", true);
                        }else{
                            response.put("Message", "Data Not Added!");
                            response.put("Success", false);
                        }
                    } else {
                        response.put("Message", "User is Not valid");
                        response.put("Success", false);
                    }
                } else {
                    response.put("Message", "User is Not valid");
                    response.put("Success", false);
                }
            }else {
                response.put("Message", "User is Not valid");
                response.put("Success", false);
            }
        }catch (Exception e){
            response.put("Message", e.getMessage());
            response.put("Success", false);
        }
        return response;
    }

    @Override
    public Map<String, Object> delete(String Token, String pId) {
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
                        int pid = Integer.parseInt(authjwtrepository.BookidDecrypt(pId));
                        String newQuery= "UPDATE producttypetable SET IsDeleted = ?, IsActive = ?  WHERE id = ?;";
                        KeyHolder keyHolder = new GeneratedKeyHolder();
                        long rowsAffected = jdbcTemplate.update(connection -> {
                            PreparedStatement ps = connection.prepareStatement(newQuery, Statement.RETURN_GENERATED_KEYS);
                            ps.setInt(1, 1);
                            ps.setInt(2, 0);
                            ps.setInt(3, pid);
                            return ps;
                        }, keyHolder);
                        if(rowsAffected > 0) {
                            response.put("Message", "Data Deleted!");
                            response.put("Success", true);
                        }else{
                            response.put("Message", "Data Not Added!");
                            response.put("Success", false);
                        }
                    } else {
                        response.put("Message", "User is Not valid");
                        response.put("Success", false);
                    }
                } else {
                    response.put("Message", "User is Not valid");
                    response.put("Success", false);
                }
            }else {
                response.put("Message", "User is Not valid");
                response.put("Success", false);
            }
        }catch (Exception e){
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
            }else {
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

