package lk.ijse.etecmanagementsystem.model;


import lk.ijse.etecmanagementsystem.dao.ProductDAOImpl;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ProductModel {

    public int saveProductAndGetId(ProductDTO p) throws SQLException {

        ProductDAOImpl productDAO = new ProductDAOImpl();
        UnitManagementModel unitModel = new UnitManagementModel();

        Connection connection = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start Transaction

                boolean isSaved = productDAO.save(p);
                if (!isSaved) {
                    connection.rollback();
                    throw new SQLException("Failed to save product.");
                }

                int newStockId = productDAO.getLastInsertedProductId();
                if(newStockId <= 0) {
                    connection.rollback();
                    throw new SQLException("Failed to save product and retrieve ID.");
                }

                boolean isCreate = unitModel.createPlaceholderItems(newStockId, p.getQty());
                if (!isCreate) {
                    connection.rollback();
                    throw new SQLException("Failed to create placeholder items for the new product.");
                }

            connection.commit();
            return newStockId;

        } catch (Exception e) {
            if (connection != null) connection.rollback(); // Undo if error
            throw e;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
        }
    }

    public boolean update(ProductDTO p) throws SQLException {
        ProductDAOImpl productDAO = new ProductDAOImpl();

        Connection connection = null;

        String sqlItem = "UPDATE ProductItem SET customer_warranty_mo = ? WHERE stock_id = ? AND status = 'AVAILABLE'";

        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false);


//            try (PreparedStatement pstm = connection.prepareStatement(sqlProduct)) {
//                pstm.setString(1, p.getName());
//                pstm.setString(2, p.getDescription());
//                pstm.setDouble(3, p.getSellPrice());
//                pstm.setString(4, p.getCategory());
//                pstm.setString(5, p.getCondition().getLabel());
//                pstm.setDouble(6, p.getBuyPrice());
//                pstm.setInt(7, p.getWarrantyMonth());
//                pstm.setInt(8, p.getQty());
//                pstm.setString(9, p.getImagePath());
//                pstm.setString(10, p.getId());
//
//                int rows = pstm.executeUpdate();
//                if (rows == 0) {
//                    connection.rollback();
//                    return false;
//                }
//            }

            boolean isUpdated = productDAO.update(p);
            if (!isUpdated) {
                connection.rollback();
                return false;
            }



            try (PreparedStatement pstmItem = connection.prepareStatement(sqlItem)) {
                // Set new warranty period
                pstmItem.setInt(1, p.getWarrantyMonth());
                // Only for this product ID
                pstmItem.setInt(2, Integer.parseInt(p.getId()));

                pstmItem.executeUpdate();
            }

            connection.commit(); // Save both changes
            return true;

        } catch (SQLException e) {
            if (connection != null) connection.rollback(); // Undo if error
            throw e;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
        }
    }

    public int getRealItemCount(int stockId) throws SQLException {
        System.out.println("DEBUG: Querying Real Item Count for Stock ID: " + stockId);

        String sql = "SELECT COUNT(*) FROM ProductItem WHERE stock_id = ? AND serial_number NOT LIKE 'PENDING-%' AND status = 'AVAILABLE'";

        try (ResultSet rs = CrudUtil.execute(sql, stockId)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("DEBUG: Database returned count: " + count);
                return count;
            }
        }
        return 0;
    }

    public boolean updateProductWithQtySync(ProductDTO p) throws SQLException {
        ProductDAOImpl productDAO = new ProductDAOImpl();

        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start Transaction


//            String sqlProduct = "UPDATE Product SET name=?, description=?, sell_price=?, category=?, p_condition=?, buy_price=?, warranty_months=?, qty=?, image_path=? WHERE stock_id=?";
//            try (PreparedStatement pstm = connection.prepareStatement(sqlProduct)) {
//                pstm.setString(1, p.getName());
//                pstm.setString(2, p.getDescription());
//                pstm.setDouble(3, p.getSellPrice());
//                pstm.setString(4, p.getCategory());
//                pstm.setString(5, p.getCondition().getLabel());
//                pstm.setDouble(6, p.getBuyPrice());
//                pstm.setInt(7, p.getWarrantyMonth());
//                pstm.setInt(8, p.getQty()); // Set the NEW Total Qty
//                pstm.setString(9, p.getImagePath());
//                pstm.setString(10, p.getId());
//
//                if (pstm.executeUpdate() == 0) {
//                    connection.rollback();
//                    return false;
//                }
//            }

            boolean isUpdated = productDAO.update(p);
            if (!isUpdated) {
                connection.rollback();
                return false;
            }

            String sqlWarranty = "UPDATE ProductItem SET customer_warranty_mo = ? WHERE stock_id = ? AND status = 'AVAILABLE'";
            try (PreparedStatement pstmWar = connection.prepareStatement(sqlWarranty)) {
                pstmWar.setInt(1, p.getWarrantyMonth());
                pstmWar.setString(2, p.getId());
                pstmWar.executeUpdate();
            }

            String countSql = "SELECT COUNT(*) FROM ProductItem WHERE stock_id = ? AND status = 'AVAILABLE'";
            int currentTotalItems = 0;
            try (PreparedStatement psCount = connection.prepareStatement(countSql)) {
                psCount.setString(1, p.getId());
                ResultSet rs = psCount.executeQuery();
                if (rs.next()) currentTotalItems = rs.getInt(1);
            }

            int targetQty = p.getQty();
            int difference = targetQty - currentTotalItems;

            if (difference > 0) {
                // INCREASE QTY: Add 'difference' amount of Placeholders
                String insertSql = "INSERT INTO ProductItem (stock_id, serial_number, status, added_date) VALUES (?, ?, 'AVAILABLE', NOW())";
                try (PreparedStatement pstmAdd = connection.prepareStatement(insertSql)) {
                    for (int i = 0; i < difference; i++) {
                        pstmAdd.setString(1, p.getId());
                        String tempSerial = "PENDING-" + p.getId() + "-" + System.nanoTime() + "-" + i;
                        pstmAdd.setString(2, tempSerial);
                        pstmAdd.addBatch();
                    }
                    pstmAdd.executeBatch();
                }
            } else if (difference < 0) {

                int removeCount = Math.abs(difference);
                String deleteSql = "DELETE FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' AND status = 'AVAILABLE' LIMIT ?";

                try (PreparedStatement pstmDel = connection.prepareStatement(deleteSql)) {
                    pstmDel.setString(1, p.getId());
                    pstmDel.setInt(2, removeCount);
                    int deletedRows = pstmDel.executeUpdate();

                    if (deletedRows < removeCount) {
                        throw new SQLException("Cannot reduce Quantity below " + (currentTotalItems - deletedRows) + ". You have real items registered that cannot be auto-deleted.");
                    }
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            throw e;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
        }
    }

    public boolean deleteById(String stockId) throws SQLException {
        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start Transaction

            String deleteItemsSql = "DELETE FROM ProductItem WHERE stock_id = ?";
            try (PreparedStatement pstm = connection.prepareStatement(deleteItemsSql)) {
                pstm.setString(1, stockId);
                pstm.executeUpdate();
            }

//            String deleteProductSql = "DELETE FROM Product WHERE stock_id = ?";
//            try (PreparedStatement pstm = connection.prepareStatement(deleteProductSql)) {
//                pstm.setString(1, stockId);
//                int rows = pstm.executeUpdate();
//
//                if (rows > 0) {
//                    connection.commit(); // Success!
//                    return true;
//                } else {
//                    connection.rollback(); // Product didn't exist?
//                    return false;
//                }
//            }

            ProductDAOImpl productDAO = new ProductDAOImpl();
            boolean isDeleted = productDAO.delete(Integer.parseInt(stockId));
            if (!isDeleted) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            if (connection != null) connection.rollback();

            if (e.getMessage().contains("constraint") || e.getMessage().contains("foreign key")) {
                throw new SQLException("Cannot delete this Product because some items have already been SOLD. You cannot delete history.");
            }
            throw e;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
        }
    }

    public ItemDeleteStatus checkItemStatusForDelete(String stockId) throws SQLException {
        String sql = "SELECT serial_number, status FROM ProductItem WHERE stock_id = ?";

        int realAvailableCount = 0;
        int restrictedCount = 0; // SOLD, RMA, etc.

        try (ResultSet rs = CrudUtil.execute(sql, stockId)) {
            while (rs.next()) {
                String serial = rs.getString("serial_number");
                String status = rs.getString("status");

                if (serial != null && serial.startsWith("PENDING")) continue;

                if ("AVAILABLE".equals(status)) {
                    realAvailableCount++;
                } else {

                    restrictedCount++;
                }
            }
        }
        return new ItemDeleteStatus(realAvailableCount, restrictedCount);
    }

    public static class ItemDeleteStatus {
        public final int realAvailableCount;
        public final int restrictedCount;

        public ItemDeleteStatus(int real, int restricted) {
            this.realAvailableCount = real;
            this.restrictedCount = restricted;
        }
    }

