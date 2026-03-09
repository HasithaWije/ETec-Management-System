package lk.ijse.etecmanagementsystem.dao.custom.impl;

import lk.ijse.etecmanagementsystem.dao.custom.SupplierDAO;
import lk.ijse.etecmanagementsystem.dao.CrudUtil;
import lk.ijse.etecmanagementsystem.entity.Supplier;
import lk.ijse.etecmanagementsystem.reports.GenerateReports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAOImpl implements SupplierDAO {

    @Override
    public List<Supplier> getAll() throws SQLException {
        String sql = "SELECT * FROM Supplier";
        List<Supplier> suppliers = new ArrayList<>();

        try (ResultSet rs = CrudUtil.execute(sql)) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        }

        return suppliers;
    }

    @Override
    public boolean save(Supplier entity) throws SQLException {
        String sql = "INSERT INTO Supplier(supplier_name,contact_number,email,address) VALUES(?,?,?,?)";
        return CrudUtil.execute(sql,
                entity.getSupplier_name(),
                entity.getContact_number(),
                entity.getEmail() == null ? "" : entity.getEmail(),
                entity.getAddress());
    }

    @Override
    public boolean update(Supplier entity) throws SQLException {
        String sql = "UPDATE Supplier SET supplier_name=?,contact_number=?,email=?,address=? WHERE supplier_id=?";
        return CrudUtil.execute(sql,
                entity.getSupplier_name(),
                entity.getContact_number(),
                entity.getEmail() == null ? "" : entity.getEmail(),
                entity.getAddress(),
                entity.getSupplier_id());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Supplier WHERE supplier_id=?";
        return CrudUtil.execute(sql, id);
    }

    @Override
    public Supplier search(int id) throws SQLException {
        String sql = "SELECT * FROM Supplier WHERE supplier_id=?";

        Supplier supplier = null;

        try (ResultSet rs = CrudUtil.execute(sql, id)) {
            if (rs.next()) {
                supplier = new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("address")
                );
            }
        }
        return supplier;
    }

    @Override
    public List<Supplier> getSupplierByName(String name) throws SQLException {
        String sql = "SELECT * FROM Supplier WHERE supplier_name=?";
        List<Supplier> suppliers = new ArrayList<>();

        try (ResultSet rs = CrudUtil.execute(sql, name)) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        }
        return suppliers;
    }

    @Override
    public int getSupplierCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Supplier";
        return GenerateReports.getTotalCount(sql);
    }
}
