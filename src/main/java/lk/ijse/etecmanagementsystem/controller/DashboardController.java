package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lk.ijse.etecmanagementsystem.dto.tm.DashboardTM;
import lk.ijse.etecmanagementsystem.dto.tm.DebtTM;
import lk.ijse.etecmanagementsystem.dto.tm.UrgentRepairTM;
import lk.ijse.etecmanagementsystem.model.DashboardModel;
import lk.ijse.etecmanagementsystem.component.DebtListCell;
import lk.ijse.etecmanagementsystem.component.UrgentRepairListCell;

import java.sql.SQLException;

public class DashboardController {

    // --- FXML Injections ---
    @FXML private Label lblTodayIncome;
    @FXML private Label lblActiveRepairs;
    @FXML private Label lblPendingPayment;
    @FXML private Label lblLowStock;

    @FXML private ListView<UrgentRepairTM> listUrgentRepairs;
    @FXML private ListView<DebtTM> listUnpaid;
    @FXML private BarChart<String, Number> chartSales;

    // --- Model Instance ---
    private final DashboardModel dashboardModel = new DashboardModel();

    // --- Initialization ---
    public void initialize() {


        try{
        listUrgentRepairs.setCellFactory(param -> new UrgentRepairListCell());
        listUnpaid.setCellFactory(param -> new DebtListCell());

        loadAllData();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void loadAllData() {
        try {

            DashboardTM stats = dashboardModel.getDashboardStats();

            lblTodayIncome.setText(String.format("%.2f", stats.getTodayIncome()));
            lblPendingPayment.setText(String.format("%.2f", stats.getPendingPayments()));


            lblActiveRepairs.setText(String.valueOf(stats.getActiveRepairs()));
            lblLowStock.setText(String.valueOf(stats.getLowStock()));


            listUrgentRepairs.setItems(dashboardModel.getUrgentRepairs());
            listUnpaid.setItems(dashboardModel.getUnpaidDebts());


            chartSales.getData().clear();
            chartSales.getData().add(dashboardModel.getSalesChartData());

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading dashboard data: " + e.getMessage()).show();
        }
    }
}