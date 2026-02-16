package lk.ijse.etecmanagementsystem.dao;

import lk.ijse.etecmanagementsystem.dto.UserDTO;
import lk.ijse.etecmanagementsystem.util.CrudUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl {
    public List<UserDTO> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM User";
        List<UserDTO> users = new ArrayList<>();

        try (ResultSet rs = CrudUtil.execute(sql)) {
            while (rs.next()) {
                users.add(new UserDTO(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("user_name"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        }
        return users;
    }

    public UserDTO getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM User WHERE user_id=?";
        UserDTO user = null;

        try (ResultSet rs = CrudUtil.execute(sql, id)) {
            if (rs.next()) {
                user = new UserDTO(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("user_name"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        }
        return user;
    }

    public boolean saveUser(UserDTO user) throws SQLException {
        String sql = "INSERT INTO User(name, contact, address, email, user_name, password, role) VALUES(?,?,?,?,?,?,?)";
        return CrudUtil.execute(sql,
                user.getName(),
                user.getContact(),
                user.getAddress(),
                user.getEmail(),
                user.getUserName(),
                user.getPassword(),
                user.getRole());
    }

    public boolean updateUser(UserDTO user) throws SQLException {
        String sql = "UPDATE User SET name=?, contact=?, address=?, email=?, user_name=?, password=?, role=? WHERE user_id=?";
        return CrudUtil.execute(sql,
                user.getName(),
                user.getContact(),
                user.getAddress(),
                user.getEmail(),
                user.getUserName(),
                user.getPassword(),
                user.getRole(),
                user.getUserId());
    }

    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM User WHERE user_id=?";
        return CrudUtil.execute(sql, id);
    }
}
