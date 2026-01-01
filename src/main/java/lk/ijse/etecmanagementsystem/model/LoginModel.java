package lk.ijse.etecmanagementsystem.model;

import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginModel {

    public boolean validateCredentials(String username, String password) throws SQLException {
        // Logic to validate credentials against a data source
        String sql = "SELECT * FROM User WHERE user_name = ? AND password = ?";
        try (ResultSet resultSet = CrudUtil.execute(sql, username, password)) {
            return resultSet.next();
        }
    }

    public boolean validateUserName(String username) throws SQLException {
        String sql = "SELECT * FROM User WHERE user_name = ?";
        try (ResultSet resultSet = CrudUtil.execute(sql, username)) {
            return resultSet.next();
        }
    }

    public String getUserRole(String username) throws SQLException {
        String sql = "SELECT role FROM User WHERE user_name = ?";
        try (ResultSet resultSet = CrudUtil.execute(sql, username)) {
            if (resultSet.next()) {
                return resultSet.getString("role");
            } else {
                return null;
            }
        }
    }

    public String getName(String username) throws SQLException {
        String sql = "SELECT name FROM User WHERE user_name = ?";
        try (ResultSet resultSet = CrudUtil.execute(sql, username)) {
            if (resultSet.next()) {
                return resultSet.getString("name");
            } else {
                return null;
            }
        }
    }

    public int getUserId(String username) throws SQLException {
        String sql = "SELECT user_id FROM User WHERE user_name = ?";
        try (ResultSet resultSet = CrudUtil.execute(sql, username)) {
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            } else {
                return -1;
            }
        }
    }

    public String getUserEmail(String username) throws SQLException {
        String sql = "SELECT email FROM User WHERE user_name = ?";
        try (ResultSet resultSet = CrudUtil.execute(sql, username)) {
            if (resultSet.next()) {
                return resultSet.getString("email");
            } else {
                return null;
            }
        }
    }

    public boolean validateUserEmail(String username, String email) throws SQLException {
        String sql = "SELECT * FROM User WHERE user_name = ? AND email = ?";
        try (ResultSet resultSet = CrudUtil.execute(sql, username, email)) {
            return resultSet.next();
        }
    }

    public String getUserPassword(String username) throws SQLException {
        String sql = "SELECT password FROM User WHERE user_name = ?";

        try (ResultSet resultSet = CrudUtil.execute(sql, username)) {
            if (resultSet.next()) {
                return resultSet.getString("password");
            } else {
                return null;
            }
        }

    }
}
