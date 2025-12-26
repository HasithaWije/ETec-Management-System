package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.model.RepairPartsModel; // Import Model
import java.sql.SQLException;
import java.util.List;

public class SelectRepairPartController {

    @FXML private TextField txtSearch;
    @FXML private TableView<RepairPartTM> tblStock;
    @FXML private TableColumn<RepairPartTM, Integer> colId;
    @FXML private TableColumn<RepairPartTM, String> colName;
    @FXML private TableColumn<RepairPartTM, String> colSerial;
    @FXML private TableColumn<RepairPartTM, String> colCondition;
    @FXML private TableColumn<RepairPartTM, Double> colPrice;

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
    private void handleAdd() {
        RepairPartTM selectedItem = tblStock.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an item.").show();
            return;
        }
        if (mainController != null) {
            mainController.addPartToTable(selectedItem);
        }
        ((Stage) txtSearch.getScene().getWindow()).close();
    }

    @FXML private void handleCancel() { ((Stage) txtSearch.getScene().getWindow()).close(); }
}