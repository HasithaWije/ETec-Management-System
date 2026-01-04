package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.tm.PendingRepairTM;
import lk.ijse.etecmanagementsystem.dto.tm.PendingSaleTM;
import lk.ijse.etecmanagementsystem.dto.tm.TransactionTM;
import lk.ijse.etecmanagementsystem.model.TransactionsModel;
import lk.ijse.etecmanagementsystem.util.LoginUtil;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionsController {

    // --- UI Components (Same as before) ---
    @FXML
    private Label lblTotalIncome, lblTotalExpense, lblNetProfit;
    @FXML
    private DatePicker dpFromDate, dpToDate;
    @FXML
    private ComboBox<String> comboTypeFilter;
    @FXML
    private TextField txtSearchHistory;

    // Tables
    @FXML
    private TableView<TransactionTM> tblHistory;
    @FXML
    private TableColumn<TransactionTM, Integer> colHistId;
    @FXML
    private TableColumn<TransactionTM, String> colHistDate, colHistType, colHistRef, colHistFlow, colHistUser;
    @FXML
    private TableColumn<TransactionTM, Double> colHistAmount;

    @FXML
    private TableView<PendingSaleTM> tblPendingSales;
    @FXML
    private TableColumn<PendingSaleTM, Integer> colSaleId;
    @FXML
    private TableColumn<PendingSaleTM, String> colSaleCustomer;
    @FXML
    private TableColumn<PendingSaleTM, Double> colSaleTotal, colSaleDue;
    @FXML
    private TableColumn<PendingSaleTM, Void> colSaleAction;

    @FXML
    private TableView<PendingRepairTM> tblPendingRepairs;
    @FXML
    private TableColumn<PendingRepairTM, Integer> colRepairId;
    @FXML
    private TableColumn<PendingRepairTM, String> colRepairDevice, colRepairCustomer;
    @FXML
    private TableColumn<PendingRepairTM, Double> colRepairDue;
    @FXML
    private TableColumn<PendingRepairTM, Void> colRepairAction;

    // --- Model Instance ---
    private final TransactionsModel transactionsModel = new TransactionsModel();

    public void initialize() {
        setupTables();
        setupFilters();
        loadDashboardData();
        loadHistory();
        loadPendingSettlements();

    }

    private void setupTables() {
        colHistId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colHistDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHistType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colHistRef.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colHistFlow.setCellValueFactory(new PropertyValueFactory<>("flow"));
        colHistAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colHistUser.setCellValueFactory(new PropertyValueFactory<>("user"));

        colSaleId.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        colSaleCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colSaleTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colSaleDue.setCellValueFactory(new PropertyValueFactory<>("balanceDue"));
        addSettleButtonToSales();

        colRepairId.setCellValueFactory(new PropertyValueFactory<>("repairId"));
        colRepairDevice.setCellValueFactory(new PropertyValueFactory<>("device"));
        colRepairCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colRepairDue.setCellValueFactory(new PropertyValueFactory<>("balanceDue"));
        addSettleButtonToRepairs();
    }

    private void setupFilters() {
        dpFromDate.setValue(LocalDate.now());
        dpToDate.setValue(LocalDate.now());
        comboTypeFilter.setItems(FXCollections.observableArrayList("All", "SALE_PAYMENT", "REPAIR_PAYMENT", "EXPENSE", "SUPPLIER_PAYMENT"));
        comboTypeFilter.getSelectionModel().selectFirst();
    }

    @FXML
    public void loadHistory() {
        try {
            Date fromD = Date.valueOf(dpFromDate.getValue());
            Date toD = Date.valueOf(dpToDate.getValue());
            List<TransactionTM> list = transactionsModel.getAllTransactions(fromD, toD);
            tblHistory.setItems(FXCollections.observableArrayList(list));
            handleComboTypeFilter();
            handleSearchHistory();
            loadDashboardData();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading history: " + e.getMessage()).show();
        }
    }

    private void handleComboTypeFilter() {
        try {
            String selectedType = comboTypeFilter.getSelectionModel().getSelectedItem();
            ObservableList<TransactionTM> allTransactions = tblHistory.getItems();

            if (selectedType.equals("All")) {
                tblHistory.setItems(allTransactions);
            } else {
                ObservableList<TransactionTM> filteredList = allTransactions.filtered(
                        transaction -> transaction.getType().equalsIgnoreCase(selectedType)
                );
                tblHistory.setItems(filteredList);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void handleSearchHistory() {
        try {
            String searchText = txtSearchHistory.getText().toLowerCase();
            ObservableList<TransactionTM> allTransactions = tblHistory.getItems();

            if (searchText.isEmpty()) {
                tblHistory.setItems(allTransactions);
            } else {
                ObservableList<TransactionTM> filteredList = allTransactions.filtered(
                        transaction -> transaction.getReference().toLowerCase().contains(searchText) ||
                                transaction.getUser().toLowerCase().contains(searchText)
                );
                tblHistory.setItems(filteredList);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @FXML
    private void handleResetHistoryFilters() {
        dpFromDate.setValue(LocalDate.now());
        dpToDate.setValue(LocalDate.now());
        comboTypeFilter.getSelectionModel().selectFirst();
        txtSearchHistory.setText("");
        loadHistory();
    }

    public void loadPendingSettlements() {
        try {
            tblPendingSales.setItems(transactionsModel.getPendingSales());
            tblPendingRepairs.setItems(transactionsModel.getPendingRepairs());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading pending items: " + e.getMessage()).show();
        }
    }

    private void loadDashboardData() {
        try {
            double[] stats = transactionsModel.getDashboardStats(Date.valueOf(dpFromDate.getValue()), Date.valueOf(dpToDate.getValue()));
            double in = stats[0];
            double out = stats[1];
            double net = in - out;

            lblTotalIncome.setText(String.format("%.2f", in));
            lblTotalExpense.setText(String.format("%.2f", out));
            lblNetProfit.setText(String.format("%.2f", net));

            if (net < 0) lblNetProfit.setStyle("-fx-text-fill: red;");
            else lblNetProfit.setStyle("-fx-text-fill: #2980b9;");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Actions ---

    @FXML
    private void handleManualTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/ManualTransaction.fxml"));
            Parent root = loader.load();

            // Get Controller
            ManualTransactionController controller = loader.getController();

            // Handle Save
            controller.setOnSave(result -> {
                saveManualTransaction(result.getType(), result.getAmount(), result.getMethod(), result.getNote());
            });

            Stage stage = new Stage();
            stage.setTitle("New Transaction");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // FIX: Use any existing node to get the Window, instead of 'tblView' if it causes issues
            if (lblTotalIncome.getScene() != null) {
                stage.initOwner(lblTotalIncome.getScene().getWindow());
            }

            stage.showAndWait();

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load transaction window.").show();
        }
    }

    private void saveManualTransaction(String type, double amount, String method, String note) {
        try {
            // Call Model
            boolean success = transactionsModel.saveManualTransaction(type, amount, method, note, LoginUtil.getUserId()); // User ID 1
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Transaction Saved!").show();
                loadDashboardData();
                loadHistory();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to save.").show();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    // --- Settle Payment Logic ---

    private void addSettleButtonToSales() {
        colSaleAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Pay");

            {
                btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    PendingSaleTM sale = getTableView().getItems().get(getIndex());
                    showPaymentDialog("SALE", sale.getSaleId(), sale.getBalanceDue());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void addSettleButtonToRepairs() {
        colRepairAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Pay");

            {
                btn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    PendingRepairTM repair = getTableView().getItems().get(getIndex());
                    showPaymentDialog("REPAIR", repair.getRepairId(), repair.getBalanceDue());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void showPaymentDialog(String type, int id, double dueAmount) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(dueAmount));
        dialog.setTitle("Settle Balance");
        dialog.setHeaderText("Enter Payment for " + type + " #" + id);
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                String newPayStatus = "PAID";
                if (amount < 0) {
                    new Alert(Alert.AlertType.ERROR, "Amount cannot be negative!").show();
                    return;
                }
                if (amount > dueAmount) {
                    new Alert(Alert.AlertType.ERROR, "Amount cannot exceed balance due!").show();
                    return;
                }
                if(amount < dueAmount){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "The amount entered is less than the balance due. This will mark the " + type.toLowerCase() + " as PARTIALLY PAID. Do you want to proceed?",
                            ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Confirm Partial Payment");
                    alert.setHeaderText(null);
                    Optional<ButtonType> confirmationResult = alert.showAndWait();
                    if (confirmationResult.isEmpty() || confirmationResult.get() != ButtonType.YES) {
                        return;
                    }
                    newPayStatus = "PARTIAL";
                }

                // Call Process Method
                processPayment(type, id, amount, newPayStatus);
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid Amount").show();
            }
        });
    }

    private void processPayment(String type, int id, double amount, String newPayStatus) {
        try {
            // Delegate complex logic to Model
            boolean success = transactionsModel.settlePayment(type, id, amount, LoginUtil.getUserId(), newPayStatus); // User ID 1
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Payment Successful!").show();
                loadPendingSettlements();
                loadHistory();
                loadDashboardData();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }


}