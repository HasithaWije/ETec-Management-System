package lk.ijse.etecmanagementsystem.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.tm.PendingRepairTM;
import lk.ijse.etecmanagementsystem.dto.tm.PendingSaleTM;
import lk.ijse.etecmanagementsystem.dto.tm.TransactionTM;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class TransactionsModel {
//    public List<TransactionTM> getAllTransactions(Date dpFromDate, Date dpToDate) throws SQLException {
//        List<TransactionTM> list = new java.util.ArrayList<>();
//        String sql = "SELECT t.*, u.user_name FROM TransactionRecord t JOIN User u ON t.user_id = u.user_id WHERE DATE(t.transaction_date) BETWEEN ? AND ?";
//
//        ResultSet rs = CrudUtil.execute(sql, dpFromDate, dpToDate);
//        while (rs.next()) {
//            list.add(new TransactionTM(
//                    rs.getInt("transaction_id"),
//                    rs.getString("transaction_date"),
//                    rs.getString("transaction_type"),
//                    rs.getString("reference_note"),
//                    rs.getString("flow"),
//                    rs.getDouble("amount"),
//                    rs.getString("user_name")
//            ));
//        }
//        rs.close();
//        return list;
//    }
//
//    public ObservableList<PendingSaleTM> getPendingSales() throws SQLException {
//        String saleSql = "SELECT s.sale_id, c.name, s.grand_total, s.paid_amount FROM Sales s LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
//                "WHERE s.payment_status IN ('PENDING', 'PARTIAL') AND s.description LIKE 'Point of Sale Transaction'";
//
//
//        ResultSet rs = CrudUtil.execute(saleSql);
//        ObservableList<PendingSaleTM> pendingSalesList = FXCollections.observableArrayList();
//
//        while (rs.next()) {
//            double total = rs.getDouble("grand_total");
//            double paid = rs.getDouble("paid_amount");
//            pendingSalesList.add(new PendingSaleTM(rs.getInt("sale_id"), rs.getString("name"), total, total - paid));
//        }
//        rs.close();
//        return pendingSalesList;
//    }

    public ObservableList<PendingRepairTM> getPendingRepairs() throws SQLException {
        String repairSql = "SELECT r.repair_id, r.device_name, c.name, r.total_amount, r.paid_amount FROM RepairJob r JOIN Customer c ON r.cus_id = c.cus_id WHERE r.payment_status IN ('PENDING','PARTIAL') AND r.status IN ('DELIVERED')";


        ResultSet rs = CrudUtil.execute(repairSql);
        ObservableList<PendingRepairTM> pendingRepairsList = FXCollections.observableArrayList();

        while (rs.next()) {
            double balanceDue = rs.getDouble("total_amount") - rs.getDouble("paid_amount");


//            (int repairId, String device, String customerName, double balanceDue)
            pendingRepairsList.add(new PendingRepairTM(rs.getInt("repair_id"),
                    rs.getString("device_name"),
                    rs.getString("name"),
                    balanceDue));
        }
        rs.close();
        return pendingRepairsList;
    }


//    public boolean saveManualTransaction(String type, double amount, String method, String note, int userId) throws SQLException {
//        String flow = (type.equals("EXPENSE") || type.equals("SUPPLIER_PAYMENT")) ? "OUT" : "IN";
//        String sql = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, user_id, reference_note) VALUES (?, ?, ?, ?, ?, ?)";
//        return CrudUtil.execute(sql, type, method, amount, flow, userId, note);
//    }
//
//    public double[] getDashboardStats(Date fromDate, Date toDate) throws SQLException {
//        String sql = "SELECT " +
//                "SUM(CASE WHEN flow = 'IN' THEN amount ELSE 0 END) as total_in, " +
//                "SUM(CASE WHEN flow = 'OUT' THEN amount ELSE 0 END) as total_out " +
//                "FROM TransactionRecord WHERE DATE(transaction_date) BETWEEN ? AND ?";
//
//        ResultSet rs = CrudUtil.execute(sql, fromDate, toDate);
//        double[] newDoubleArray;
//        if (rs.next()) {
//
//            newDoubleArray = new double[]{rs.getDouble("total_in"), rs.getDouble("total_out")};
//        } else {
//            newDoubleArray = new double[]{0.0, 0.0};
//        }
//        rs.close();
//        return newDoubleArray;
//    }

    public boolean settlePayment(String type, int id, double amount, int userId, String newPaymentStatus) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start Transaction

            String insertTrans = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, " +
                    (type.equals("SALE") ? "sale_id" : "repair_id") + ", user_id, reference_note) VALUES (?, 'CASH', ?, 'IN', ?, ?, 'Partial Settlement')";

            String transType = type.equals("SALE") ? "SALE_PAYMENT" : "REPAIR_PAYMENT";

            try (PreparedStatement ps1 = conn.prepareStatement(insertTrans)) {
                ps1.setString(1, transType);
                ps1.setDouble(2, amount);
                ps1.setInt(3, id);
                ps1.setInt(4, userId);
                if (ps1.executeUpdate() <= 0) throw new SQLException("Failed to insert transaction");
            }

            String updateSql;
            if (type.equals("SALE")) {
                updateSql = "UPDATE Sales SET paid_amount = paid_amount + ?, payment_status = ? WHERE sale_id = ?";
            } else {
                updateSql = "UPDATE RepairJob SET paid_amount = paid_amount + ?, payment_status = ? WHERE repair_id = ?";
            }

            try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                ps2.setDouble(1, amount);
                ps2.setString(2, newPaymentStatus);
                ps2.setInt(3, id);
                if (ps2.executeUpdate() <= 0) throw new SQLException("Failed to update status");
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
}