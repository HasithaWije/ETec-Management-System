package lk.ijse.etecmanagementsystem.bo.custom.impl;

import lk.ijse.etecmanagementsystem.bo.custom.CategoryBO;
import lk.ijse.etecmanagementsystem.dao.DAOFactory;
import lk.ijse.etecmanagementsystem.dao.custom.CategoryDAO;
import lk.ijse.etecmanagementsystem.entity.Category;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryBOImpl implements CategoryBO {
    CategoryDAO categoryDAO = (CategoryDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.CATEGORY);

    @Override
    public List<String> getAllCategories() throws SQLException {
        List<String> list = new ArrayList<>();
        List<Category> categories = categoryDAO.getAll();
        for (Category category  : categories) {
            list.add(category.getCategory_name());
        }
        return list;
    }

    @Override
    public boolean saveCategory(String category) throws SQLException {
        Category categoryObj = new Category(category);
        return categoryDAO.save(categoryObj);
    }

    @Override
    public boolean updateCategory(String newName, String oldName) throws SQLException {
        return categoryDAO.updateCategoryName(newName, oldName);
    }

    @Override
    public boolean deleteCategory(String categoryName) throws SQLException {
        return categoryDAO.deleteCategory(categoryName);
    }
}
