package lk.ijse.etecmanagementsystem.bo.custom;

import lk.ijse.etecmanagementsystem.bo.SuperBO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.sql.SQLException;

public interface RepairsBO extends SuperBO {
    ProductItemDTO getProductItem(int itemId) throws SQLException;

    boolean saveRepairJob(RepairJobDTO repairJobDTO) throws SQLException;

    int getLastInsertedRepairId() throws SQLException;

    boolean updateRepairJob(RepairJobDTO repairJobDTO) throws SQLException;

    boolean updateStatus(int repairId, RepairStatus newStatus) throws SQLException;

    boolean deleteRepairJob(int repairId) throws SQLException;
}
