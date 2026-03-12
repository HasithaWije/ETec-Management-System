package lk.ijse.etecmanagementsystem.bo.custom.impl;

import lk.ijse.etecmanagementsystem.bo.custom.InventoryBO;
import lk.ijse.etecmanagementsystem.dao.DAOFactory;
import lk.ijse.etecmanagementsystem.dao.custom.*;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.*;
import lk.ijse.etecmanagementsystem.entity.Product;
import lk.ijse.etecmanagementsystem.entity.ProductItem;
import lk.ijse.etecmanagementsystem.entity.Supplier;
import lk.ijse.etecmanagementsystem.dto.ProductCondition;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryBOImpl implements InventoryBO {
    ProductItemDAO productItemDAO = (ProductItemDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.PRODUCT_ITEM);
    SupplierDAO supplierDAO = (SupplierDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.SUPPLIER);
    QueryDAO queryDAO = (QueryDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
    ProductDAO productDAO = (ProductDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.PRODUCT);

    @Override
    public int saveProductAndGetId(ProductDTO p) throws SQLException {
        Connection connection = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start Transaction


            boolean isSaved = productDAO.save(new Product(
                    0,
                    p.getName(),
                    p.getDescription(),
                    p.getCategory(),
                    p.getCondition().getLabel(),
                    p.getQty(),
                    p.getWarrantyMonth(),
                    p.getImagePath(),
                    p.getBuyPrice(),
                    p.getSellPrice()
            ));

            if (!isSaved) {
                connection.rollback();
                throw new SQLException("Failed to save product.");
            }

            int newStockId = productDAO.getLastInsertedProductId();
            if (newStockId <= 0) {
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

    @Override
    public List<ProductDTO> getAllProduct() throws SQLException {
        List<ProductDTO> products = new ArrayList<>();
        List<Product> productEntities = productDAO.getAll();
        for (Product entity : productEntities) {

            products.add(new ProductDTO(
                    String.valueOf(entity.getStock_id()),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getSell_price(),
                    entity.getCategory(),
                    fromConditionString(entity.getP_condition()),
                    entity.getBuy_price(),
                    entity.getWarranty_months(),
                    entity.getQty(),
                    entity.getImage_path()
            ));
        }
        return products;

    }

    @Override
    public boolean updateProductWithQtySync(ProductDTO p) throws SQLException {

        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // Start Transaction

            boolean isUpdated = productDAO.update(new Product(
                    Integer.parseInt(p.getId()),
                    p.getName(),
                    p.getDescription(),
                    p.getCategory(),
                    p.getCondition().getLabel(),
                    p.getQty(),
                    p.getWarrantyMonth(),
                    p.getImagePath(),
                    p.getBuyPrice(),
                    p.getSellPrice()
            ));

            if (!isUpdated) {
                connection.rollback();
                return false;
            }

            boolean isItemUpdated = updateCustomerWarranty(p.getWarrantyMonth(), Integer.parseInt(p.getId()));
            if (!isItemUpdated) {
                connection.rollback();
                return false;
            }

            int currentTotalItems = productItemDAO.getAvailableItemCount(Integer.parseInt(p.getId()));
            if (currentTotalItems <= 0) {
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

    @Override
    public boolean deleteById(String stockId) throws SQLException {
        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false);

            boolean isItemDeleted = delete(Integer.parseInt(stockId));
            if (!isItemDeleted) {
                connection.rollback();
                System.out.println("DEBUG: Failed to delete product items for Stock ID " + stockId);
                return false;
            }

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

    @Override
    public int getIdByName(String name) throws SQLException {
        return productDAO.getIdByName(name);
    }

    @Override
    public CustomDTO checkItemStatusForDelete(String stockId) throws SQLException {

        int realAvailableCount = getRealItemCount(Integer.parseInt(stockId));
        System.out.println("DEBUG: Real Available Item Count for Stock ID " + stockId + " is " + realAvailableCount);
        int restrictedCount = getRestrictedRealItemCount(Integer.parseInt(stockId));
        System.out.println("DEBUG: Restricted Item Count for Stock ID " + stockId + " is " + restrictedCount);

        return new CustomDTO(realAvailableCount, restrictedCount);
    }

    @Override
    public boolean addNewSerialNo(ArrayList<ProductItemDTO> itemDTOS) throws SQLException {

        Connection conn = null;

        ArrayList<ProductItem> entities = new ArrayList<>();
        for (ProductItemDTO productItemDTO : itemDTOS) {
            entities.add(new ProductItem(
                    productItemDTO.getItemId(),
                    productItemDTO.getStockId(),
                    productItemDTO.getSupplierId(),
                    productItemDTO.getSerialNumber(),
                    productItemDTO.getStatus(),
                    productItemDTO.getAddedDate(),
                    productItemDTO.getSupplierWarranty(),
                    productItemDTO.getSoldDate(),
                    productItemDTO.getCustomerWarranty()
            ));
        }

        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);


            ArrayList<ProductItemDTO> placeholders = getPlaceHolderItems(itemDTOS.getFirst().getStockId());


            int i = 0;
            for (ProductItem entity : entities) {
                int placeholderId = -1;

                if (!placeholders.isEmpty() && i < placeholders.size()) {
                    placeholderId = placeholders.get(i++).getItemId();
                }
                if (placeholderId != -1) {

                    entity.setItem_id(placeholderId);
                    boolean isUpdated = productItemDAO.update(entity);
                    if (!isUpdated) {
                        conn.rollback();
                        throw new SQLException("Failed to update placeholder item with ID " + placeholderId);
                    }
                } else {

                    boolean isAdded = productItemDAO.save(entity);
                    if (!isAdded) {
                        conn.rollback();
                        throw new SQLException("Failed to add new item for Stock ID " + entity.getStock_id());
                    }

                    boolean isQtyUpdated = productDAO.updateQty(entity.getStock_id(), 1);
                    if (!isQtyUpdated) {
                        conn.rollback();
                        throw new SQLException("Failed to update product quantity for Stock ID " + entity.getStock_id());
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

    @Override
    public ProductDTO findById(String id) throws SQLException {
        Product entity = productDAO.search(Integer.parseInt(id));
        return new ProductDTO(
                String.valueOf(entity.getStock_id()),
                entity.getName(),
                entity.getDescription(),
                entity.getSell_price(),
                entity.getCategory(),
                fromConditionString(entity.getP_condition()),
                entity.getBuy_price(),
                entity.getWarranty_months(),
                entity.getQty(),
                entity.getImage_path()
        );

    }

    @Override
    public Map<Integer, String> getAllProductMap() throws SQLException {

        Map<Integer, String> map = new HashMap<>();
        List<ProductItemDTO> items = getAllProductItems();
        for (ProductItemDTO item : items) {
            map.put(item.getStockId(), item.getProductName());
        }
        return map;
    }

    @Override
    public Map<Integer, String> getAllSuppliersMap() throws SQLException {

        Map<Integer, String> map = new HashMap<>();
        List<Supplier> suppliers = supplierDAO.getAll();
        for (Supplier sup : suppliers) {
            map.put(sup.getSupplier_id(), sup.getSupplier_name());
        }
        return map;

    }

    @Override
    public boolean correctItemMistake(String oldSerial, String newSerial, int newStockId, Integer newSupplierId, int newSupWar) throws SQLException {

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            con.setAutoCommit(false);


            ProductItemDTO oldItem = getItemBySerial(oldSerial);
            if (oldItem == null) return false;
            int oldStockId = oldItem.getStockId();

            ProductItemDTO newItem = new ProductItemDTO();
            newItem.setSerialNumber(newSerial);
            newItem.setStockId(newStockId);
            if (newSupplierId == null) newItem.setSupplierId(0);
            else newItem.setSupplierId(newSupplierId);
            newItem.setSupplierWarranty(newSupWar);


            boolean isUpdated = productItemDAO.update(new ProductItem(
                    oldItem.getItemId(),
                    newItem.getStockId(),
                    newItem.getSupplierId(),
                    newItem.getSerialNumber(),
                    oldItem.getStatus(),
                    oldItem.getAddedDate(),
                    newItem.getSupplierWarranty(),
                    oldItem.getSoldDate(),
                    oldItem.getCustomerWarranty()
            ));
            if (!isUpdated) {
                con.rollback();
                throw new SQLException("Failed to update item with serial " + oldSerial);
            }

            // Adjust stock counts if the product type changed
            if (oldStockId > 0 && oldStockId != newStockId) {

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

    @Override
    public boolean updateItemStatus(String serial, String newStatus) throws SQLException {

        ProductItemDTO item = getItemBySerial(serial);
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

            } else {
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

    @Override
    public ArrayList<ProductItemDTO> getPlaceHolderItems(int stockId) throws SQLException {

        ArrayList<ProductItem> entity = productItemDAO.getPlaceHolderItems(stockId);
        ArrayList<ProductItemDTO> placeholderItems = new ArrayList<>();

        for (ProductItem productItem : entity) {
            placeholderItems.add(new ProductItemDTO(
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
        return placeholderItems;
    }

    @Override
    public List<ProductItemDTO> getAllAvailableItems() throws SQLException {

        List<ProductItem> entity = productItemDAO.getAll();
        List<ProductItemDTO> productItemDTOS = new ArrayList<>();

        for (ProductItem productItem : entity) {
            productItemDTOS.add(new ProductItemDTO(
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
        return productItemDTOS;
    }

    @Override
    public boolean updateCustomerWarranty(int customerWarranty, int stockId) throws SQLException {
        return productItemDAO.updateCustomerWarranty(customerWarranty, stockId);
    }

    @Override
    public int getRealItemCount(int stockId) throws SQLException {
        return productItemDAO.getRealItemCount(stockId);
    }

    @Override
    public boolean delete(int stockId) throws SQLException {
        return productItemDAO.delete(stockId);
    }

    @Override
    public boolean checkSerialExists(String serial) throws SQLException {
        return productItemDAO.checkSerialExists(serial);
    }

    @Override
    public int getRestrictedRealItemCount(int stockId) throws SQLException {
        return productItemDAO.getRestrictedRealItemCount(stockId);
    }

    @Override
    public List<ProductItemDTO> getAllProductItems() throws SQLException{
        List<CustomDTO> entities = queryDAO.getAllProductItems();
        List<ProductItemDTO> productItemDTOS = new ArrayList<>();

        for (CustomDTO customDTO : entities) {
            productItemDTOS.add(new ProductItemDTO(
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

            ));
        }
        return productItemDTOS;
    }

    @Override
    public List<CustomDTO> getAllAvailableRealItems() throws SQLException {

        return queryDAO.getAllAvailableRealItems();
    }

    @Override
    public List<ProductItemDTO> getUnitsByStockId(int stockId, String productName) throws SQLException {
        List<ProductItemDTO> list = new ArrayList<>();
        List<CustomDTO> entities = queryDAO.getUnitsByStockId(stockId, productName);
        for (CustomDTO customDTO : entities) {
            list.add(new ProductItemDTO(
                    customDTO.getProductItemId(),
                    stockId,
                    customDTO.getProductItemSupplierId(),
                    customDTO.getProductItemSerialNumber(),
                    productName,
                    customDTO.getProductItemSupplierName(),
                    customDTO.getProductItemSupplierWarranty(),
                    customDTO.getProductItemCustomerWarranty(),
                    customDTO.getProductItemStatus(),
                    customDTO.getProductItemAddedDate(),
                    customDTO.getProductItemSoldDate()
            ));
        }
        return list;
    }

    @Override
    public ProductItemDTO getItemBySerial(String serial) throws SQLException {

        CustomDTO customDTO = queryDAO.getItemBySerial(serial);
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
    public boolean updateSerialNumber(int itemId, String serialNumber) throws SQLException {
        return productItemDAO.updateSerialNumber(itemId, serialNumber);
    }

    private ProductCondition fromConditionString(String s) {
        if (s == null) return null;
        try {
            if (s.equals("USED")) {
                return ProductCondition.USED;
            } else if (s.equals("BRAND NEW")) {
                return ProductCondition.BRAND_NEW;
            }
            return ProductCondition.BOTH;
        } catch (IllegalArgumentException ex) {
            return ProductCondition.BOTH; // unknown condition value
        }
    }
}
