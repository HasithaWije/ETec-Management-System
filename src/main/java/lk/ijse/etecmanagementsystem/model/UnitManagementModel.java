package lk.ijse.etecmanagementsystem.model;

import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitManagementModel {

    // --- 1. CHANGED: Get Map of ID -> Name ---
    public Map<Integer, String> getAllProductMap() throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        try (ResultSet rs = CrudUtil.execute("SELECT stock_id, name FROM Product")) {
            while (rs.next()) {
                map.put(rs.getInt("stock_id"), rs.getString("name"));
            }
        }
        return map;
    }

    // --- 2. CHANGED: Get Map of ID -> Name ---
    public Map<Integer, String> getAllSuppliersMap() throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        try (ResultSet rs = CrudUtil.execute("SELECT supplier_id, supplier_name FROM Supplier")) {
            while (rs.next()) {
                map.put(rs.getInt("supplier_id"), rs.getString("supplier_name"));
            }
        }
        return map;
    }

    // --- 3. NEW: Get Meta by ID ---
    public ProductMeta getProductMetaById(int stockId) throws SQLException {
        try (ResultSet rs = CrudUtil.execute("SELECT name, warranty_months FROM Product WHERE stock_id = ?", stockId)) {
            if (rs.next()) {
                return new ProductMeta(stockId, rs.getInt("warranty_months"));
            }
        }
        return null;
    }

    // --- 4. NEW: Helper to get specific IDs for a serial (Used in Fix Tab) ---
    public ItemIds getIdsBySerial(String serial) throws SQLException {
        String sql = "SELECT stock_id, supplier_id FROM ProductItem WHERE serial_number = ?";
        try (ResultSet rs = CrudUtil.execute(sql, serial)) {
            if (rs.next()) {
                return new ItemIds(rs.getInt("stock_id"), rs.getInt("supplier_id"));
            }
        }
        return null;
    }

    // --- EXISTING METHODS (Optimized) ---

    public boolean saveBatch(int stockId, Integer supplierId, int supWar, int custWar, List<String> serials) throws SQLException {
        if (serials.isEmpty()) return false;

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO ProductItem (stock_id, supplier_id, serial_number, supplier_warranty_mo, customer_warranty_mo, status, added_date) VALUES (?, ?, ?, ?, ?, 'AVAILABLE', NOW())")) {
                for (String serial : serials) {
                    ps.setInt(1, stockId);
                    if (supplierId == null) ps.setNull(2, Types.INTEGER);
                    else ps.setInt(2, supplierId);
                    ps.setString(3, serial);
                    ps.setInt(4, supWar);
                    ps.setInt(5, custWar);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE Product SET qty = qty + ? WHERE stock_id = ?")) {
                ps.setInt(1, serials.size());
                ps.setInt(2, stockId);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.setAutoCommit(true);
        }
    }

    public List<ProductItemDTO> getUnitsByStockId(int stockId, String productName) throws SQLException {
        List<ProductItemDTO> list = new ArrayList<>();
        String sql = "SELECT pi.serial_number, pi.supplier_warranty_mo, pi.customer_warranty_mo, " +
                "pi.status, pi.added_date, pi.sold_date, s.supplier_name " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "WHERE pi.stock_id = ? ORDER BY pi.item_id DESC";

        Connection conn = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setInt(1, stockId);
        ResultSet rs = pstm.executeQuery();

        while (rs.next()) {
            String supName = rs.getString("supplier_name");
            if (supName == null) supName = "No Supplier";

            list.add(new ProductItemDTO(
                    rs.getString("serial_number"),
                    productName,
                    supName,
                    rs.getInt("supplier_warranty_mo"),
                    rs.getInt("customer_warranty_mo"),
                    rs.getString("status"),
                    rs.getDate("added_date"),
                    rs.getDate("sold_date")
            ));
        }
        rs.close();
        pstm.close();
        return list;
    }

    public ProductItemDTO getItemBySerial(String serial) throws SQLException {
        String sql = "SELECT pi.serial_number, p.name as product_name, COALESCE(s.supplier_name, 'No Supplier') as supplier_name, " +
                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.serial_number = ?";

        Connection conn = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = conn.prepareStatement(sql);
        pstm.setString(1, serial);
        ResultSet rs = pstm.executeQuery();
        ProductItemDTO productItemDTO = null;

        if (rs.next()) {
             productItemDTO = new ProductItemDTO(
                    rs.getString("serial_number"), rs.getString("product_name"), rs.getString("supplier_name"),
                    rs.getInt("supplier_warranty_mo"), rs.getInt("customer_warranty_mo"),
                    rs.getString("status"), rs.getDate("added_date"), rs.getDate("sold_date")
            );
        }
        pstm.close();
        rs.close();
        return productItemDTO;
    }

    public boolean correctItemMistake(String oldSerial, String newSerial, int newStockId, Integer newSupplierId, int newSupWar) throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            int oldStockId = -1;
            try (PreparedStatement ps = con.prepareStatement("SELECT stock_id FROM ProductItem WHERE serial_number = ?")) {
                ps.setString(1, oldSerial);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) oldStockId = rs.getInt("stock_id");
                    else return false;
                }
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE ProductItem SET serial_number=?, stock_id=?, supplier_id=?, supplier_warranty_mo=? WHERE serial_number=?")) {
                ps.setString(1, newSerial);
                ps.setInt(2, newStockId);
                if (newSupplierId == null) ps.setNull(3, Types.INTEGER);
                else ps.setInt(3, newSupplierId);
                ps.setInt(4, newSupWar);
                ps.setString(5, oldSerial);
                ps.executeUpdate();
            }

            // Adjust stock counts if the product type changed
            if (oldStockId != -1 && oldStockId != newStockId) {
                try (PreparedStatement p1 = con.prepareStatement("UPDATE Product SET qty = qty - 1 WHERE stock_id = ?")) {
                    p1.setInt(1, oldStockId);
                    p1.executeUpdate();
                }
                try (PreparedStatement p2 = con.prepareStatement("UPDATE Product SET qty = qty + 1 WHERE stock_id = ?")) {
                    p2.setInt(1, newStockId);
                    p2.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.setAutoCommit(true);
        }
    }

    public boolean updateItemStatus(String serial, String newStatus) throws SQLException {
        String sql = "UPDATE ProductItem SET status = ?, sold_date = CASE WHEN ? = 'SOLD' THEN NOW() ELSE sold_date END WHERE serial_number = ?";
        return CrudUtil.execute(sql, newStatus, newStatus, serial);
    }

    // --- Data Classes ---
    public static class ProductMeta {
        public final int stockId;
        public final int defaultWarranty;
        public ProductMeta(int id, int war) { this.stockId = id; this.defaultWarranty = war; }
        public int getStockId() { return stockId; }
        public int getDefaultWarranty() { return defaultWarranty; }
    }

    public static class ItemIds {
        public final int stockId;
        public final int supplierId;
        public ItemIds(int stId, int supId) { this.stockId = stId; this.supplierId = supId; }
    }
}