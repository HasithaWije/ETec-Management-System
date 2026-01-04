package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.model.RepairJobModel;
import lk.ijse.etecmanagementsystem.util.GenerateReports;
import lk.ijse.etecmanagementsystem.util.LoginUtil;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepairCheckoutController {

    @FXML
    private Label lblJobId;
    @FXML
    private Label lblCustomer;
    @FXML
    private Label lblPartsTotal;
    @FXML
    private Label lblLaborTotal;
    @FXML
    private Label lblGrandTotal;
    @FXML
    private Label lblBalance;

    @FXML
    private Label lblDiscount;
    @FXML
    private TextField txtDiscount;
    @FXML
    private Label lblSubTotal;

    @FXML
    private ComboBox<String> cmbPaymentMethod;
    @FXML
    private TextField txtAmountPaid;

    // --- DATA ---
    private RepairJobTM jobTM;
    private RepairDashboardController mainController;
    private final RepairJobModel repairModel = new RepairJobModel();

    private double subTotal = 0.0;
    private double grandTotal = 0.0;
    private double discount = 0.0;
    private String serialNumber = "";

    @FXML
    public void initialize() {
        // Setup Payment Methods
        cmbPaymentMethod.setItems(FXCollections.observableArrayList("CASH", "CARD", "TRANSFER"));
        cmbPaymentMethod.getSelectionModel().selectFirst();

        // Add Listener to calculate Balance/Due in real-time
        txtAmountPaid.textProperty().addListener((obs, oldVal, newVal) -> calculateBalance());

        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal != null && newVal.isEmpty()) {
                    return;
                }
                double discount = Double.parseDouble(newVal);
                if (discount < 0 || discount > subTotal) {
                    throw new NumberFormatException();
                } else {
                    lblDiscount.setText(String.format("%.2f", discount));
                    double grandTotal = subTotal - discount;
                    lblGrandTotal.setText(String.format("%.2f", grandTotal));
                    this.grandTotal = grandTotal;
                    txtAmountPaid.setText(String.format("%.2f", grandTotal));
                    this.discount = discount;

                }
            } catch (NumberFormatException e) {
                txtDiscount.setText(oldVal);
            }
        });

        txtAmountPaid.requestFocus();
    }

    public void setInvoiceData(RepairJobTM job, List<RepairPartTM> repairParts, RepairDashboardController main) {
        this.jobTM = job;
        this.mainController = main;

        // Display Basic Info
        lblJobId.setText("#" + job.getRepairId());
        lblCustomer.setText(job.getCustomerName());


        double labor = job.getOriginalDto().getLaborCost();
        double parts = job.getOriginalDto().getPartsCost();
        this.subTotal = job.getOriginalDto().getTotalAmount();

        lblPartsTotal.setText(String.format("%.2f", parts));
        lblLaborTotal.setText(String.format("%.2f", labor));
        lblSubTotal.setText(String.format("%.2f", subTotal));
        lblGrandTotal.setText(String.format("%.2f", subTotal));
        this.grandTotal = subTotal;
        for (RepairPartTM part : repairParts) {
            if (part.getSerialNumber() != null) {
                // Check for null OR empty to be safe
                if (serialNumber == null || serialNumber.isEmpty() || part.getSerialNumber().startsWith("REPAIR-")) {
                    System.out.println("Adding serial: " + part.getSerialNumber());
                    serialNumber = part.getSerialNumber();
                }
            }
        }

        // Default: Assume they pay full amount
        txtAmountPaid.setText(String.valueOf(grandTotal));
    }

    private void calculateBalance() {
        try {
            String text = txtAmountPaid.getText();
            if (text.isEmpty()) {
                lblBalance.setText("Due: " + grandTotal);
                lblBalance.setStyle("-fx-text-fill: red;");
                return;
            }

            double paid = Double.parseDouble(text);
            double balance = paid - grandTotal;

            if (balance >= 0) {
                // Change to give back
                lblBalance.setText("Change: " + String.format("%.2f", balance));
                lblBalance.setStyle("-fx-text-fill: green;");
            } else {
                // Still owing money (Partial)
                lblBalance.setText("Due: " + String.format("%.2f", Math.abs(balance)));
                lblBalance.setStyle("-fx-text-fill: red;");
            }

        } catch (NumberFormatException e) {
            lblBalance.setText("Invalid Amount");
        }
    }

    @FXML
    private void handleConfirm() {
        try {
            double paid = Double.parseDouble(txtAmountPaid.getText());

            if (paid < 0) throw new NumberFormatException();
            if (paid > grandTotal) {
                new Alert(Alert.AlertType.ERROR, "Paid amount cannot exceed the grand total.").showAndWait();
                return;
            }
            if (paid == 0) {
                new Alert(Alert.AlertType.ERROR, "Paid amount cannot be zero.").showAndWait();
                return;
            } else if (paid < grandTotal) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "The amount paid is less than the grand total. This will mark the job as PARTIALLY PAID. Do you want to proceed?",
                        ButtonType.YES, ButtonType.NO);
                alert.setTitle("Confirm Partial Payment");
                alert.setHeaderText(null);
                alert.showAndWait();
                if (alert.getResult() != ButtonType.YES) {
                    return;
                }
            }


            String method = cmbPaymentMethod.getValue();
            int userId = LoginUtil.getUserId();
            int cusId = jobTM.getOriginalDto().getCusId();
            double partsTotal = jobTM.getOriginalDto().getPartsCost();

            // CALL MODEL TRANSACTION
            boolean success = repairModel.completeCheckout(
                    jobTM.getRepairId(),
                    cusId,
                    userId,
                    grandTotal,
                    discount,
                    partsTotal,
                    paid,
                    method,
                    serialNumber
            );

            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Job Delivered Successfully!").showAndWait();
                mainController.refreshList(); // Reload Dashboard
//                generateInvoice(jobTM.getRepairId());
                GenerateReports.generateInvoice(jobTM.getRepairId(), "repairInvoice2", "REPAIR");
                closeWindow();
            }

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Please enter a valid amount.").show();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) lblJobId.getScene().getWindow()).close();
    }
}