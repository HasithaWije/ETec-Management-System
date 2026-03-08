package lk.ijse.etecmanagementsystem.dao.custom;

import lk.ijse.etecmanagementsystem.dao.CrudDAO;
import lk.ijse.etecmanagementsystem.dto.RepairItemDTO;
import lk.ijse.etecmanagementsystem.entity.RepairItem;

import java.sql.SQLException;

public interface RepairItemDAO extends CrudDAO<RepairItem> {
    int getRepairItemId(int repairId, int itemId) throws SQLException;

    boolean deleteRepairItem(int repairId, int itemId) throws SQLException;
}

