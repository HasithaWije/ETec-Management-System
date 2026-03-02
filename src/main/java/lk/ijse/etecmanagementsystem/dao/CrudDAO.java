package lk.ijse.etecmanagementsystem.dao;

import java.sql.SQLException;
import java.util.List;

public interface CrudDAO<T> {

    List<T> getAll() throws SQLException;

    boolean save(T entity) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(int id) throws SQLException;

    T search(int id) throws SQLException;
}
