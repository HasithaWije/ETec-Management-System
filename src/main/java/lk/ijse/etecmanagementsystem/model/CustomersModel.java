//package lk.ijse.etecmanagementsystem.model;
//
//import lk.ijse.etecmanagementsystem.db.DBConnection;
//import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
//import lk.ijse.etecmanagementsystem.dto.SupplierDTO;
//import lk.ijse.etecmanagementsystem.util.CrudUtil;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CustomersModel {
//
//
////    public List<CustomerDTO> getAllCustomers() throws SQLException {
////        String sql = "SELECT * FROM Customer";
////        List<CustomerDTO> customers = new ArrayList<>();
////
////        try (ResultSet rs = CrudUtil.execute(sql)) {
////            while (rs.next()) {
////                customers.add(new CustomerDTO(
////                        rs.getInt("cus_id"),
////                        rs.getString("name"),
////                        rs.getString("number"),
////                        rs.getString("email"),
////                        rs.getString("address")
////                ));
////            }
////        }
////
////        return customers;
////    }
//
////    public CustomerDTO getCustomerById(int id) throws SQLException {
////        String sql = "SELECT * FROM Customer WHERE cus_id=?";
////
////        CustomerDTO customer = null;
////
////        try (ResultSet rs = CrudUtil.execute(sql, id)) {
////            if (rs.next()) {
////                customer = new CustomerDTO(
////                        rs.getInt("cus_id"),
////                        rs.getString("name"),
////                        rs.getString("number"),
////                        rs.getString("email"),
////                        rs.getString("address")
////                );
////            }
////        }
////        return customer;
////    }
////
////    public List<CustomerDTO> getCustomerByName(String name) throws SQLException {
////        String sql = "SELECT * FROM Customer WHERE name=?";
////        List<CustomerDTO> customers = new ArrayList<>();
////
////        try (ResultSet rs = CrudUtil.execute(sql, name)) {
////            while (rs.next()) {
////                customers.add(new CustomerDTO(
////                        rs.getInt("cus_id"),
////                        rs.getString("name"),
////                        rs.getString("number"),
////                        rs.getString("email"),
////                        rs.getString("address")
////                ));
////            }
////        }
////        return customers;
////    }
////
////    public boolean saveCustomer(CustomerDTO customer) throws SQLException {
////        String sql = "INSERT INTO Customer(name,number,email,address) VALUES(?,?,?,?)";
////        return CrudUtil.execute(sql,
////                customer.getName(),
////                customer.getNumber(),
////                customer.getEmailAddress() == null ? "" : customer.getEmailAddress(),
////                customer.getAddress());
////    }
////
////    public int insertCustomerAndGetId(CustomerDTO customer) throws SQLException {
////        String sql = "INSERT INTO Customer(name,number,email,address) VALUES(?,?,?,?)";
////
////        int generatedKey = -2;
////
////        // 1. Pass 'Statement.RETURN_GENERATED_KEYS' as the second argument
////        Connection conn = DBConnection.getInstance().getConnection();
////        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
////
////            pstmt.setString(1, customer.getName());
////            pstmt.setString(2, customer.getNumber() == null ? "" : customer.getNumber());
////            pstmt.setString(3, customer.getEmailAddress() == null ? "" : customer.getEmailAddress());
////            pstmt.setString(4, customer.getAddress() == null ? "" : customer.getAddress());
////
////            int affectedRows = pstmt.executeUpdate();
////
////            if (affectedRows > 0) {
////                // 2. Retrieve the generated keys ResultSet
////                try (ResultSet rs = pstmt.getGeneratedKeys()) {
////                    if (rs.next()) {
////                        // 3. Get the ID (usually the first column)
////                        generatedKey = rs.getInt(1);
////                        System.out.println("Inserted Record ID: " + generatedKey);
////                    }
////                }
////            }
////        } catch (SQLException e) {
////            throw e;
////        }
////
////        return generatedKey;
////    }
////
////    public boolean updateCustomer(CustomerDTO customer) throws SQLException {
////        String sql = "UPDATE Customer SET name=?,number=?,email=?,address=? WHERE cus_id=?";
////        return CrudUtil.execute(sql,
////                customer.getName(),
////                customer.getNumber(),
////                customer.getEmailAddress() == null ? "" : customer.getEmailAddress(),
////                customer.getAddress(),
////                customer.getId());
////    }
////
////    public boolean deleteCustomer(int id) throws SQLException {
////        String sql = "DELETE FROM Customer WHERE cus_id=?";
////        return CrudUtil.execute(sql, id);
////    }
//}
