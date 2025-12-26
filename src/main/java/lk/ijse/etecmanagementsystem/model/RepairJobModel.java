package lk.ijse.etecmanagementsystem.model;




import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.util.PaymentStatus;
import lk.ijse.etecmanagementsystem.util.ProductCondition;
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
        String sql = "SELECT r.repair_id, r.cus_id, r.user_id, r.device_name, r.device_sn, " +
                "r.problem_desc, r.diagnosis_desc, r.repair_results, " + // <--- Added here
                "r.status, r.date_in, r.date_out, r.labor_cost, r.parts_cost, r.total_amount, r.payment_status, " +
                "c.name AS cus_name, c.number AS cus_contact " +
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
                    resultSet.getString("diagnosis_desc"), // <--- Get New Col
                    resultSet.getString("repair_results"), // <--- Get New Col
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

    // 2. NEW METHOD: To save the text from the 3 Tabs
    public boolean updateRepairDescriptions(int repairId, String diagnosis, String results) throws SQLException {
        String sql = "UPDATE RepairJob SET diagnosis_desc = ?, repair_results = ? WHERE repair_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1, diagnosis);
        pstm.setString(2, results);
        pstm.setInt(3, repairId);
        return pstm.executeUpdate() > 0;
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
    // 1. GET USED PARTS (Load from RepairItem table)
    public List<RepairPartTM> getUsedParts(int repairId) throws SQLException {
        List<RepairPartTM> list = new ArrayList<>();

        // JOIN: RepairItem -> ProductItem -> Product (To get Name & Price)
        String sql = "SELECT pi.item_id, p.name, pi.serial_number, p.p_condition, p.sell_price " +
                "FROM RepairItem ri " +
                "JOIN ProductItem pi ON ri.item_id = pi.item_id " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE ri.repair_id = ?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setInt(1, repairId);
        ResultSet rs = pstm.executeQuery();

        while (rs.next()) {


            list.add(new RepairPartTM(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getString("serial_number"),
                    fromConditionString(rs.getString("p_condition")),
                    rs.getDouble("sell_price")
            ));
        }
        return list;
    }

    // 2. SAVE JOB & PARTS (Transactional)
    public boolean updateRepairJobDetails(int repairId, String intake, String diagnosis, String resolution,
                                          double laborCost, double partsCost, double totalAmount,
                                          List<RepairPartTM> activeParts,
                                          List<RepairPartTM> returnedParts) throws SQLException {

        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false); // START TRANSACTION

            // A. Update RepairJob Text & Costs
            String sqlJob = "UPDATE RepairJob SET problem_desc=?, diagnosis_desc=?, repair_results=?, " +
                    "labor_cost=?, parts_cost=?, total_amount=? WHERE repair_id=?";
            PreparedStatement pstmJob = connection.prepareStatement(sqlJob);
            pstmJob.setString(1, intake);
            pstmJob.setString(2, diagnosis);
            pstmJob.setString(3, resolution);
            pstmJob.setDouble(4, laborCost);
            pstmJob.setDouble(5, partsCost);
            pstmJob.setDouble(6, totalAmount);
            pstmJob.setInt(7, repairId);
            pstmJob.executeUpdate();

            // B. ADD NEW PARTS
            // We check if the link already exists to avoid duplicates
            String sqlCheck = "SELECT id FROM RepairItem WHERE repair_id=? AND item_id=?";
            String sqlInsertLink = "INSERT INTO RepairItem (repair_id, item_id) VALUES (?, ?)";
            String sqlMarkSold = "UPDATE ProductItem SET status='SOLD', sold_date=NOW() WHERE item_id=?";

            PreparedStatement pstmCheck = connection.prepareStatement(sqlCheck);
            PreparedStatement pstmLink = connection.prepareStatement(sqlInsertLink);
            PreparedStatement pstmSold = connection.prepareStatement(sqlMarkSold);

            for (RepairPartTM part : activeParts) {
                // Check duplicate
                pstmCheck.setInt(1, repairId);
                pstmCheck.setInt(2, part.getItemId());
                if (!pstmCheck.executeQuery().next()) {
                    // 1. Insert Link in RepairItem
                    pstmLink.setInt(1, repairId);
                    pstmLink.setInt(2, part.getItemId());
                    pstmLink.addBatch();

                    // 2. Mark Stock as SOLD in ProductItem
                    pstmSold.setInt(1, part.getItemId());
                    pstmSold.addBatch();
                }
            }
            pstmLink.executeBatch();
            pstmSold.executeBatch();

            // C. REMOVE RETURNED PARTS (Restock)
            if (!returnedParts.isEmpty()) {
                String sqlDeleteLink = "DELETE FROM RepairItem WHERE repair_id=? AND item_id=?";
                String sqlRestock = "UPDATE ProductItem SET status='AVAILABLE', sold_date=NULL WHERE item_id=?";

                PreparedStatement pstmDel = connection.prepareStatement(sqlDeleteLink);
                PreparedStatement pstmStock = connection.prepareStatement(sqlRestock);

                for (RepairPartTM part : returnedParts) {
                    // 1. Remove Link
                    pstmDel.setInt(1, repairId);
                    pstmDel.setInt(2, part.getItemId());
                    pstmDel.addBatch();

                    // 2. Mark Stock as AVAILABLE
                    pstmStock.setInt(1, part.getItemId());
                    pstmStock.addBatch();
                }
                pstmDel.executeBatch();
                pstmStock.executeBatch();
            }

            connection.commit(); // COMMIT
            return true;

        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
        }
    }

    private ProductCondition fromConditionString(String s) {
        if (s == null) return ProductCondition.BOTH;
        try {
            if (s.equals("USED")) {
                return ProductCondition.USED;
            } else if (s.equals("BRAND NEW")) {
                return ProductCondition.BRAND_NEW;
            }
            return ProductCondition.BOTH;
        } catch (IllegalArgumentException ex) {
            return ProductCondition.BOTH; // unknown condition value
        }
    }
}