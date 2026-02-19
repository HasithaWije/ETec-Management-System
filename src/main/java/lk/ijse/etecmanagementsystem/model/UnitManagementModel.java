//package lk.ijse.etecmanagementsystem.model;
//
//import javafx.scene.control.Alert;
//import lk.ijse.etecmanagementsystem.dao.ProductDAOImpl;
//import lk.ijse.etecmanagementsystem.dao.ProductItemDAOImpl;
//import lk.ijse.etecmanagementsystem.dao.SupplierDAOImpl;
//import lk.ijse.etecmanagementsystem.db.DBConnection;
//import lk.ijse.etecmanagementsystem.dto.InventoryItemDTO;
//import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
//import lk.ijse.etecmanagementsystem.dto.SupplierDTO;
//import lk.ijse.etecmanagementsystem.util.CrudUtil;
//import lk.ijse.etecmanagementsystem.util.ProductCondition;
//
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class UnitManagementModel {
//
//
////    public List<ProductItemDTO> getAllProductItems() throws SQLException {
////        List<ProductItemDTO> itemList = new ArrayList<>();
////        String sql = "SELECT pi.item_id, pi.stock_id, pi.supplier_id, pi.serial_number, " +
////                "p.name AS product_name, COALESCE(s.supplier_name, 'No Supplier') AS supplier_name, " +
////                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
////                "FROM ProductItem pi " +
////                "JOIN Product p ON pi.stock_id = p.stock_id " +
////                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
////                "ORDER BY pi.item_id DESC";
////
////        ResultSet rs = CrudUtil.execute(sql);
////
////        while (rs.next()) {
////            ProductItemDTO item = new ProductItemDTO(
////                    rs.getInt("item_id"),
////                    rs.getInt("stock_id"),
////                    rs.getInt("supplier_id"),
////                    rs.getString("serial_number"),
////                    rs.getString("product_name"),
////                    rs.getString("supplier_name"),
////                    rs.getInt("supplier_warranty_mo"),
////                    rs.getInt("customer_warranty_mo"),
////                    rs.getString("status"),
////                    rs.getDate("added_date"),
////                    rs.getDate("sold_date")
////
////            );
////            itemList.add(item);
////        }
////        rs.close();
////        return itemList;
////    }
//
////    public boolean createPlaceholderItems(int stockId, int qty) throws SQLException {
////        String sql = "INSERT INTO ProductItem (stock_id, serial_number, status, added_date) VALUES (?, ?, 'AVAILABLE', NOW())";
////
////        Connection conn = DBConnection.getInstance().getConnection();
////        try {
////
////            try (PreparedStatement pstm = conn.prepareStatement(sql)) {
////                for (int i = 0; i < qty; i++) {
////                    pstm.setInt(1, stockId);
////                    // Create a UNIQUE temporary serial: PENDING-{StockID}-{CurrentTime}-{Index}
////                    // Example: PENDING-55-1703345200-1
////                    String tempSerial = "PENDING-" + stockId + "-" + System.nanoTime() + "-" + i;
////                    pstm.setString(2, tempSerial);
////                    pstm.addBatch();
////                }
////                pstm.executeBatch();
////            }
////
////            return true;
////        } catch (Exception e) {
////            if (conn != null) conn.rollback();
////            throw new SQLException("Failed to create placeholder items: " + e.getMessage());
////        }
////    }
//
//    public boolean addNewSerialNo(ArrayList<ProductItemDTO> itemDTOS) throws SQLException {
//        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
//
//        Connection conn = null;
//        try {
//            conn = DBConnection.getInstance().getConnection();
//            conn.setAutoCommit(false);
//
//            ArrayList<ProductItemDTO> placeholders = productItemDAO.getPlaceHolderItems(itemDTOS.get(0).getStockId());
//
//            int i = 0;
//            for(ProductItemDTO dto : itemDTOS){
//                int placeholderId = -1;
//
//                if (!placeholders.isEmpty() && i < placeholders.size()) {
//                    placeholderId = placeholders.get(i++).getItemId();
//                    System.out.println("DEBUG: Found placeholder with ID " + placeholderId + " for Stock ID " + dto.getStockId());
//                }
//                if (placeholderId != -1) {
//
//                    boolean isUpdated = productItemDAO.updateItem(dto);
//                    if (!isUpdated) {
//                        conn.rollback();
//                        throw new SQLException("Failed to update placeholder item with ID " + placeholderId);
//                    }
//                } else {
//
//                    boolean isAdded = productItemDAO.addProductItem(dto);
//                    if (!isAdded) {
//                        conn.rollback();
//                        throw new SQLException("Failed to add new item for Stock ID " + dto.getStockId());
//                    }
//
//                    ProductDAOImpl productDAO = new ProductDAOImpl();
//                    boolean isQtyUpdated = productDAO.updateQty(dto.getStockId(), 1);
//                    if (!isQtyUpdated) {
//                        conn.rollback();
//                        throw new SQLException("Failed to update product quantity for Stock ID " + dto.getStockId());
//                    }
//                }
//            }
//            conn.commit();
//            return true;
//        } catch (SQLException e) {
//            if (conn != null) conn.rollback();
//            throw e;
//        } finally {
//            if (conn != null) conn.setAutoCommit(true);
//        }
//    }
//
//
////    public List<Integer> getAvailablePendingSlot(int productId) throws SQLException {
////
////            String findSql = "SELECT item_id  FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%'";
////            List<Integer> slotList = new ArrayList<>();
////
////
////            try (ResultSet rs = CrudUtil.execute(findSql, productId)) {
////                while (rs.next()) {
////                    slotList.add(rs.getInt("item_id"));
////                }
////            }
////            return slotList;
////    }
//
////    public int updateSerialNumber(int itemId, String newSerial) throws SQLException {
////        String sql = "UPDATE ProductItem SET serial_number = ? WHERE item_id = ?";
////        Connection conn = DBConnection.getInstance().getConnection();
////        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
////
////            pstm.setString(1, newSerial);
////            pstm.setInt(2, itemId);
////
////            return pstm.executeUpdate();
////        }
////    }
//
////    public boolean checkSerialExists(String serial) throws SQLException {
////        String sql = "SELECT 1 FROM ProductItem WHERE serial_number = ?";
////        try (ResultSet rs = CrudUtil.execute(sql, serial)) {
////            return rs.next();
////        }
////    }
//
//    public Map<Integer, String> getAllProductMap() throws SQLException {
//        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
//
//        Map<Integer, String> map = new HashMap<>();
//        List<ProductItemDTO> items = productItemDAO.getAllProductItems();
//        for (ProductItemDTO item : items) {
//            map.put(item.getStockId(), item.getProductName());
//        }
//        return map;
//    }
//
//    public Map<Integer, String> getAllSuppliersMap() throws SQLException {
//        SupplierDAOImpl supplierDAO = new SupplierDAOImpl();
//
//        Map<Integer, String> map = new HashMap<>();
//        List<SupplierDTO> suppliers = supplierDAO.getAllSuppliers();
//        for (SupplierDTO sup : suppliers) {
//            map.put(sup.getSupplierId(), sup.getSupplierName());
//        }
//        return map;
//
//    }
//
//    public ProductMeta getProductMetaById(int stockId) throws SQLException {
//        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
//
//        List<ProductItemDTO> items = productItemDAO.getAllProductItems();
//        for (ProductItemDTO item : items) {
//            if (item.getStockId() == stockId) {
//                return new ProductMeta(stockId, item.getSupplierWarranty());
//            }
//        }
//        return null;
//    }
//
////    public ItemIds getIdsBySerial(String serial) throws SQLException {
////        String sql = "SELECT stock_id, supplier_id FROM ProductItem WHERE serial_number = ?";
////        try (ResultSet rs = CrudUtil.execute(sql, serial)) {
////            if (rs.next()) {
////                return new ItemIds(rs.getInt("stock_id"), rs.getInt("supplier_id"));
////            }
////        }
////        return null;
////    }
//
//
////    public boolean saveBatch(int stockId, Integer supplierId, int supWar, int custWar, List<String> serials) throws SQLException {
////        if (serials.isEmpty()) return false;
////
////        Connection con = null;
////        try {
////            con = DBConnection.getInstance().getConnection();
////            con.setAutoCommit(false);
////
////            try (PreparedStatement ps = con.prepareStatement("INSERT INTO ProductItem (stock_id, supplier_id, serial_number, supplier_warranty_mo, customer_warranty_mo, status, added_date) VALUES (?, ?, ?, ?, ?, 'AVAILABLE', NOW())")) {
////                for (String serial : serials) {
////                    ps.setInt(1, stockId);
////                    if (supplierId == null) ps.setNull(2, Types.INTEGER);
////                    else ps.setInt(2, supplierId);
////                    ps.setString(3, serial);
////                    ps.setInt(4, supWar);
////                    ps.setInt(5, custWar);
////                    ps.addBatch();
////                }
////                ps.executeBatch();
////            }
////
////            try (PreparedStatement ps = con.prepareStatement("UPDATE Product SET qty = qty + ? WHERE stock_id = ?")) {
////                ps.setInt(1, serials.size());
////                ps.setInt(2, stockId);
////                ps.executeUpdate();
////            }
////
////            con.commit();
////            return true;
////        } catch (SQLException e) {
////            if (con != null) con.rollback();
////            throw e;
////        } finally {
////            if (con != null) con.setAutoCommit(true);
////        }
////    }
//
////    public List<ProductItemDTO> getUnitsByStockId(int stockId, String productName) throws SQLException {
////        List<ProductItemDTO> list = new ArrayList<>();
////        String sql = "SELECT pi.serial_number, pi.supplier_warranty_mo, pi.customer_warranty_mo, " +
////                "pi.status, pi.added_date, pi.sold_date, s.supplier_name " +
////                "FROM ProductItem pi " +
////                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
////                "WHERE pi.stock_id = ? ORDER BY pi.item_id DESC";
////
////        Connection conn = DBConnection.getInstance().getConnection();
////        PreparedStatement pstm = conn.prepareStatement(sql);
////        pstm.setInt(1, stockId);
////        ResultSet rs = pstm.executeQuery();
////
////        while (rs.next()) {
////            String supName = rs.getString("supplier_name");
////            if (supName == null) supName = "No Supplier";
////
////            list.add(new ProductItemDTO(
////                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
////                    productName,
////                    supName,
////                    rs.getInt("supplier_warranty_mo"),
////                    rs.getInt("customer_warranty_mo"),
////                    rs.getString("status"),
////                    rs.getDate("added_date"),
////                    rs.getDate("sold_date")
////            ));
////        }
////        rs.close();
////        pstm.close();
////        return list;
////    }
//
////    public ProductItemDTO getItemBySerial(String serial) throws SQLException {
////        String sql = "SELECT pi.serial_number, p.name as product_name, COALESCE(s.supplier_name, 'No Supplier') as supplier_name, " +
////                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
////                "FROM ProductItem pi " +
////                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
////                "JOIN Product p ON pi.stock_id = p.stock_id " +
////                "WHERE pi.serial_number = ?";
////
////        Connection conn = DBConnection.getInstance().getConnection();
////        PreparedStatement pstm = conn.prepareStatement(sql);
////        pstm.setString(1, serial);
////        ResultSet rs = pstm.executeQuery();
////        ProductItemDTO productItemDTO = null;
////
////        if (rs.next()) {
////            productItemDTO = new ProductItemDTO(
////                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
////                    rs.getString("product_name"), rs.getString("supplier_name"),
////                    rs.getInt("supplier_warranty_mo"), rs.getInt("customer_warranty_mo"),
////                    rs.getString("status"), rs.getDate("added_date"), rs.getDate("sold_date")
////            );
////        }
////        pstm.close();
////        rs.close();
////        return productItemDTO;
////    }
//
//    public boolean correctItemMistake(String oldSerial, String newSerial, int newStockId, Integer newSupplierId, int newSupWar) throws SQLException {
//        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
//
//        Connection con = null;
//        try {
//            con = DBConnection.getInstance().getConnection();
//            con.setAutoCommit(false);
//
//
//            ProductItemDTO oldItem = productItemDAO.getItemBySerial(oldSerial);
//            if (oldItem == null) return false;
//            int oldStockId = oldItem.getStockId();
//
//                ProductItemDTO newItem = new ProductItemDTO();
//                newItem.setSerialNumber(newSerial);
//                newItem.setStockId(newStockId);
//                if (newSupplierId == null) newItem.setSupplierId(0);
//                else newItem.setSupplierId(newSupplierId);
//                newItem.setSupplierWarranty(newSupWar);
//
//                boolean isUpdated = productItemDAO.updateItem(newItem);
//                if (!isUpdated) {
//                    con.rollback();
//                    throw new SQLException("Failed to update item with serial " + oldSerial);
//                }
//
//            // Adjust stock counts if the product type changed
//            if (oldStockId > 0 && oldStockId != newStockId) {
//                ProductDAOImpl productDAO = new ProductDAOImpl();
//
//                boolean isOldStockUpdated = productDAO.updateQty(oldStockId, 1);
//                if (!isOldStockUpdated) {
//                    con.rollback();
//                    throw new SQLException("Failed to update old product quantity for Stock ID " + oldStockId);
//                }
//
//                boolean isNewStockUpdated = productDAO.updateQty(newStockId, -1);
//                if (!isNewStockUpdated) {
//                    con.rollback();
//                    throw new SQLException("Failed to update new product quantity for Stock ID " + newStockId);
//                }
//            }
//
//            con.commit();
//            return true;
//        } catch (SQLException e) {
//            if (con != null) con.rollback();
//            throw e;
//        } finally {
//            if (con != null) con.setAutoCommit(true);
//        }
//    }
//
//    public boolean updateItemStatus(String serial, String newStatus) throws SQLException {
//        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
//        ProductDAOImpl productDAO = new ProductDAOImpl();
//
//        ProductItemDTO item = productItemDAO.getItemBySerial(serial);
//        if (item == null) {
//            throw new SQLException("Item with serial number " + serial + " does not exist.");
//        }
//        String currentStatus = item.getStatus();
//        if (currentStatus.equals(newStatus)) {
//            return false;
//        }
//
//        Connection con = null;
//        try {
//            con = DBConnection.getInstance().getConnection();
//            con.setAutoCommit(false);
//
//            boolean isStatusChange = productItemDAO.updateStatus(serial, newStatus);
//            if (!isStatusChange) {
//                con.rollback();
//                throw new SQLException("Failed to update status for item with serial number " + serial);
//            }
//
//            if (newStatus.equals("AVAILABLE") && !currentStatus.equals("AVAILABLE")) {
//                boolean isUpdated = productDAO.updateQty(item.getStockId(), 1);
//                if (!isUpdated) {
//                    con.rollback();
//                    throw new SQLException("Failed to update product quantity for Stock ID " + item.getStockId());
//                }
//
//            }else {
//                boolean isUpdated = productDAO.updateQty(item.getStockId(), -1);
//                if (!isUpdated) {
//                    con.rollback();
//                    throw new SQLException("Failed to update product quantity for Stock ID " + item.getStockId());
//                }
//            }
//
//            con.commit();
//            return true;
//
//        } catch (SQLException e) {
//            if (con != null) con.rollback();
//            throw e;
//        } finally {
//            if (con != null) con.setAutoCommit(true);
//        }
//    }
//
//    public static class ProductMeta {
//        public final int stockId;
//        public final int defaultWarranty;
//
//        public ProductMeta(int id, int war) {
//            this.stockId = id;
//            this.defaultWarranty = war;
//        }
//
//        public int getStockId() {
//            return stockId;
//        }
//
//        public int getDefaultWarranty() {
//            return defaultWarranty;
//        }
//    }
//}