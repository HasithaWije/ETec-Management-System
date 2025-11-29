package lk.ijse.etecmanagementsystem.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private final String URL = "jdbc:mysql://localhost:3306/ETec";
    private final String USERNAME = "root";
    private final String PASSWORD = "mysql";

    private final Connection connection;
    private static DBConnection dbConnection;


    private DBConnection() throws Exception {
        this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }


    public static DBConnection getInstance() throws Exception {
        if (dbConnection == null) {
            dbConnection = new DBConnection();
        }
        return dbConnection;
    }
    public Connection getConnection() {
        return connection;
    }

}
