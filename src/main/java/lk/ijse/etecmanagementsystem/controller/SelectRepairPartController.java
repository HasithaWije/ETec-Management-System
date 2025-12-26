package lk.ijse.etecmanagementsystem.controller;


import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.util.ProductCondition; // <--- Import Enum
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;

public class SelectRepairPartController {

    @FXML private TextField txtSearch;
    @FXML private TableView<RepairPartTM> tblStock;
    @FXML private TableColumn<RepairPartTM, Integer> colId;
    @FXML private TableColumn<RepairPartTM, String> colName;
    @FXML private TableColumn<RepairPartTM, String> colSerial;
    @FXML private TableColumn<RepairPartTM, String> colCondition; // <--- NEW INJECTION
    @FXML private TableColumn<RepairPartTM, Double> colPrice;

    private RepairDashboardController mainController;
    private ObservableList<RepairPartTM> stockList = FXCollections.observableArrayList();

    public void setMainController(RepairDashboardController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));

        loadAvailableStock();

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
        stockList.clear();

        // MOCK DATA UPDATED WITH CONDITION
        stockList.add(new RepairPartTM(501, "iPhone X Display", "SN-LCD-001", ProductCondition.BRAND_NEW, 15000.00));
        stockList.add(new RepairPartTM(502, "iPhone X Battery", "SN-BAT-009", ProductCondition.USED, 5000.00));
        stockList.add(new RepairPartTM(503, "Dell 15 Ram 8GB", "SN-RAM-888", ProductCondition.BRAND_NEW, 8500.00));
        stockList.add(new RepairPartTM(504, "SSD 256GB Samsung", "SN-SSD-256", ProductCondition.USED, 12000.00));
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