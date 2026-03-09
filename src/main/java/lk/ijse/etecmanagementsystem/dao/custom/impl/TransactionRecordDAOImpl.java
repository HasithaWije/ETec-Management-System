package lk.ijse.etecmanagementsystem.dao.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.TransactionRecordDAO;
import lk.ijse.etecmanagementsystem.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.reports.GenerateReports;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransactionRecordDAOImpl implements TransactionRecordDAO {

    @Override
    public List<TransactionRecord> getAllTransactions(Date dpFromDate, Date dpToDate) throws SQLException {
        List<TransactionRecord> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM TransactionRecord  WHERE DATE(transaction_date) BETWEEN ? AND ?";

        ResultSet rs = CrudUtil.execute(sql, dpFromDate, dpToDate);
        while (rs.next()) {
            list.add(new TransactionRecord(
                    rs.getInt("transaction_id"),
                    rs.getInt("user_id"),
                    rs.getString("transaction_date"),
                    rs.getString("transaction_type"),
                    rs.getString("reference_note"),
                    rs.getString("flow"),
                    rs.getDouble("amount")
            ));
        }
        rs.close();
        return list;
    }

    @Override
    public boolean saveManualTransaction(String type, double amount, String method, String note, int userId) throws SQLException {
        String flow = (type.equals("EXPENSE") || type.equals("SUPPLIER_PAYMENT")) ? "OUT" : "IN";
        String sql = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, user_id, reference_note) VALUES (?, ?, ?, ?, ?, ?)";
        return CrudUtil.execute(sql, type, method, amount, flow, userId, note);
    }

    @Override
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

    @Override
    public boolean settleTransaction(TransactionRecord entity, String type) throws SQLException {
        String insertTrans = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, " +
                (type.equals("SALE") ? "sale_id" : "repair_id") + ", user_id, reference_note) VALUES (?, 'CASH', ?, 'IN', ?, ?, 'Partial Settlement')";

            if (type .equals("SALE")) {
                return CrudUtil.execute(insertTrans,
                        "SALE_PAYMENT",
                        entity.getAmount(),
                        entity.getSale_id(),
                        entity.getUser_id()
                );
            } else {
                return CrudUtil.execute(insertTrans,
                        "REPAIR_PAYMENT",
                        entity.getAmount(),
                        entity.getRepair_id(),
                        entity.getUser_id()
                );
            }
    }

    @Override
    public int getTransactionCount(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TransactionRecord WHERE transaction_date BETWEEN ? AND ?";
        return GenerateReports.getCountByDateRange(sql, from, to);
    }

    @Override
    public double getTodayIncome(LocalDate today) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM TransactionRecord WHERE flow='IN' AND DATE(transaction_date) = ?";
        ResultSet rs1 = CrudUtil.execute(sql, today);
        double income = 0;
        if (rs1.next()) income = rs1.getDouble(1);
        rs1.close();
        return income;
    }

    @Override
    public List<TransactionRecord> getAll() throws SQLException {
        return List.of();
    }

    @Override
    public boolean save(TransactionRecord entity) throws SQLException {
        String sqlTrans = "";

        if(entity.getTransaction_type().equals("SALE_PAYMENT") || entity.getSale_id() == 0) {
            sqlTrans = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, sale_id, user_id, customer_id, reference_note) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }else{
            sqlTrans = "INSERT INTO TransactionRecord (transaction_type, payment_method, amount, flow, repair_id, user_id, customer_id, reference_note) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }
        return CrudUtil.execute(sqlTrans,
                entity.getTransaction_type(),
                entity.getTransaction_method(),
                entity.getAmount(),
                entity.getFlow(),
                entity.getSale_id() == 0 ? entity.getRepair_id() : entity.getSale_id(),
                entity.getUser_id(),
                entity.getCustomer_id() == 0 ? null : entity.getCustomer_id(),
                entity.getReference_note()
        );
    }

    @Override
    public boolean update(TransactionRecord entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    @Override
    public TransactionRecord search(int id) throws SQLException {
        return null;
    }
}
