package lk.ijse.etecmanagementsystem.bo.custom.impl;

import lk.ijse.etecmanagementsystem.bo.custom.TransactionBO;
import lk.ijse.etecmanagementsystem.dao.DAOFactory;
import lk.ijse.etecmanagementsystem.dao.custom.*;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.dto.TransactionDTO;
import lk.ijse.etecmanagementsystem.entity.RepairJob;
import lk.ijse.etecmanagementsystem.entity.TransactionRecord;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.PaymentStatus;
import lk.ijse.etecmanagementsystem.dto.RepairStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionBOImpl implements TransactionBO {

    UserDAO userDAO = (UserDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.USER);
    RepairJobDAO repairDAO = (RepairJobDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.REPAIR_JOB);
    SalesDAO salesDAO = (SalesDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.SALES);
    TransactionRecordDAO transactionRecordDAO = (TransactionRecordDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.TRANSACTION_RECORD);

    @Override
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

            boolean transactionSaved = transactionRecordDAO.settleTransaction(transaction, type);
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

    @Override
    public List<TransactionDTO> getAllTransactions(Date dpFromDate, Date dpToDate) throws SQLException {
        List<TransactionRecord> entities = transactionRecordDAO.getAllTransactions(dpFromDate, dpToDate);
        List<TransactionDTO> dtos = new ArrayList<>();
        for(TransactionRecord entity : entities) {
            String userName = userDAO.search(entity.getUser_id()).getName();
            dtos.add(new TransactionDTO(
                    String.valueOf(entity.getTransaction_id()),
                    entity.getTransaction_date(),
                    entity.getTransaction_type(),
                    entity.getTransaction_method(),
                    entity.getAmount(),
                    entity.getFlow(),
                    entity.getSale_id(),
                    entity.getRepair_id(),
                    entity.getCustomer_id(),
                    entity.getUser_id(),
                    entity.getReference_note(),
                    userName
            ));
        }
        return dtos;
    }

    @Override
    public boolean saveManualTransaction(String type, double amount, String method, String note, int userId) throws SQLException {
        return transactionRecordDAO.saveManualTransaction(type, amount, method, note, userId);
    }

    @Override
    public double[] getDashboardStats(Date fromDate, Date toDate) throws SQLException {
        return transactionRecordDAO.getDashboardStats(fromDate, toDate);
    }
}
