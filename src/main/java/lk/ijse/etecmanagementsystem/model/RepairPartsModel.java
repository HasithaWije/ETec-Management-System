package lk.ijse.etecmanagementsystem.model;


import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepairPartsModel {

    public List<RepairPartTM> getAllAvailableParts() throws SQLException {
        List<RepairPartTM> list = new ArrayList<>();

        String sql = "SELECT pi.item_id, p.name, pi.serial_number, p.p_condition, p.sell_price " +
                "FROM ProductItem pi " +
                "JOIN Product p ON pi.stock_id = p.stock_id " +
                "WHERE pi.status = 'AVAILABLE'";


        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet resultSet = pstm.executeQuery();

        while (resultSet.next()) {

            RepairPartTM tm = new RepairPartTM(
                    resultSet.getInt("item_id"),
                    resultSet.getString("name"),
                    resultSet.getString("serial_number"),
                    fromConditionString(resultSet.getString("p_condition")),
                    resultSet.getDouble("sell_price")
            );
            list.add(tm);
        }

        return list;
    }



    private ProductCondition fromConditionString(String s) {
        if (s == null) return ProductCondition.BOTH;
        try {
            if (s.equals("USED")) {
                return ProductCondition.USED;
            } else if (s.equals("BRAND NEW")) {
                return ProductCondition.BRAND_NEW;
            }
            return ProductCondition.BOTH;
        } catch (IllegalArgumentException ex) {
            return ProductCondition.BOTH; // unknown condition value
        }
    }
}