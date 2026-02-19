package lk.ijse.etecmanagementsystem.bo;

import lk.ijse.etecmanagementsystem.dao.ProductDAOImpl;
import lk.ijse.etecmanagementsystem.dao.ProductItemDAOImpl;
import lk.ijse.etecmanagementsystem.dao.SupplierDAOImpl;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dto.SupplierDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryBOImpl {
    ProductDAOImpl productDAO = new ProductDAOImpl();
    ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();

    public int saveProductAndGetId(ProductDTO p) throws SQLException {
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

            boolean isItemSaved = productItemDAO.addPlaceHolderItem(newStockId, p.getQty());
            if (!isItemSaved) {
                connection.rollback();
                throw new SQLException("Failed to create product items after saving product.");
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

        Connection connection = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false);

            boolean isUpdated = productDAO.update(p);
            if (!isUpdated) {
                connection.rollback();
                return false;
            }

            boolean isItemUpdated = productItemDAO.updateCustomerWarranty(p.getWarrantyMonth(), Integer.parseInt(p.getId()));
            if (!isItemUpdated) {
                connection.rollback();
                return false;
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

    public boolean updateProductWithQtySync(ProductDTO p) throws SQLException {

        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start Transaction

            boolean isUpdated = productDAO.update(p);
            if (!isUpdated) {
                connection.rollback();
                return false;
            }

            boolean isItemUpdated = productItemDAO.updateCustomerWarranty(p.getWarrantyMonth(), Integer.parseInt(p.getId()));
            if (!isItemUpdated) {
                connection.rollback();
                return false;
            }

            int currentTotalItems = productItemDAO.getAvailableItemCount(Integer.parseInt(p.getId()));
            if(currentTotalItems <= 0) {
                connection.rollback();
                throw new SQLException("Data integrity issue: No available items found for this product. Cannot sync quantity.");
            }

            int targetQty = p.getQty();
            int difference = targetQty - currentTotalItems;

            if (difference > 0) {

                boolean isCreate = productItemDAO.addPlaceHolderItem(Integer.parseInt(p.getId()), difference);
                if (!isCreate) {
                    connection.rollback();
                    throw new SQLException("Failed to create placeholder items to sync quantity.");
                }
            } else if (difference < 0) {

                boolean isDeleted = productItemDAO.deletePlaceHolderItems(Integer.parseInt(p.getId()), Math.abs(difference));
                if (!isDeleted) {
                    connection.rollback();
                    System.out.println("DEBUG: Failed to delete placeholder items to sync quantity. Difference: " + difference);
                    return false;
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
            connection.setAutoCommit(false);

            boolean isItemDeleted = new ProductItemDAOImpl().delete(Integer.parseInt(stockId));
            if (!isItemDeleted) {
                connection.rollback();
                System.out.println("DEBUG: Failed to delete product items for Stock ID " + stockId);
                return false;
            }

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

        int realAvailableCount = productItemDAO.getRealItemCount(Integer.parseInt(stockId));
        System.out.println("DEBUG: Real Available Item Count for Stock ID " + stockId + " is " + realAvailableCount);
        int restrictedCount = productItemDAO.getRestrictedRealItemCount(Integer.parseInt(stockId));
        System.out.println("DEBUG: Restricted Item Count for Stock ID " + stockId + " is " + restrictedCount);

        return new ItemDeleteStatus(realAvailableCount, restrictedCount);
    }

    public boolean addNewSerialNo(ArrayList<ProductItemDTO> itemDTOS) throws SQLException {
        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            ArrayList<ProductItemDTO> placeholders = productItemDAO.getPlaceHolderItems(itemDTOS.get(0).getStockId());

            int i = 0;
            for(ProductItemDTO dto : itemDTOS){
                int placeholderId = -1;

                if (!placeholders.isEmpty() && i < placeholders.size()) {
                    placeholderId = placeholders.get(i++).getItemId();
                    System.out.println("DEBUG: Found placeholder with ID " + placeholderId + " for Stock ID " + dto.getStockId());
                }
                if (placeholderId != -1) {

                    boolean isUpdated = productItemDAO.updateItem(dto);
                    if (!isUpdated) {
                        conn.rollback();
                        throw new SQLException("Failed to update placeholder item with ID " + placeholderId);
                    }
                } else {

                    boolean isAdded = productItemDAO.addProductItem(dto);
                    if (!isAdded) {
                        conn.rollback();
                        throw new SQLException("Failed to add new item for Stock ID " + dto.getStockId());
                    }

                    ProductDAOImpl productDAO = new ProductDAOImpl();
                    boolean isQtyUpdated = productDAO.updateQty(dto.getStockId(), 1);
                    if (!isQtyUpdated) {
                        conn.rollback();
                        throw new SQLException("Failed to update product quantity for Stock ID " + dto.getStockId());
                    }
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

    public Map<Integer, String> getAllProductMap() throws SQLException {
        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();

        Map<Integer, String> map = new HashMap<>();
        List<ProductItemDTO> items = productItemDAO.getAllProductItems();
        for (ProductItemDTO item : items) {
            map.put(item.getStockId(), item.getProductName());
        }
        return map;
    }

    public Map<Integer, String> getAllSuppliersMap() throws SQLException {
        SupplierDAOImpl supplierDAO = new SupplierDAOImpl();

        Map<Integer, String> map = new HashMap<>();
        List<SupplierDTO> suppliers = supplierDAO.getAllSuppliers();
        for (SupplierDTO sup : suppliers) {
            map.put(sup.getSupplierId(), sup.getSupplierName());
        }
        return map;

    }

    public ProductItemDTO getProductMetaById(int stockId) throws SQLException {
        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();

        List<ProductItemDTO> items = productItemDAO.getAllProductItems();
        for (ProductItemDTO item : items) {
            if (item.getStockId() == stockId) {
                return new ProductItemDTO(stockId, item.getSupplierWarranty());
            }
        }
        return null;
    }

    public boolean correctItemMistake(String oldSerial, String newSerial, int newStockId, Integer newSupplierId, int newSupWar) throws SQLException {
        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);


            ProductItemDTO oldItem = productItemDAO.getItemBySerial(oldSerial);
            if (oldItem == null) return false;
            int oldStockId = oldItem.getStockId();

            ProductItemDTO newItem = new ProductItemDTO();
            newItem.setSerialNumber(newSerial);
            newItem.setStockId(newStockId);
            if (newSupplierId == null) newItem.setSupplierId(0);
            else newItem.setSupplierId(newSupplierId);
            newItem.setSupplierWarranty(newSupWar);

            boolean isUpdated = productItemDAO.updateItem(newItem);
            if (!isUpdated) {
                con.rollback();
                throw new SQLException("Failed to update item with serial " + oldSerial);
            }

            // Adjust stock counts if the product type changed
            if (oldStockId > 0 && oldStockId != newStockId) {
                ProductDAOImpl productDAO = new ProductDAOImpl();

                boolean isOldStockUpdated = productDAO.updateQty(oldStockId, 1);
                if (!isOldStockUpdated) {
                    con.rollback();
                    throw new SQLException("Failed to update old product quantity for Stock ID " + oldStockId);
                }

                boolean isNewStockUpdated = productDAO.updateQty(newStockId, -1);
                if (!isNewStockUpdated) {
                    con.rollback();
                    throw new SQLException("Failed to update new product quantity for Stock ID " + newStockId);
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
        ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
        ProductDAOImpl productDAO = new ProductDAOImpl();

        ProductItemDTO item = productItemDAO.getItemBySerial(serial);
        if (item == null) {
            throw new SQLException("Item with serial number " + serial + " does not exist.");
        }
        String currentStatus = item.getStatus();
        if (currentStatus.equals(newStatus)) {
            return false;
        }

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            boolean isStatusChange = productItemDAO.updateStatus(serial, newStatus);
            if (!isStatusChange) {
                con.rollback();
                throw new SQLException("Failed to update status for item with serial number " + serial);
            }

            if (newStatus.equals("AVAILABLE") && !currentStatus.equals("AVAILABLE")) {
                boolean isUpdated = productDAO.updateQty(item.getStockId(), 1);
                if (!isUpdated) {
                    con.rollback();
                    throw new SQLException("Failed to update product quantity for Stock ID " + item.getStockId());
                }

            }else {
                boolean isUpdated = productDAO.updateQty(item.getStockId(), -1);
                if (!isUpdated) {
                    con.rollback();
                    throw new SQLException("Failed to update product quantity for Stock ID " + item.getStockId());
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

    public static class ItemDeleteStatus {
        public final int realAvailableCount;
        public final int restrictedCount;

        public ItemDeleteStatus(int real, int restricted) {
            this.realAvailableCount = real;
            this.restrictedCount = restricted;
        }
    }
}
