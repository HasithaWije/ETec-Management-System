package lk.ijse.etecmanagementsystem.bo;

import lk.ijse.etecmanagementsystem.dao.RepairJobDAOImpl;
import lk.ijse.etecmanagementsystem.dao.SalesDAOImpl;
import lk.ijse.etecmanagementsystem.dao.TransactionRecordDAOImpl;
import lk.ijse.etecmanagementsystem.dao.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.db.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionBOImpl {
    public boolean settlePayment(String type, int id, double amount, int userId, String newPaymentStatus) throws SQLException {
        TransactionRecordDAOImpl transactionDAO = new TransactionRecordDAOImpl();
        SalesDAOImpl salesDAO = new SalesDAOImpl();
        RepairJobDAOImpl repairDAO = new RepairJobDAOImpl();

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start Transaction

            TransactionRecord transaction = new TransactionRecord();
            transaction.setTransaction_type(type.equals("SALE") ? "SALE_PAYMENT" : "REPAIR_PAYMENT");
            transaction.setTransaction_method("CASH");
            transaction.setAmount(amount);
            transaction.setFlow("IN");
            transaction.setUser_id(userId);
            transaction.setReference_note("Partial Settlement");
            transaction.setSale_id(type.equals("SALE") ? id : 0);
            transaction.setRepair_id(type.equals("REPAIR") ? id : 0);

            boolean transactionSaved = transactionDAO.settleTransaction(transaction, type);
            if(!transactionSaved){
                conn.rollback();
                return false;
            }

            boolean isFullySettled;

            if (type.equals("SALE")) {
                isFullySettled = salesDAO.updateSalePayment(id, amount, newPaymentStatus);
            } else {
                isFullySettled = repairDAO.updateRepairPayment(amount, newPaymentStatus, id);
            }

            if (!isFullySettled){
                conn.rollback();
                return false;
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
