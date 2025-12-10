package lk.ijse.etecmanagementsystem.util;

import lk.ijse.etecmanagementsystem.db.DBConnection;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CrudUtil {

    public static <T>T execute(String sql, Object... obj) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        PreparedStatement ptsm = conn.prepareStatement(sql);

        for(int i = 0; i < obj.length; i++){
            ptsm.setObject(i + 1, obj[i]);
        }

        if(sql.startsWith("SELECT") || sql.startsWith("select")){
            ResultSet rs = ptsm.executeQuery();
            return (T)rs;
        }else{
            int result = ptsm.executeUpdate();
            return (T)(Boolean)(result > 0);
        }

    }

//    public static <T> T execute(String sql, Object... obj) throws SQLException {
//        Connection conn = DBConnection.getInstance().getConnection();
//
//        // 1. Identify if it is a SELECT query
//        if (sql.trim().toUpperCase().startsWith("SELECT")) {
//
//            // USE TRY-WITH-RESOURCES HERE
//            // This ensures the PreparedStatement and ResultSet are CLOSED immediately after we extract the data.
//            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
//
//                // Add parameters
//                for (int i = 0; i < obj.length; i++) {
//                    ptsm.setObject(i + 1, obj[i]);
//                }
//
//                try (ResultSet rs = ptsm.executeQuery()) {
//                    // Create a CachedRowSet
//                    // This is a disconnected ResultSet. It copies the data into memory.
//                    // This allows us to CLOSE the database resources (ptsm and rs)
//                    // but still return a ResultSet to the Model.
//                    CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
//                    crs.populate(rs);
//                    return (T) crs;
//                }
//            }
//
//        } else {
//            // 2. Handle INSERT, UPDATE, DELETE
//            // USE TRY-WITH-RESOURCES HERE
//            try (PreparedStatement ptsm = conn.prepareStatement(sql)) {
//
//                for (int i = 0; i < obj.length; i++) {
//                    ptsm.setObject(i + 1, obj[i]);
//                }
//
//                int result = ptsm.executeUpdate();
//                return (T) (Boolean) (result > 0);
//
//                // When this block finishes, 'ptsm' is auto-closed.
//            }
//        }
//    }
}
