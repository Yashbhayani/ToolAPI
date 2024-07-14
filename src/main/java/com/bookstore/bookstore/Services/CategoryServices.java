package com.bookstore.bookstore.Services;

import com.bookstore.bookstore.CustomModel.ListModel.Category.CategoryTypesModel;
import com.bookstore.bookstore.CustomModel.Model.CategoryTypeModel;
import com.bookstore.bookstore.EntityModels.ICategoryModel;
import com.bookstore.bookstore.Enum.ProjectCodes;
import com.bookstore.bookstore.Repository.AuthJwtRepository;
import com.bookstore.bookstore.Repository.CategoryRepository;
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

    @Override
    public Map<String, Object> getselectCategorylist(String token, int Pid) {
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
                                    callableStatement.setInt(1, Pid);
                                    List<ISelectModel> resultList = new ArrayList<>();
                                    boolean hasResults = callableStatement.execute();

                                    if (hasResults) {
                                        try (ResultSet rs = callableStatement.getResultSet()) {
                                            while (rs != null && rs.next()) {
                                                ISelectModel iSelectModel = new ISelectModel();
                                                iSelectModel.setId(rs.getInt("Id"));
                                                iSelectModel.setName(rs.getString("Value"));
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
    public Map<String, Object> getCategoryCode(String Token, String Code) {
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
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.VERIFYCATEGORYCODE.name()}, String.class);
                        boolean categoryCode = Boolean.TRUE.equals(jdbcTemplate.execute(SpResult, (CallableStatementCallback<Boolean>) callableStatement -> {
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

                        response.put("data", categoryCode);
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

    @Override
    public Map<String, Object> getCategoryPath(String Token, String Path) {
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
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.VERIFYCATEGORYPATH.name()}, String.class);
                        boolean categoryPath = Boolean.TRUE.equals(jdbcTemplate.execute(SpResult, (CallableStatementCallback<Boolean>) callableStatement -> {
                            callableStatement.setString(1, Path);
                            boolean hasResults = callableStatement.execute();
                            if (hasResults) {
                                try (ResultSet rs = callableStatement.getResultSet()) {
                                    if (rs != null && rs.next()) {
                                        return rs.getBoolean("VerifyPath");
                                    }
                                }
                            }
                            return false;
                        }));

                        response.put("data", categoryPath);
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

    @Override
    public Map<String, Object> save(String Token, ICategoryModel iCategoryModel) {
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
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.PRODUCTSTATUS.name()}, String.class);
                        Map<String, Object> S_result = jdbcTemplate.queryForMap(SpResult, new Object[]{iCategoryModel.pID});
                        Long statusResultLong = (Long) S_result.get("Status"); // Change to Long
                        if(statusResultLong == 1 ? true : false){
                            String newQuery = "INSERT INTO categorytable (producttypeID, Code, path, Value) VALUES (?, ?, ?, ?);";
                            KeyHolder keyHolder = new GeneratedKeyHolder();

                            jdbcTemplate.update(connection -> {
                                PreparedStatement ps = connection.prepareStatement(newQuery, Statement.RETURN_GENERATED_KEYS);
                                ps.setInt(1, iCategoryModel.pID);         // Index 1 for producttypeID
                                ps.setString(2, iCategoryModel.code);     // Index 2 for Code
                                ps.setString(3, iCategoryModel.path);     // Index 3 for path
                                ps.setString(4, iCategoryModel.name);     // Index 4 for Value
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
                        }else {
                            response.put("Message", "Product is Not valid");
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
    public Map<String, Object> update(String Token, ICategoryModel iCategoryModel) {

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
                        int cid = Integer.parseInt(authjwtrepository.BookidDecrypt(iCategoryModel.sID));
                        SpResult = jdbcTemplate.queryForObject(SP, new Object[]{ProjectCodes.ProjectSpCodes.PRODUCTSTATUS.name()}, String.class);
                        Map<String, Object> S_result = jdbcTemplate.queryForMap(SpResult, new Object[]{iCategoryModel.pID});
                        Long statusResultLong = (Long) S_result.get("Status"); // Change to Long
                        if(statusResultLong == 1){
                            String newQuery = "UPDATE categorytable SET producttypeID = ?, Code = ?, path = ?, Value = ?, IsActive = ? WHERE Id = ?;";
                            KeyHolder keyHolder = new GeneratedKeyHolder();

                            long rowsAffected = jdbcTemplate.update(connection -> {
                                PreparedStatement ps = connection.prepareStatement(newQuery, Statement.RETURN_GENERATED_KEYS);
                                ps.setInt(1, iCategoryModel.pID);
                                ps.setString(2, iCategoryModel.code);
                                ps.setString(3, iCategoryModel.path);
                                ps.setString(4, iCategoryModel.name);
                                ps.setInt(5, iCategoryModel.isActive ? 1 : 0);
                                ps.setInt(6, cid);
                                return ps;
                            }, keyHolder);


                            if(rowsAffected >= 1) {
                                response.put("Message", "Data Updated!");
                                response.put("Success", true);
                            }else{
                                response.put("Message", "Data Not Added!");
                                response.put("Success", false);
                            }
                        }else {
                            response.put("Message", "Product is Not valid");
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
    public Map<String, Object> delete(String Token, String cId) {
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
                        int cid = Integer.parseInt(authjwtrepository.BookidDecrypt(cId));
                        String newQuery = "UPDATE categorytable SET IsDeleted = ?, IsActive = ? WHERE Id = ?;";
                        KeyHolder keyHolder = new GeneratedKeyHolder();
                        long rowsAffected = jdbcTemplate.update(connection -> {
                            PreparedStatement ps = connection.prepareStatement(newQuery, Statement.RETURN_GENERATED_KEYS);
                            ps.setInt(1, 1);
                            ps.setInt(2, 0);
                            ps.setInt(3, cid);
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
}
