//package lk.ijse.etecmanagementsystem.model;
//
//import lk.ijse.etecmanagementsystem.dto.CategoryDTO;
//import lk.ijse.etecmanagementsystem.util.CrudUtil;
//
//import java.sql.ResultSet;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CategoryModel {
//
////    public List<String> getAllCategories() throws Exception {
////        String sql = "SELECT * FROM Category";
////
////        List<String> list = new ArrayList<>();
////        try (ResultSet rs = CrudUtil.execute(sql)) {
////            while (rs.next()) {
////                list.add(rs.getString("category_name"));
////            }
////        }
////        return list;
////
////    }
//
////    public boolean saveCategory(String category) throws Exception {
////        String sql = "INSERT INTO Category (category_name) VALUES (?)";
////        return CrudUtil.execute(sql, category);
////    }
////
////    public boolean updateCategory(String newName, String oldName) throws Exception {
////        String sql = "UPDATE Category SET category_name=? WHERE category_name=?";
////        return CrudUtil.execute(sql, newName, oldName);
////    }
////
////    public boolean deleteCategory(String categoryName) throws Exception {
////        String sql = "DELETE FROM Category WHERE category_name=?";
////        return CrudUtil.execute(sql, categoryName);
////    }
//}
