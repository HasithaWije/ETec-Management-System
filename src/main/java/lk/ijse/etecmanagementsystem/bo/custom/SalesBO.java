package lk.ijse.etecmanagementsystem.bo.custom;

import lk.ijse.etecmanagementsystem.bo.SuperBO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;

import java.sql.SQLException;
import java.util.List;

public interface SalesBO extends SuperBO {
    ProductItemDTO getProductItem(int itemId) throws SQLException;

    List<SalesDTO> getAllSale() throws SQLException;
}
