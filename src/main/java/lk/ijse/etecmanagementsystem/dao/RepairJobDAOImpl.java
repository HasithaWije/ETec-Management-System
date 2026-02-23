package lk.ijse.etecmanagementsystem.dao;

import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;
import lk.ijse.etecmanagementsystem.util.CrudUtil;
import lk.ijse.etecmanagementsystem.util.PaymentStatus;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepairJobDAOImpl {

    public List<RepairJobDTO> getAllRepairJobs() throws SQLException {
        List<RepairJobDTO> list = new ArrayList<>();

        String sql = "SELECT * FROM RepairJob ORDER BY date_in DESC";


        ResultSet resultSet = CrudUtil.execute(sql);

        while (resultSet.next()) {
            RepairJobDTO dto = new RepairJobDTO(
                    resultSet.getInt("repair_id"),
                    resultSet.getInt("cus_id"),
                    resultSet.getInt("user_id"),
                    resultSet.getString("device_name"),
                    resultSet.getString("device_sn"),
                    resultSet.getString("problem_desc"),
                    resultSet.getString("diagnosis_desc"),
                    resultSet.getString("repair_results"),
                    RepairStatus.valueOf(resultSet.getString("status")),
                    resultSet.getTimestamp("date_in"),
                    resultSet.getTimestamp("date_out"),
                    resultSet.getDouble("labor_cost"),
                    resultSet.getDouble("parts_cost"),
                    resultSet.getDouble("total_amount"),
                    resultSet.getDouble("paid_amount"),
                    resultSet.getDouble("discount"),
                    PaymentStatus.valueOf(resultSet.getString("payment_status"))
            );

            list.add(dto);
        }

        resultSet.close();
        return list;
    }



    public boolean updateRepairPayment(double amount, String paymentStatus, int repairId) throws SQLException {
        String updateSql = "UPDATE RepairJob SET paid_amount = paid_amount + ?, payment_status = ? WHERE repair_id = ?";
        return CrudUtil.execute(updateSql, amount, paymentStatus, repairId);
    }
}
