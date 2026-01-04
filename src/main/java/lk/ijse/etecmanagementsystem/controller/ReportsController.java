package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import lk.ijse.etecmanagementsystem.model.ReportsModel;
import lk.ijse.etecmanagementsystem.util.ETecAlerts;
import lk.ijse.etecmanagementsystem.util.GenerateReports;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class ReportsController {

    @FXML
    private Button btnGenerateRepairInvoice;
    @FXML
    private Button btnGenerateReportCus;
    @FXML
    private Button btnGenerateReportInven;
    @FXML
    private Button btnGenerateReportRepair;
    @FXML
    private Button btnGenerateReportSale;
    @FXML
    private Button btnGenerateReportSup;
    @FXML
    private Button btnGenerateReportTransf;
    @FXML
    private Button btnGenerateSalesInvoice;

    @FXML
    private Button btnLoadCustomer;
    @FXML
    private Button btnLoadInventory;
    @FXML
    private Button btnLoadRepair;
    @FXML
    private Button btnLoadSale;
    @FXML
    private Button btnLoadSupplier;
    @FXML
    private Button btnLoadTransaction;

    @FXML
    private ComboBox<String> cmbDatePreset;
    @FXML
    private DatePicker dpFromDate;
    @FXML
    private DatePicker dpToDate;
    @FXML
    private HBox hBoxDate;

    @FXML
    private Label lblRecordCountCus;
    @FXML
    private Label lblRecordCountInven;
    @FXML
    private Label lblRecordCountRepair;
    @FXML
    private Label lblRecordCountSales;
    @FXML
    private Label lblRecordCountSup;
    @FXML
    private Label lblRecordCountTransf;
    @FXML
    private Label lblRepairExists;
    @FXML
    private Label lblSalesExist;

    @FXML
    private Tab tabCustomer;
    @FXML
    private Tab tabInventory;
    @FXML
    private Tab tabRepair;
    @FXML
    private Tab tabSales;
    @FXML
    private Tab tabSupplier;
    @FXML
    private Tab tabTransaction;

    @FXML
    private TextField txtRLimitCus;
    @FXML
    private TextField txtRLimitInven;
    @FXML
    private TextField txtRLimitSup;
    @FXML
    private TextField txtRepairId;
    @FXML
    private TextField txtSalesId;

    private final ReportsModel reportsModel = new ReportsModel();

    @FXML
    public void initialize() {
        initDatePresets();
        initTabListeners();

        // Set Default Date (Today)
        dpFromDate.setValue(LocalDate.now());
        dpToDate.setValue(LocalDate.now());

        btnGenerateSalesInvoice.setDisable(true);
        btnGenerateRepairInvoice.setDisable(true);
    }

    private void initDatePresets() {
        ObservableList<String> presets = FXCollections.observableArrayList(
                "Today", "This Week", "This Month", "Last Month", "This Year"
        );
        cmbDatePreset.setItems(presets);
        cmbDatePreset.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                applyDatePreset(newValue);
            }
        });
    }

    private void applyDatePreset(String preset) {
        LocalDate today = LocalDate.now();
        switch (preset) {
            case "Today":
                dpFromDate.setValue(today);
                dpToDate.setValue(today);
                break;
            case "This Week":
                dpFromDate.setValue(today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
                dpToDate.setValue(today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)));
                break;
            case "This Month":
                dpFromDate.setValue(today.with(TemporalAdjusters.firstDayOfMonth()));
                dpToDate.setValue(today.with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case "Last Month":
                LocalDate lastMonth = today.minusMonths(1);
                dpFromDate.setValue(lastMonth.with(TemporalAdjusters.firstDayOfMonth()));
                dpToDate.setValue(lastMonth.with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case "This Year":
                dpFromDate.setValue(today.with(TemporalAdjusters.firstDayOfYear()));
                dpToDate.setValue(today.with(TemporalAdjusters.lastDayOfYear()));
                break;
        }
    }

    private void initTabListeners() {
        // Logic: Disable Date Filters for Supplier and Customer tabs

        tabSales.setOnSelectionChanged(e -> {
            if (tabSales.isSelected()) hBoxDate.setDisable(false);
        });
        tabRepair.setOnSelectionChanged(e -> {
            if (tabRepair.isSelected()) hBoxDate.setDisable(false);
        });
        tabTransaction.setOnSelectionChanged(e -> {
            if (tabTransaction.isSelected()) hBoxDate.setDisable(false);
        });

        // Disable for Supplier
        tabSupplier.setOnSelectionChanged(e -> {
            if (tabSupplier.isSelected()) hBoxDate.setDisable(true);
        });

        // Disable for Customer
        tabCustomer.setOnSelectionChanged(e -> {
            if (tabCustomer.isSelected()) hBoxDate.setDisable(true);
        });

        tabInventory.setOnSelectionChanged(e -> {
            if (tabInventory.isSelected()) {
                btnLoadInventoryOnAction(null);
                hBoxDate.setDisable(true);
            }
        });
    }

    // ----------------- LOAD BUTTON ACTIONS -----------------

    @FXML
    void btnLoadSaleOnAction(ActionEvent event) {
        System.out.println("Loading Sales Count...");
        System.out.println("From Date: " + dpFromDate.getValue());
        System.out.println("To Date: " + dpToDate.getValue());
        try {
            int count = reportsModel.getSalesCount(dpFromDate.getValue(), dpToDate.getValue());
            System.out.println(dpFromDate.getValue());
            System.out.println(dpToDate.getValue());
            lblRecordCountSales.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnLoadRepairOnAction(ActionEvent event) {
        try {
            int count = reportsModel.getRepairCount(dpFromDate.getValue(), dpToDate.getValue());
            lblRecordCountRepair.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnLoadTransactionOnAction(ActionEvent event) {
        try {
            int count = reportsModel.getTransactionCount(dpFromDate.getValue(), dpToDate.getValue());
            lblRecordCountTransf.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnLoadInventoryOnAction(ActionEvent event) {
        try {
            int count = reportsModel.getInventoryCount();
            lblRecordCountInven.setText(String.valueOf(count));
            txtRLimitInven.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnLoadSupplierOnAction(ActionEvent event) {
        try {
            int count = reportsModel.getSupplierCount();
            lblRecordCountSup.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnLoadCustomerOnAction(ActionEvent event) {
        try {
            int count = reportsModel.getCustomerCount();
            lblRecordCountCus.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    // ----------------- INVOICE / REPORT GENERATION -----------------

    @FXML
    void btnGenerateSalesInvoiceOnAction(ActionEvent event) {
        try {
            int saleId = Integer.parseInt(txtSalesId.getText());
        } catch (Exception e) {
            txtSalesId.setText("");
        }
        String id = txtSalesId.getText();
        if (id.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter Sales ID").show();
            return;
        }

        try {
            if (reportsModel.isSaleExist(id)) {
                lblSalesExist.setText("Invoice Found!");
                lblSalesExist.setStyle("-fx-text-fill: green;");
                btnGenerateSalesInvoice.setDisable(false);

                System.out.println("Printing Invoice for Sale ID: " + id);
            } else {
                lblSalesExist.setText("Invoice Not Found");
                lblSalesExist.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateSalesInvoice(ActionEvent event) {
        String saleId = txtSalesId.getText();
        GenerateReports.generateInvoice(Integer.parseInt(saleId), "salesInvoice", "SALE");
        System.out.println("Generating Sales Invoice for Sale ID: " + saleId);
        btnGenerateSalesInvoice.setDisable(true);
    }

    @FXML
    void btnGenerateRepairInvoiceOnAction(ActionEvent event) {
        try {
            int repairId = Integer.parseInt(txtRepairId.getText());
        } catch (Exception e) {
            txtRepairId.setText("");
        }
        String id = txtRepairId.getText();
        if (id.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter Repair ID").show();
            return;
        }

        try {
            if (reportsModel.isRepairExist(id)) {
                lblRepairExists.setText("Job Found!");
                lblRepairExists.setStyle("-fx-text-fill: green;");
                btnGenerateRepairInvoice.setDisable(false);
                System.out.println("Printing Invoice for Repair ID: " + id);
            } else {
                lblRepairExists.setText("Job Not Found");
                lblRepairExists.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateRepairInvoice(ActionEvent event) {
        String repairId = txtRepairId.getText();
        GenerateReports.generateInvoice(Integer.parseInt(repairId), "repairInvoice2", "REPAIR");
        System.out.println("Generating Repair Invoice for Repair ID: " + repairId);
        btnGenerateRepairInvoice.setDisable(true);
    }


    // ----------------- REPORT GENERATION BUTTON ACTIONS -----------------
    @FXML
    void btnGenerateReportSaleOnAction(ActionEvent event) {

        Date fromDate = Date.valueOf(dpFromDate.getValue());
        Date toDate = Date.valueOf(dpToDate.getValue());

        GenerateReports.generateReport(dpFromDate.getValue(), dpToDate.getValue(), "sales_report");
        System.out.println("Generating Sales Report...");
    }

    @FXML
    void btnGenerateReportRepairOnAction(ActionEvent event) {
        Date fromDate = Date.valueOf(dpFromDate.getValue());
        Date toDate = Date.valueOf(dpToDate.getValue());

        GenerateReports.generateReport(dpFromDate.getValue(), dpToDate.getValue(), "repair_report");
        System.out.println("Generating Repair Report...");
    }

    @FXML
    void btnGenerateReportTransfOnAction(ActionEvent event) {
        Date fromDate = Date.valueOf(dpFromDate.getValue());
        Date toDate = Date.valueOf(dpToDate.getValue());

        GenerateReports.generateReport(dpFromDate.getValue(), dpToDate.getValue(), "transaction_report");
        System.out.println("Generating Repair Report...");
        System.out.println("Generating Transaction Report...");
    }

    @FXML
    void btnGenerateReportInvenOnAction(ActionEvent event) {
        try{

        if(txtRLimitInven.getText().isEmpty()){
            new Alert(Alert.AlertType.WARNING, "Please enter record limit").show();
            return;
        }


            int limit = Integer.parseInt(txtRLimitInven.getText());
            if(limit<=0){
                new Alert(Alert.AlertType.WARNING, "Please enter a valid record limit").show();
                return;
            }
            GenerateReports.generateReport("inventory_report", limit);
        } catch (Exception e) {
            ETecAlerts.showAlert(Alert.AlertType.ERROR, "Report Generation Error", "Cannot generate inventory report");
            return;
        }

        System.out.println("Generating Inventory Report...");
    }

    @FXML
    void btnGenerateReportSupOnAction(ActionEvent event) {
        System.out.println("Generating Supplier Report...");
    }

    @FXML
    void btnGenerateReportCusOnAction(ActionEvent event) {
        System.out.println("Generating Customer Report...");
    }
}