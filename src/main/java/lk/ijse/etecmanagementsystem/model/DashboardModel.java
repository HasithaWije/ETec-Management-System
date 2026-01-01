package lk.ijse.etecmanagementsystem.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.tm.DashboardTM;
import lk.ijse.etecmanagementsystem.dto.tm.DebtTM;
import lk.ijse.etecmanagementsystem.dto.tm.UrgentRepairTM;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardModel {

    public DashboardTM getDashboardStats() throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        LocalDate today = LocalDate.now();

        double income = 0;
        int repairs = 0;
        int stock = 0;
        double debts = 0;

        // A. Today's Income (Only 'IN' transactions)
        ResultSet rs1 = stmt.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM TransactionRecord WHERE flow='IN' AND DATE(transaction_date) = '" + today + "'");
        if (rs1.next()) income = rs1.getDouble(1);

        // B. Active Repairs (Not Completed/Delivered/Cancelled)
        ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM RepairJob WHERE status NOT IN ('COMPLETED', 'DELIVERED', 'CANCELLED')");
        if (rs2.next()) repairs = rs2.getInt(1);

        // C. Low Stock (Items with qty < 5)
        ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM Product WHERE qty < 5");
        if (rs3.next()) stock = rs3.getInt(1);

        // D. Total Pending Payments (Sales Due + Repair Due)
        String sqlDebts = "SELECT " +
                "(SELECT COALESCE(SUM(grand_total - paid_amount),0) FROM Sales WHERE payment_status != 'PAID') + " +
                "(SELECT COALESCE(SUM(total_amount - paid_amount),0) FROM RepairJob WHERE payment_status != 'PAID' AND status != 'CANCELLED')";
        ResultSet rs4 = stmt.executeQuery(sqlDebts);
        if (rs4.next()) debts = rs4.getDouble(1);

        stmt.close();
        return new DashboardTM(income, repairs, stock, debts);
    }

    public ObservableList<UrgentRepairTM> getUrgentRepairs() throws SQLException {
        ObservableList<UrgentRepairTM> list = FXCollections.observableArrayList();
        String sql = "SELECT repair_id, device_name, status, DATE(date_in) as d_in FROM RepairJob " +
                "WHERE status IN ('PENDING', 'DIAGNOSIS', 'WAITING_PARTS') " +
                "ORDER BY date_in ASC LIMIT 15";

        Connection conn = DBConnection.getInstance().getConnection();
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            list.add(new UrgentRepairTM(
                    rs.getInt("repair_id"),
                    rs.getString("device_name"),
                    rs.getString("status"),
                    rs.getString("d_in")
            ));
        }
        return list;
    }

    public ObservableList<DebtTM> getUnpaidDebts() throws SQLException {
        ObservableList<DebtTM> list = FXCollections.observableArrayList();
        // Uses UNION ALL to combine Sales and Repair tables
        String sql = "SELECT 'SALE' as type, s.sale_id as ref_id, c.name, (s.grand_total - s.paid_amount) as due " +
                "FROM Sales s LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "WHERE s.payment_status != 'PAID' AND s.description LIKE 'Point of Sale Transaction'" +
                "UNION ALL " +
                "SELECT 'REPAIR' as type, r.repair_id as ref_id, c.name, (r.total_amount - r.paid_amount) as due " +
                "FROM RepairJob r JOIN Customer c ON r.cus_id = c.cus_id " +
                "WHERE r.payment_status != 'PAID' AND r.status = 'DELIVERED'";

        Connection conn = DBConnection.getInstance().getConnection();
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            list.add(new DebtTM(
                    rs.getInt("ref_id"), // The ID (Sale ID or Repair ID)
                    rs.getString("type"),
                    rs.getString("name"),
                    rs.getDouble("due")
            ));
        }
        return list;
    }

    public XYChart.Series<String, Number> getSalesChartData() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        String sql = "SELECT DATE(transaction_date) as d, SUM(amount) as total FROM TransactionRecord " +
                "WHERE flow='IN' AND transaction_date >= DATE(NOW()) - INTERVAL 7 DAY " +
                "GROUP BY DATE(transaction_date) ORDER BY DATE(transaction_date)";

        Connection conn = DBConnection.getInstance().getConnection();
        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            series.getData().add(new XYChart.Data<>(rs.getString("d"), rs.getDouble("total")));
        }
        return series;
    }

    public List<XYChart.Series<String, Number>> getTrafficChartData() throws SQLException {
        List<XYChart.Series<String, Number>> allSeries = new ArrayList<>();

        XYChart.Series<String, Number> seriesSales = new XYChart.Series<>();
        seriesSales.setName("Sales");

        XYChart.Series<String, Number> seriesRepairs = new XYChart.Series<>();
        seriesRepairs.setName("Repairs");

        Connection conn = DBConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();

        String sqlSales = "SELECT DATE(sale_date) as d, COUNT(*) as c FROM Sales " +
                "WHERE sale_date >= DATE(NOW()) - INTERVAL 7 DAY " +
                "GROUP BY DATE(sale_date) ORDER BY DATE(sale_date)";
        ResultSet rs1 = stmt.executeQuery(sqlSales);
        while (rs1.next()) {
            seriesSales.getData().add(new XYChart.Data<>(rs1.getString("d"), rs1.getInt("c")));
        }

        String sqlRepairs = "SELECT DATE(date_in) as d, COUNT(*) as c FROM RepairJob " +
                "WHERE date_in >= DATE(NOW()) - INTERVAL 7 DAY " +
                "GROUP BY DATE(date_in) ORDER BY DATE(date_in)";
        ResultSet rs2 = stmt.executeQuery(sqlRepairs);
        while (rs2.next()) {
            seriesRepairs.getData().add(new XYChart.Data<>(rs2.getString("d"), rs2.getInt("c")));
        }

        allSeries.add(seriesSales);
        allSeries.add(seriesRepairs);
        return allSeries;
    }
}