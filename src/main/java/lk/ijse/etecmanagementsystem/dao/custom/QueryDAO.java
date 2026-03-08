package lk.ijse.etecmanagementsystem.dao.custom;

import javafx.collections.ObservableList;
import lk.ijse.etecmanagementsystem.dto.CustomDTO;
import lk.ijse.etecmanagementsystem.dto.tm.PendingRepairTM;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;

import java.sql.SQLException;
import java.util.List;

public interface QueryDAO {
    ObservableList<PendingRepairTM> getPendingRepairs() throws SQLException;

    List<RepairPartTM> getUsedParts(int repairId) throws SQLException;

    CustomDTO getProductItem(int itemId) throws SQLException;
}

