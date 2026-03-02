package lk.ijse.etecmanagementsystem.dao.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.CustomerDAO;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.Customer;
import lk.ijse.etecmanagementsystem.util.GenerateReports;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public boolean insertCustomerAndGetId(CustomerDTO customer) throws SQLException {
        String sql = "INSERT INTO Customer(name,number,email,address) VALUES(?,?,?,?)";

        return CrudUtil.execute(sql,
                customer.getName(),
                customer.getNumber() == null ? "" : customer.getNumber(),
                customer.getEmailAddress() == null ? "" : customer.getEmailAddress(),
                customer.getAddress() == null ? "" : customer.getAddress());


    }

    @Override
    public int getLastInsertedCustomerId() throws SQLException {
        String idQuery = "SELECT LAST_INSERT_ID() AS id FROM Customer";
        ResultSet rs = CrudUtil.execute(idQuery);
        if (rs.next()) {
            return rs.getInt("id");
        } else {
            throw new SQLException("Failed to retrieve Customer ID");
        }
    }

    @Override
    public int getCustomerCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer";
        return GenerateReports.getTotalCount(sql);
    }

    @Override
    public List<Customer> getAll() throws SQLException {
        String sql = "SELECT * FROM Customer";
        List<Customer> customers = new ArrayList<>();

        try (ResultSet rs = CrudUtil.execute(sql)) {
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("cus_id"),
                        rs.getString("name"),
                        rs.getString("number"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        }

        return customers;
    }

    @Override
    public boolean save(Customer entity) throws SQLException {
        String sql = "INSERT INTO Customer(name,number,email,address) VALUES(?,?,?,?)";

        return CrudUtil.execute(sql,
                entity.getName(),
                entity.getNumber(),
                entity.getEmail() == null ? "" : entity.getEmail(),
                entity.getAddress() == null ? "" : entity.getAddress());
    }

    @Override
    public boolean update(Customer entity) throws SQLException {
        String sql = "UPDATE Customer SET name=?,number=?,email=?,address=? WHERE cus_id=?";
        return CrudUtil.execute(sql,
                entity.getName(),
                entity.getNumber(),
                entity.getEmail() == null ? "" : entity.getEmail(),
                entity.getAddress() == null ? "" : entity.getAddress(),
                entity.getCus_id());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Customer WHERE cus_id=?";
        return CrudUtil.execute(sql, id);
    }

    @Override
    public Customer search(int id) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE cus_id=?";

        ResultSet rs = CrudUtil.execute(sql, id);
        Customer customer = null;
        if (rs.next()) {
            customer = new Customer(
                    rs.getInt("cus_id"),
                    rs.getString("name"),
                    rs.getString("number"),
                    rs.getString("email"),
                    rs.getString("address")
            );
        }
        rs.close();
        return customer;
    }
}
