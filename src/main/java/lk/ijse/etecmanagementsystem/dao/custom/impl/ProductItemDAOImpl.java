package lk.ijse.etecmanagementsystem.dao.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.ProductItemDAO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.ProductItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductItemDAOImpl implements ProductItemDAO {

    @Override
    public List<ProductItem> getAll() throws SQLException {
        String sql = "SELECT * FROM ProductItem WHERE status = 'AVAILABLE'";
        ResultSet rs = CrudUtil.execute(sql);

        List<ProductItem> items = new ArrayList<>();
        while (rs.next()) {


            ProductItem item = new ProductItem(
                    rs.getInt("item_id"),
                    rs.getInt("stock_id"),
                    rs.getInt("supplier_id"),
                    rs.getString("serial_number"),
                    rs.getString("status"),
                    rs.getDate("added_date"),
                    rs.getInt("supplier_warranty_mo"),
                    rs.getDate("added_date"),
                    rs.getInt("customer_warranty_mo")
            );
            items.add(item);
        }
        rs.close();
        return items;
    }

    @Override
    public boolean save(ProductItem entity) throws SQLException {
        String insertSql = "INSERT INTO ProductItem (stock_id, supplier_id, serial_number, supplier_warranty_mo, " +
                "customer_warranty_mo, status, added_date) VALUES (?, ?, ?, ?, ?, 'AVAILABLE', NOW())";

        if (entity.getSupplier_id() > 0) {
            return CrudUtil.execute(insertSql, entity.getStock_id(), entity.getSupplier_id(), entity.getSerial_number(),
                    entity.getSupplier_warranty_mo(), entity.getCustomer_warranty_mo());
        } else {
            return CrudUtil.execute(insertSql, entity.getStock_id(), null, entity.getSerial_number(),
                    entity.getSupplier_warranty_mo(), entity.getCustomer_warranty_mo());
        }
    }

    @Override
    public boolean update(ProductItem entity) throws SQLException {
        String updateSql = "UPDATE ProductItem SET serial_number = ?, supplier_id = ?, " +
                "supplier_warranty_mo = ?, customer_warranty_mo = ?, status = 'AVAILABLE', added_date = NOW() WHERE item_id = ?";

        if (entity.getSupplier_id() > 0) {
            return CrudUtil.execute(updateSql, entity.getSerial_number(), entity.getSupplier_id(),
                    entity.getSupplier_warranty_mo(), entity.getCustomer_warranty_mo(), entity.getItem_id());
        } else {
            return CrudUtil.execute(updateSql, entity.getSerial_number(), null,
                    entity.getSupplier_warranty_mo(), entity.getCustomer_warranty_mo(), entity.getItem_id());
        }
    }

    @Override
    public boolean delete(int stockId) throws SQLException {
        String deleteItemsSql = "DELETE FROM ProductItem WHERE stock_id = ?";
        return CrudUtil.execute(deleteItemsSql, stockId);
    }

    @Override
    public ProductItem search(int id) throws SQLException {
        return null;
    }

    @Override
    public boolean addPlaceHolderItem(int stockId, int limit) throws SQLException {
        String insertSql = "INSERT INTO ProductItem (stock_id, serial_number, status, added_date) VALUES (?, ?, 'AVAILABLE', NOW())";
        for (int i = 0; i < limit; i++) {
            String tempSerial = "PENDING-" + stockId + "-" + System.nanoTime() + "-" + i;
            boolean isAdded = CrudUtil.execute(insertSql, stockId, tempSerial);
            if (!isAdded) {
                System.out.println("DEBUG: Failed to add placeholder item for Stock ID " + stockId);
                return false;
            }
        }
        return true;
    }

    @Override
    public ArrayList<ProductItem> getPlaceHolderItems(int stockId) throws SQLException {
        ArrayList<ProductItem> placeholderItems = new ArrayList<>();
        String sql = "SELECT item_id, stock_id, serial_number FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' AND status = 'AVAILABLE'";
        ResultSet rs = CrudUtil.execute(sql, stockId);

        while (rs.next()) {

            ProductItem item = new ProductItem(
                    rs.getInt("item_id"),
                    rs.getInt("stock_id"),
                    0,
                    rs.getString("serial_number"),
                    "AVAILABLE",
                    null,
                    0,
                    null,
                    0
            );
            placeholderItems.add(item);

        }
        rs.close();
        return placeholderItems;
    }

    @Override
    public boolean updateSerialNumber(int itemId, String serialNumber) throws SQLException {
        String sql = "UPDATE ProductItem SET serial_number = ? WHERE item_id = ?";
        return CrudUtil.execute(sql, serialNumber, itemId);
    }

    @Override
    public boolean updateStatus(String serialNumber, String status) throws SQLException {
        String sql = "UPDATE ProductItem SET status = ?, sold_date = CASE WHEN ? = 'SOLD' THEN NOW() ELSE sold_date END WHERE serial_number = ?";
        return CrudUtil.execute(sql, status, status, serialNumber);
    }

    @Override
    public boolean updateItemAvailability(int itemId) throws SQLException {
        String sqlRestock = "UPDATE ProductItem SET status='AVAILABLE', sold_date = null  WHERE item_id=?";
        return CrudUtil.execute(sqlRestock, itemId);
    }

    @Override
    public boolean updateCustomerWarranty(int customerWarranty, int stockId) throws SQLException {
        String sqlItem = "UPDATE ProductItem SET customer_warranty_mo = ? WHERE stock_id = ? AND status = 'AVAILABLE'";
        return CrudUtil.execute(sqlItem, customerWarranty, stockId);
    }

    @Override
    public boolean updateItemForSale(ProductItemDTO item) throws SQLException {
        String sqlUpdItem = "UPDATE ProductItem SET status = 'SOLD', sold_date = NOW(), customer_warranty_mo = ?, serial_number = ? WHERE item_id = ? AND status = 'AVAILABLE'";
        return CrudUtil.execute(sqlUpdItem, item.getCustomerWarranty(), item.getSerialNumber(), item.getItemId());
    }

    @Override
    public boolean updateItemForRepair(int itemId) throws SQLException {
        String sqlMarkSold = "UPDATE ProductItem SET status='IN_REPAIR_USE' WHERE item_id=?";
        return  CrudUtil.execute(sqlMarkSold, itemId);
    }

    @Override
    public boolean fixSerialForRepair(int itemId) throws SQLException {
        String sqlFixPlaceholders = "UPDATE ProductItem " +
                "SET serial_number = CONCAT('REPAIR-', SUBSTRING(serial_number, 9)) " +
                "WHERE item_id=? AND serial_number LIKE 'PENDING-%'";
        return CrudUtil.execute(sqlFixPlaceholders, itemId);
    }

    @Override
    public boolean replaceSerialForReturned(int itemId) throws SQLException {
        String sqlReplacePlaceholders = "UPDATE ProductItem " +
                "SET serial_number = CONCAT('PENDING-', SUBSTRING(serial_number, 8)) " +
                "WHERE item_id=? AND serial_number LIKE 'REPAIR-%'";
        return CrudUtil.execute(sqlReplacePlaceholders, itemId);
    }

    @Override
    public int getRealItemCount(int stockId) throws SQLException {
        System.out.println("DEBUG: Querying Real Item Count for Stock ID: " + stockId);

        String sql = "SELECT COUNT(*) AS count FROM ProductItem WHERE stock_id = ? AND serial_number NOT LIKE 'PENDING-%' AND status = 'AVAILABLE'";

        ResultSet rs = CrudUtil.execute(sql, stockId);
        if (rs.next()) {
            int count = rs.getInt("count");
            System.out.println("DEBUG: Real Item Count for Stock ID " + stockId + " is " + count);
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public int getAvailableItemCount(int stockId) throws SQLException {
        String countSql = "SELECT COUNT(*) AS count FROM ProductItem WHERE stock_id = ? AND status = 'AVAILABLE'";
        ResultSet rs = CrudUtil.execute(countSql, stockId);
        if (rs.next()) {
            return rs.getInt("count");
        } else {
            return 0;
        }
    }

    @Override
    public boolean deletePlaceHolderItems(int stockId, int removeCount) throws SQLException {
        String deleteSql = "DELETE FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' AND status = 'AVAILABLE' LIMIT ?";
        return CrudUtil.execute(deleteSql, stockId, removeCount);
    }

    @Override
    public boolean checkSerialExists(String serial) throws SQLException {
        String sql = "SELECT 1 FROM ProductItem WHERE serial_number = ?";
        ResultSet rs = CrudUtil.execute(sql, serial);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }

    @Override
    public int getRestrictedRealItemCount(int stockId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM ProductItem WHERE stock_id = ? AND serial_number NOT LIKE 'PENDING-%' AND status != 'AVAILABLE'";
        ResultSet rs = CrudUtil.execute(sql, stockId);
        if (rs.next()) {
            return rs.getInt("count");
        } else {
            return 0;
        }
    }
}
