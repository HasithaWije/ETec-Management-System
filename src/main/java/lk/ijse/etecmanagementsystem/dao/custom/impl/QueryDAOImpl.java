package lk.ijse.etecmanagementsystem.dao.custom.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import lk.ijse.etecmanagementsystem.dao.custom.QueryDAO;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.CustomDTO;
import lk.ijse.etecmanagementsystem.dto.InventoryItemDTO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dto.tm.*;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QueryDAOImpl implements QueryDAO {
    public ObservableList<PendingRepairTM> getPendingRepairs() throws SQLException {
        String repairSql = "SELECT r.repair_id, r.device_name, c.name, r.total_amount, r.paid_amount FROM RepairJob r JOIN Customer c ON r.cus_id = c.cus_id WHERE r.payment_status IN ('PENDING','PARTIAL') AND r.status IN ('DELIVERED')";


        ResultSet rs = CrudUtil.execute(repairSql);
        ObservableList<PendingRepairTM> pendingRepairsList = FXCollections.observableArrayList();

        while (rs.next()) {
            double balanceDue = rs.getDouble("total_amount") - rs.getDouble("paid_amount");

            pendingRepairsList.add(new PendingRepairTM(rs.getInt("repair_id"),
                    rs.getString("device_name"),
                    rs.getString("name"),
                    balanceDue));
        }
        rs.close();
        return pendingRepairsList;
    }

    public List<RepairPartTM> getUsedParts(int repairId) throws SQLException {
        List<RepairPartTM> list = new ArrayList<>();
//
        String sql = "SELECT pi.item_id, p.name, pi.serial_number, p.p_condition, ri.unit_price " +
                "FROM RepairItem ri " +
                "JOIN ProductItem pi ON ri.item_id = pi.item_id " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE ri.repair_id = ?";

        ResultSet rs = CrudUtil.execute(sql, repairId);

        while (rs.next()) {
            list.add(new RepairPartTM(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getString("serial_number"),
                    fromConditionString(rs.getString("p_condition")),
                    rs.getDouble("unit_price")
            ));
        }
        rs.close();
        return list;

    }

    public List<CustomDTO> getUrgentRepairs() throws SQLException {
        List<CustomDTO> list = new ArrayList<>();
        String sql = "SELECT repair_id, device_name, status, DATE(date_in) as d_in FROM RepairJob " +
                "WHERE status IN ('PENDING', 'DIAGNOSIS', 'WAITING_PARTS') " +
                "ORDER BY date_in ASC LIMIT 15";


        ResultSet rs = CrudUtil.execute(sql);
        while (rs.next()) {
            list.add(new CustomDTO(
                    rs.getInt("repair_id"),
                    rs.getString("device_name"),
                    rs.getString("status"),
                    rs.getString("d_in")
            ));
        }
        rs.close();
        return list;
    }

    public List<CustomDTO> getUnpaidDebts() throws SQLException {
        List<CustomDTO> list = new ArrayList<>();

        String sql = "SELECT 'SALE' as type, s.sale_id as ref_id, c.name, (s.grand_total - s.paid_amount) as due " +
                "FROM Sales s LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "WHERE s.payment_status != 'PAID' AND s.description LIKE 'Point of Sale Transaction'" +
                "UNION ALL " +
                "SELECT 'REPAIR' as type, r.repair_id as ref_id, c.name, (r.total_amount - r.paid_amount) as due " +
                "FROM RepairJob r JOIN Customer c ON r.cus_id = c.cus_id " +
                "WHERE r.payment_status != 'PAID' AND r.status = 'DELIVERED'";


        ResultSet rs = CrudUtil.execute(sql);
        while (rs.next()) {
            list.add(new CustomDTO(
                    rs.getInt("ref_id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getDouble("due")
            ));
        }
        rs.close();
        return list;
    }

    public double getDebts() throws SQLException {
        String sqlDebts = "SELECT " +
                "(SELECT COALESCE(SUM(grand_total - paid_amount),0) FROM Sales WHERE payment_status != 'PAID') + " +
                "(SELECT COALESCE(SUM(total_amount - paid_amount),0) FROM RepairJob WHERE payment_status != 'PAID' AND status != 'CANCELLED')";
        double debts = 0.0;
        ResultSet rs4 = CrudUtil.execute(sqlDebts);
        if (rs4.next()) {
            debts = rs4.getDouble(1);
        }
        System.out.println("Total Debts: " + debts);
        rs4.close();
        return debts;
    }

    public List<CustomDTO> getAllProductItems() throws SQLException {
        List<CustomDTO> itemList = new ArrayList<>();
        String sql = "SELECT pi.item_id, pi.stock_id, pi.supplier_id, pi.serial_number, " +
                "p.name AS product_name, COALESCE(s.supplier_name, 'No Supplier') AS supplier_name, " +
                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
                "FROM ProductItem pi " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "ORDER BY pi.item_id DESC";

        ResultSet rs = CrudUtil.execute(sql);

        while (rs.next()) {

            CustomDTO item = new CustomDTO(
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
            itemList.add(item);
        }
        rs.close();
        return itemList;
    }

    public List<CustomDTO> getAllAvailableRealItems() throws SQLException {
        List<CustomDTO> itemList = new ArrayList<>();

        String sql = "SELECT pi.item_id, p.name AS product_name, pi.serial_number, " +
                "p.warranty_months, p.sell_price, p.p_condition " +
                "FROM ProductItem pi " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.status = 'AVAILABLE' AND pi.serial_number NOT LIKE 'PENDING-%' ";

        ResultSet rs = CrudUtil.execute(sql);

        while (rs.next()) {
            CustomDTO item = new CustomDTO(
                    rs.getInt("item_id"),
                    rs.getString("product_name"),
                    rs.getString("serial_number") == null ? "" : rs.getString("serial_number"),
                    ProductCondition.fromString(rs.getString("p_condition")),
                    rs.getInt("warranty_months"), // Default warranty from Product definition
                    rs.getDouble("sell_price")
            );
            itemList.add(item);
        }
        rs.close();
        return itemList;
    }

    public CustomDTO getProductItem(int itemId) throws SQLException {
        String sql = "SELECT pi.item_id, pi.stock_id, pi.supplier_id, pi.serial_number, p.name as product_name, COALESCE(s.supplier_name, 'No Supplier') as supplier_name, " +
                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.item_id = ?";

        ResultSet rs = CrudUtil.execute(sql, itemId);
        CustomDTO item = null;

        if (rs.next()) {

            item = getProductItemCustomDTO(rs);
        }

        rs.close();
        return item;
    }


    public List<CustomDTO> getUnitsByStockId(int stockId, String productName) throws SQLException {
        List<CustomDTO> list = new ArrayList<>();
        String sql = "SELECT pi.item_id, pi.supplier_id, pi.serial_number, pi.supplier_warranty_mo, pi.customer_warranty_mo, " +
                "pi.status, pi.added_date, pi.sold_date, s.supplier_name " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "WHERE pi.stock_id = ? ORDER BY pi.item_id DESC";

        ResultSet rs = CrudUtil.execute(sql, stockId);
        while (rs.next()) {
            String supName = rs.getString("supplier_name");
            if (supName == null) supName = "No Supplier";

            list.add(new CustomDTO(
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


    public CustomDTO getItemBySerial(String serial) throws SQLException {
        String sql = "SELECT pi.item_id, pi.stock_id, pi.supplier_id, pi.serial_number, p.name as product_name, COALESCE(s.supplier_name, 'No Supplier') as supplier_name, " +
                "pi.supplier_warranty_mo, pi.customer_warranty_mo, pi.status, pi.added_date, pi.sold_date " +
                "FROM ProductItem pi " +
                "LEFT JOIN Supplier s ON pi.supplier_id = s.supplier_id " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.serial_number = ?";

        ResultSet rs = CrudUtil.execute(sql, serial);
        CustomDTO customDTO = null;

        if (rs.next()) {

            customDTO = getProductItemCustomDTO(rs);
        }

        rs.close();
        return customDTO;
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

        ResultSet rs = CrudUtil.execute(sql, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to));
        while (rs.next()) {
            salesList.add(new SalesTM(
                    rs.getInt("sale_id"),
                    rs.getString("customer_name") != null ? rs.getString("customer_name") : "Walk-in", // Handle null customers
                    rs.getString("user_name"),
                    rs.getString("description"),
                    rs.getDouble("sub_total"),
                    rs.getDouble("discount"),
                    rs.getDouble("grand_total"),
                    rs.getDouble("paid_amount")
            ));
        }
        rs.close();
        return salesList;
    }

    public ObservableList<PendingSaleTM> getPendingSales() throws SQLException {
        String saleSql = "SELECT s.sale_id, c.name, s.grand_total, s.paid_amount FROM Sales s LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "WHERE s.payment_status IN ('PENDING', 'PARTIAL') AND s.description LIKE 'Point of Sale Transaction'";


        ResultSet rs = CrudUtil.execute(saleSql);
        ObservableList<PendingSaleTM> pendingSalesList = FXCollections.observableArrayList();

        while (rs.next()) {
            double total = rs.getDouble("grand_total");
            double paid = rs.getDouble("paid_amount");
            pendingSalesList.add(new PendingSaleTM(rs.getInt("sale_id"), rs.getString("name"), total, total - paid));
        }
        rs.close();
        return pendingSalesList;
    }

    public CustomDTO getProductItemCustomDTO(ResultSet rs) throws SQLException {
        return new CustomDTO(
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

    private ProductCondition fromConditionString(String s) {
        if (s == null) return ProductCondition.BOTH;
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
