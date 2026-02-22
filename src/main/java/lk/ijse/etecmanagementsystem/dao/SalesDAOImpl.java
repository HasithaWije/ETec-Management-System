package lk.ijse.etecmanagementsystem.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.PendingSaleTM;
import lk.ijse.etecmanagementsystem.dto.tm.SalesTM;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesDAOImpl {
    public List<SalesTM> getAllSales() throws SQLException {
        List<SalesTM> salesList = new ArrayList<>();

        String sql = "SELECT s.sale_id, c.name AS customer_name, u.user_name, s.description, " +
                "s.sub_total, s.discount, s.grand_total, s.paid_amount " +
                "FROM Sales s " +
                "LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "JOIN User u ON s.user_id = u.user_id " +
                "ORDER BY s.sale_date DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            ResultSet resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                salesList.add(new SalesTM(
                        resultSet.getInt("sale_id"),
                        resultSet.getString("customer_name") != null ? resultSet.getString("customer_name") : "Walk-in", // Handle null customers
                        resultSet.getString("user_name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("sub_total"),
                        resultSet.getDouble("discount"),
                        resultSet.getDouble("grand_total"),
                        resultSet.getDouble("paid_amount")
                ));
            }
        }
        return salesList;
    }

    public List<SalesTM> getSalesByDateRange(LocalDate from, LocalDate to) throws SQLException {
        List<SalesTM> salesList = new ArrayList<>();

        String sql = "SELECT s.sale_id, c.name AS customer_name, u.user_name, s.description, " +
                "s.sub_total, s.discount, s.grand_total, s.paid_amount " +
                "FROM Sales s " +
                "LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "JOIN User u ON s.user_id = u.user_id " +
                "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                "ORDER BY s.sale_date DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement pstm = connection.prepareStatement(sql)) {
            pstm.setDate(1, java.sql.Date.valueOf(from));
            pstm.setDate(2, java.sql.Date.valueOf(to));

            ResultSet resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                salesList.add(new SalesTM(
                        resultSet.getInt("sale_id"),
                        resultSet.getString("customer_name") != null ? resultSet.getString("customer_name") : "Walk-in", // Handle null customers
                        resultSet.getString("user_name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("sub_total"),
                        resultSet.getDouble("discount"),
                        resultSet.getDouble("grand_total"),
                        resultSet.getDouble("paid_amount")
                ));
            }
        }
        return salesList;
    }

    public ObservableList<PendingSaleTM> getPendingSales() throws SQLException {
        String saleSql = "SELECT s.sale_id, c.name, s.grand_total, s.paid_amount FROM Sales s LEFT JOIN Customer c ON s.customer_id = c.cus_id " +
                "WHERE s.payment_status IN ('PENDING', 'PARTIAL') AND s.description LIKE 'Point of Sale Transaction'";


        ResultSet rs = CrudUtil.execute(saleSql);
        ObservableList<PendingSaleTM> pendingSalesList = FXCollections.observableArrayList();

        while (rs.next()) {
            double total = rs.getDouble("grand_total");
            double paid = rs.getDouble("paid_amount");
            pendingSalesList.add(new PendingSaleTM(rs.getInt("sale_id"), rs.getString("name"), total, total - paid));
        }
        rs.close();
        return pendingSalesList;
    }

    public boolean saveSale(SalesDTO salesDTO) throws SQLException {
        String sqlSales = "INSERT INTO Sales (customer_id, user_id, sale_date, sub_total, discount, " +
                "grand_total, paid_amount, payment_status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        pstmSales.setObject(1, salesDTO.getCustomerId() == 0 ? null : salesDTO.getCustomerId());
//        pstmSales.setInt(2, salesDTO.getUserId());
//        pstmSales.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
//        pstmSales.setDouble(4, salesDTO.getSubtotal());
//        pstmSales.setDouble(5, salesDTO.getDiscount());
//        pstmSales.setDouble(6, salesDTO.getGrandTotal());
//        pstmSales.setDouble(7, salesDTO.getPaidAmount());
//        pstmSales.setString(8, salesDTO.getPaymentStatus().toString());
//        pstmSales.setString(9, salesDTO.getDescription());
        return CrudUtil.execute(sqlSales,
                salesDTO.getCustomerId() == 0 ? null : salesDTO.getCustomerId(),
                salesDTO.getUserId(),
                new Timestamp(System.currentTimeMillis()),
                salesDTO.getSubtotal(),
                salesDTO.getDiscount(),
                salesDTO.getGrandTotal(),
                salesDTO.getPaidAmount(),
                salesDTO.getPaymentStatus().toString(),
                salesDTO.getDescription()
        );
    }

    public int getLastInsertedSalesId() throws SQLException {
        String idQuery = "SELECT LAST_INSERT_ID() AS id FROM Sales";
        ResultSet rs = CrudUtil.execute(idQuery);
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Failed to retrieve sales ID");
        }
    }
}
