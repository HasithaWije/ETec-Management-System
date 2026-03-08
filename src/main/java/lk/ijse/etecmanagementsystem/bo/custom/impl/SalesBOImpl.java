package lk.ijse.etecmanagementsystem.bo.custom.impl;

import javafx.scene.control.Alert;
import lk.ijse.etecmanagementsystem.bo.BOFactory;
import lk.ijse.etecmanagementsystem.bo.custom.InventoryBO;
import lk.ijse.etecmanagementsystem.bo.custom.SalesBO;
import lk.ijse.etecmanagementsystem.dao.custom.impl.*;
import lk.ijse.etecmanagementsystem.dto.CustomDTO;
import lk.ijse.etecmanagementsystem.entity.ProductItem;
import lk.ijse.etecmanagementsystem.entity.SalesItem;
import lk.ijse.etecmanagementsystem.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesBOImpl implements SalesBO {

    ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
    ProductDAOImpl productDAO = new ProductDAOImpl();
    SalesItemDAOImpl salesItemDAO = new SalesItemDAOImpl();
    TransactionRecordDAOImpl transactionRecordDAO = new TransactionRecordDAOImpl();
    QueryDAOImpl queryDAO = new QueryDAOImpl();

    public boolean placeOrder(SalesDTO salesDTO, List<ItemCartTM> cartItems) throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false); // 1. Start Transaction

            // --- PHASE 1: PREPARE INVENTORY (Split Multi-Qty Items) ---
            List<ItemCartTM> finalItemsToSave = new ArrayList<>();

            for (ItemCartTM item : cartItems) {
                if (item.getQuantity() > 1) {

                    // A. Get Stock ID
                    int stockId = getProductItem(item.getItemId()).getStockId();
                    if (stockId <= 0) {
                        throw new SQLException("Product ID not found in database: " + stockId);
                    }

//                    ArrayList<ProductItemDTO> productItems = productItemDAO.getPlaceHolderItems(stockId);
                    ArrayList<ProductItem> entity = productItemDAO.getPlaceHolderItems(stockId);
                    ArrayList<ProductItemDTO> productItems = new ArrayList<>();
                    for (ProductItem productItem : entity) {
                        productItems.add(new ProductItemDTO(
                                productItem.getItem_id(),
                                productItem.getStock_id(),
                                0,
                                productItem.getSerial_number(),
                                null,
                                null,
                                0,
                                0,
                                productItem.getStatus(),
                                null,
                                null
                        ));
                    }

                    if (productItems.isEmpty()) {
                        System.out.println("No placeholder items found for stock ID: " + stockId);
                        throw new SQLException("No placeholder items found for stock ID: " + stockId);
                    }

                    int placeHoldersCount = productItems.size();
                    if (placeHoldersCount < item.getQuantity()) {
                        System.out.println("not enough placeholder count to add item within qty");
                        throw new SQLException("Not enough placeholder count to add item within qty");
                    }

                    for (int i = 0; i < item.getQuantity(); i++) {
                        ProductItemDTO pi = productItems.get(i);
                        finalItemsToSave.add(new ItemCartTM(
                                pi.getItemId(), item.getItemName(), null, item.getWarrantyMonths(),
                                1, item.getCondition(), item.getUnitPrice(),
                                item.getDiscount(), item.getTotal()
                        ));
                    }

                } else {
                    finalItemsToSave.add(item);
                }
            }

            // --- PHASE 2: INSERT SALE HEADER ---
            String sqlSales = "INSERT INTO Sales (customer_id, user_id, sale_date, sub_total, discount, grand_total, paid_amount, payment_status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            int saleId = 0;
            SalesDAOImpl salesDAO = new SalesDAOImpl();
            boolean isSaved = salesDAO.saveSale(salesDTO);
            if (!isSaved) {
                con.rollback();
                return false;
            }
            saleId = salesDAO.getLastInsertedSalesId();
            System.out.println("Inserted Sale ID: " + saleId);
            if (saleId <= 0) {
                con.rollback();
                return false;
            } else {
                salesDTO.setSaleId(saleId);
            }

            // --- PHASE 3: BATCH PROCESSING (Sales Items & Status Updates) ---
            for (ItemCartTM item : finalItemsToSave) {

                SalesItem salesItem = new SalesItem(
                        0,
                        saleId,
                        item.getItemId(),
                        item.getWarrantyMonths(),
                        item.getUnitPrice(),
                        item.getDiscount()
                );
                boolean isSalesItemSaved = salesItemDAO.createSalesItem(salesItem);
                if (!isSalesItemSaved) {
                    con.rollback();
                    new Alert(Alert.AlertType.ERROR, "Transaction Failed: Unable to save sales item details.").show();
                    return false;
                }

                // Batch 2: Mark as Sold
                ProductItemDTO itemToUpdate = new ProductItemDTO();

                itemToUpdate.setItemId(item.getItemId());
                itemToUpdate.setCustomerWarranty(item.getWarrantyMonths());
                itemToUpdate.setSerialNumber(item.getSerialNo());

                boolean isUpdated = productItemDAO.updateItemForSale(itemToUpdate);
                if (!isUpdated) {
                    con.rollback();
                    new Alert(Alert.AlertType.ERROR, "Transaction Failed: One or more items are already SOLD or Unavailable.").show();
                    return false;
                }


                // Batch 3: Reduce Global Qty
                int stockId = getProductItem(item.getItemId()).getStockId();
                if (stockId <= 0) {
                    con.rollback();
                    new Alert(Alert.AlertType.ERROR, "Product ID not found in database").show();
                    return false;
                }
                boolean isQtyUpdated = productDAO.updateQty(stockId, -1);
                if (!isQtyUpdated) {
                    con.rollback();
                    new Alert(Alert.AlertType.ERROR, "Transaction Failed: Unable to update product quantity.").show();
                    return false;
                }
            }

            TransactionRecord transactionRecord = new TransactionRecord(
                    saleId,
                    salesDTO.getCustomerId(),
                    salesDTO.getUserId(),
                    "SALE_PAYMENT",
                    "CASH",
                    salesDTO.getPaidAmount(),
                    "IN",
                    "Sale #" + saleId
            );

            boolean isTransSaved = transactionRecordDAO.insertTransactionRecord(transactionRecord);


            if (!isTransSaved) {
                con.rollback();
                new Alert(Alert.AlertType.ERROR, "Transaction Failed: Unable to save transaction record.").show();
                return false;
            }

            // 5. Commit Transaction
            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) con.rollback();

            e.printStackTrace();
            throw e;
        } finally {
            if (con != null) con.setAutoCommit(true);
        }
    }

    @Override
    public ProductItemDTO getProductItem(int itemId) throws SQLException {
        CustomDTO customDTO = queryDAO.getProductItem(itemId);
        if (customDTO == null) {
            return null;
        }
        return new ProductItemDTO(
                customDTO.getProductItemId(),
                customDTO.getProductItemStockId(),
                customDTO.getProductItemSupplierId(),
                customDTO.getProductItemSerialNumber(),
                customDTO.getProductItemProductName(),
                customDTO.getProductItemSupplierName(),
                customDTO.getProductItemSupplierWarranty(),
                customDTO.getProductItemCustomerWarranty(),
                customDTO.getProductItemStatus(),
                customDTO.getProductItemAddedDate(),
                customDTO.getProductItemSoldDate()
        );
    }
}
