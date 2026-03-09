package lk.ijse.etecmanagementsystem.reports;

import javafx.scene.control.Alert;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.util.ETecAlerts;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class GenerateReports {

    public static void generateReport(LocalDate fromDate, LocalDate toDate, String jasper) {
        try {

            JasperReport jasperReport = getReport(jasper);
            if (jasperReport == null) {
                ETecAlerts.showAlert(Alert.AlertType.WARNING, "Report Not Found", "Could not find the specified report: " + jasper);
                return;
            }

            Map<String, Object> parameters = new HashMap<>();

            parameters.put("fromDate", Date.valueOf(fromDate));
            parameters.put("toDate", Date.valueOf(toDate));


//            parameters.put("saleId", saleId);

            Connection connection = DBConnection.getInstance().getConnection();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            JasperViewer.viewReport(jasperPrint, false); // false = Don't close app on exit

        } catch (JRException | java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public static void generateReport(String jasper, int limit) {
        try {

            JasperReport jasperReport = getReport(jasper);
            if (jasperReport == null) {
                ETecAlerts.showAlert(Alert.AlertType.WARNING, "Report Not Found", "Could not find the specified report: " + jasper);
                return;
            }

            Map<String, Object> parameters = new HashMap<>();

            if (limit == 0) {
                ETecAlerts.showAlert(Alert.AlertType.WARNING, "Invalid Limit", "Please enter a valid limit greater than 0.");
                return;
            }

            parameters.put("rowLimit", limit);


            Connection connection = DBConnection.getInstance().getConnection();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            JasperViewer.viewReport(jasperPrint, false); // false = Don't close app on exit

        } catch (JRException | java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public static void generateInvoice(int id, String jasper, String invoiceType) {
        try {

            JasperReport jasperReport = getReport(jasper);
            if (jasperReport == null) {
                ETecAlerts.showAlert(Alert.AlertType.WARNING, "Report Not Found", "Could not find the specified report: " + jasper);
                return;
            }

            Map<String, Object> parameters = new HashMap<>();
            if (invoiceType.equalsIgnoreCase("SALE")) {
                parameters.put("saleId", id);
            } else if (invoiceType.equalsIgnoreCase("REPAIR")) {
                parameters.put("repairId", id);
            }

//            parameters.put("saleId", saleId);

            Connection connection = DBConnection.getInstance().getConnection();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            JasperViewer.viewReport(jasperPrint, false); // false = Don't close app on exit

        } catch (JRException | java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private static JasperReport getReport(String jasper) throws JRException {
        JasperReport jasperReport = null;

        String path = "reports/" + jasper + ".jasper";

        InputStream reportStream = App.class.getResourceAsStream(path);

        if (reportStream == null) {
            System.err.println("Error: Could not find salesReceipt.jasper at " + path);
            return null;
        }

        jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
        return jasperReport;

    }

    public static int getCountByDateRange(String sql, LocalDate from, LocalDate to) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);


        // Start of the 'From' day (00:00:00)
        pstm.setString(1, from.toString() + " 00:00:00");

        // End of the 'To' day (23:59:59)
        pstm.setString(2, to.toString() + " 23:59:59");

        ResultSet resultSet = pstm.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    public static int getTotalCount(String sql) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet resultSet = pstm.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
        return 0;
    }

    public static boolean checkIdExists(String sql, String id) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1, id);
        ResultSet resultSet = pstm.executeQuery();
        return resultSet.next();
    }
}
