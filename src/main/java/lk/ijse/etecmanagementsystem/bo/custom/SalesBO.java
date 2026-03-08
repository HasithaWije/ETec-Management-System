package lk.ijse.etecmanagementsystem.bo.custom;

import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;

import java.sql.SQLException;

public interface SalesBO {
    ProductItemDTO getProductItem(int itemId) throws SQLException;
}
