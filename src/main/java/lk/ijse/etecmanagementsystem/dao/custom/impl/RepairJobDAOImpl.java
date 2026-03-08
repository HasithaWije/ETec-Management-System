package lk.ijse.etecmanagementsystem.dao.custom.impl;

import javafx.scene.chart.XYChart;
import lk.ijse.etecmanagementsystem.dao.custom.RepairJobDAO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.RepairJob;
import lk.ijse.etecmanagementsystem.util.GenerateReports;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RepairJobDAOImpl implements RepairJobDAO {

    @Override
    public List<RepairJob> getAll() throws SQLException {
        List<RepairJob> list = new ArrayList<>();

        String sql = "SELECT * FROM RepairJob ORDER BY date_in DESC";


        ResultSet resultSet = CrudUtil.execute(sql);

        while (resultSet.next()) {

            RepairJob entity = getEntity(resultSet);

            list.add(entity);
        }

        resultSet.close();
        return list;
    }

    @Override
    public RepairJob search(int repair_id) throws SQLException {
        String sql = "SELECT * FROM RepairJob WHERE repair_id = ?";
        ResultSet resultSet = CrudUtil.execute(sql, repair_id);

        if (resultSet.next()) {
            RepairJob entity = getEntity(resultSet);
            resultSet.close();
            return entity;
        } else {
            resultSet.close();
            return null; // Not found
        }
    }

    @Override
    public boolean save(RepairJob entity) throws SQLException {
        String sql = "INSERT INTO RepairJob " +
                "(cus_id, user_id, device_name, device_sn, problem_desc, status, date_in, payment_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Date date;
        if (entity.getDate_in() != null) {
            date = new java.sql.Timestamp(entity.getDate_in().getTime());
        } else {
            date = new java.sql.Timestamp(System.currentTimeMillis());
        }
        return CrudUtil.execute(
                sql,
                entity.getCus_id(),
                entity.getUser_id(),
                entity.getDevice_name(),
                entity.getDevice_sn(),
                entity.getProblem_desc(),
                entity.getStatus(),
                date,
                "PENDING"
        );
    }

    @Override
    public boolean update(RepairJob entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int repairId) throws SQLException {
        String sql = "DELETE FROM RepairJob WHERE repair_id=?";

        return CrudUtil.execute(sql, repairId);
    }

    @Override
    public int getLastInsertedRepairId() throws SQLException {
        String idQuery = "SELECT LAST_INSERT_ID() AS id FROM RepairJob";
        ResultSet rs = CrudUtil.execute(idQuery);
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Failed to retrieve Repair ID");
        }
    }

    @Override
    public boolean updateRepairJob(RepairJob entity) throws SQLException {
        String sql = "UPDATE RepairJob SET cus_id=?, device_name=?, device_sn=?, problem_desc=? WHERE repair_id=?";

        return CrudUtil.execute(
                sql,
                entity.getCus_id(),
                entity.getDevice_name(),
                entity.getDevice_sn(),
                entity.getProblem_desc(),
                entity.getRepair_id()
        );
    }

    @Override
    public boolean updateRepairPayment(double amount, double totalAmount, double discount, String paymentStatus, int repairId) throws SQLException {
        String updateSql = "UPDATE RepairJob SET paid_amount = paid_amount + ?, total_amount = ?, discount = ?, payment_status = ? " +
                "WHERE repair_id = ?";
        return CrudUtil.execute(updateSql, amount, totalAmount, discount, paymentStatus, repairId);
    }

    @Override
    public boolean updateRepairCosts(RepairJob entity) throws SQLException {
        String sqlJob = "UPDATE RepairJob SET problem_desc=?, diagnosis_desc=?, repair_results=?, " +
                "labor_cost=?, parts_cost=?, total_amount=? WHERE repair_id=?";

        return CrudUtil.execute(
                sqlJob,
                entity.getProblem_desc(),
                entity.getDiagnosis_desc(),
                entity.getRepair_results(),
                entity.getLabor_cost(),
                entity.getParts_cost(),
                entity.getTotal_amount(),
                entity.getRepair_id()
        );
    }

    @Override
    public boolean updateStatus(int repairId, RepairStatus newStatus) throws SQLException {
        String sql = "UPDATE RepairJob SET status = ? WHERE repair_id = ?";

        return CrudUtil.execute(sql, newStatus.name(), repairId);
    }

    @Override
    public boolean updateDateOut(String paymentStatus, int repairId) throws SQLException {
        String sqlUpdateJob = "UPDATE RepairJob SET status='DELIVERED', date_out=NOW(), payment_status=? WHERE repair_id=?";
        return CrudUtil.execute(sqlUpdateJob, paymentStatus, repairId);
    }

    @Override
    public int getRepairCount(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM RepairJob WHERE date_in BETWEEN ? AND ?";
        return GenerateReports.getCountByDateRange(sql, from, to);
    }

    @Override
    public boolean isRepairExist(String repairId) throws SQLException {
        String sql = "SELECT repair_id FROM RepairJob WHERE repair_id = ?";
        return GenerateReports.checkIdExists(sql, repairId);
    }

    @Override
    public XYChart.Series<String, Number> getRepairChartData() throws SQLException {
        String sqlRepairs = "SELECT DATE(date_in) as d, COUNT(*) as c FROM RepairJob " +
                "WHERE date_in >= DATE(NOW()) - INTERVAL 7 DAY AND status = 'DELIVERED'" +
                "GROUP BY DATE(date_in) ORDER BY DATE(date_in)";

        XYChart.Series<String, Number> seriesRepairs = new XYChart.Series<>();
        seriesRepairs.setName("Repairs");

        ResultSet rs2 = CrudUtil.execute(sqlRepairs);

        while (rs2.next()) {
            seriesRepairs.getData().add(new XYChart.Data<>(rs2.getString("d"), rs2.getInt("c")));
        }
        return seriesRepairs;
    }

    @Override
    public int getActiveRepairCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM RepairJob WHERE status NOT IN ('COMPLETED', 'DELIVERED', 'CANCELLED')";
        int repairs = 0;
        ResultSet rs = CrudUtil.execute(sql);
        if (rs.next()) repairs = rs.getInt(1);
        rs.close();
        return repairs;
    }

    public RepairJob getEntity(ResultSet resultSet) throws SQLException {
        return new RepairJob(
                resultSet.getInt("repair_id"),
                resultSet.getInt("cus_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("device_name"),
                resultSet.getString("device_sn"),
                resultSet.getString("problem_desc"),
                resultSet.getString("diagnosis_desc"),
                resultSet.getString("repair_results"),
                resultSet.getString("status"),
                resultSet.getTimestamp("date_in"),
                resultSet.getTimestamp("date_out"),
                resultSet.getDouble("labor_cost"),
                resultSet.getDouble("parts_cost"),
                resultSet.getDouble("discount"),
                resultSet.getDouble("total_amount"),
                resultSet.getDouble("paid_amount"),
                resultSet.getString("payment_status")
        );
    }
}
