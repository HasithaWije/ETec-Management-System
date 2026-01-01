package lk.ijse.etecmanagementsystem.model;

import javafx.scene.control.Alert;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.InventoryItemDTO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitManagementModel {


    public List<ProductItemDTO> getAllProductItems() throws SQLException {
        List<ProductItemDTO> itemList = new ArrayList<>();
        String sql = "SELECT pi.item_id, pi.stock_id, pi.supplier_id, pi.serial_number, " +
                "p.name AS product_name, COALESCE(s.supplier_name, 'No Supplier') AS supplier_name, " +
                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
                "FROM ProductItem pi " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "ORDER BY pi.item_id DESC";

        ResultSet rs = CrudUtil.execute(sql);

        while (rs.next()) {
            ProductItemDTO item = new ProductItemDTO(
                    rs.getInt("item_id"),
                    rs.getInt("stock_id"),
                    rs.getInt("supplier_id"),
                    rs.getString("serial_number"),
                    rs.getString("product_name"),
                    rs.getString("supplier_name"),
                    rs.getInt("supplier_warranty_mo"),
                    rs.getInt("customer_warranty_mo"),
                    rs.getString("status"),
                    rs.getDate("added_date"),
                    rs.getDate("sold_date")

            );
            itemList.add(item);
        }
        rs.close();
        return itemList;
    }

    public boolean createPlaceholderItems(int stockId, int qty) throws SQLException {
        String sql = "INSERT INTO ProductItem (stock_id, serial_number, status, added_date) VALUES (?, ?, 'AVAILABLE', NOW())";

        Connection conn = DBConnection.getInstance().getConnection();
        try {

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

            return true;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Failed to create placeholder items: " + e.getMessage());
        }
    }

    public boolean registerRealItem(ProductItemDTO dto) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

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

            String findSql = "SELECT item_id FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' LIMIT 1 FOR UPDATE";
            int existingId = -1;
            if (dto.getSerialNumber().isEmpty()) {
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

            conn.commit();
            return existingId <= 0 ? -1 : existingId;

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

    public Map<Integer, String> getAllProductMap() throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        try (ResultSet rs = CrudUtil.execute("SELECT stock_id, name FROM Product")) {
            while (rs.next()) {
                map.put(rs.getInt("stock_id"), rs.getString("name"));
            }
        }
        return map;
    }

    public Map<Integer, String> getAllSuppliersMap() throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        try (ResultSet rs = CrudUtil.execute("SELECT supplier_id, supplier_name FROM Supplier")) {
            while (rs.next()) {
                map.put(rs.getInt("supplier_id"), rs.getString("supplier_name"));
            }
        }
        return map;
    }

    public ProductMeta getProductMetaById(int stockId) throws SQLException {
        try (ResultSet rs = CrudUtil.execute("SELECT name, warranty_months FROM Product WHERE stock_id = ?", stockId)) {
            if (rs.next()) {
                return new ProductMeta(stockId, rs.getInt("warranty_months"));
            }
        }
        return null;
    }

    public ItemIds getIdsBySerial(String serial) throws SQLException {
        String sql = "SELECT stock_id, supplier_id FROM ProductItem WHERE serial_number = ?";
        try (ResultSet rs = CrudUtil.execute(sql, serial)) {
            if (rs.next()) {
                return new ItemIds(rs.getInt("stock_id"), rs.getInt("supplier_id"));
            }
        }
        return null;
    }


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
        String sqlQty = "UPDATE Product SET qty = CASE WHEN ? = 'AVAILABLE' THEN qty + 1 ELSE qty - 1 END WHERE stock_id = (SELECT stock_id FROM ProductItem WHERE serial_number = ?)";
        Connection con = null;
        boolean isStatusChange = false;
        boolean isQtyChange = false;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            isStatusChange = CrudUtil.execute(sql, newStatus, newStatus, serial);
            isQtyChange = CrudUtil.execute(sqlQty, newStatus, serial);

            con.commit();
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) con.setAutoCommit(true);
        }
        return isStatusChange && isQtyChange;

    }

    public static class ProductMeta {
        public final int stockId;
        public final int defaultWarranty;

        public ProductMeta(int id, int war) {
            this.stockId = id;
            this.defaultWarranty = war;
        }

        public int getStockId() {
            return stockId;
        }

        public int getDefaultWarranty() {
            return defaultWarranty;
        }
    }

    public static class ItemIds {
        public final int stockId;
        public final int supplierId;

        public ItemIds(int stId, int supId) {
            this.stockId = stId;
            this.supplierId = supId;
        }
    }
}