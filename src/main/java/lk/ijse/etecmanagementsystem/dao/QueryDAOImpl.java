package lk.ijse.etecmanagementsystem.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lk.ijse.etecmanagementsystem.dto.tm.PendingRepairTM;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryDAOImpl {
    public ObservableList<PendingRepairTM> getPendingRepairs() throws SQLException {
        String repairSql = "SELECT r.repair_id, r.device_name, c.name, r.total_amount, r.paid_amount FROM RepairJob r JOIN Customer c ON r.cus_id = c.cus_id WHERE r.payment_status IN ('PENDING','PARTIAL') AND r.status IN ('DELIVERED')";


        ResultSet rs = CrudUtil.execute(repairSql);
        ObservableList<PendingRepairTM> pendingRepairsList = FXCollections.observableArrayList();

        while (rs.next()) {
            double balanceDue = rs.getDouble("total_amount") - rs.getDouble("paid_amount");


//            (int repairId, String device, String customerName, double balanceDue)
            pendingRepairsList.add(new PendingRepairTM(rs.getInt("repair_id"),
                    rs.getString("device_name"),
                    rs.getString("name"),
                    balanceDue));
        }
        rs.close();
        return pendingRepairsList;
    }

}
