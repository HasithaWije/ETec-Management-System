package lk.ijse.etecmanagementsystem.model;

import javafx.scene.control.Alert;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.InventoryItemDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;
import lk.ijse.etecmanagementsystem.dto.tm.SalesTM;
import lk.ijse.etecmanagementsystem.util.CrudUtil;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SalesModel {


    private SalesTM getSalesTMFromResultSet(ResultSet resultSet) throws SQLException {
        return new SalesTM(
                resultSet.getInt("sale_id"),
                resultSet.getString("customer_name") != null ? resultSet.getString("customer_name") : "Walk-in", // Handle null customers
                resultSet.getString("user_name"),
                resultSet.getString("description"),
                resultSet.getDouble("sub_total"),
                resultSet.getDouble("discount"),
                resultSet.getDouble("grand_total"),
                resultSet.getDouble("paid_amount")
        );
    }

    public List<SalesTM> getAllSales() throws SQLException {
        List<SalesTM> salesList = new ArrayList<>();

        String sql = "SELECT s.sale_id, c.name AS customer_name, u.user_name, s.description, " +
                "s.sub_total, s.discount, s.grand_total, s.paid_amount " +
                "FROM Sales s " +
                "LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "JOIN User u ON s.user_id = u.user_id " +
                "ORDER BY s.sale_date DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            ResultSet resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                salesList.add(getSalesTMFromResultSet(resultSet));
            }
        }
        return salesList;
    }

    public List<SalesTM> getSalesByDateRange(LocalDate from, LocalDate to) throws SQLException {
        List<SalesTM> salesList = new ArrayList<>();

        String sql = "SELECT s.sale_id, c.name AS customer_name, u.user_name, s.description, " +
                "s.sub_total, s.discount, s.grand_total, s.paid_amount " +
                "FROM Sales s " +
                "LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "JOIN User u ON s.user_id = u.user_id " +
                "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                "ORDER BY s.sale_date DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            pstm.setDate(1, java.sql.Date.valueOf(from));
            pstm.setDate(2, java.sql.Date.valueOf(to));

            ResultSet resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                salesList.add(getSalesTMFromResultSet(resultSet));
            }
        }
        return salesList;
    }

    public List<InventoryItemDTO> getAllAvailableItems() throws SQLException {
        List<InventoryItemDTO> itemList = new ArrayList<>();

        String sql = "SELECT pi.item_id, p.name AS product_name, pi.serial_number, " +
                "p.warranty_months, p.sell_price, p.p_condition " +
                "FROM ProductItem pi " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.status = 'AVAILABLE' AND pi.serial_number NOT LIKE 'PENDING-%' ";

        ResultSet rs = CrudUtil.execute(sql);

        while (rs.next()) {
            InventoryItemDTO item = new InventoryItemDTO(
                    rs.getInt("item_id"),
                    rs.getString("product_name"),
                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
                    rs.getInt("warranty_months"), // Default warranty from Product definition
                    rs.getDouble("sell_price"),
                    ProductCondition.fromString(rs.getString("p_condition"))
            );
            itemList.add(item);
        }
        return itemList;
    }

    public int getPendingItemCount(int stockId) throws SQLException {
        String sql = "SELECT COUNT(*) AS pending_count FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%'";

        try (ResultSet rs = CrudUtil.execute(sql, stockId)) {
            if (rs.next()) {
                return rs.getInt("pending_count");
            }
        }
        return 0;
    }

    public List<ItemCartTM> replacePendingSerialNumberAndGetIds(ItemCartTM cartItem, int qty) throws SQLException {
        String sqlSelectPending = "SELECT item_id FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' LIMIT ?";
        String sqlUpdateSerial = "UPDATE ProductItem SET serial_number = ? WHERE item_id = ?";

        List<ItemCartTM> updatedItems = new ArrayList<>();


        String sqlGetStockId = "SELECT stock_id FROM ProductItem WHERE item_id = ?";
        int stockId = -1;

        try (ResultSet rsStock = CrudUtil.execute(sqlGetStockId, cartItem.getItemId())) {
            if (rsStock.next()) {
                stockId = rsStock.getInt("stock_id");
            } else {
                throw new SQLException("Item ID not found in database: " + cartItem.getItemId());
            }
        }

        try (ResultSet rs = CrudUtil.execute(sqlSelectPending, stockId, qty)) {

            while (rs.next()) {
                int itemId = rs.getInt("item_id");

                CrudUtil.execute(sqlUpdateSerial, null, itemId);
                ItemCartTM updatedItem = new ItemCartTM(
                        itemId,
                        cartItem.getItemName(),
                        null,
                        cartItem.getWarrantyMonths(),
                        1,// Each item now has qty = 1
                        cartItem.getCondition(),
                        cartItem.getUnitPrice(),
                        cartItem.getDiscount(),
                        cartItem.getTotal()
                );
                updatedItems.add(updatedItem);
            }
        }
        return updatedItems;


    }

