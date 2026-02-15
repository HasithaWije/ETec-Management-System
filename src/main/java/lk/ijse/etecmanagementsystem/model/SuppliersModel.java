package lk.ijse.etecmanagementsystem.model;

import lk.ijse.etecmanagementsystem.dto.SupplierDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SuppliersModel {


//    public List<SupplierDTO> getAllSuppliers() throws SQLException {
//        String sql = "SELECT * FROM Supplier";
//        List<SupplierDTO> suppliers = new ArrayList<>();
//
//        try (ResultSet rs = CrudUtil.execute(sql)) {
//            while (rs.next()) {
//                suppliers.add(new SupplierDTO(
//                        rs.getInt("supplier_id"),
//                        rs.getString("supplier_name"),
//                        rs.getString("contact_number"),
//                        rs.getString("email"),
//                        rs.getString("address")
//                ));
//            }
//        }
//
//        return suppliers;
//    }
//
//    public SupplierDTO getSupplierById(int id) throws SQLException {
//        String sql = "SELECT * FROM Supplier WHERE supplier_id=?";
//
//        SupplierDTO supplier = null;
//
//        try (ResultSet rs = CrudUtil.execute(sql, id)) {
//            if (rs.next()) {
//                supplier = new SupplierDTO(
//                        rs.getInt("supplier_id"),
//                        rs.getString("supplier_name"),
//                        rs.getString("contact_number"),
//                        rs.getString("email"),
//                        rs.getString("address")
//                );
//            }
//        }
//        return supplier;
//    }
//
//    public List<SupplierDTO> getSupplierByName(String name) throws SQLException {
//        String sql = "SELECT * FROM Supplier WHERE supplier_name=?";
//        List<SupplierDTO> suppliers = new ArrayList<>();
//
//        try (ResultSet rs = CrudUtil.execute(sql, name)) {
//            while (rs.next()) {
//                suppliers.add(new SupplierDTO(
//                        rs.getInt("supplier_id"),
//                        rs.getString("supplier_name"),
//                        rs.getString("contact_number"),
//                        rs.getString("email"),
//                        rs.getString("address")
//                ));
//            }
//        }
//        return suppliers;
//    }
//
//    public boolean saveSuppliers(SupplierDTO supplier) throws SQLException {
//        String sql = "INSERT INTO Supplier(supplier_name,contact_number,email,address) VALUES(?,?,?,?)";
//        return CrudUtil.execute(sql,
//                supplier.getSupplierName(),
//                supplier.getContactNumber(),
//                supplier.getEmailAddress() == null ? "" : supplier.getEmailAddress(),
//                supplier.getAddress());
//    }
//
//    public boolean updateSuppliers(SupplierDTO supplier) throws SQLException {
//        String sql = "UPDATE Supplier SET supplier_name=?,contact_number=?,email=?,address=? WHERE supplier_id=?";
//        return CrudUtil.execute(sql,
//                supplier.getSupplierName(),
//                supplier.getContactNumber(),
//                supplier.getEmailAddress() == null ? "" : supplier.getEmailAddress(),
//                supplier.getAddress(),
//                supplier.getSupplierId());
//    }
//
//    public boolean deleteSuppliers(int id) throws SQLException {
//        String sql = "DELETE FROM Supplier WHERE supplier_id=?";
//        return CrudUtil.execute(sql, id);
//    }
}
