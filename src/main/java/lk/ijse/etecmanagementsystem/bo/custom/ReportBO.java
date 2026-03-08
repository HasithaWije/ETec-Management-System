package lk.ijse.etecmanagementsystem.bo.custom;

import lk.ijse.etecmanagementsystem.bo.SuperBO;

import java.sql.SQLException;
import java.time.LocalDate;

public interface ReportBO extends SuperBO {

    int getSalesCount(LocalDate from, LocalDate to) throws SQLException;

    boolean isSaleExist(String saleId) throws SQLException;

    int getRepairCount(LocalDate from, LocalDate to) throws SQLException;

    boolean isRepairExist(String repairId) throws SQLException;

    int getTransactionCount(LocalDate from, LocalDate to) throws SQLException;

    int getInventoryCount() throws SQLException;

    int getSupplierCount() throws SQLException;

    int getCustomerCount() throws SQLException;
}
