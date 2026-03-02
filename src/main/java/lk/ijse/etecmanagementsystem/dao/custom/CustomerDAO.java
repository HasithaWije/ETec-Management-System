package lk.ijse.etecmanagementsystem.dao.custom;

import lk.ijse.etecmanagementsystem.dao.CrudDAO;
import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
import lk.ijse.etecmanagementsystem.entity.Customer;

import java.sql.SQLException;
import java.util.List;

public interface CustomerDAO extends CrudDAO<Customer> {

    boolean insertCustomerAndGetId(CustomerDTO customer) throws SQLException;

    int getCustomerCount() throws SQLException;

    int getLastInsertedCustomerId() throws SQLException;
}

