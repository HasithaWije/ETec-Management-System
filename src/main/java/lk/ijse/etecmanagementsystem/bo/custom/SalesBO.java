package lk.ijse.etecmanagementsystem.bo.custom;

import lk.ijse.etecmanagementsystem.bo.SuperBO;
import lk.ijse.etecmanagementsystem.dto.CustomDTO;
import lk.ijse.etecmanagementsystem.dto.ItemCartDTO;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface SalesBO extends SuperBO {

    boolean placeOrder(SalesDTO salesDTO, List<ItemCartDTO> cartItems) throws SQLException;

    ProductItemDTO getProductItem(int itemId) throws SQLException;

    List<SalesDTO> getAllSale() throws SQLException;

    List<CustomDTO> getSalesByDateRange(LocalDate from, LocalDate to) throws SQLException;

    List<CustomDTO> getPendingSales() throws SQLException;
}
