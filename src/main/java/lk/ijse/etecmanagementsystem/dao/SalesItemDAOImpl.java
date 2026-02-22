package lk.ijse.etecmanagementsystem.dao;

import lk.ijse.etecmanagementsystem.dao.entity.SalesItem;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.SQLException;

public class SalesItemDAOImpl {
    public boolean createSalesItem(SalesItem entity) throws SQLException {
        String sqlSalesItem = "INSERT INTO SalesItem (sale_id, item_id, customer_warranty_months, unit_price, discount) VALUES (?, ?, ?, ?, ?)";
        return CrudUtil.execute(sqlSalesItem,
                entity.getSale_id(),
                entity.getItem_id(),
                entity.getCustomer_warranty_months(),
                entity.getUnit_price(),
                entity.getDiscount()
        );
    }
}
