package com.bookstore.bookstore.Services;

import com.bookstore.bookstore.CustomModel.ListModel.Category.CategoryTypesModel;
import com.bookstore.bookstore.CustomModel.ListModel.SubCategory.SubCategoryTypesModel;
import com.bookstore.bookstore.CustomModel.Model.CategoryTypeModel;
import com.bookstore.bookstore.CustomModel.Model.SubCategoryTypeModel;
import com.bookstore.bookstore.EntityModels.ISubCategoryModel;
import com.bookstore.bookstore.Enum.ProjectCodes;
import com.bookstore.bookstore.Repository.AuthJwtRepository;
import com.bookstore.bookstore.Repository.SubCategoryRepository;
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
public class SubCategoryServices implements SubCategoryRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public String SP = "SELECT Sp_Name FROM sp_table WHERE Sp_Code = ?";
    public String SpResult = null;
    private final AuthJwtRepository authjwtrepository;
    public SubCategoryServices(AuthJwtRepository authjwtrepository) {
        this.authjwtrepository = authjwtrepository;
    }


    @Override
    public Map<String, Object> getSubCategory(String Token, String report) {
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
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ReportCods.ALLSUBCATEGORYTYPES.name()}, String.class);
                        var subCategoryList = jdbcTemplate.execute(SpResult, (CallableStatementCallback<SubCategoryTypesModel>) callableStatement -> {
                                SubCategoryTypesModel subCategoryTypesModel = new SubCategoryTypesModel();
                                callableStatement.setString(1, report);
                                boolean hasResults = callableStatement.execute();
                                if (hasResults) {
                                    try (ResultSet rs = callableStatement.getResultSet()) {
                                        if (rs != null && rs.next()) {
                                            subCategoryTypesModel.setSubCategoryCount(rs.getInt("total_SubCategory"));
                                        }
                                    }

                                    // Second result set: product details
                                    if (callableStatement.getMoreResults()) {
                                        try (ResultSet rs = callableStatement.getResultSet()) {
                                            List<SubCategoryTypeModel> subCategoryTypeModels = new ArrayList<>();
                                            while (rs != null && rs.next()) {
                                                SubCategoryTypeModel subCategoryTypeModel = new SubCategoryTypeModel();
                                                try {
                                                    subCategoryTypeModel.setSubCategoryId(authjwtrepository.BookidEncrypt(rs.getInt("SubCategoryID")));
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                                subCategoryTypeModel.setProductName(rs.getString("ProductName"));
                                                subCategoryTypeModel.setCategoryName(rs.getString("CategoryName"));
                                                subCategoryTypeModel.setSubCategoryCode(rs.getString("SubCategoryCode"));
                                                subCategoryTypeModel.setSubCategoryPath(rs.getString("SubCategoryPath"));
                                                subCategoryTypeModel.setSubCategoryValue(rs.getString("SubCategoryValue"));
                                                subCategoryTypeModel.setActive(rs.getBoolean("IsActive"));
                                                subCategoryTypeModels.add(subCategoryTypeModel);
                                            }
                                            subCategoryTypesModel.setSubCategoryTypeModels(subCategoryTypeModels);
                                        }
                                    }
                                }
                                return subCategoryTypesModel;
                        });
                        if (subCategoryList != null ) {
                            response.put("data", subCategoryList);
                            response.put("Success", true);
                            response.put("Code", 200);
                        } else {
                            response.put("Message", "subCategoryList List not found for the specified language.");
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
    public Map<String, Object> getSubCategoryCode(String Token, String Code) {
        return Map.of();
    }

    @Override
    public Map<String, Object> getSubCategoryPath(String Token, String Code) {
        return Map.of();
    }

    @Override
    public Map<String, Object> save(String Token, ISubCategoryModel iSubCategoryModel) {
        return Map.of();
    }

    @Override
    public Map<String, Object> update(String Token, ISubCategoryModel iSubCategoryModel) {
        return Map.of();
    }

    @Override
    public Map<String, Object> delete(String Token, String scid) {
        return Map.of();
    }
}
