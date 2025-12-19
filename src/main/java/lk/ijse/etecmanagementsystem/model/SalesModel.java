package lk.ijse.etecmanagementsystem.model;

import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.InventoryItemDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;
import lk.ijse.etecmanagementsystem.util.CrudUtil;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SalesModel {

    /**
     * Loads all items with status 'AVAILABLE' from the database.
     * Joins ProductItem and Product tables to get Name and Price.
     */
    public List<InventoryItemDTO> getAllAvailableItems() throws SQLException {
        List<InventoryItemDTO> itemList = new ArrayList<>();

        // We join ProductItem with Product to get the Name (from Product) and Price (sell_price)
        String sql = "SELECT pi.item_id, p.name AS product_name, pi.serial_number, " +
                "p.warranty_months, p.sell_price, p.p_condition " +
                "FROM ProductItem pi " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.status = 'AVAILABLE'";

        ResultSet rs = CrudUtil.execute(sql);

        while (rs.next()) {
            InventoryItemDTO item = new InventoryItemDTO(
                    rs.getInt("item_id"),
                    rs.getString("product_name"),
                    rs.getString("serial_number"),
                    rs.getInt("warranty_months"), // Default warranty from Product definition
                    rs.getDouble("sell_price"),
                    ProductCondition.fromString(rs.getString("p_condition"))
            );
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * THE MAIN TRANSACTION method.
     * 1. Saves Sale Header
     * 2. Saves Sales Items
     * 3. Updates Inventory Status to SOLD
     * 4. Records the Transaction (Payment)
     */
    public boolean placeOrder(SalesDTO salesDTO, List<ItemCartTM> cartItems) throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();

            // 1. Start Transaction
            con.setAutoCommit(false);

            // ---------------------------------------------------------------------------------
            // STEP 2: Insert into Sales Table
            // ---------------------------------------------------------------------------------
            String sqlSales = "INSERT INTO Sales " +
                    "(customer_id, user_id, sale_date, sub_total, discount, grand_total, " +
                    "payment_status, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmSales = con.prepareStatement(sqlSales, Statement.RETURN_GENERATED_KEYS);

            // Handle nullable Customer ID
            if (salesDTO.getCustomerId() == 0) {
                pstmSales.setNull(1, Types.INTEGER);
            } else {
                pstmSales.setInt(1, salesDTO.getCustomerId());
            }

            pstmSales.setInt(2, salesDTO.getUserId());
            pstmSales.setTimestamp(3, new Timestamp(new java.util.Date().getTime())); // Current Time
            pstmSales.setDouble(4, salesDTO.getSubtotal());
            pstmSales.setDouble(5, salesDTO.getDiscount());
            pstmSales.setDouble(6, salesDTO.getGrandTotal());

            pstmSales.setString(7, salesDTO.getPaymentStatus().toString()); // Enum to String
            pstmSales.setString(8, salesDTO.getDescription());

            int affectedRows = pstmSales.executeUpdate();
            if (affectedRows == 0) {
                con.rollback();
                return false;
            }

            // Get the generated Sale ID
            int saleId = 0;
            ResultSet generatedKeys = pstmSales.getGeneratedKeys();
            if (generatedKeys.next()) {
                saleId = generatedKeys.getInt(1);
                salesDTO.setSaleId(saleId); // Update DTO just in case
            } else {
                con.rollback();
                return false;
            }

            // ---------------------------------------------------------------------------------
            // STEP 3: Process Items (Insert SalesItem + Update ProductItem)
            // ---------------------------------------------------------------------------------
            String sqlSalesItem = "INSERT INTO SalesItem (sale_id, item_id, qty, customer_warranty_months, "+
                    "unit_price, discount, total) VALUES (?, ?, ?, ?, ?, ?, ?)";

            String sqlUpdateProductItem = "UPDATE ProductItem SET status = 'SOLD', sold_date = ?, " +
                    "customer_warranty_mo = ? WHERE item_id = ?";

            PreparedStatement pstmSalesItem = con.prepareStatement(sqlSalesItem);
            PreparedStatement pstmUpdateItem = con.prepareStatement(sqlUpdateProductItem);

            for (ItemCartTM item : cartItems) {
                // A. Add to SalesItem Table
                pstmSalesItem.setInt(1, saleId);
                pstmSalesItem.setInt(2, item.getItemId());
                pstmSalesItem.setInt(3, item.getQuantity());
                pstmSalesItem.setInt(4, salesDTO.getCustomerWarrantyMonths()); // Using SalesDTO warranty for all items
                pstmSalesItem.setDouble(5, item.getUnitPrice());
                pstmSalesItem.setDouble(6, item.getDiscount());
                pstmSalesItem.setDouble(7, item.getTotal());
                pstmSalesItem.addBatch();

                // B. Update ProductItem Table (Mark as SOLD)
                pstmUpdateItem.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
                // Use warranty from SaleDTO or specific item logic? using SalesDTO logic for now as per schema implies flexibility
                pstmUpdateItem.setInt(2, salesDTO.getCustomerWarrantyMonths());
                pstmUpdateItem.setInt(3, item.getItemId());
                pstmUpdateItem.addBatch();
            }

            pstmSalesItem.executeBatch();
            pstmUpdateItem.executeBatch();

            // ---------------------------------------------------------------------------------
            // STEP 4: Record Transaction (Money In)
            // ---------------------------------------------------------------------------------
            // Assuming full payment for now based on controller flow.
            // If Partial, logic needs to adjust 'amount' here.

            String sqlTransaction = "INSERT INTO TransactionRecord " +
                    "(transaction_type, payment_method, amount, flow, sale_id, user_id, customer_id, reference_note) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmTrans = con.prepareStatement(sqlTransaction);
            pstmTrans.setString(1, "SALE_PAYMENT");
            pstmTrans.setString(2, "CASH"); // You can pass this from Controller if needed
            pstmTrans.setDouble(3, salesDTO.getGrandTotal());
            pstmTrans.setString(4, "IN");
            pstmTrans.setInt(5, saleId);
            pstmTrans.setInt(6, salesDTO.getUserId());

            if (salesDTO.getCustomerId() == 0) {
                pstmTrans.setNull(7, Types.INTEGER);
            } else {
                pstmTrans.setInt(7, salesDTO.getCustomerId());
            }

            pstmTrans.setString(8, "Sale #" + saleId);

            pstmTrans.executeUpdate();

            // ---------------------------------------------------------------------------------
            // COMMIT
            // ---------------------------------------------------------------------------------
            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Rollback failed: " + ex.getMessage());
                }
            }
            e.printStackTrace();
            System.out.println("Transaction failed: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Failed to reset auto-commit: " + e.getMessage());
                }
            }
        }
    }
}