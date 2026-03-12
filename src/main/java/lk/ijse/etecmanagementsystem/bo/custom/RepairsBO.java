package lk.ijse.etecmanagementsystem.bo.custom;

import lk.ijse.etecmanagementsystem.bo.SuperBO;
import lk.ijse.etecmanagementsystem.dao.custom.impl.RepairSalesDAOImpl;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.*;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.entity.RepairItem;
import lk.ijse.etecmanagementsystem.entity.RepairJob;
import lk.ijse.etecmanagementsystem.entity.Sales;
import lk.ijse.etecmanagementsystem.entity.TransactionRecord;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static lk.ijse.etecmanagementsystem.controller.RepairDashboardController.getRepairPartTMS;

public interface RepairsBO extends SuperBO {
    List<RepairJobDTO> getAllRepairJobs() throws SQLException;

    boolean updateRepairJobDetails(int repairId, String intake, String diagnosis, String resolution,
                                   double laborCost, double partsCost, double totalAmount,
                                   List<ProductItemDTO> activeParts,
                                   List<ProductItemDTO> returnedParts) throws SQLException;

    boolean completeCheckout(int repairId, int customerId, int userId,
                             double totalAmount, double discount, double partsTotal, double paidAmount, String paymentMethod, String serialNumber) throws SQLException;

    ProductItemDTO getProductItem(int itemId) throws SQLException;

    boolean saveRepairJob(RepairJobDTO repairJobDTO) throws SQLException;

    int getLastInsertedRepairId() throws SQLException;

    boolean updateRepairJob(RepairJobDTO repairJobDTO) throws SQLException;

    boolean updateStatus(int repairId, RepairStatus newStatus) throws SQLException;

    boolean deleteRepairJob(int repairId) throws SQLException;

    List<CustomDTO> getPendingRepairs() throws SQLException;

    List<CustomDTO> getUsedParts(int repairId) throws SQLException;
}
