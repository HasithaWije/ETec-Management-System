package lk.ijse.etecmanagementsystem.dao;

import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.InventoryItemDTO;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductItemDAOImpl {

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

    public boolean addProductItem(ProductItemDTO item) throws SQLException {
        String insertSql = "INSERT INTO ProductItem (stock_id, supplier_id, serial_number, supplier_warranty_mo, " +
                "customer_warranty_mo, status, added_date) VALUES (?, ?, ?, ?, ?, 'AVAILABLE', NOW())";

        System.out.println("DEBUG: Adding Product Item with Stock ID: " + item.getStockId() + ", Serial Number: " + item.getSerialNumber() +
                ", Supplier ID: " + item.getSupplierId() + ", Supplier Warranty: " + item.getSupplierWarranty() +
                ", Customer Warranty: " + item.getCustomerWarranty());

        if (item.getSupplierId() > 0) {
            return CrudUtil.execute(insertSql, item.getStockId(), item.getSupplierId(), item.getSerialNumber(),
                    item.getSupplierWarranty(), item.getCustomerWarranty());
        } else {
            return CrudUtil.execute(insertSql, item.getStockId(), null, item.getSerialNumber(),
                    item.getSupplierWarranty(), item.getCustomerWarranty());
        }

    }

    public ArrayList<ProductItemDTO> getPlaceHolderItems(int stockId) throws SQLException {
        ArrayList<ProductItemDTO> placeholderItems = new ArrayList<>();
        String sql = "SELECT item_id, stock_id, serial_number FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' AND status = 'AVAILABLE'";
        ResultSet rs = CrudUtil.execute(sql, stockId);
        while (rs.next()) {
            ProductItemDTO item = new ProductItemDTO(
                    rs.getInt("item_id"),
                    rs.getInt("stock_id"),
                    0,
                    rs.getString("serial_number"),
                    null,
                    null,
                    0,
                    0,
                    "AVAILABLE",
                    null,
                    null
            );
            placeholderItems.add(item);
        }
        rs.close();
        return placeholderItems;
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

    public boolean updateItem(ProductItemDTO item) throws SQLException {
        String updateSql = "UPDATE ProductItem SET serial_number = ?, supplier_id = ?, " +
                "supplier_warranty_mo = ?, customer_warranty_mo = ?, status = 'AVAILABLE', added_date = NOW() WHERE item_id = ?";

        System.out.println("DEBUG: Updating Item ID " + item.getItemId() + " with Serial Number: " + item.getSerialNumber() +
                ", Supplier ID: " + item.getSupplierId() + ", Supplier Warranty: " + item.getSupplierWarranty() +
                ", Customer Warranty: " + item.getCustomerWarranty());

        if (item.getSupplierId() > 0) {
            return CrudUtil.execute(updateSql, item.getSerialNumber(), item.getSupplierId(),
                    item.getSupplierWarranty(), item.getCustomerWarranty(), item.getItemId());
        } else {
            return CrudUtil.execute(updateSql, item.getSerialNumber(), null,
                    item.getSupplierWarranty(), item.getCustomerWarranty(), item.getItemId());
        }
    }

    public boolean updateSerialNumber(int itemId, String serialNumber) throws SQLException {
        String sql = "UPDATE ProductItem SET serial_number = ? WHERE item_id = ?";
        return CrudUtil.execute(sql, serialNumber, itemId);
    }

    public boolean updateStatus(String serialNumber, String status) throws SQLException {
        String sql = "UPDATE ProductItem SET status = ?, sold_date = CASE WHEN ? = 'SOLD' THEN NOW() ELSE sold_date END WHERE serial_number = ?";
        return CrudUtil.execute(sql, status, status, serialNumber);
    }

    public boolean updateCustomerWarranty(int customerWarranty, int stockId) throws SQLException {
        String sqlItem = "UPDATE ProductItem SET customer_warranty_mo = ? WHERE stock_id = ? AND status = 'AVAILABLE'";
        return CrudUtil.execute(sqlItem, customerWarranty, stockId);
    }

    public boolean updateItemForSale(ProductItemDTO item) throws SQLException {
        String sqlUpdItem = "UPDATE ProductItem SET status = 'SOLD', sold_date = NOW(), customer_warranty_mo = ?, serial_number = ? WHERE item_id = ? AND status = 'AVAILABLE'";

        return CrudUtil.execute(sqlUpdItem, item.getCustomerWarranty(), item.getSerialNumber(), item.getItemId());
    }

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

    public ProductItemDTO getProductItem(int itemId)throws  SQLException {
        String sql = "SELECT pi.item_id, pi.stock_id, pi.supplier_id, pi.serial_number, p.name as product_name, COALESCE(s.supplier_name, 'No Supplier') as supplier_name, " +
                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.item_id = ?";

        ResultSet rs = CrudUtil.execute(sql, itemId);
        ProductItemDTO productItemDTO = null;

        if (rs.next()) {

            productItemDTO = new ProductItemDTO(
                    rs.getInt("item_id"),
                    rs.getInt("stock_id"),
                    rs.getInt("supplier_id"),
                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
                    rs.getString("product_name"),
                    rs.getString("supplier_name"),
                    rs.getInt("supplier_warranty_mo"),
                    rs.getInt("customer_warranty_mo"),
                    rs.getString("status"),
                    rs.getDate("added_date"),
                    rs.getDate("sold_date")
            );
        }

        rs.close();
        return productItemDTO;
    }

    public int getAvailableItemCount(int stockId) throws SQLException {
        String countSql = "SELECT COUNT(*) AS count FROM ProductItem WHERE stock_id = ? AND status = 'AVAILABLE'";
        ResultSet rs = CrudUtil.execute(countSql, stockId);
        if (rs.next()) {
            return rs.getInt("count");
        } else {
            return 0;
        }
    }

    public List<ProductItemDTO> getUnitsByStockId(int stockId, String productName) throws SQLException {
        List<ProductItemDTO> list = new ArrayList<>();
        String sql = "SELECT pi.item_id, pi.supplier_id, pi.serial_number, pi.supplier_warranty_mo, pi.customer_warranty_mo, " +
                "pi.status, pi.added_date, pi.sold_date, s.supplier_name " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "WHERE pi.stock_id = ? ORDER BY pi.item_id DESC";

        ResultSet rs = CrudUtil.execute(sql, stockId);
        while (rs.next()) {
            String supName = rs.getString("supplier_name");
            if (supName == null) supName = "No Supplier";

            list.add(new ProductItemDTO(
                    rs.getInt("item_id"),
                    stockId,
                    rs.getInt("supplier_id"),
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
        return list;
    }

    public ProductItemDTO getItemBySerial(String serial) throws SQLException {
        String sql = "SELECT pi.item_id, pi.stock_id, pi.supplier_id, pi.serial_number, p.name as product_name, COALESCE(s.supplier_name, 'No Supplier') as supplier_name, " +
                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.serial_number = ?";

        ResultSet rs = CrudUtil.execute(sql, serial);
        ProductItemDTO productItemDTO = null;

        if (rs.next()) {

            productItemDTO = new ProductItemDTO(
                    rs.getInt("item_id"),
                    rs.getInt("stock_id"),
                    rs.getInt("supplier_id"),
                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
                    rs.getString("product_name"),
                    rs.getString("supplier_name"),
                    rs.getInt("supplier_warranty_mo"),
                    rs.getInt("customer_warranty_mo"),
                    rs.getString("status"),
                    rs.getDate("added_date"),
                    rs.getDate("sold_date")
            );
        }

        rs.close();
        return productItemDTO;
    }

    public boolean deletePlaceHolderItems(int stockId, int removeCount) throws SQLException {
        String deleteSql = "DELETE FROM ProductItem WHERE stock_id = ? AND serial_number LIKE 'PENDING-%' AND status = 'AVAILABLE' LIMIT ?";
        return CrudUtil.execute(deleteSql, stockId, removeCount);
    }

    public boolean delete(int stockId) throws SQLException {
        String deleteItemsSql = "DELETE FROM ProductItem WHERE stock_id = ?";
        return CrudUtil.execute(deleteItemsSql, stockId);
    }

    public boolean checkSerialExists(String serial) throws SQLException {
        String sql = "SELECT 1 FROM ProductItem WHERE serial_number = ?";
        ResultSet rs = CrudUtil.execute(sql, serial);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }

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
