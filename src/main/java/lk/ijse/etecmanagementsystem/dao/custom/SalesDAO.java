package lk.ijse.etecmanagementsystem.dao.custom;

import javafx.scene.chart.XYChart;
import lk.ijse.etecmanagementsystem.dao.CrudDAO;
import lk.ijse.etecmanagementsystem.entity.Sales;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface SalesDAO extends CrudDAO<Sales> {

    int getLastInsertedSalesId() throws SQLException;

    boolean updateSalePayment(int saleId, double newPaidAmount, String newPaymentStatus) throws SQLException;

    int getSalesCount(LocalDate from, LocalDate to) throws SQLException;

    boolean isSaleExist(String saleId) throws SQLException;

    XYChart.Series<String, Number> getSalesChartData() throws SQLException;
}

