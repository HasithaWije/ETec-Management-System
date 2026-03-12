package lk.ijse.etecmanagementsystem.bo.custom.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import lk.ijse.etecmanagementsystem.bo.BOFactory;
import lk.ijse.etecmanagementsystem.bo.custom.InventoryBO;
import lk.ijse.etecmanagementsystem.bo.custom.SalesBO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.dao.DAOFactory;
import lk.ijse.etecmanagementsystem.dao.custom.*;
import lk.ijse.etecmanagementsystem.dao.custom.impl.*;
import lk.ijse.etecmanagementsystem.dto.CustomDTO;
import lk.ijse.etecmanagementsystem.dto.ItemCartDTO;
import lk.ijse.etecmanagementsystem.entity.ProductItem;
import lk.ijse.etecmanagementsystem.entity.Sales;
import lk.ijse.etecmanagementsystem.entity.SalesItem;
import lk.ijse.etecmanagementsystem.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesBOImpl implements SalesBO {

    SalesItemDAO salesItemDAO = (SalesItemDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.SALES_ITEM);
    ProductItemDAO productItemDAO = (ProductItemDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.PRODUCT_ITEM);
    SalesDAO salesDAO = (SalesDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.SALES);
    QueryDAO queryDAO = (QueryDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
    TransactionRecordDAO transactionRecordDAO = (TransactionRecordDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.TRANSACTION_RECORD);
    ProductDAO productDAO = (ProductDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.PRODUCT);

    @Override
    public boolean placeOrder(SalesDTO salesDTO, List<ItemCartDTO> cartItems) throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false); // 1. Start Transaction

            // --- PHASE 1: PREPARE INVENTORY (Split Multi-Qty Items) ---
            List<ItemCartDTO> finalItemsToSave = new ArrayList<>();

            for (ItemCartDTO item : cartItems) {
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
                        finalItemsToSave.add(new ItemCartDTO(
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
            boolean isSaved = salesDAO.save(new Sales(
                    saleId,
                    salesDTO.getCustomerId(),
                    salesDTO.getUserId(),
                    new java.sql.Timestamp(System.currentTimeMillis()),
                    salesDTO.getSubtotal(),
                    salesDTO.getDiscount(),
                    salesDTO.getGrandTotal(),
                    salesDTO.getPaidAmount(),
                    salesDTO.getPaymentStatus().getLabel(),
                    salesDTO.getDescription()
            ));
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
            for (ItemCartDTO item : finalItemsToSave) {

                SalesItem salesItem = new SalesItem(
                        0,
                        saleId,
                        item.getItemId(),
                        item.getWarrantyMonths(),
                        item.getUnitPrice(),
                        item.getDiscount()
                );
                boolean isSalesItemSaved = salesItemDAO.save(salesItem);
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

            boolean isTransSaved = transactionRecordDAO.save(transactionRecord);


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

    @Override
    public List<SalesDTO> getAllSale() throws SQLException {
        List<SalesDTO> salesList = new ArrayList<>();

        List<Sales> sales = salesDAO.getAll();
        for(Sales sale : sales) {
            salesList.add(new SalesDTO(
                    sale.getSale_id(),
                    sale.getCustomer_id(),
                    sale.getUser_id(),
                    sale.getSale_date(),
                    sale.getSub_total(),
                    sale.getDiscount(),
                    sale.getGrand_total(),
                    sale.getPaid_amount(),
                    sale.getPayment_status(),
                    sale.getDescription()
            ));
        }
        return salesList;
    }

    @Override
    public List<CustomDTO> getSalesByDateRange(LocalDate from, LocalDate to) throws SQLException {

        return queryDAO.getSalesByDateRange(from, to);
    }

    @Override
    public List<CustomDTO> getPendingSales() throws SQLException {
        return queryDAO.getPendingSales();
    }
}
