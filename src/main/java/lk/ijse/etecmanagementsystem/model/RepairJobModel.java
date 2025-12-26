package lk.ijse.etecmanagementsystem.model;




import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;
import lk.ijse.etecmanagementsystem.util.PaymentStatus;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepairJobModel {

    public List<RepairJobTM> getAllRepairJobs() throws SQLException {
        List<RepairJobTM> list = new ArrayList<>();

        // We assume you have a DBConnection class
        Connection connection = DBConnection.getInstance().getConnection();

        // JOIN Query to get Customer details along with Repair Job
        String sql = "SELECT r.*, c.name AS cus_name, c.number AS cus_contact " +
                "FROM RepairJob r " +
                "JOIN Customer c ON r.cus_id = c.cus_id " +
                "ORDER BY r.date_in DESC";

        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet resultSet = pstm.executeQuery();

        while (resultSet.next()) {
            // 1. Create the DTO from the Result Set
            RepairJobDTO dto = new RepairJobDTO(
                    resultSet.getInt("repair_id"),
                    resultSet.getInt("cus_id"),
                    resultSet.getInt("user_id"),
                    resultSet.getString("device_name"),
                    resultSet.getString("device_sn"),
                    resultSet.getString("problem_desc"),
                    RepairStatus.valueOf(resultSet.getString("status")),
                    resultSet.getTimestamp("date_in"),
                    resultSet.getTimestamp("date_out"),
                    resultSet.getDouble("labor_cost"),
                    resultSet.getDouble("parts_cost"),
                    resultSet.getDouble("total_amount"),
                    PaymentStatus.valueOf(resultSet.getString("payment_status"))
            );

            // 2. Extract Customer Info (Not in DTO, but needed for TM)
            String cusName = resultSet.getString("cus_name");
            String cusContact = resultSet.getString("cus_contact");

            // 3. Create the TM
            RepairJobTM tm = new RepairJobTM(dto, cusName, cusContact);
            list.add(tm);
        }

        return list;
    }

    // Add update methods here later (e.g., updateStatus)
    public boolean updateStatus(int repairId, RepairStatus newStatus) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        String sql = "UPDATE RepairJob SET status = ? WHERE repair_id = ?";
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1, newStatus.name());
        pstm.setInt(2, repairId);
        return pstm.executeUpdate() > 0;
    }
}