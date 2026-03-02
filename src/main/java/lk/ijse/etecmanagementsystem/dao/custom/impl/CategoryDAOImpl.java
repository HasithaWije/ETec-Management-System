package lk.ijse.etecmanagementsystem.dao.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.CategoryDAO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.Category;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public List<Category> getAll() throws SQLException {
        String sql = "SELECT * FROM Category";

        List<Category> list = new ArrayList<>();
        try (ResultSet rs = CrudUtil.execute(sql)) {
            while (rs.next()) {
                String categoryName = rs.getString("category_name");
                list.add(new Category(categoryName));
            }
        }
        return list;

    }

    @Override
    public boolean save(Category entity) throws SQLException {
        String sql = "INSERT INTO Category (category_name) VALUES (?)";
        return CrudUtil.execute(sql, entity.getCategory_name());
    }

    @Override
    public boolean update(Category entity) throws SQLException {
        return false;
    }

    @Override
    public boolean updateCategoryName(String newName, String oldName) throws SQLException {
        String sql = "UPDATE Category SET category_name=? WHERE category_name=?";
        return CrudUtil.execute(sql, newName, oldName);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    @Override
    public boolean deleteCategory(String categoryName) throws SQLException {
        String sql = "DELETE FROM Category WHERE category_name=?";
        return CrudUtil.execute(sql, categoryName);
    }

    @Override
    public Category search(int id) throws SQLException {
        return null;
    }
}
