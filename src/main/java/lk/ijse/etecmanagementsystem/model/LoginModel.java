package lk.ijse.etecmanagementsystem.model;

import lk.ijse.etecmanagementsystem.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginModel {

    public boolean validateCredentials(String username, String password) throws SQLException {
        // Logic to validate credentials against a data source
        String sql = "SELECT * FROM user WHERE user_name = ? AND password = ?";
        Connection con = DBConnection.getInstance().getConnection();

        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, username);
            pstm.setString(2, password);
            ResultSet resultSet = pstm.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String getUserRole(String username) throws SQLException {
        String sql = "SELECT role FROM user WHERE user_name = ?";
        Connection con = DBConnection.getInstance().getConnection();

        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, username);
            ResultSet resultSet = pstm.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("role");
            } else {
                return null;
            }
        }
    }

    public String getName(String username) throws SQLException {
        String sql = "SELECT name FROM user WHERE user_name = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, username);
            ResultSet resultSet = pstm.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("name");
            } else {
                return null;
            }
        }
    }

    public int getUserId(String username) throws SQLException {
        String sql = "SELECT user_id FROM user WHERE user_name = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, username);
            ResultSet resultSet = pstm.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            } else {
                return -1;
            }
        }
    }
}
