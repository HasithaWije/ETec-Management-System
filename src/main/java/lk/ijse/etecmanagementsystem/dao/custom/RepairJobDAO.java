package lk.ijse.etecmanagementsystem.dao.custom;

import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.entity.RepairJob;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.sql.SQLException;
import java.util.List;

public interface RepairJobDAO {
    List<RepairJob> getAll() throws SQLException;

    RepairJob search(int repair_id) throws SQLException;

    boolean saveRepairJob(RepairJob entity) throws SQLException;

    int getLastInsertedRepairId() throws SQLException;

    boolean updateRepairJob(RepairJob entity) throws SQLException;

    boolean updateRepairPayment(double amount, double totalAmount, double discount, String paymentStatus, int repairId) throws SQLException;

    boolean updateRepairCosts(RepairJobDTO dto) throws SQLException;

    boolean updateStatus(int repairId, RepairStatus newStatus) throws SQLException;

    boolean updateDateOut(String paymentStatus, int repairId) throws SQLException;

    boolean deleteRepairJob(int repairId) throws SQLException;
}

