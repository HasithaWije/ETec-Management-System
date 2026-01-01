package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DashboardController {

    @FXML
    private Label lblTodayIncome;
    @FXML
    private Label lblActiveRepairs;
    @FXML
    private Label lblPendingPayment;
    @FXML
    private Label lblLowStock;

    @FXML
    private ListView<UrgentRepairTM> listUrgentRepairs;
    @FXML
    private ListView<DebtTM> listUnpaid;
    @FXML
    private BarChart<String, Number> chartSales;
    @FXML
    private LineChart<String, Number> chartTraffic;


    private final DashboardModel dashboardModel = new DashboardModel();


    private final String[] barColors = {"#f1c40f", "#2ecc71", "#3498db", "#9b59b6", "#e67e22", "#1abc9c", "#e74c3c"};


    public void initialize() {


        try {
            listUrgentRepairs.setCellFactory(param -> new UrgentRepairListCell());
            listUnpaid.setCellFactory(param -> new DebtListCell());

            loadAllData();
            loadCharts();
        } catch (Exception e) {
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


        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading dashboard data: " + e.getMessage()).show();
        }
    }

    private void loadCharts() {
        try {
            chartSales.getData().clear();
            chartSales.setLegendVisible(false);
            XYChart.Series<String, Number> revenueSeries = dashboardModel.getSalesChartData();
            chartSales.getData().add(revenueSeries);

            int i = 0;
            for (XYChart.Data<String, Number> data : revenueSeries.getData()) {
                data.getNode().setStyle("-fx-bar-fill: " + barColors[i % barColors.length] + "; -fx-background-radius: 5 5 0 0;");
                i++;
            }

            chartTraffic.getData().clear();
            List<XYChart.Series<String, Number>> trafficSeries = dashboardModel.getTrafficChartData();

            chartTraffic.getData().addAll(trafficSeries);

            Set<String> uniqueDates = new TreeSet<>();
            for (XYChart.Series<String, Number> series : trafficSeries) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    uniqueDates.add(data.getXValue());
                }
            }

            CategoryAxis xAxis = (CategoryAxis) chartTraffic.getXAxis();
            xAxis.setCategories(FXCollections.observableArrayList(uniqueDates));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}