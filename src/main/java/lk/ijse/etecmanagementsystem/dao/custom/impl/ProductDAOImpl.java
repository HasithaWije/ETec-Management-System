package lk.ijse.etecmanagementsystem.dao.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.ProductDAO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.Product;
import lk.ijse.etecmanagementsystem.reports.GenerateReports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public boolean save(Product entity) throws SQLException {
        String sql = "INSERT INTO Product (name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        return CrudUtil.execute(sql,
                entity.getName(),
                entity.getDescription(),
                entity.getSell_price(),
                entity.getCategory(),
                entity.getP_condition(),
                entity.getBuy_price(),
                entity.getWarranty_months(),
                entity.getQty(),
                entity.getImage_path()
        );
    }

    @Override
    public int getLastInsertedProductId() throws SQLException {
        String idQuery = "SELECT LAST_INSERT_ID() AS id FROM Product";
        ResultSet rs = CrudUtil.execute(idQuery);
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Failed to retrieve product ID");
        }
    }

    @Override
    public boolean update(Product entity) throws SQLException {
        String sqlProduct = "UPDATE Product SET name=?, description=?, sell_price=?, category=?, p_condition=?, buy_price=?, warranty_months=?, qty=?, image_path=? WHERE stock_id=?";

        return CrudUtil.execute(sqlProduct,
                entity.getName(),
                entity.getDescription(),
                entity.getSell_price(),
                entity.getCategory(),
                entity.getP_condition(),
                entity.getBuy_price(),
                entity.getWarranty_months(),
                entity.getQty(),
                entity.getImage_path(),
                entity.getStock_id()
        );
    }

    @Override
    public boolean updateQty(int stockId, int value) throws SQLException {
        if(value < 0){
            int val = value * -1;
             String qtySql = "UPDATE Product SET qty = qty - ? WHERE stock_id = ?";
            return CrudUtil.execute(qtySql, val, stockId);
        }else {
            String qtySql = "UPDATE Product SET qty = qty + ? WHERE stock_id = ?";
            return CrudUtil.execute(qtySql, value, stockId);
        }

    }

    @Override
    public boolean delete(int id) throws SQLException {
        String deleteProductSql = "DELETE FROM Product WHERE stock_id = ?";
        return CrudUtil.execute(deleteProductSql, id);
    }

    @Override
    public Product search(int id) throws SQLException {
        String sql = "SELECT stock_id, name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path FROM Product WHERE stock_id=?";

        Product entity = null;
        ResultSet rs = CrudUtil.execute(sql, id);
        if (rs.next()) {
            entity = new Product(
                    rs.getInt("stock_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("p_condition"),
                    rs.getInt("qty"),
                    rs.getInt("warranty_months"),
                    rs.getString("image_path"),
                    rs.getDouble("buy_price"),
                    rs.getDouble("sell_price")
            );
        }
        return entity;
    }

    @Override
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

    @Override
    public List<Product> getAll() throws SQLException {
        String sql = "SELECT * FROM Product";
        List<Product> products = new ArrayList<>();

        try (ResultSet rs = CrudUtil.execute(sql)) {
            while (rs.next()) {

                products.add(new Product(
                        rs.getInt("stock_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("p_condition"),
                        rs.getInt("qty"),
                        rs.getInt("warranty_months"),
                        rs.getString("image_path"),
                        rs.getDouble("buy_price"),
                        rs.getDouble("sell_price")
                ));
            }
        }

        return products;
    }

    @Override
    public int getInventoryCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Product ";
        return GenerateReports.getTotalCount(sql);
    }

    @Override
    public int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Product WHERE qty < 5";
        int stock = 0;
        ResultSet rs = CrudUtil.execute(sql);
        if (rs.next()) stock = rs.getInt(1);
        rs.close();
        return stock;
    }
}
