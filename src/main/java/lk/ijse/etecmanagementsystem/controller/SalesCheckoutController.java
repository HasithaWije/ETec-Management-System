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
import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
import lk.ijse.etecmanagementsystem.dto.SalesDTO;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;
import lk.ijse.etecmanagementsystem.model.SalesModel;
import lk.ijse.etecmanagementsystem.util.Login;
import lk.ijse.etecmanagementsystem.util.PaymentStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class SalesCheckoutController {

    // --- FXML Bindings (Must match fx:ids in checkout_form.fxml) ---
    @FXML private Label lblDate;
    @FXML private Label lblTime;
    @FXML private Label lblEmployee;

    // Customer Details
    @FXML private Label lblCusName;
    @FXML private Label lblCusContact;
    @FXML private Label lblCusEmail;
    @FXML private Label lblCusAddress;

    // Table
    @FXML private TableView<ItemCartTM> tblReceipt;
    @FXML private TableColumn<ItemCartTM, String> colItem;
    @FXML private TableColumn<ItemCartTM, String> colSerial;
    @FXML private TableColumn<ItemCartTM, Integer> colWarranty;
    @FXML private TableColumn<ItemCartTM, Integer> colQty;
    @FXML private TableColumn<ItemCartTM, Double> colTotal;

    // Totals
    @FXML private Label lblSubTotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblGrandTotal;
    @FXML private Label lblPayableAmount;

    // Payment Inputs
    @FXML private TextField txtCash;
    @FXML private TextField txtBalance;
    @FXML private Button btnPayPrint;
    @FXML private Button btnCancel;

    // --- Controller Data ---
    private ObservableList<ItemCartTM> cartItems;
    private double subTotalAmount;
    private double discountAmount;
    private double grandTotalAmount;
    private CustomerDTO customer;
    private SalesController salesControllerInstance; // Reference to refresh the main window

    // --- MODEL ---
    private final SalesModel salesModel = new SalesModel();

    @FXML
    public void initialize() {
        setupClock();
        setupTable();

        // 1. Set current user
        if (Login.getUserName() != null) {
            lblEmployee.setText(Login.getUserName());
        } else {
            lblEmployee.setText("Admin");
        }

        // 2. Add Listener for Cash Input (Calculate Balance real-time)
        txtCash.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow only numbers and one decimal point
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtCash.setText(oldValue);
                return;
            }
            calculateBalance();
        });

        // 3. Cancel Button Action
        btnCancel.setOnAction(event -> closeWindow());

        // 4. Pay Button Action
        btnPayPrint.setOnAction(this::handlePayment);
    }

    /**
     * This method is called from SalesController to pass data into this popup.
     */
    public void setInvoiceData(SalesController parentController, CustomerDTO customer,
                               ObservableList<ItemCartTM> items,
                               double subTotal, double discount, double grandTotal) {

        this.salesControllerInstance = parentController;
        this.customer = customer;
        this.cartItems = items;
        this.subTotalAmount = subTotal;
        this.discountAmount = discount;
        this.grandTotalAmount = grandTotal;

        // Populate Customer Labels
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

        // Populate Table
        tblReceipt.setItems(items);

        // Populate Totals
        lblSubTotal.setText(String.format("%.2f", subTotal));
        lblDiscount.setText(String.format("%.2f", discount));
        lblGrandTotal.setText(String.format("%.2f", grandTotal));
        lblPayableAmount.setText(String.format("%.2f", grandTotal));
    }

    private void handlePayment(ActionEvent event) {
        // 1. Validate Cash
        String cashText = txtCash.getText();
        if (cashText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter the cash amount received.").show();
            return;
        }

        double cashGiven = Double.parseDouble(cashText);
        if (cashGiven < grandTotalAmount) {
            new Alert(Alert.AlertType.ERROR, "Insufficient cash! Need " + (grandTotalAmount - cashGiven) + " more.").show();
            return;
        }

        // 2. Prepare Sales Data
        int totalQty = cartItems.stream().mapToInt(ItemCartTM::getQuantity).sum();
        int customerId = (customer != null) ? customer.getId() : 0; // 0 represents NULL in your logic
        int userId = Login.getUserId(); // Ensure Login class has getUserId(), or use 1 for default Admin

        // NOTE: Your SalesModel applies this single warranty value to ALL items.
        // Ideally, warranty should be per-item, but we must follow your SalesDTO structure.
        int generalWarranty = 0;
        if(!cartItems.isEmpty()) {
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
                generalWarranty,        // Customer Warranty (See Note above)
                PaymentStatus.PAID,     // Payment Status Enum
                "Point of Sale Transaction" // Description
        );

        // 3. Save to Database via SalesModel
        try {
            boolean isPlaced = salesModel.placeOrder(salesDTO, new ArrayList<>(cartItems));

            if (isPlaced) {
                // 4. Success Flow
                new Alert(Alert.AlertType.INFORMATION, "Order placed successfully!").showAndWait();

                // Print Bill Logic
                double balance = cashGiven - grandTotalAmount;
                printBill(salesDTO.getSaleId(), cashGiven, balance);

                // Clear the main Sales UI
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
        System.out.println("Printing Bill for Sale ID: " + saleId);
    }
}