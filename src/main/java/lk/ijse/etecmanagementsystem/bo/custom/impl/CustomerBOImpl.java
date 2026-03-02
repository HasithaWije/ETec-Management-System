package lk.ijse.etecmanagementsystem.bo.custom.impl;

import lk.ijse.etecmanagementsystem.bo.custom.CustomerBO;
import lk.ijse.etecmanagementsystem.dao.custom.CustomerDAO;
import lk.ijse.etecmanagementsystem.dao.custom.impl.CustomerDAOImpl;
import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
import lk.ijse.etecmanagementsystem.entity.Customer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerBOImpl implements CustomerBO {

    CustomerDAO customerDAO = new CustomerDAOImpl();

    @Override
    public List<CustomerDTO> getAllCustomers() throws SQLException {
        List<Customer> customers = customerDAO.getAll();
        List<CustomerDTO> customerDTOS = new ArrayList<>();
        for(Customer customer:customers){
            customerDTOS.add(new CustomerDTO(
                    customer.getCus_id(),
                    customer.getName(),
                    customer.getNumber(),
                    customer.getEmail(),
                    customer.getAddress()
            ));
        }
        return customerDTOS;
    }

    @Override
    public CustomerDTO getCustomerById(int id) throws SQLException {
        Customer customer = customerDAO.search(id);

        return new CustomerDTO(
                customer.getCus_id(),
                customer.getName(),
                customer.getNumber(),
                customer.getEmail(),
                customer.getAddress()
        );
    }

    @Override
    public boolean saveCustomer(CustomerDTO customer) throws SQLException {
        Customer entity = new Customer(
                -1,
                customer.getName(),
                customer.getNumber(),
                customer.getEmailAddress(),
                customer.getAddress()
        );
        return customerDAO.save(entity);
    }

    @Override
    public int insertCustomerAndGetId(CustomerDTO customer) throws SQLException {
        boolean isSaved =  customerDAO.insertCustomerAndGetId(customer);
        int generatedId = -2; // Default value indicating failure
        if(isSaved){
            generatedId = customerDAO.getLastInsertedCustomerId();
        }
        return generatedId;
    }

    @Override
    public boolean updateCustomer(CustomerDTO customer) throws SQLException {
        Customer entity = new Customer(
                customer.getId(),
                customer.getName(),
                customer.getNumber(),
                customer.getEmailAddress(),
                customer.getAddress()
        );
        return customerDAO.update(entity);
    }

    @Override
    public boolean deleteCustomer(int id) throws SQLException {
        return customerDAO.delete(id);
    }
}
