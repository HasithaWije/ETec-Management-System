package lk.ijse.etecmanagementsystem.dao.custom.impl;

import javafx.scene.chart.XYChart;
import lk.ijse.etecmanagementsystem.dao.custom.SalesDAO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.Sales;
import lk.ijse.etecmanagementsystem.reports.GenerateReports;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;



public class SalesDAOImpl implements SalesDAO {

    @Override
    public List<Sales> getAll() throws SQLException {
        List<Sales> salesList = new ArrayList<>();

        String sql = "SELECT * FROM Sales s ORDER BY s.sale_date DESC";

        ResultSet resultSet = CrudUtil.execute(sql);
        while (resultSet.next()) {
            salesList.add(new Sales(
                    resultSet.getInt("sale_id"),
                    resultSet.getInt("customer_id"),
                    resultSet.getInt("user_id"),
                    resultSet.getTimestamp("sale_date"),
                    resultSet.getDouble("sub_total"),
                    resultSet.getDouble("discount"),
                    resultSet.getDouble("grand_total"),
                    resultSet.getDouble("paid_amount"),
                    resultSet.getString("payment_status"),
                    resultSet.getString("description")
            ));
        }
        resultSet.close();
        return salesList;
    }

    @Override
    public boolean save(Sales entity) throws SQLException {
        String sqlSales = "INSERT INTO Sales (customer_id, user_id, sale_date, sub_total, discount, " +
                "grand_total, paid_amount, payment_status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return CrudUtil.execute(
                sqlSales,
                entity.getCustomer_id() == 0 ? null : entity.getCustomer_id(),
                entity.getUser_id(),
                new Timestamp(System.currentTimeMillis()),
                entity.getSub_total(),
                entity.getDiscount(),
                entity.getGrand_total(),
                entity.getPaid_amount(),
                entity.getPayment_status(),
                entity.getDescription()
        );
    }

    @Override
    public boolean update(Sales entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    @Override
    public Sales search(int id) throws SQLException {
        String sql = "SELECT * FROM Sales WHERE sale_id = ?";
        ResultSet rs = CrudUtil.execute(sql, id);
        if (rs.next()) {
            return new Sales(
                    rs.getInt("sale_id"),
                    rs.getInt("customer_id"),
                    rs.getInt("user_id"),
                    rs.getTimestamp("sale_date"),
                    rs.getDouble("sub_total"),
                    rs.getDouble("discount"),
                    rs.getDouble("grand_total"),
                    rs.getDouble("paid_amount"),
                    rs.getString("payment_status"),
                    rs.getString("description")
            );

        }
        rs.close();
        return null; // Sale not found
    }

    @Override
    public int getLastInsertedSalesId() throws SQLException {
        String idQuery = "SELECT LAST_INSERT_ID() AS id FROM Sales";
        ResultSet rs = CrudUtil.execute(idQuery);
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Failed to retrieve sales ID");
        }
    }

    @Override
    public boolean updateSalePayment(int saleId, double newPaidAmount, String newPaymentStatus) throws SQLException {
        String updateSql = "UPDATE Sales SET paid_amount = paid_amount + ?, payment_status = ? WHERE sale_id = ?";
        return CrudUtil.execute(updateSql, newPaidAmount, newPaymentStatus, saleId);
    }

    @Override
    public int getSalesCount(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Sales WHERE sale_date BETWEEN ? AND ?";
        return GenerateReports.getCountByDateRange(sql, from, to);
    }

    @Override
    public boolean isSaleExist(String saleId) throws SQLException {
        String sql = "SELECT sale_id FROM Sales WHERE sale_id = ?";
        return GenerateReports.checkIdExists(sql, saleId);
    }

    @Override
    public XYChart.Series<String, Number> getSalesChartData() throws SQLException {
        String sqlSales = "SELECT DATE(sale_date) as d, COUNT(*) as c FROM Sales " +
                "WHERE sale_date >= DATE(NOW()) - INTERVAL 7 DAY " +
                "GROUP BY DATE(sale_date) ORDER BY DATE(sale_date)";
        XYChart.Series<String, Number> seriesSales = new XYChart.Series<>();
        seriesSales.setName("Sales");

        ResultSet rs1 = CrudUtil.execute(sqlSales);
        while (rs1.next()) {
            seriesSales.getData().add(new XYChart.Data<>(rs1.getString("d"), rs1.getInt("c")));
        }
        return seriesSales;
    }
}
