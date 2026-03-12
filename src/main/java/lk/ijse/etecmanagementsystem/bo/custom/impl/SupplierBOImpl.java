package lk.ijse.etecmanagementsystem.bo.custom.impl;

import lk.ijse.etecmanagementsystem.bo.custom.SupplierBO;
import lk.ijse.etecmanagementsystem.dao.DAOFactory;
import lk.ijse.etecmanagementsystem.dao.custom.SupplierDAO;
import lk.ijse.etecmanagementsystem.dto.SupplierDTO;
import lk.ijse.etecmanagementsystem.entity.Supplier;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class SupplierBOImpl implements SupplierBO {

    SupplierDAO supplierDAO = (SupplierDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.SUPPLIER);

    @Override
    public List<SupplierDTO> getAllSuppliers() throws SQLException {
        List<Supplier> entities = supplierDAO.getAll();
        List<SupplierDTO> dtos = new ArrayList<>();
        for (Supplier s : entities) {
            dtos.add(new SupplierDTO(
                    s.getSupplier_id(),
                    s.getSupplier_name(),
                    s.getContact_number(),
                    s.getEmail(),
                    s.getAddress()
            ));
        }
        return dtos;
    }

    @Override
    public SupplierDTO getSupplierById(int id) throws SQLException {
        Supplier entity = supplierDAO.search(id);
        return new SupplierDTO(
                entity.getSupplier_id(),
                entity.getSupplier_name(),
                entity.getContact_number(),
                entity.getEmail(),
                entity.getAddress()
        );
    }

    @Override
    public List<SupplierDTO> getSupplierByName(String name) throws SQLException {

        List<Supplier> entities = supplierDAO.getSupplierByName(name);
        List<SupplierDTO> suppliers = new ArrayList<>();

        for (Supplier s : entities) {
            suppliers.add(new SupplierDTO(
                    s.getSupplier_id(),
                    s.getSupplier_name(),
                    s.getContact_number(),
                    s.getEmail(),
                    s.getAddress()
            ));
        }
        return suppliers;
    }

    @Override
    public boolean saveSuppliers(SupplierDTO supplierDTO) throws SQLException {
        return supplierDAO.save(new Supplier(
                supplierDTO.getSupplierId(),
                supplierDTO.getSupplierName(),
                supplierDTO.getContactNumber(),
                supplierDTO.getEmailAddress(),
                supplierDTO.getAddress()
        ));
    }

    @Override
    public boolean updateSuppliers(SupplierDTO supplierDTO) throws SQLException {

        return supplierDAO.update(new Supplier(
                supplierDTO.getSupplierId(),
                supplierDTO.getSupplierName(),
                supplierDTO.getContactNumber(),
                supplierDTO.getEmailAddress(),
                supplierDTO.getAddress()
        ));
    }

    @Override
    public boolean deleteSuppliers(int id) throws SQLException {
        return supplierDAO.delete(id);
    }
}