//    public boolean placeOrder(SalesDTO salesDTO, List<ItemCartTM> cartItems) throws SQLException {
//        Connection con = null;
//        try {
//            con = DBConnection.getInstance().getConnection();
//
//            // 1. Start Transaction
//            con.setAutoCommit(false);
//
//
//            List<ItemCartTM> finalItemsToSave = new ArrayList<>();
//
//// PREPARE STATEMENTS using the TRANSACTIONAL connection 'con'
//            String sqlSelectPending = "SELECT item_id FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' LIMIT ? FOR UPDATE"; // Added FOR UPDATE to lock rows
//            String sqlUpdateSerial = "UPDATE ProductItem SET serial_number = ? WHERE item_id = ?";
//            String sqlGetStockId = "SELECT stock_id FROM ProductItem WHERE item_id = ?";
//
//            PreparedStatement pstmSelectPending = con.prepareStatement(sqlSelectPending);
//            PreparedStatement pstmUpdateSerial = con.prepareStatement(sqlUpdateSerial);
//            PreparedStatement pstmGetStockId = con.prepareStatement(sqlGetStockId);
//
//            for (ItemCartTM item : cartItems) {
//                if (item.getQuantity() > 1) {
//                    // 1. Get Stock ID using the transaction connection
//                    int stockId = -1;
//                    pstmGetStockId.setInt(1, item.getItemId());
//                    try (ResultSet rsStock = pstmGetStockId.executeQuery()) {
//                        if (rsStock.next()) stockId = rsStock.getInt("stock_id");
//                        else throw new SQLException("Item ID not found: " + item.getItemId());
//                    }
//
//                    // 2. Find Pending Items using the transaction connection
//                    pstmSelectPending.setInt(1, stockId);
//                    pstmSelectPending.setInt(2, item.getQuantity());
//
//                    try (ResultSet rs = pstmSelectPending.executeQuery()) {
//                        while (rs.next()) {
//                            int itemId = rs.getInt("item_id");
//
//                            // 3. Update Serial using the transaction connection
//                            pstmUpdateSerial.setString(1, null); // Or generate a temp serial if needed
//                            pstmUpdateSerial.setInt(2, itemId);
//                            pstmUpdateSerial.executeUpdate();
//
//                            // Add to final list
//                            finalItemsToSave.add(new ItemCartTM(
//                                    itemId, item.getItemName(), null, item.getWarrantyMonths(),
//                                    1, item.getCondition(), item.getUnitPrice(),
//                                    item.getDiscount(), item.getTotal()
//                            ));
//                        }
//                    }
//                } else {
//                    finalItemsToSave.add(item);
//                }
//            }
//
//
//            cartItems = finalItemsToSave;
//
//            String sqlSales = "INSERT INTO Sales " +
//                    "(customer_id, user_id, sale_date, sub_total, discount, grand_total, paid_amount, " +
//                    "payment_status, description) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//            PreparedStatement pstmSales = con.prepareStatement(sqlSales, Statement.RETURN_GENERATED_KEYS);
//
//            if (salesDTO.getCustomerId() == 0) {
//                pstmSales.setNull(1, Types.INTEGER);
//            } else {
//                pstmSales.setInt(1, salesDTO.getCustomerId());
//            }
//
//            pstmSales.setInt(2, salesDTO.getUserId());
//            pstmSales.setTimestamp(3, new Timestamp(new java.util.Date().getTime())); // Current Time
//            pstmSales.setDouble(4, salesDTO.getSubtotal());
//            pstmSales.setDouble(5, salesDTO.getDiscount());
//            pstmSales.setDouble(6, salesDTO.getGrandTotal());
//            pstmSales.setDouble(7, salesDTO.getPaidAmount());
//
//
//            pstmSales.setString(8, salesDTO.getPaymentStatus().toString()); // Enum to String
//            pstmSales.setString(9, salesDTO.getDescription());
//
//            int affectedRows = pstmSales.executeUpdate();
//            if (affectedRows == 0) {
//                con.rollback();
//                return false;
//            }
//
//            int saleId = 0;
//            ResultSet generatedKeys = pstmSales.getGeneratedKeys();
//            if (generatedKeys.next()) {
//                saleId = generatedKeys.getInt(1);
//                salesDTO.setSaleId(saleId); // Update DTO just in case
//            } else {
//                con.rollback();
//                return false;
//            }
//
//
//            // --- FIXED ITEM PROCESSING ---
//            String sqlSalesItem = "INSERT INTO SalesItem (sale_id, item_id, customer_warranty_months, unit_price, discount) VALUES (?, ?, ?, ?, ?)";
//
//            // CRITICAL CHANGE: Added "AND status = 'AVAILABLE'"
//            String sqlUpdateProductItem = "UPDATE ProductItem SET status = 'SOLD', sold_date = NOW(), " +
//                    "customer_warranty_mo = ?, serial_number = ? " +
//                    "WHERE item_id = ? AND status = 'AVAILABLE'";
//
//            String sqlUpdateProductQuantity = "UPDATE Product p " +
//                    "JOIN ProductItem pi ON p.stock_id = pi.stock_id " +
//                    "SET p.qty = p.qty - 1 " +
//                    "WHERE pi.item_id = ?";
//
//            PreparedStatement pstmSalesItem = con.prepareStatement(sqlSalesItem);
//            PreparedStatement pstmUpdateItem = con.prepareStatement(sqlUpdateProductItem);
//            PreparedStatement pstmUpdateProductQty = con.prepareStatement(sqlUpdateProductQuantity);
//
//            for (ItemCartTM item : cartItems) {
//                // Batch 1: Insert Sales Item
//                pstmSalesItem.setInt(1, saleId); // saleId from your earlier insert
//                pstmSalesItem.setInt(2, item.getItemId());
//                pstmSalesItem.setInt(3, item.getWarrantyMonths());
//                pstmSalesItem.setDouble(4, item.getUnitPrice());
//                pstmSalesItem.setDouble(5, item.getDiscount());
//                pstmSalesItem.addBatch();
//
//                // Batch 2: Update Item Status (Only if AVAILABLE)
//                pstmUpdateItem.setInt(1, item.getWarrantyMonths());
//                pstmUpdateItem.setString(2, item.getSerialNo());
//                pstmUpdateItem.setInt(3, item.getItemId());
//                pstmUpdateItem.addBatch();
//
//                // Batch 3: Update Qty
//                pstmUpdateProductQty.setInt(1, item.getItemId());
//                pstmUpdateProductQty.addBatch();
//            }
//
//            pstmSalesItem.executeBatch();
//
//            // CRITICAL CHECK: Did we actually update the items?
//            int[] updateCounts = pstmUpdateItem.executeBatch();
//            for (int count : updateCounts) {
//                if (count == 0) {
//                    // Count 0 means the row wasn't found or wasn't AVAILABLE.
//                    con.rollback();
//                    new Alert(Alert.AlertType.ERROR, "Transaction Failed: One or more items are already SOLD or Unavailable.").show();
//                    return false;
//                }
//            }
//
//            pstmUpdateProductQty.executeBatch();
//
//
//            String sqlTransaction = "INSERT INTO TransactionRecord " +
//                    "(transaction_type, payment_method, amount, flow, sale_id, user_id, customer_id, reference_note) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
//
//            PreparedStatement pstmTrans = con.prepareStatement(sqlTransaction);
//            pstmTrans.setString(1, "SALE_PAYMENT");
//            pstmTrans.setString(2, "CASH"); // You can pass this from Controller if needed
//            pstmTrans.setDouble(3, salesDTO.getPaidAmount());
//            pstmTrans.setString(4, "IN");
//            pstmTrans.setInt(5, saleId);
//            pstmTrans.setInt(6, salesDTO.getUserId());
//
//            if (salesDTO.getCustomerId() == 0) {
//                pstmTrans.setNull(7, Types.INTEGER);
//            } else {
//                pstmTrans.setInt(7, salesDTO.getCustomerId());
//            }
//
//            pstmTrans.setString(8, "Sale #" + saleId);
//
//            pstmTrans.executeUpdate();
//
//            con.commit();
//            return true;
//
//        } catch (SQLException e) {
//            if (con != null) {
//                try {
//                    con.rollback();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                    System.out.println("Rollback failed: " + ex.getMessage());
//                }
//            }
//            System.out.println("Transaction failed: " + e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            if (con != null) {
//                try {
//                    con.rollback();
//                } catch (SQLException ex) {
//                    System.out.println("Rollback failed: " + ex.getMessage());
//
//                }
//            }
//            System.out.println("Unexpected error: " + e.getMessage());
//            throw e;
//        } finally {
//            if (con != null) {
//                try {
//                    con.setAutoCommit(true);
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                    System.out.println("Failed to reset auto-commit: " + e.getMessage());
//                }
//            }
//        }
//    }

