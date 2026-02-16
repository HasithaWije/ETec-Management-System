package lk.ijse.etecmanagementsystem.dao;

import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl {
    public boolean save(ProductDTO p) throws SQLException {
        String sql = "INSERT INTO Product (name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        return CrudUtil.execute(sql,
                p.getName(),
                p.getDescription(),
                p.getSellPrice(),
                p.getCategory(),
                p.getCondition().getLabel(),
                p.getBuyPrice(),
                p.getWarrantyMonth(),
                p.getQty(),
                p.getImagePath()
        );
    }

    public int getLastInsertedProductId() throws SQLException {
        String idQuery = "SELECT LAST_INSERT_ID() AS id";
        ResultSet rs = CrudUtil.execute(idQuery);
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Failed to retrieve product ID");
        }
    }

    public boolean update(ProductDTO p) throws SQLException {
        String sqlProduct = "UPDATE Product SET name=?, description=?, sell_price=?, category=?, p_condition=?, buy_price=?, warranty_months=?, qty=?, image_path=? WHERE stock_id=?";

        return CrudUtil.execute(sqlProduct,
                p.getName(),
                p.getDescription(),
                p.getSellPrice(),
                p.getCategory(),
                p.getCondition().getLabel(),
                p.getBuyPrice(),
                p.getWarrantyMonth(),
                p.getQty(),
                p.getImagePath(),
                p.getId()
        );
    }

    public boolean delete(int id) throws SQLException {
        String deleteProductSql = "DELETE FROM Product WHERE stock_id = ?";
        return CrudUtil.execute(deleteProductSql, id);
    }

    public ProductDTO findById(String id) throws Exception {
        String sql = "SELECT stock_id, name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path FROM Product WHERE stock_id=?";

        ProductDTO product = null;
        ResultSet rs = CrudUtil.execute(sql, id);
        if (rs.next()) {
            product = new ProductDTO(
                    rs.getString("stock_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("sell_price"),
                    rs.getString("category"),
                    fromConditionString(rs.getString("p_condition")),
                    rs.getDouble("buy_price"),
                    rs.getInt("warranty_months"),
                    rs.getInt("qty"),
                    rs.getString("image_path")
            );
        }
        return product;
    }

    public int getIdByName(String name) throws SQLException {

        String sql = "SELECT stock_id FROM Product WHERE name=?";
        try (ResultSet rs = CrudUtil.execute(sql, name)) {
            if (rs.next()) {
                return rs.getInt("stock_id");
            } else {
                return -1;
            }
        }
    }

    public List<ProductDTO> findAll() throws Exception {
        String sql = "SELECT stock_id, name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path FROM Product ORDER BY name";

        List<ProductDTO> products = new ArrayList<>();

        try (ResultSet rs = CrudUtil.execute(sql)) {
            while (rs.next()) {

                products.add(new ProductDTO(
                        rs.getString("stock_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("sell_price"),
                        rs.getString("category"),
                        fromConditionString(rs.getString("p_condition")),
                        rs.getDouble("buy_price"),
                        rs.getInt("warranty_months"),
                        rs.getInt("qty"),
                        rs.getString("image_path")
                ));
            }
        }

        return products;
    }

    private ProductCondition fromConditionString(String s) {
        if (s == null) return null;
        try {
            if (s.equals("Used")) {
                return ProductCondition.USED;
            } else if (s.equals("Brand New")) {
                return ProductCondition.BRAND_NEW;
            }
            return ProductCondition.BOTH;
        } catch (IllegalArgumentException ex) {
            return ProductCondition.BOTH; // unknown condition value
        }
    }
}
