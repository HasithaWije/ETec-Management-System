package lk.ijse.etecmanagementsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.bo.InventoryBOImpl;
import lk.ijse.etecmanagementsystem.dao.ProductItemDAOImpl;
import lk.ijse.etecmanagementsystem.dao.SupplierDAOImpl;
import lk.ijse.etecmanagementsystem.dto.ProductItemDTO;
import lk.ijse.etecmanagementsystem.server.BarcodeServer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitManagementController {

    @FXML
    private TabPane tabPaneProductItem;
    @FXML
    private Tab tPAddNew;
    @FXML
    private Tab tPUpdateStatus;

    // --- TAB 1: VIEW ---
    @FXML
    private ComboBox<String> cmbViewProduct;
    @FXML
    private TextField txtViewSearch;
    @FXML
    private TableView<ProductItemDTO> tblView;
    @FXML
    private TableColumn<?, ?> colViewSerial, colViewSupplier, colViewRemaining, colViewSupWar, colViewCustWar, colViewStatus;

    // --- TAB 2: ADD ---
    @FXML
    private ComboBox<String> cmbProduct, cmbSupplier;
    @FXML
    private Label lblProductId; // <--- NEW: Add this to FXML (fx:id="lblProductId")
    @FXML
    private TextField txtSupplierWarranty, txtCustomerWarranty, txtSerialNumber;
    @FXML
    private Label lblStagingCount;
    @FXML
    private TableView<String> tblStaging;
    @FXML
    private TableColumn<String, String> colStagedSerial;
    @FXML
    private Button btnSaveAll;

    @FXML
    private TableView<ProductItemDTO> tblHistory;
    @FXML
    private TableColumn<?, ?> colHistSerial, colHistSupplier, colHistSupWar, colHistCustWar, colHistStatus;

    // --- TAB 3: CORRECTION ---
    @FXML
    private TextField txtFixSearch, txtFixSerial, txtFixSupWar;
    @FXML
    private ComboBox<String> cmbFixProduct, cmbFixSupplier;
    @FXML
    private VBox vboxFixDetails;

    // --- TAB 4: STATUS ---
    @FXML
    private TextField txtStatusSearch;
    @FXML
    private VBox vboxStatusUpdate;
    @FXML
    private Label lblCurrentStatus;
    @FXML
    private Label lblUpdateProductName;
    @FXML
    private Label lblUpdateSupplier;
    @FXML
    private ComboBox<String> cmbNewStatus;

    @FXML
    private ToggleButton tglBtnScan;
    @FXML
    private BorderPane rootPane;

    // Barcode Scanning
    private final TextField barcodeInput = new TextField();
    private final BarcodeServer barcodeServer = BarcodeServer.getBarcodeServerInstance(barcodeInput);
    private final Stage newStage = new Stage();


    // --- DATA ---
    private int selectedStockId = -1;
    private String currentFixingSerial = "";
    private String currentStatusSerial = "";

    // Maps to handle Name(ID) -> ID conversion
    // Key: "Cable (ID: 55)", Value: 55
    private final Map<String, Integer> productSelectionMap = new HashMap<>();
    private final Map<String, Integer> supplierSelectionMap = new HashMap<>();

    // Reverse Maps to handle ID -> Name(ID) conversion (For Fix Tab)
    private final Map<Integer, String> idToProductDisplayMap = new HashMap<>();
    private final Map<Integer, String> idToSupplierDisplayMap = new HashMap<>();

    private final InventoryBOImpl inventoryBO = new InventoryBOImpl();

    private final ObservableList<String> stagingList = FXCollections.observableArrayList();
    private final ObservableList<ProductItemDTO> historyList = FXCollections.observableArrayList();
    private final ObservableList<ProductItemDTO> viewList = FXCollections.observableArrayList();
    private final ObservableList<ProductItemDTO> productItemList = FXCollections.observableArrayList();

    ProductItemDAOImpl productItemDAO = new ProductItemDAOImpl();
    SupplierDAOImpl supplierDAO = new SupplierDAOImpl();

    public void initialize() {
        setupTables();
        loadInitialData();
        setupListeners();
        setupTooltipForTableView();
        cmbNewStatus.setItems(FXCollections.observableArrayList("AVAILABLE", "SOLD", "RMA", "RETURNED_TO_SUPPLIER", "IN_REPAIR_USE", "DAMAGED"));
    }


    private void setupTables() {
        // Tab 1
        colViewSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        colViewSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colViewRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingLife"));
        colViewSupWar.setCellValueFactory(new PropertyValueFactory<>("supplierWarranty"));
        colViewCustWar.setCellValueFactory(new PropertyValueFactory<>("customerWarranty"));
        colViewStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblView.setItems(viewList);

        // Tab 2
        colStagedSerial.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()));
        tblStaging.setItems(stagingList);
        colHistSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        colHistSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colHistSupWar.setCellValueFactory(new PropertyValueFactory<>("supplierWarranty"));
        colHistCustWar.setCellValueFactory(new PropertyValueFactory<>("customerWarranty"));
        colHistStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblHistory.setItems(historyList);
    }

    private void loadInitialData() {
        try {
            // --- LOAD PRODUCTS (BY ID) ---
            productSelectionMap.clear();
            idToProductDisplayMap.clear();
            productItemList.clear();


            Map<Integer, String> dbProducts = inventoryBO.getAllProductMap();
            ObservableList<String> prodObList = FXCollections.observableArrayList();


            for (Map.Entry<Integer, String> entry : dbProducts.entrySet()) {
                int id = entry.getKey();
                String name = entry.getValue();

                // Format: Name (ID: 123)
                String displayStr = name + " (ID: " + id + ")";

                productSelectionMap.put(displayStr, id);
                idToProductDisplayMap.put(id, displayStr);
                prodObList.add(displayStr);
            }

            cmbProduct.setItems(prodObList);
            cmbViewProduct.setItems(prodObList);
            cmbFixProduct.setItems(prodObList);

            // --- LOAD SUPPLIERS (BY ID) ---
            supplierSelectionMap.clear();
            idToSupplierDisplayMap.clear();

            Map<Integer, String> dbSuppliers = inventoryBO.getAllSuppliersMap();
            ObservableList<String> supObList = FXCollections.observableArrayList();

            for (Map.Entry<Integer, String> entry : dbSuppliers.entrySet()) {
                int id = entry.getKey();
                String name = entry.getValue();

                String displayStr = name + " (ID: " + id + ")";

                supplierSelectionMap.put(displayStr, id);
                idToSupplierDisplayMap.put(id, displayStr);
                supObList.add(displayStr);
            }

            cmbSupplier.setItems(supObList);
            cmbFixSupplier.setItems(supObList);
            // Load all items to View Table initially
            loadAllItemsToViewTable();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Something went wrong!").show();
            System.out.println(e.getMessage());
        }
    }

    private void setupListeners() {
        // Look up ID based on Selection String
        cmbProduct.getSelectionModel().selectedItemProperty().addListener((o, old, newVal) -> {
            if (newVal != null) {
                Integer id = productSelectionMap.get(newVal);
                if (id != null) handleProductSelection(id, newVal);
            }
        });

        cmbViewProduct.getSelectionModel().selectedItemProperty().addListener((o, old, newVal) -> {
            if (newVal != null) handleViewFilter();
        });

        txtViewSearch.textProperty().addListener((o, old, newVal) -> {
            if (newVal == null) {
                return;
            }
            String searchTxt = newVal.trim();
            filterViewList(searchTxt);
        });

        tglBtnScan.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tglBtnScan.setText("Stop");
            } else {
                tglBtnScan.setText("Scan");
                barcodeServer.stopServer();
                newStage.close();
            }
        });

        barcodeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                String scannedCode = newValue.trim();
                Platform.runLater(() -> {
                    txtSerialNumber.setText(scannedCode);
                    handleAddToStaging(null);
                    barcodeInput.clear();
                });
            }
        });

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                barcodeServer.stopServer();
                newStage.close();
            }
        });
    }

    // --- TAB 1 LOGIC ---
    @FXML
    private void handleMouseDoubleClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ProductItemDTO selectedItem = tblView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getSerialNumber() != null) {
                if (selectedItem.getSerialNumber().isEmpty()) {
                    return;
                } else if (selectedItem.getSerialNumber().contains("PENDING")) {
                    new Alert(Alert.AlertType.WARNING, "Cannot update status of a place holders.").show();
                    return;
                }
                tabPaneProductItem.getSelectionModel().select(tPUpdateStatus);
                txtStatusSearch.setText(selectedItem.getSerialNumber());
                handleStatusSearch(null);
                tblView.getSelectionModel().clearSelection();
            }
        }
    }

    private void setupTooltipForTableView() {
        Tooltip hintTooltip = new Tooltip("Double-click a row to update status");
        hintTooltip.setStyle("-fx-font-size: 12px;");

        // Install it on the TableView
        Tooltip.install(tblView, hintTooltip);

        // Optional: Make it appear faster (default delay is often too long for hints)
        hintTooltip.setShowDelay(javafx.util.Duration.millis(200));
    }

    private void loadAllItemsToViewTable() {
        try {
            List<ProductItemDTO> allProductItems = productItemDAO.getAllProductItems();
            productItemList.setAll(allProductItems);

            viewList.clear();
            viewList.addAll(productItemList);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "DB Error").show();
            System.out.println(e.getMessage());
        }
    }

    private void filterViewList(String searchText) {
        List<ProductItemDTO> filteredList = new ArrayList<>();

        filteredList = viewList.stream()
                .filter(item -> {
                    // Prepare search text
                    String lowerSearch = searchText.toLowerCase();
                    String serial = item.getSerialNumber();
                    String idStr = String.valueOf(item.getStockId());
                    String itemName = item.getProductName().toLowerCase();

                    boolean matchSerial = serial != null && serial.toLowerCase().contains(lowerSearch);
                    boolean matchId = idStr.equals(searchText);
                    boolean matchName = itemName != null && itemName.contains(lowerSearch);
                    return matchSerial || matchId || matchName;
                })
                .toList();
        if (!searchText.isEmpty()) {
            tblView.setItems(FXCollections.observableArrayList(filteredList));
        } else {
            tblView.setItems(viewList);
        }
    }

    @FXML
    private void handleViewFilter() {
        String selection = cmbViewProduct.getValue();
        Integer stockId = productSelectionMap.get(selection);

        if (selection == null || stockId == null) {
            loadAllItemsToViewTable();
            return;
        }

        try {
            // Get Name just for the DTO display (optional, can extract from selection string too)
            String name = selection.substring(0, selection.lastIndexOf(" (ID:"));
            name = name != null ? name.trim() : "";

            if (name.isEmpty()) {
                loadAllItemsToViewTable();
                return;
            }

            viewList.clear();
            viewList.addAll(productItemDAO.getUnitsByStockId(stockId, name));
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, "DB Error").show();
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void handleRefreshView(ActionEvent e) {
        handleViewFilter();
    }

    @FXML
    private void handleResetView(ActionEvent event) {
        cmbViewProduct.getSelectionModel().clearSelection();
        txtViewSearch.clear();
        loadAllItemsToViewTable();
    }

    // --- TAB 2 LOGIC ---
    @FXML
    private void handleClearSupplier(ActionEvent e) {
        cmbSupplier.getSelectionModel().clearSelection();
    }

    private void handleProductSelection(int stockId, String fullDisplayName) {
        try {
            // Update Label to show ID side-by-side
            if (lblProductId != null) lblProductId.setText("ID: " + stockId);

            ProductItemDTO meta = inventoryBO.getProductMetaById(stockId);
            if (meta != null) {
                selectedStockId = meta.getStockId();
                txtCustomerWarranty.setText(String.valueOf(meta.getSupplierWarranty()));

                historyList.clear();
                // Pass name for display
                String cleanName = fullDisplayName.substring(0, fullDisplayName.lastIndexOf(" (ID:"));
                historyList.addAll(productItemDAO.getUnitsByStockId(selectedStockId, cleanName));
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Something went wrong!").show();
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleAddToStaging(ActionEvent e) {
        String serial = txtSerialNumber.getText().trim();
        if (serial.isEmpty()) return;
        if (stagingList.contains(serial)) {
            showAlert(Alert.AlertType.WARNING, "Duplicate in list");
            return;
        }

        try {
            if (productItemDAO.getItemBySerial(serial) != null) {
                showAlert(Alert.AlertType.ERROR, "Duplicate in DB");
                return;
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                showAlert(Alert.AlertType.ERROR, "Duplicate in DB");
                return;
            } else {
                showAlert(Alert.AlertType.ERROR, "Something went wrong!");
                return;
            }
        }

        stagingList.add(serial);
        txtSerialNumber.clear();
        lblStagingCount.setText(stagingList.size() + " Items");
        btnSaveAll.setDisable(false);
        txtSerialNumber.requestFocus();
    }

    @FXML
    private void handleScanBarcode(ActionEvent actionEvent) {
        try {


            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/barcode.fxml"));
            Parent root = loader.load();

            BarcodeScanController controller = loader.getController();
            controller.setBarcodeInput(barcodeInput);

            newStage.setTitle("Barcode Scanner");
            newStage.setScene(new Scene(root));
            newStage.setResizable(false);
            newStage.show();
            barcodeServer.startServer();

            newStage.setOnCloseRequest(event -> {
                barcodeServer.stopServer();
                tglBtnScan.setSelected(false);
            });
            rootPane.getScene().getWindow().setOnCloseRequest(event -> {
                barcodeServer.stopServer();
                newStage.close();
            });

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to start barcode server: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void handleRemoveFromStaging(ActionEvent e) {
        String s = tblStaging.getSelectionModel().getSelectedItem();
        if (s != null) {
            stagingList.remove(s);
            lblStagingCount.setText(stagingList.size() + " Items");
            if (stagingList.isEmpty()) btnSaveAll.setDisable(true);
        }
    }

    @FXML
    void handleSaveAll(ActionEvent e) {
        if (selectedStockId == -1) return;

        // Get Common Data
        int supId = (cmbSupplier.getValue() != null) ? supplierSelectionMap.get(cmbSupplier.getValue()) : 0;
        int supWar = txtSupplierWarranty.getText().isEmpty() ? 0 : Integer.parseInt(txtSupplierWarranty.getText());
        int custWar = Integer.parseInt(txtCustomerWarranty.getText());

        try {
            int successCount = 0;

            // Loop through every serial in your Staging Table
            ArrayList<ProductItemDTO> savedItems = new ArrayList<>();
            for (String serial : stagingList) {

                ProductItemDTO itemDTO = new ProductItemDTO();
                itemDTO.setStockId(selectedStockId);
                itemDTO.setSupplierId(supId);
                itemDTO.setSerialNumber(serial);
                itemDTO.setSupplierWarranty(supWar);
                itemDTO.setCustomerWarranty(custWar);

                savedItems.add(itemDTO);

            }
            boolean allSaved = inventoryBO.addNewSerialNo(savedItems);
            if (allSaved) {
                successCount = savedItems.size();
                System.out.println("DEBUG: All items saved successfully."+" Count: " + successCount);
            }

            showAlert(Alert.AlertType.INFORMATION, "Successfully registered " + successCount + " items.");

            // Cleanup UI
            stagingList.clear();
            lblStagingCount.setText("0 Items");
            btnSaveAll.setDisable(true);

            // Refresh History Table
            String currentComboVal = cmbProduct.getValue();
            if (currentComboVal != null) {
                historyList.clear();
                String cleanName = currentComboVal.substring(0, currentComboVal.lastIndexOf(" (ID:"));
                historyList.addAll(productItemDAO.getUnitsByStockId(selectedStockId, cleanName));
            }

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                showAlert(Alert.AlertType.ERROR, "One of these Serial Numbers already exists!");
            } else {
                showAlert(Alert.AlertType.ERROR, ex.getMessage());
            }
        }
    }

    // --- TAB 3: CORRECTION ---
    @FXML
    void handleFixSearch(ActionEvent e) {
        String s = txtFixSearch.getText().trim();
        if (s.isEmpty()) return;
        try {
            ProductItemDTO item = productItemDAO.getItemBySerial(s);
            if (item != null) {
                currentFixingSerial = item.getSerialNumber();
                vboxFixDetails.setDisable(false);
                txtFixSerial.setText(item.getSerialNumber());
                txtFixSupWar.setText(String.valueOf(item.getSupplierWarranty()));

                // --- CRITICAL FIX: Select correct ID in ComboBox ---
//                UnitManagementModel.ItemIds ids = model.getIdsBySerial(s);
//                if (ids != null) {
//                    // Find the string "Cable (ID: 5)" using the ID
//                    String prodStr = idToProductDisplayMap.get(ids.stockId);
//                    if (prodStr != null) cmbFixProduct.setValue(prodStr);
//
//                    if (ids.supplierId != 0) {
//                        String supStr = idToSupplierDisplayMap.get(ids.supplierId);
//                        if (supStr != null) cmbFixSupplier.setValue(supStr);
//                    } else {
//                        cmbFixSupplier.setValue(null);
//                    }
//                }

                ProductItemDAOImpl itemDAO = new ProductItemDAOImpl();

                ProductItemDTO Item = itemDAO.getItemBySerial(s);
                if (Item != null) {
                    // Find the string "Cable (ID: 5)" using the ID
                    String prodStr = idToProductDisplayMap.get(item.getStockId());
                    if (prodStr != null) cmbFixProduct.setValue(prodStr);

                    if (item.getSupplierId() != 0) {
                        String supStr = idToSupplierDisplayMap.get(item.getSupplierId());
                        if (supStr != null) cmbFixSupplier.setValue(supStr);
                    } else {
                        cmbFixSupplier.setValue(null);
                    }
                }

            } else {
                showAlert(Alert.AlertType.WARNING, "Not Found");
                vboxFixDetails.setDisable(true);
            }
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, "Something went wrong!").show();
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    void handleFixSave(ActionEvent e) {
        try {
            String newSerial = txtFixSerial.getText().trim();
            String pVal = cmbFixProduct.getValue();
            if (newSerial.isEmpty() || pVal == null) return;

            // Get IDs
            int newStockId = productSelectionMap.get(pVal);
            Integer newSupId = (cmbFixSupplier.getValue() != null) ? supplierSelectionMap.get(cmbFixSupplier.getValue()) : null;
            int newSupWar = Integer.parseInt(txtFixSupWar.getText());

            if (inventoryBO.correctItemMistake(currentFixingSerial, newSerial, newStockId, newSupId, newSupWar)) {
                showAlert(Alert.AlertType.INFORMATION, "Corrected");
                vboxFixDetails.setDisable(true);
                txtFixSearch.clear();
                handleViewFilter();
            }
        } catch (Exception ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                new Alert(Alert.AlertType.WARNING, "The new serial number already exists. Please use a different serial number.").showAndWait();
                return;
            }
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        }
    }

    // --- TAB 4: STATUS ---
    @FXML
    void handleStatusSearch(ActionEvent e) {
        String s = txtStatusSearch.getText().trim();
        if (s.isEmpty()) return;
        try {
            ProductItemDTO item = productItemDAO.getItemBySerial(s);
            if (item != null) {
                currentStatusSerial = item.getSerialNumber();
                vboxStatusUpdate.setDisable(false);

                lblCurrentStatus.setText(item.getStatus());
                lblUpdateProductName.setText(item.getProductName());
                lblUpdateSupplier.setText(item.getSupplierName());

                cmbNewStatus.setValue(item.getStatus());
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Found");
                vboxStatusUpdate.setDisable(true);
            }
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, "Something went wrong!").show();
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    void handleStatusSave(ActionEvent e) {
        String st = cmbNewStatus.getValue();
        if (st == null) return;
        try {
            if (inventoryBO.updateItemStatus(currentStatusSerial, st)) {
                showAlert(Alert.AlertType.INFORMATION, "Updated");
                vboxStatusUpdate.setDisable(true);
                txtStatusSearch.clear();
                handleViewFilter();
            }else {
                showAlert(Alert.AlertType.ERROR, "Update Failed");
            }
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType t, String m) {
        Alert alert = new Alert(t, m);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}