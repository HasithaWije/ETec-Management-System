package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.model.RepairPartsModel; // Import Model

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SelectRepairPartController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<RepairPartTM> tblStock;
    @FXML
    private TableColumn<RepairPartTM, Integer> colId;
    @FXML
    private TableColumn<RepairPartTM, String> colName;
    @FXML
    private TableColumn<RepairPartTM, String> colSerial;
    @FXML
    private TableColumn<RepairPartTM, String> colCondition;
    @FXML
    private TableColumn<RepairPartTM, Double> colPrice;

    private RepairDashboardController mainController;
    private final ObservableList<RepairPartTM> stockList = FXCollections.observableArrayList();

    // Model Instance
    private final RepairPartsModel repairPartsModel = new RepairPartsModel();

    public void setMainController(RepairDashboardController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Setup Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));

        // Load Real Data
        loadAvailableStock();

        // Setup Search Filter
        FilteredList<RepairPartTM> filteredList = new FilteredList<>(stockList, p -> true);
        tblStock.setItems(filteredList);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerVal = newVal.toLowerCase();
                return item.getItemName().toLowerCase().contains(lowerVal) ||
                        item.getSerialNumber().toLowerCase().contains(lowerVal);
            });
        });
    }

    private void loadAvailableStock() {
        try {
            stockList.clear();
            List<RepairPartTM> dbList = repairPartsModel.getAllAvailableParts();
            stockList.addAll(dbList);

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load stock: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleAddNewItem() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/unitManagement.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Product Unit Management");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh stock list after adding new item
            loadAvailableStock();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not open Product Unit Management").show();

        }
    }

    @FXML
    private void handleAdd() {
        RepairPartTM selectedItem = tblStock.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an item.").show();
            return;
        }
        showDetailsDialog(selectedItem);
        if (mainController != null) {
            mainController.addPartToTable(selectedItem);
        }
        ((Stage) txtSearch.getScene().getWindow()).close();
    }

    private void showDetailsDialog(RepairPartTM item) {
        // Create the custom dialog.
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Item Details");
        dialog.setHeaderText("Please confirm the details for the repair part.");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Add to Repair", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the layout for the popup
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create UI controls
        TextField txtPrice = new TextField();
        txtPrice.setText(String.valueOf(item.getUnitPrice())); // Pre-fill with existing price

        // Add controls to grid (Context info is Label, Editable info is TextField)
        grid.add(new Label("Item ID:"), 0, 0);
        grid.add(new Label(String.valueOf(item.getItemId())), 1, 0);

        grid.add(new Label("Item Name:"), 0, 1);
        grid.add(new Label(item.getItemName()), 1, 1);

        grid.add(new Label("Selling Price:"), 0, 2);
        grid.add(txtPrice, 1, 2);


        dialog.getDialogPane().setContent(grid);

        // Convert the result when the button is clicked
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == loginButtonType) {
            try {
                // 1. Get the new price
                double newPrice = Double.parseDouble(txtPrice.getText());

                if (newPrice < 0) {
                    new Alert(Alert.AlertType.ERROR, "Price cannot be negative.").show();
                    return;
                }
                if (newPrice == 0 || newPrice < item.getUnitPrice() / 2) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "The price seems unusually low. Please confirm.", ButtonType.OK, ButtonType.CANCEL);
                    alert.setTitle("Confirm Low Price");
                    alert.setHeaderText("Unusually Low Price");
                    alert.showAndWait();
                    if (alert.getResult() != ButtonType.OK) {
                        return;
                    }
                }

                // 2. Update the item with new price and warranty
                item.setUnitPrice(newPrice);


            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid Price. Please enter a valid number.").show();
            }
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) txtSearch.getScene().getWindow()).close();
    }
}