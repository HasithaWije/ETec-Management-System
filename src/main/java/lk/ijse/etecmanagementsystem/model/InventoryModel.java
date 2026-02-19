//package lk.ijse.etecmanagementsystem.model;
//
//
//import lk.ijse.etecmanagementsystem.dto.ProductDTO;
//import lk.ijse.etecmanagementsystem.util.CrudUtil;
//import lk.ijse.etecmanagementsystem.util.ProductCondition;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class InventoryModel {
//
//    public List<ProductDTO> findAll() throws Exception {
////        String sql = "SELECT stock_id, name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path FROM Product ORDER BY name";
//        String sql = "SELECT * FROM Product";
//
//        List<ProductDTO> products = new ArrayList<>();
//
//        try (ResultSet rs = CrudUtil.execute(sql)) {
//            while (rs.next()) {
//                products.add(mapRow(rs));
//            }
//        }
//        return products;
//    }
//
//
//    private ProductDTO mapRow(ResultSet rs) throws SQLException {
//        String id = rs.getString("stock_id");
//        String name = rs.getString("name");
//        String description = rs.getString("description");
//        double sellPrice = rs.getDouble("sell_price");
//        String category = rs.getString("category");
//        String condStr = rs.getString("p_condition");
//        double buyPrice = rs.getDouble("buy_price");
//        int warrantyMonth = rs.getInt("warranty_months");
//        int qty = rs.getInt("qty");
//        String imagePath = rs.getString("image_path");
//
//        ProductCondition condition = fromConditionString(condStr);
//        return new ProductDTO(id, name, description, sellPrice, category, condition, buyPrice, warrantyMonth, qty, imagePath);
//    }
//
//
//    private ProductCondition fromConditionString(String s) {
//        if (s == null) return ProductCondition.BOTH;
//        try {
//            if (s.equals("USED")) {
//                return ProductCondition.USED;
//            } else if (s.equals("BRAND NEW")) {
//                return ProductCondition.BRAND_NEW;
//            }
//            return ProductCondition.BOTH;
//        } catch (IllegalArgumentException ex) {
//            return ProductCondition.BOTH; // unknown condition value
//        }
//    }
//}