// --- OPTIMIZED TRANSACTION METHOD ---
public boolean placeOrder(SalesDTO salesDTO, List<ItemCartTM> cartItems) throws SQLException {
    Connection con = null;
    try {
        con = DBConnection.getInstance().getConnection();
        con.setAutoCommit(false); // 1. Start Transaction

        // --- PHASE 1: PREPARE INVENTORY (Split Multi-Qty Items) ---
        List<ItemCartTM> finalItemsToSave = new ArrayList<>();

        // PreparedStatements for inventory logic (Using 'con' ensures transaction safety)
        String sqlGetStock = "SELECT stock_id FROM ProductItem WHERE item_id = ?";
        // Added FOR UPDATE to lock these rows and prevent race conditions
        String sqlGetPending = "SELECT item_id FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' LIMIT ? FOR UPDATE";
        String sqlUpdateSerial = "UPDATE ProductItem SET serial_number = NULL WHERE item_id = ?";

        try (PreparedStatement psGetStock = con.prepareStatement(sqlGetStock);
             PreparedStatement psGetPending = con.prepareStatement(sqlGetPending);
             PreparedStatement psUpdateSerial = con.prepareStatement(sqlUpdateSerial)) {

            for (ItemCartTM item : cartItems) {
                if (item.getQuantity() > 1) {
                    // A. Get Stock ID
                    int stockId = -1;
                    psGetStock.setInt(1, item.getItemId());
                    try (ResultSet rs = psGetStock.executeQuery()) {
                        if (rs.next()) stockId = rs.getInt("stock_id");
                        else throw new SQLException("Item ID not found: " + item.getItemId());
                    }

                    // B. Find Pending Items to split
                    psGetPending.setInt(1, stockId);
                    psGetPending.setInt(2, item.getQuantity());

                    try (ResultSet rs = psGetPending.executeQuery()) {
                        while (rs.next()) {
                            int pendingItemId = rs.getInt("item_id");

                            // C. Clear Serial (making it ready for sale as a unique unit)
                            psUpdateSerial.setInt(1, pendingItemId);
                            psUpdateSerial.executeUpdate();

                            // D. Add as individual item to final list
                            finalItemsToSave.add(new ItemCartTM(
                                    pendingItemId, item.getItemName(), null, item.getWarrantyMonths(),
                                    1, item.getCondition(), item.getUnitPrice(),
                                    item.getDiscount(), item.getTotal()
                            ));
                        }
                    }
                } else {
                    finalItemsToSave.add(item);
                }
            }
        }

        // --- PHASE 2: INSERT SALE HEADER ---
        String sqlSales = "INSERT INTO Sales (customer_id, user_id, sale_date, sub_total, discount, grand_total, paid_amount, payment_status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int saleId = 0;
        try (PreparedStatement pstmSales = con.prepareStatement(sqlSales, Statement.RETURN_GENERATED_KEYS)) {
            pstmSales.setObject(1, salesDTO.getCustomerId() == 0 ? null : salesDTO.getCustomerId());
            pstmSales.setInt(2, salesDTO.getUserId());
            pstmSales.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmSales.setDouble(4, salesDTO.getSubtotal());
            pstmSales.setDouble(5, salesDTO.getDiscount());
            pstmSales.setDouble(6, salesDTO.getGrandTotal());
            pstmSales.setDouble(7, salesDTO.getPaidAmount());
            pstmSales.setString(8, salesDTO.getPaymentStatus().toString());
            pstmSales.setString(9, salesDTO.getDescription());

            if (pstmSales.executeUpdate() == 0) {
                con.rollback();
                return false;
            }

            try (ResultSet generatedKeys = pstmSales.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    saleId = generatedKeys.getInt(1);
                    salesDTO.setSaleId(saleId);
                } else {
                    con.rollback();
                    return false;
                }
            }
        }

        // --- PHASE 3: BATCH PROCESSING (Sales Items & Status Updates) ---
        String sqlSalesItem = "INSERT INTO SalesItem (sale_id, item_id, customer_warranty_months, unit_price, discount) VALUES (?, ?, ?, ?, ?)";
        // Ensure we only update if currently AVAILABLE
        String sqlUpdItem = "UPDATE ProductItem SET status = 'SOLD', sold_date = NOW(), customer_warranty_mo = ?, serial_number = ? WHERE item_id = ? AND status = 'AVAILABLE'";
        String sqlUpdQty = "UPDATE Product p JOIN ProductItem pi ON p.stock_id = pi.stock_id SET p.qty = p.qty - 1 WHERE pi.item_id = ?";

        try (PreparedStatement psSaleItem = con.prepareStatement(sqlSalesItem);
             PreparedStatement psUpdItem = con.prepareStatement(sqlUpdItem);
             PreparedStatement psUpdQty = con.prepareStatement(sqlUpdQty)) {

            for (ItemCartTM item : finalItemsToSave) {
                // Batch 1: Link Item to Sale
                psSaleItem.setInt(1, saleId);
                psSaleItem.setInt(2, item.getItemId());
                psSaleItem.setInt(3, item.getWarrantyMonths());
                psSaleItem.setDouble(4, item.getUnitPrice());
                psSaleItem.setDouble(5, item.getDiscount());
                psSaleItem.addBatch();

                // Batch 2: Mark as Sold
                psUpdItem.setInt(1, item.getWarrantyMonths());
                psUpdItem.setString(2, item.getSerialNo());
                psUpdItem.setInt(3, item.getItemId());
                psUpdItem.addBatch();

                // Batch 3: Reduce Global Qty
                psUpdQty.setInt(1, item.getItemId());
                psUpdQty.addBatch();
            }

            psSaleItem.executeBatch();

            // Critical: Check if items were actually updated (i.e., were they AVAILABLE?)
            int[] updateCounts = psUpdItem.executeBatch();
            for (int count : updateCounts) {
                if (count == 0) {
                    con.rollback();
                    new Alert(Alert.AlertType.ERROR, "Transaction Failed: One or more items are already SOLD or Unavailable.").show();
                    return false;
                }
            }

            psUpdQty.executeBatch();
        }

        // --- PHASE 4: TRANSACTION RECORD ---
        String sqlTrans = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, sale_id, user_id, customer_id, reference_note) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmTrans = con.prepareStatement(sqlTrans)) {
            pstmTrans.setString(1, "SALE_PAYMENT");
            pstmTrans.setString(2, "CASH");
            pstmTrans.setDouble(3, salesDTO.getPaidAmount());
            pstmTrans.setString(4, "IN");
            pstmTrans.setInt(5, saleId);
            pstmTrans.setInt(6, salesDTO.getUserId());
            pstmTrans.setObject(7, salesDTO.getCustomerId() == 0 ? null : salesDTO.getCustomerId());
            pstmTrans.setString(8, "Sale #" + saleId);
            pstmTrans.executeUpdate();
        }

        // 5. Commit Transaction
        con.commit();
        return true;

    } catch (SQLException e) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        e.printStackTrace();
        throw e;
    } finally {
        if (con != null) {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
}