package lk.ijse.etecmanagementsystem.util;

import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class GenerateReports {

    public static void generateReport(LocalDate fromDate, LocalDate toDate, String jasper) {
        try {

            String path = "reports/"+jasper+".jasper";

            InputStream reportStream = App.class.getResourceAsStream(path);

            if (reportStream == null) {
                System.err.println("Error: Could not find salesReceipt.jasper at " + path);
                return;
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

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
}
