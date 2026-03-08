package lk.ijse.etecmanagementsystem.dao.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.RepairItemDAO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.RepairItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RepairItemDAOImpl implements RepairItemDAO {

    @Override
    public List<RepairItem> getAll() throws SQLException {
        return List.of();
    }

    @Override
    public boolean save(RepairItem entity) throws SQLException {
        String sqlInsertLink = "INSERT INTO RepairItem (repair_id, item_id, unit_price) VALUES (?, ?, ?)";
        return CrudUtil.execute(
                sqlInsertLink,
                entity.getRepair_id(),
                entity.getItem_id(),
                entity.getUnit_price()
        );
    }

    @Override
    public boolean update(RepairItem entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    @Override
    public RepairItem search(int id) throws SQLException {
        String sql = "SELECT * FROM RepairItem WHERE repair_id = ?";
        ResultSet rs = CrudUtil.execute(sql, id);
        if (rs.next()) {
            return new RepairItem(
                    rs.getInt("id"),
                    rs.getInt("repair_id"),
                    rs.getInt("item_id"),
                    rs.getDouble("unit_price")
            );
        }
        return null;
    }

    @Override
    public int getRepairItemId(int repairId, int itemId) throws SQLException {
        String sqlCheck = "SELECT id FROM RepairItem WHERE repair_id=? AND item_id=?";
        ResultSet rs =  CrudUtil.execute(sqlCheck, repairId, itemId);
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1; // Not found
    }

    @Override
    public boolean deleteRepairItem(int repairId, int itemId) throws SQLException {
        String sqlDeleteLink = "DELETE FROM RepairItem WHERE repair_id=? AND item_id=?";
        return CrudUtil.execute(sqlDeleteLink, repairId, itemId);
    }
}
