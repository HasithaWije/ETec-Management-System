package lk.ijse.etecmanagementsystem.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.bo.SalesBOImpl;
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;
import lk.ijse.etecmanagementsystem.util.GenerateReports;
import lk.ijse.etecmanagementsystem.util.LoginUtil;
import lk.ijse.etecmanagementsystem.util.PaymentStatus;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SalesCheckoutController {

    @FXML
    private Label lblDate;
    @FXML
    private Label lblTime;
    @FXML
    private Label lblEmployee;

    @FXML
    private Label lblCusName;
    @FXML
    private Label lblCusContact;
    @FXML
    private Label lblCusEmail;
    @FXML
    private Label lblCusAddress;

    @FXML
    private TableView<ItemCartTM> tblReceipt;
    @FXML
    private TableColumn<ItemCartTM, String> colItem;
    @FXML
    private TableColumn<ItemCartTM, String> colSerial;
    @FXML
    private TableColumn<ItemCartTM, Integer> colWarranty;
    @FXML
    private TableColumn<ItemCartTM, Integer> colQty;
    @FXML
    private TableColumn<ItemCartTM, Double> colTotal;

    @FXML
    private Label lblSubTotal;
    @FXML
    private Label lblDiscount;
    @FXML
    private Label lblGrandTotal;
    @FXML
    private Label lblPayableAmount;

    @FXML
    private TextField txtCash;
    @FXML
    private TextField txtBalance;
    @FXML
    private Button btnPayPrint;
    @FXML
    private Button btnCancel;

    private ObservableList<ItemCartTM> cartItems;
    private double subTotalAmount;
    private double discountAmount;
    private double grandTotalAmount;
    private CustomerDTO customer;
    private SalesController salesControllerInstance;

    SalesBOImpl salesBO = new SalesBOImpl();

    @FXML
    public void initialize() {
        setupClock();
        setupTable();


        if (LoginUtil.getUserName() != null) {
            lblEmployee.setText(LoginUtil.getUserName());
        } else {
            lblEmployee.setText("Admin");
        }


        txtCash.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow only numbers and one decimal point
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtCash.setText(oldValue);
                return;
            }
            calculateBalance();
        });

        btnCancel.setOnAction(event -> closeWindow());

        btnPayPrint.setOnAction(this::handlePayment);
    }

    public void setInvoiceData(SalesController parentController, CustomerDTO customer,
                               ObservableList<ItemCartTM> items,
                               double subTotal, double discount, double grandTotal) {

        this.salesControllerInstance = parentController;
        this.customer = customer;
        this.cartItems = items;
        this.subTotalAmount = subTotal;
        this.discountAmount = discount;
        this.grandTotalAmount = grandTotal;

        if (customer != null) {
            lblCusName.setText(customer.getName());
            lblCusContact.setText(customer.getNumber());
            lblCusEmail.setText(customer.getEmailAddress() == null ? "-" : customer.getEmailAddress());
            lblCusAddress.setText(customer.getAddress() == null ? "-" : customer.getAddress());
        } else {
            lblCusName.setText("Walk-in Customer");
            lblCusContact.setText("-");
            lblCusEmail.setText("-");
            lblCusAddress.setText("-");
        }

        tblReceipt.setItems(items);

        lblSubTotal.setText(String.format("%.2f", subTotal));
        lblDiscount.setText(String.format("%.2f", discount));
        lblGrandTotal.setText(String.format("%.2f", grandTotal));
        lblPayableAmount.setText(String.format("%.2f", grandTotal));
    }

    private void handlePayment(ActionEvent event) {
        // 1. Validate Cash
        String cashText = txtCash.getText();
        double cashGiven = 0.0;
        PaymentStatus paymentStatus = PaymentStatus.PAID;
        try {
            cashGiven = Double.parseDouble(cashText);

            if (cashGiven < 0) {
                throw new Exception();
            }
            if (cashGiven > grandTotalAmount) {
                new Alert(Alert.AlertType.ERROR, "Cash given cannot exceed the grand total!").show();
                return;
            }

            if (cashText.isEmpty() || cashGiven == 0.0) {
                new Alert(Alert.AlertType.ERROR, "Please enter a valid cash amount!").show();
                return;
            } else if (cashGiven < grandTotalAmount) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Provided cash amount not enough! Do you accept partial Payment?", ButtonType.YES, ButtonType.NO);
                alert.setTitle("Confirm Partial Payment");
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    paymentStatus = PaymentStatus.PARTIAL;
                } else {
                    return;
                }
            }

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Insufficient cash!").show();
            return;
        }


        int totalQty = cartItems.stream().mapToInt(ItemCartTM::getQuantity).sum();
        int customerId = (customer != null) ? customer.getId() : 0;
        int userId = LoginUtil.getUserId();


        int generalWarranty = 0;
        if (!cartItems.isEmpty()) {
            generalWarranty = cartItems.get(0).getWarrantyMonths();
        }

        SalesDTO salesDTO = new SalesDTO(
                0,                      // Sale ID (Auto-generated)
                customerId,             // Customer ID
                userId,                 // User ID
                totalQty,               // Total Qty
                new Date(),             // Sale Date
                subTotalAmount,         // Sub Total
                discountAmount,         // Discount
                grandTotalAmount,       // Grand Total
                cashGiven,              // Paid Amount
                generalWarranty,        // Customer Warranty (See Note above)
                paymentStatus,     // Payment Status Enum
                "Point of Sale Transaction" // Description
        );


        try {
            boolean isPlaced = salesBO.placeOrder(salesDTO, new ArrayList<>(cartItems));

            if (isPlaced) {

                new Alert(Alert.AlertType.INFORMATION, "Order placed successfully!").showAndWait();

                double balance = cashGiven - grandTotalAmount;
                printBill(salesDTO.getSaleId(), cashGiven, balance);

                if (salesControllerInstance != null) {
                    salesControllerInstance.resetAllFields();
                }

                closeWindow();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to place order. Please try again.").show();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }

    private void calculateBalance() {
        try {
            if (txtCash.getText().isEmpty()) {
                txtBalance.setText("0.00");
                return;
            }
            double cash = Double.parseDouble(txtCash.getText());
            double balance = cash - grandTotalAmount;
            txtBalance.setText(String.format("%.2f", balance));

            if (balance < 0) {
                txtBalance.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-background-color: #ecf0f1;");
            } else {
                txtBalance.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 18px; -fx-background-color: #ecf0f1;");
            }

        } catch (NumberFormatException e) {
            txtBalance.setText("Error");
        }
    }

    private void setupTable() {
        // Columns map to ItemCartTM getters
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serialNo"));
        colWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyMonths"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void setupClock() {
        lblDate.setText(LocalDate.now().toString());
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblTime.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void printBill(int saleId, double cash, double balance) {

        // Implement JasperReports logic here using the 'saleId'
        GenerateReports.generateInvoice(saleId, "salesInvoice", "SALE");
        System.out.println("Printing Bill for Sale ID: " + saleId);

    }


}