//    public ResultSet findById(String id) throws Exception {
//        String sql = "SELECT stock_id, name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path FROM Product WHERE stock_id=?";
//
//        return CrudUtil.execute(sql, id);
//
//    }

//    public int getIdByName(String name) throws SQLException {
//
//        String sql = "SELECT stock_id FROM Product WHERE name=?";
//        try (ResultSet rs = CrudUtil.execute(sql, name)) {
//            if (rs.next()) {
//                return rs.getInt("stock_id");
//            } else {
//                return -1;
//            }
//        }
//    }

//    public List<ProductDTO> findAll() throws Exception {
//        String sql = "SELECT stock_id, name, description, sell_price, category, p_condition, buy_price, warranty_months, qty, image_path FROM Product ORDER BY name";
//
//        List<ProductDTO> products = new ArrayList<>();
//
//        try (ResultSet rs = CrudUtil.execute(sql)) {
//            while (rs.next()) {
//                products.add(mapRow(rs));
//            }
//        }
//
//        return products;
//    }

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

//    private ProductCondition fromConditionString(String s) {
//        if (s == null) return ProductCondition.BOTH;
//        try {
//            if (s.equalsIgnoreCase("USED")) {
//                return ProductCondition.USED;
//            } else if (s.equalsIgnoreCase("BRAND NEW")) {
//                return ProductCondition.BRAND_NEW;
//            }
//            return ProductCondition.BOTH;
//        } catch (IllegalArgumentException ex) {
//            return ProductCondition.BOTH; // unknown condition value
//        }
//    }


}
