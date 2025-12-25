package lk.ijse.etecmanagementsystem.model;

import javafx.scene.control.Alert;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitManagementModel {

    // --- METHOD A: Create Empty Slots (Call this when adding a NEW Product) ---
    public boolean createPlaceholderItems(int stockId, int qty) throws SQLException {
        String sql = "INSERT INTO ProductItem (stock_id, serial_number, status, added_date) VALUES (?, ?, 'AVAILABLE', NOW())";

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstm = conn.prepareStatement(sql)) {
                for (int i = 0; i < qty; i++) {
                    pstm.setInt(1, stockId);
                    // Create a UNIQUE temporary serial: PENDING-{StockID}-{CurrentTime}-{Index}
                    // Example: PENDING-55-1703345200-1
                    String tempSerial = "PENDING-" + stockId + "-" + System.nanoTime() + "-" + i;
                    pstm.setString(2, tempSerial);
                    pstm.addBatch();
                }
                pstm.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    // --- METHOD B: The "Smart Add" (Call this when scanning items) ---
    public boolean registerRealItem(ProductItemDTO dto) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Check for an available "Placeholder" for this product
            // We look for serials starting with 'PENDING-' for this stock_id
            String findSql = "SELECT item_id FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' LIMIT 1 FOR UPDATE";
            int placeholderId = -1;

            try (PreparedStatement findPstm = conn.prepareStatement(findSql)) {
                findPstm.setInt(1, dto.getStockId());
                try (ResultSet rs = findPstm.executeQuery()) {
                    if (rs.next()) {
                        placeholderId = rs.getInt("item_id");
                    }
                }
            }

            if (placeholderId != -1) {
                // CASE 1: Empty Slot Found -> UPDATE it (Qty does NOT change)
                String updateSql = "UPDATE ProductItem SET serial_number = ?, supplier_id = ?, supplier_warranty_mo = ?, customer_warranty_mo = ?, status = 'AVAILABLE', added_date = NOW() WHERE item_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, dto.getSerialNumber());
                    if (dto.getSupplierId() == 0) ps.setNull(2, java.sql.Types.INTEGER);
                    else ps.setInt(2, dto.getSupplierId());

                    ps.setInt(3, dto.getSupplierWarranty());
                    ps.setInt(4, dto.getCustomerWarranty());
                    ps.setInt(5, placeholderId);
                    ps.executeUpdate();
                }
            } else {
                // CASE 2: No Slot Found -> INSERT new row AND Increase Product Qty
                String insertSql = "INSERT INTO ProductItem (stock_id, supplier_id, serial_number, supplier_warranty_mo, customer_warranty_mo, status, added_date) VALUES (?, ?, ?, ?, ?, 'AVAILABLE', NOW())";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, dto.getStockId());
                    if (dto.getSupplierId() == 0) ps.setNull(2, java.sql.Types.INTEGER);
                    else ps.setInt(2, dto.getSupplierId());

                    ps.setString(3, dto.getSerialNumber());
                    ps.setInt(4, dto.getSupplierWarranty());
                    ps.setInt(5, dto.getCustomerWarranty());
                    ps.executeUpdate();
                }

                // Sync the main Product Qty
                String qtySql = "UPDATE Product SET qty = qty + 1 WHERE stock_id = ?";
                try (PreparedStatement qtyPs = conn.prepareStatement(qtySql)) {
                    qtyPs.setInt(1, dto.getStockId());
                    qtyPs.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }




    public int addItemAndGetGeneratedId(ProductItemDTO dto) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Check if an Empty Slot (Placeholder) exists for this Product
            String findSql = "SELECT item_id FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' LIMIT 1 FOR UPDATE";
            int existingId = -1;
            if(dto.getSerialNumber().isEmpty()){
                dto.setSerialNumber(null);
            }

            try (PreparedStatement findPstm = conn.prepareStatement(findSql)) {
                findPstm.setInt(1, dto.getStockId());
                try (ResultSet rs = findPstm.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("item_id");
                    }
                }
            }

            int finalId;

            if (existingId != -1) {
                // --- SCENARIO A: Placeholder Found -> UPDATE it ---
                String updateSql = "UPDATE ProductItem SET serial_number = ?, customer_warranty_mo = ?, status = 'AVAILABLE', added_date = NOW() WHERE item_id = ?";
                try (PreparedStatement updatePstm = conn.prepareStatement(updateSql)) {
                    updatePstm.setString(1, dto.getSerialNumber());
                    updatePstm.setInt(2, dto.getCustomerWarranty());
                    updatePstm.setInt(3, existingId);
                    updatePstm.executeUpdate();
                }
                finalId = existingId; // Return the ID of the slot we just filled
            } else {
                // --- SCENARIO B: No Placeholder -> INSERT New ---
                String insertSql = "INSERT INTO ProductItem (stock_id, serial_number, customer_warranty_mo, status, added_date) VALUES (?, ?, ?, 'AVAILABLE', NOW())";

                try (PreparedStatement insertPstm = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertPstm.setInt(1, dto.getStockId());
                    insertPstm.setString(2, dto.getSerialNumber());
                    insertPstm.setInt(3, dto.getCustomerWarranty());

                    int affectedRows = insertPstm.executeUpdate();
                    if (affectedRows == 0) throw new SQLException("Creating item failed, no rows affected.");

                    try (ResultSet generatedKeys = insertPstm.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            finalId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating item failed, no ID obtained.");
                        }
                    }
                }

                // CRITICAL: Since we added a NEW line, we must increase the Product Qty
                String updateQtySql = "UPDATE Product SET qty = qty + 1 WHERE stock_id = ?";
                try (PreparedStatement qtyPstm = conn.prepareStatement(updateQtySql)) {
                    qtyPstm.setInt(1, dto.getStockId());
                    qtyPstm.executeUpdate();
                }
            }

            conn.commit();
            return finalId;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    public int updateSerialNumber(int itemId, String newSerial) throws SQLException {
        String sql = "UPDATE ProductItem SET serial_number = ? WHERE item_id = ?";
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, newSerial);
            pstm.setInt(2, itemId);

            return pstm.executeUpdate();
        }
    }

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
                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
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
                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
                     rs.getString("product_name"), rs.getString("supplier_name"),
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