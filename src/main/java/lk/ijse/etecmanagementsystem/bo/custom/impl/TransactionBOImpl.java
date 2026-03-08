package lk.ijse.etecmanagementsystem.bo.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.impl.RepairJobDAOImpl;
import lk.ijse.etecmanagementsystem.dao.custom.impl.SalesDAOImpl;
import lk.ijse.etecmanagementsystem.dao.custom.impl.TransactionRecordDAOImpl;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.entity.RepairJob;
import lk.ijse.etecmanagementsystem.entity.Sales;
import lk.ijse.etecmanagementsystem.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.util.PaymentStatus;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionBOImpl {

    TransactionRecordDAOImpl transactionDAO = new TransactionRecordDAOImpl();
    SalesDAOImpl salesDAO = new SalesDAOImpl();
    RepairJobDAOImpl repairDAO = new RepairJobDAOImpl();

    public boolean settlePayment(String type, int id, double amount, int userId, String newPaymentStatus) throws SQLException {


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
            if (!transactionSaved) {
                conn.rollback();
                return false;
            }

            double currentPaidAmount;
            boolean isSettled;

            if (type.equals("SALE")) {

                currentPaidAmount = salesDAO.search(id).getPaid_amount();
                isSettled = salesDAO.updateSalePayment(id, currentPaidAmount + amount, newPaymentStatus);
            } else {
                RepairJob entity = repairDAO.search(id);
                RepairJobDTO repairJobDTO = new RepairJobDTO(
                        entity.getRepair_id(),
                        entity.getCus_id(),
                        entity.getUser_id(),
                        entity.getDevice_name(),
                        entity.getDevice_sn(),
                        entity.getProblem_desc(),
                        entity.getDiagnosis_desc(),
                        entity.getRepair_results(),
                        RepairStatus.valueOf(entity.getStatus()),
                        entity.getDate_in(),
                        entity.getDate_out(),
                        entity.getLabor_cost(),
                        entity.getParts_cost(),
                        entity.getTotal_amount(),
                        entity.getPaid_amount(),
                        entity.getDiscount(),
                        PaymentStatus.valueOf(entity.getPayment_status()));

                currentPaidAmount = repairJobDTO.getPaidAmount();
                double totalAmount = repairJobDTO.getTotalAmount();
                double discount = repairJobDTO.getDiscount();
                isSettled = repairDAO.updateRepairPayment(currentPaidAmount + amount, totalAmount, discount, newPaymentStatus, id);
            }

            if (!isSettled) {
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
