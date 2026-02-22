package lk.ijse.etecmanagementsystem.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lk.ijse.etecmanagementsystem.dao.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.dto.tm.PendingSaleTM;
import lk.ijse.etecmanagementsystem.dto.tm.TransactionTM;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TransactionRecordDAOImpl {
    public boolean insertTransactionRecord(TransactionRecord entity) throws SQLException {
        String sqlTrans = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, sale_id, user_id, customer_id, reference_note) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return CrudUtil.execute(sqlTrans,
                entity.getTransaction_type(),
                entity.getTransaction_method(),
                entity.getAmount(),
                entity.getFlow(),
                entity.getSale_id(),
                entity.getUser_id(),
                entity.getCustomer_id() == 0 ? null : entity.getCustomer_id(),
                entity.getReference_note()
        );
    }

    public List<TransactionTM> getAllTransactions(Date dpFromDate, Date dpToDate) throws SQLException {
        List<TransactionTM> list = new java.util.ArrayList<>();
        String sql = "SELECT t.*, u.user_name FROM TransactionRecord t JOIN User u ON t.user_id = u.user_id WHERE DATE(t.transaction_date) BETWEEN ? AND ?";

        ResultSet rs = CrudUtil.execute(sql, dpFromDate, dpToDate);
        while (rs.next()) {
            list.add(new TransactionTM(
                    rs.getInt("transaction_id"),
                    rs.getString("transaction_date"),
                    rs.getString("transaction_type"),
                    rs.getString("reference_note"),
                    rs.getString("flow"),
                    rs.getDouble("amount"),
                    rs.getString("user_name")
            ));
        }
        rs.close();
        return list;
    }

    public boolean saveManualTransaction(String type, double amount, String method, String note, int userId) throws SQLException {
        String flow = (type.equals("EXPENSE") || type.equals("SUPPLIER_PAYMENT")) ? "OUT" : "IN";
        String sql = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, user_id, reference_note) VALUES (?, ?, ?, ?, ?, ?)";
        return CrudUtil.execute(sql, type, method, amount, flow, userId, note);
    }

    public double[] getDashboardStats(Date fromDate, Date toDate) throws SQLException {
        String sql = "SELECT " +
                "SUM(CASE WHEN flow = 'IN' THEN amount ELSE 0 END) as total_in, " +
                "SUM(CASE WHEN flow = 'OUT' THEN amount ELSE 0 END) as total_out " +
                "FROM TransactionRecord WHERE DATE(transaction_date) BETWEEN ? AND ?";

        ResultSet rs = CrudUtil.execute(sql, fromDate, toDate);
        double[] newDoubleArray;
        if (rs.next()) {

            newDoubleArray = new double[]{rs.getDouble("total_in"), rs.getDouble("total_out")};
        } else {
            newDoubleArray = new double[]{0.0, 0.0};
        }
        rs.close();
        return newDoubleArray;
    }
}
