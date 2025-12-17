package lk.ijse.etecmanagementsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.*;
import lk.ijse.etecmanagementsystem.model.CustomersModel;
import lk.ijse.etecmanagementsystem.model.ProductModel;
import lk.ijse.etecmanagementsystem.model.SalesModel;
import lk.ijse.etecmanagementsystem.dto.tm.ItemCartTM;
import lk.ijse.etecmanagementsystem.server.BarcodeServer;
import lk.ijse.etecmanagementsystem.util.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SalesController {

    @FXML
    private Label lblDate;
    @FXML
    private Label lblCashierName;
    @FXML
    private TextField txtSearchProduct;
    @FXML
    private ToggleButton tglBtnScan;

    @FXML
    private TableView<InventoryItemDTO> tblProductInventory;
    @FXML
    private TableColumn<InventoryItemDTO, Integer> colItemId;
    @FXML
    private TableColumn<InventoryItemDTO, String> colItemName;
    @FXML
    private TableColumn<InventoryItemDTO, String> colSerial;
    @FXML
    private TableColumn<InventoryItemDTO, Double> colPrice;
    @FXML
    private TableColumn<InventoryItemDTO, Integer> colWarranty;
    @FXML
    private TableColumn<InventoryItemDTO, ProductCondition> colCondition;

    @FXML
    private Button btnAddToCart;
    @FXML
    private ComboBox<String> comboCustomer;
    @FXML
    private Button btnAddCustomer;

    @FXML
    private TableView<ItemCartTM> tblCart;
    @FXML
    private TableColumn<ItemCartTM, String> colCartItem;
    @FXML
    private TableColumn<ItemCartTM, String> colCartSerial;
    @FXML
    private TableColumn<ItemCartTM, Double> colCartPrice;
    @FXML
    private TableColumn<ItemCartTM, Integer> colCartQty;
    @FXML
    private TableColumn<ItemCartTM, Double> colCartDiscount;
    @FXML
    private TableColumn<ItemCartTM, Double> colCartTotal;

    // Totals
    @FXML
    private Label lblSubTotal;
    @FXML
    private Label lblDiscount;
    @FXML
    private Label lblTax;
    @FXML
    private Label lblGrandTotal;
    @FXML
    private TextField txtDisc;

    // Actions
    @FXML
    private Button btnRemoveItem;
    @FXML
    private Button btnCheckout;

    @FXML
    private ToggleButton tglBtnCusAdd;
    @FXML
    private VBox vboxCustomerDetails;

    @FXML
    private TextField txtCusName;
    @FXML
    private TextField txtCusContact;
    @FXML
    private TextField txtCusEmail;
    @FXML
    private TextField txtCusAddress;

    @FXML
    private TextField txtIProductName;
    @FXML
    private TextField txtSerialNumber;
    @FXML
    private TextField txtWarranty;

    @FXML
    private TextField txtDiscount;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtDisPercentage;
    @FXML
    private TextField txtFinalPrice;
    @FXML
    private TextField txtItemQty;

    @FXML
    private ComboBox<ProductCondition> cmbCondition;
    @FXML
    private BorderPane rootPane;

    public HashMap<String, String> customerMap = new HashMap<>();
    private List<CustomerDTO> customerList = new ArrayList<>();
    private final ObservableList<InventoryItemDTO> inventoryItemsList = FXCollections.observableArrayList();
    private final ObservableList<ItemCartTM> cartItemList = FXCollections.observableArrayList();
    private final ObservableList<ProductDTO> productsList = FXCollections.observableArrayList();

    // Barcode Scanning
    private final TextField barcodeInput = new TextField();
    private final BarcodeServer barcodeServer = BarcodeServer.getBarcodeServerInstance(barcodeInput);
    private final Stage newStage = new Stage();

    private final SalesModel salesModel = new SalesModel();
    private final CustomersModel customersModel = new CustomersModel();
    private final ProductModel productModel = new ProductModel();

    @FXML
    public void initialize() {
        if (Login.getUserName() != null) {
            lblCashierName.setText("Cashier: " + Login.getUserName());
        }
        lblDate.setText(LocalDate.now().toString());

        loadCustomers();
        loadProductItems();
        loadProducts();

        cmbCondition.getItems().setAll(ProductCondition.values());

        List<String> productNames = productsList.stream()
                .map(ProductDTO::getName)
                .collect(Collectors.toList());
        Autocomplete.setupSearchWithSuggestions(txtIProductName, productNames);

        tblProductInventory.setItems(inventoryItemsList);
        tblCart.setItems(cartItemList);
        setupTableColumns();

        setupCusCmbBox();

        // Strict numeric formatting to prevent invalid characters
        formatTxtFieldAsNumber(txtDiscount, true);
        formatTxtFieldAsNumber(txtCusContact, false);
        formatTxtFieldAsNumber(txtWarranty, false);
        formatTxtFieldAsNumber(txtPrice, true);
        formatTxtFieldAsNumber(txtItemQty, false);

        txtItemQty.setText("1");

        setupListeners();
        setupDiscountFieldListener();

        // Barcode Input Listener
        barcodeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                String scannedCode = newValue.trim();
                Platform.runLater(() -> {
                    txtSerialNumber.setText(scannedCode);
                    getItemBySerialNumber(scannedCode);
                    barcodeInput.setText("");
                });
            }
        });
    }

    @FXML
    private void handleScanAction() {
        barcodeServer.startServer();
        try {
            newStage.setTitle("Barcode Scanner");
            newStage.setScene(new Scene(App.loadFXML("barcode")));
            newStage.setResizable(false);
            newStage.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to start barcode server: " + e.getMessage()).showAndWait();
        }

        newStage.setOnCloseRequest(event -> {
            barcodeServer.stopServer();
            tglBtnScan.setSelected(false);
        });
    }

    @FXML
    private void handleProductTableClick() {
        InventoryItemDTO selectedItem = tblProductInventory.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            populateItemFields(selectedItem);
        }
    }

    @FXML
    private void handleAddToCartAction() {

        String itemName = safeGetText(txtIProductName);
        String serialNumber = safeGetText(txtSerialNumber);


        if (itemName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Item Name is required.");
            return;
        }

        // Check if price is valid number
        if (!isDouble(safeGetText(txtPrice))) {
            showAlert(Alert.AlertType.ERROR, "Price must be a valid number.");
            return;
        }
        // Check if qty is valid number
        if (!isInteger(safeGetText(txtItemQty))) {
            showAlert(Alert.AlertType.ERROR, "Quantity must be a valid whole number.");
            return;
        }


        double price = parseDoubleOrZero(txtPrice.getText());
        double discount = parseDoubleOrZero(txtDiscount.getText());
        double finalPrice = parseDoubleOrZero(txtFinalPrice.getText());
        int warranty = parseIntOrZero(txtWarranty.getText());
        int qty = parseIntOrZero(txtItemQty.getText());

        if (qty <= 0) qty = 1; // Fallback to 1 if user entered 0 or invalid

        String conditionText = cmbCondition.getValue() == null ? "N/A" : cmbCondition.getValue().toString();
        InventoryItemDTO selectedInventoryItem = tblProductInventory.getSelectionModel().getSelectedItem();


        double calculatedTotal = (price * qty) - discount;


        ItemCartTM newCartItem = new ItemCartTM(
                selectedInventoryItem == null ? 0 : selectedInventoryItem.getItemId(),
                itemName,
                serialNumber,
                warranty,
                qty,
                conditionText,
                price,
                discount,
                calculatedTotal
        );


        if (selectedInventoryItem != null) {

            boolean isModified = !selectedInventoryItem.getSerialNumber().equalsIgnoreCase(serialNumber)
                    || !selectedInventoryItem.getProductName().equalsIgnoreCase(itemName)
                    || selectedInventoryItem.getCustomerWarranty() != warranty
                    || selectedInventoryItem.getItemPrice() != price
                    || !selectedInventoryItem.getProductCondition().toString().equalsIgnoreCase(conditionText);

            if (isModified) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "You modified the selected inventory item details. Add as custom item?",
                        ButtonType.YES, ButtonType.NO);
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    addCartItemToTable(newCartItem);
                } else {
                    // Revert to strict table data
                    addInventoryItemToCart(selectedInventoryItem, qty, discount);
                }
            } else {
                // Not modified, add strictly from table
                addInventoryItemToCart(selectedInventoryItem, qty, discount);
            }
        } else {
            // No table selection, pure manual entry
            addCartItemToTable(newCartItem);
        }
    }

    private void addInventoryItemToCart(InventoryItemDTO item, int qty, double discount) {
        // Duplicate Check
        if (isItemAlreadyInCart(item.getSerialNumber())) {
            showAlert(Alert.AlertType.WARNING, "This serial number is already in the cart.");
            return;
        }

        ItemCartTM cartItem = new ItemCartTM(
                item.getItemId(),
                item.getProductName(),
                item.getSerialNumber(),
                item.getCustomerWarranty(),
                qty,
                item.getProductCondition().toString(),
                item.getItemPrice(),
                discount,
                (item.getItemPrice() * qty) - discount
        );
        cartItemList.add(cartItem);
        calculateTotals();
    }

    private void addCartItemToTable(ItemCartTM item) {
        // Validation for manual entry
        if (item.getUnitPrice() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Price must be greater than 0.");
            return;
        }

        if (isItemAlreadyInCart(item.getSerialNo())) {
            showAlert(Alert.AlertType.WARNING, "This serial number is already in the cart.");
            return;
        }

        cartItemList.add(item);
        calculateTotals();
    }

    private boolean isItemAlreadyInCart(String serial) {
        if (serial == null || serial.isEmpty()) return false;
        // Only check for duplicates if serial number is provided (not empty)
        for (ItemCartTM item : cartItemList) {
            if (item.getSerialNo() != null && item.getSerialNo().equalsIgnoreCase(serial)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void handleRemoveItemAction() {
        ItemCartTM selectedCartItem = tblCart.getSelectionModel().getSelectedItem();
        if (selectedCartItem == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an item to remove.");
            return;
        }
        cartItemList.remove(selectedCartItem);
        tblCart.getSelectionModel().clearSelection();
        calculateTotals();
    }

    @FXML
    private void handleCheckoutAction() {
        if (!handleCustomerAction()) {
            return;
        }
        if (cartItemList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cart is empty.");
            return;
        }
        System.out.println("Opening Payment Modal...");
        // openPaymentModal(currentTotal);
    }

    private boolean handleCustomerAction() {
        CustomerDTO selectedCustomer = null;
        if (comboCustomer.getValue() != null) {
            String selectedKey = comboCustomer.getValue();
            selectedCustomer = customerList.stream()
                    .filter(c -> String.valueOf(c.getId()).equals(selectedKey))
                    .findFirst()
                    .orElse(null);
        }

        int customerId = processCustomerLogic(selectedCustomer);

        if (customerId == -1) {
            // Walk-in
            clearCustomerFields();
            return true;
        } else if (customerId == -2) {
            // Error/Cancel
            return false;
        } else {
            // Valid Customer
            System.out.println("Customer ID for Transaction: " + customerId);
            return true;
        }
    }

    private int processCustomerLogic(CustomerDTO selectedCustomer) {
        String inputName = safeGetText(txtCusName);
        String inputContact = safeGetText(txtCusContact);
        String inputEmail = safeGetText(txtCusEmail);
        String inputAddress = safeGetText(txtCusAddress);

        if (inputName.isEmpty()) {
            if (!inputContact.isEmpty() || !inputEmail.isEmpty() || !inputAddress.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Customer Name is required if other details are filled.");
                return -2;
            }
            return -1; // Walk-in
        }

        boolean isIdentityChanged = selectedCustomer != null && !inputName.equalsIgnoreCase(selectedCustomer.getName());

        if (selectedCustomer == null || isIdentityChanged) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Add new customer?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                return saveNewCustomer();
            }
        } else {
            // Existing customer logic
            String currentEmail = selectedCustomer.getEmailAddress() == null ? "" : selectedCustomer.getEmailAddress();
            String currentAddress = selectedCustomer.getAddress() == null ? "" : selectedCustomer.getAddress();
            String currentContact = selectedCustomer.getNumber() == null ? "" : selectedCustomer.getNumber();

            boolean isDetailsChanged = !inputEmail.equalsIgnoreCase(currentEmail) ||
                    !inputAddress.equalsIgnoreCase(currentAddress) ||
                    !inputContact.equalsIgnoreCase(currentContact);

            if (isDetailsChanged) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Update customer details?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    updateExistingCustomer(selectedCustomer, inputEmail, inputAddress, inputContact);
                }
            }
            return selectedCustomer.getId();
        }
        return -1;
    }

    @FXML
    private int saveNewCustomer() {
        if (FieldsValidation.validateCustomerFields(txtCusName, txtCusContact, txtCusEmail, txtCusAddress, new TextField())) {
            return -2; // Validation failed
        }
        CustomerDTO newCustomer = new CustomerDTO(0, safeGetText(txtCusName), safeGetText(txtCusContact), safeGetText(txtCusEmail), safeGetText(txtCusAddress));
        try {
            int newId = customersModel.insertCustomerAndGetId(newCustomer);
            loadCustomers();
            setupCusCmbBox();
            comboCustomer.setValue(String.valueOf(newId));
            showAlert(Alert.AlertType.INFORMATION, "Customer added successfully!");
            return newId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }

    private void updateExistingCustomer(CustomerDTO customer, String newEmail, String newAddress, String newContact) {
        customer.setEmailAddress(newEmail);
        customer.setAddress(newAddress);
        customer.setNumber(newContact);

        if (FieldsValidation.validateCustomerFields(txtCusName, txtCusContact, txtCusEmail, txtCusAddress, new TextField())) {
            return;
        }

        try {
            customersModel.updateCustomer(customer);
            loadCustomers();
            setupCusCmbBox();
            comboCustomer.setValue(String.valueOf(customer.getId()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Update failed: " + e.getMessage());
        }
    }

    private void calculateTotals() {
        double subTotal = cartItemList.stream().mapToDouble(ItemCartTM::getTotal).sum(); // Summing TOTALS, not unit prices
        // Note: Logic adjustment - usually subtotal is sum of (price*qty), discount is sum of discounts.
        // If ItemCartTM.getTotal() already stores (price*qty - discount), then simply summing that is the Grand Total.
        // Assuming:
        // Subtotal = Sum of (UnitPrice * Qty)
        // TotalDiscount = Sum of (Discount)
        // GrandTotal = Subtotal - TotalDiscount

        double rawSubTotal = 0;
        double totalDiscount = 0;

        for (ItemCartTM item : cartItemList) {
            rawSubTotal += (item.getUnitPrice() * item.getQuantity());
            totalDiscount += item.getDiscount();
        }

        double grandTotal = rawSubTotal - totalDiscount;

        lblSubTotal.setText(String.format("%.2f", rawSubTotal));
        lblDiscount.setText(String.format("%.2f", totalDiscount));
        lblGrandTotal.setText(String.format("%.2f", grandTotal));
    }

    private void setupTableColumns() {
        colItemId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));
        colWarranty.setCellValueFactory(new PropertyValueFactory<>("customerWarranty"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("productCondition"));

        colCartItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCartSerial.setCellValueFactory(new PropertyValueFactory<>("serialNo"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    // --- Data Loading ---
    private void loadProducts() {
        try {
            productsList.clear();
            productsList.addAll(productModel.findAll());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load products: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        customerList.clear();
        customerMap.clear();
        try {
            customerList = customersModel.getAllCustomers();
            for (CustomerDTO c : customerList) {
                customerMap.put(String.valueOf(c.getId()), c.getName());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load customers: " + e.getMessage());
        }
    }

    private void loadProductItems() {
        try {
            inventoryItemsList.clear();
            inventoryItemsList.addAll(salesModel.getAllAvailableItems());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load inventory: " + e.getMessage());
        }
    }

    // --- Listeners & UI Helpers ---
    private void setupListeners() {
        btnRemoveItem.setDisable(true);
        txtSearchProduct.textProperty().addListener((observable, oldValue, newValue) -> getFilteredProducts(newValue.trim()));
        tblCart.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> btnRemoveItem.setDisable(newValue == null));

        tglBtnCusAdd.selectedProperty().addListener((observable, oldValue, newValue) -> {
            vboxCustomerDetails.setVisible(newValue);
            vboxCustomerDetails.setManaged(newValue);
            tglBtnCusAdd.setText(newValue ? "DETAILS ▲" : "DETAILS ▼");
        });

        txtIProductName.setOnAction(e -> getProductByName(safeGetText(txtIProductName)));
        txtSerialNumber.setOnAction(e -> getItemBySerialNumber(safeGetText(txtSerialNumber)));
        txtCusName.setOnAction(e -> getCustomerByName(safeGetText(txtCusName)));

        // Logic to prevent Serial Number + Multiple Quantity conflict
        txtSerialNumber.textProperty().addListener((obs, old, newVal) -> {
            if (!safeGetText(txtItemQty).equals("1") && !newVal.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Serial Number entered with Quantity more than 1. Resetting Quantity to 1.",
                        ButtonType.OK);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK) {

                    Platform.runLater(() -> txtItemQty.setText("1"));
                    calculateFinalPrice();
                    return;

                }

            }
            calculateFinalPrice();
        });

        txtItemQty.textProperty().addListener((obs, old, newVal) -> {
            if (!safeGetText(txtSerialNumber).isEmpty() && !newVal.equals("1") && !newVal.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Serial Number entered with Quantity more than 1. Clearing Serial Number and Warranty.",
                        ButtonType.OK);
                alert.showAndWait();
                if(alert.getResult() == ButtonType.OK){

                    Platform.runLater(() -> txtSerialNumber.setText(""));
                    txtWarranty.setText("0");
                    calculateFinalPrice();
                    return;
                }

            }
            calculateFinalPrice();
        });

        txtPrice.textProperty().addListener((obs, oldVal, newVal) -> {
            txtDiscount.setText("0.0"); // Reset discount on price change
            txtDisPercentage.setText("0.0");
            calculateFinalPrice();
        });

        tglBtnScan.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tglBtnScan.setText("SCANNING ACTIVE...");
                handleScanAction();
            } else {
                tglBtnScan.setText("START SCAN");
                barcodeServer.stopServer();
                newStage.close();
            }
        });

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                barcodeServer.stopServer();
                newStage.close();
            }
        });
    }

    private void getItemBySerialNumber(String serialNumber) {
        InventoryItemDTO item = inventoryItemsList.stream()
                .filter(i -> i.getSerialNumber().equalsIgnoreCase(serialNumber))
                .findFirst().orElse(null);
        if (item != null) {
            populateItemFields(item);
        } else {
            showAlert(Alert.AlertType.WARNING, "No product found with serial: " + serialNumber);
        }
    }

    private void getProductByName(String productName) {
        ProductDTO product = productsList.stream()
                .filter(p -> p.getName().equalsIgnoreCase(productName))
                .findFirst().orElse(null);
        if (product != null) {
            populateItemFields(new InventoryItemDTO(product.getName(), product.getCondition(), product.getWarrantyMonth(), product.getSellPrice()));
        } else {
            showAlert(Alert.AlertType.WARNING, "No product found with name: " + productName);
        }
    }

    private void getCustomerByName(String customerName) {
        CustomerDTO customer = customerList.stream()
                .filter(c -> c.getName().equalsIgnoreCase(customerName))
                .findFirst().orElse(null);
        if (customer != null) {
            populateCusField(customer);
        }
    }

    private void populateItemFields(InventoryItemDTO item) {
        txtPrice.setText(String.valueOf(item.getItemPrice()));
        txtIProductName.setText(item.getProductName() == null ? "" : item.getProductName());
        txtSerialNumber.setText(item.getSerialNumber() == null ? "" : item.getSerialNumber());
        txtWarranty.setText(String.valueOf(item.getCustomerWarranty()));
        cmbCondition.getSelectionModel().select(item.getProductCondition());
    }

    private void setupCusCmbBox() {
        comboCustomer.getItems().setAll(customerMap.keySet());
        comboCustomer.setEditable(true);

        comboCustomer.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String key) {
                return customerMap.get(key);
            }

            @Override
            public String fromString(String string) {
                return customerMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equalsIgnoreCase(string))
                        .map(Map.Entry::getKey).findFirst().orElse(null);
            }
        });

        // Setup Search Listener Logic (Simulated)
        comboCustomer.getEditor().textProperty().addListener((obs, old, newVal) -> {
            if (comboCustomer.getSelectionModel().getSelectedItem() != null) return;
            Platform.runLater(() -> {
                List<String> matches = customerMap.entrySet().stream()
                        .filter(e -> e.getValue().toLowerCase().contains(newVal.toLowerCase()))
                        .sorted(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey).collect(Collectors.toList());

                if (!matches.isEmpty()) {
                    comboCustomer.getItems().setAll(matches);
                    if (!matches.isEmpty() && comboCustomer.isFocused()) comboCustomer.show();
                }
            });
        });

        comboCustomer.setOnAction(e -> {
            String key = comboCustomer.getValue();
            if (key != null && customerMap.containsKey(key)) filterCustomers(key);
        });
    }

    private void filterCustomers(String id) {
        customerList.stream().filter(c -> String.valueOf(c.getId()).equals(id))
                .findFirst().ifPresent(this::populateCusField);
    }

    private void populateCusField(CustomerDTO c) {
        if (c == null) {
            clearCustomerFields();
            return;
        }
        txtCusName.setText(c.getName());
        txtCusContact.setText(c.getNumber());
        txtCusEmail.setText(c.getEmailAddress());
        txtCusAddress.setText(c.getAddress());
    }

    private void setupDiscountFieldListener() {
        txtPrice.textProperty().addListener((o, old, val) -> txtDiscount.setText("0.0"));

        txtDiscount.textProperty().addListener((o, old, val) -> {
            if (txtDisPercentage.isFocused()) return;
            double price = parseDoubleOrZero(txtPrice.getText());
            double discount = parseDoubleOrZero(val);
            int qty = parseIntOrZero(txtItemQty.getText());

            if (qty <= 0) qty = 1;

            if (price > 0) {
                txtDisPercentage.setText(String.format("%.1f", (discount / (price*qty)) * 100));
            }
            calculateFinalPrice();
        });

        txtDisPercentage.textProperty().addListener((o, old, val) -> {
            if (txtDiscount.isFocused()) return;
            double price = parseDoubleOrZero(txtPrice.getText());
            double pct = parseDoubleOrZero(val);
            int qty = parseIntOrZero(txtItemQty.getText());

            if (qty <= 0) qty = 1;
            txtDiscount.setText(String.format("%.2f", ((price*qty) * pct) / 100));
            calculateFinalPrice();
        });
    }



    private void calculateFinalPrice() {

        double unitPrice = parseDoubleOrZero(txtPrice.getText());
        double discountPerUnit = parseDoubleOrZero(txtDiscount.getText());
        int qty = parseIntOrZero(txtItemQty.getText());

        if (qty <= 0) qty = 1;

        double finalTotal = ((unitPrice * qty) - discountPerUnit);

        txtFinalPrice.setText(String.format("%.2f", finalTotal));
    }

    public void getFilteredProducts(String searchText) {
        String search = (searchText == null) ? "" : searchText.toLowerCase();
        List<InventoryItemDTO> filtered = inventoryItemsList.stream()
                .filter(p -> p.getProductName().toLowerCase().contains(search))
                .collect(Collectors.toList());
        tblProductInventory.setItems(FXCollections.observableArrayList(filtered));
    }

    // --- Helper Methods for Safety ---
    private String safeGetText(TextField tf) {
        return (tf == null || tf.getText() == null) ? "" : tf.getText().trim();
    }

    private int parseIntOrZero(String val) {
        if (val == null || val.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleOrZero(String val) {
        if (val == null || val.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void formatTxtFieldAsNumber(TextField textField, boolean allowDecimal) {
        String regex = allowDecimal ? "\\d*(\\.\\d{0,2})?" : "\\d*";
        textField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(regex) ? change : null));
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }

    @FXML
    private void clearItemFields() {
        cmbCondition.getSelectionModel().clearSelection();
        txtPrice.setText("");
        txtIProductName.setText("");
        txtSerialNumber.setText("");
        txtWarranty.setText("");
    }

    @FXML
    private void clearCustomerFields() {
        comboCustomer.getSelectionModel().clearSelection();
        comboCustomer.getEditor().setText("");
        txtCusName.setText("");
        txtCusContact.setText("");
        txtCusEmail.setText("");
        txtCusAddress.setText("");
    }

    @FXML
    private void clearCart() {
        cartItemList.clear();
        calculateTotals();
    }

    @FXML
    private void resetAllFields() {
        clearItemFields();
        clearCustomerFields();
        clearCart();
        tblProductInventory.getSelectionModel().clearSelection();
        tblCart.getSelectionModel().clearSelection();
    }
}