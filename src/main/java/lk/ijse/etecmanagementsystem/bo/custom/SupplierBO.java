package lk.ijse.etecmanagementsystem.bo.custom;

import lk.ijse.etecmanagementsystem.bo.SuperBO;
import lk.ijse.etecmanagementsystem.dto.SupplierDTO;

import java.sql.SQLException;
import java.util.List;

public interface SupplierBO extends SuperBO {

    List<SupplierDTO> getAllSuppliers() throws SQLException;

    SupplierDTO getSupplierById(int id) throws SQLException;

    List<SupplierDTO> getSupplierByName(String name) throws SQLException;

    boolean saveSuppliers(SupplierDTO supplierDTO) throws SQLException;

    boolean updateSuppliers(SupplierDTO supplierDTO) throws SQLException;

    boolean deleteSuppliers(int id) throws SQLException;

}

