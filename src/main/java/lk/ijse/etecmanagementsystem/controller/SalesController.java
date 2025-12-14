package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;


import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.*;
import lk.ijse.etecmanagementsystem.model.CustomersModel;
import lk.ijse.etecmanagementsystem.model.SalesModel;
import lk.ijse.etecmanagementsystem.server.BarcodeServer;
import lk.ijse.etecmanagementsystem.util.*;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private TableView<InventoryItemDTO> tblProductInventory; // Replace ? with your Product Model
    @FXML
    private TableColumn<ProductItemDTO, Integer> colItemId;
    @FXML
    private TableColumn<ProductItemDTO, String> colItemName;
    @FXML
    private TableColumn<ProductItemDTO, String> colSerial;
    @FXML
    private TableColumn<ProductItemDTO, Double> colPrice;
    @FXML
    private TableColumn<ProductItemDTO, Integer> colWarranty;
    @FXML
    private TableColumn<ProductItemDTO, String> colStatus;


    @FXML
    private Button btnAddToCart;


    @FXML
    private ComboBox<String> comboCustomer; // Replace ? with Customer Model
    @FXML
    private Button btnAddCustomer;
    @FXML
    private TableView<ItemCartDTO> tblCart;
    @FXML
    private TableColumn<ItemCartDTO, Integer> colCartItem;
    @FXML
    private TableColumn<ItemCartDTO, String> colCartSerial;
    @FXML
    private TableColumn<ItemCartDTO, Double> colCartPrice;
    @FXML
    private TableColumn<ItemCartDTO, Double> colCartDiscount;
    @FXML
    private TableColumn<ItemCartDTO, Double> colCartTotal;

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
    private TextField txtItemName;
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
    private BorderPane rootPane;

    public HashMap<String, String> customerMap = new HashMap<>();
    private List<CustomerDTO> customerList = new ArrayList<>();
    private final ObservableList<InventoryItemDTO> inventoryItemsList = FXCollections.observableArrayList();
    private final ObservableList<ItemCartDTO> cartItemList = FXCollections.observableArrayList();

    // Barcode Scanning
    private final TextField barcodeInput = new TextField();
    private final BarcodeServer barcodeServer = BarcodeServer.getBarcodeServerInstance(barcodeInput);
    private final Stage newStage = new Stage();

    private SalesModel salesModel = new SalesModel();
    CustomersModel customersModel = new CustomersModel();


    @FXML
    public void initialize() {

        lblCashierName.setText("Cashier: " + Login.getUserName());
        LocalDate localDate = LocalDate.now();
        lblDate.setText(localDate.toString());

        loadCustomers();// Load Customers from DB
        loadProductItems(); // Load Products from DB

        List<String> suggestions = new ArrayList<>();

        for (InventoryItemDTO item : inventoryItemsList) {
            suggestions.add(item.getProductName());
        }

        // Call the new SAFE method
        Autocomplete.setupSearchWithSuggestions(txtItemName, suggestions);
        Autocomplete.setupSearchWithSuggestions(txtCusName,
                customerList.stream()
                        .map(CustomerDTO::getName)
                        .collect(Collectors.toList())
        );


        tblProductInventory.setItems(inventoryItemsList);
        tblCart.setItems(cartItemList);
        setupTableColumns();

        setupCusCmbBox();// Setup Customer ComboBox

        formatTxtFieldAsNumber(txtDiscount, true);
        formatTxtFieldAsNumber(txtCusContact, false);
        formatTxtFieldAsNumber(txtWarranty, false);
        formatTxtFieldAsNumber(txtPrice, true);


        setupListeners(); // Setup various listeners
        setupDiscountFieldListener(); // Setup discount field listeners

        // Barcode Input Listener
        barcodeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                txtSerialNumber.setText(newValue.trim());
                getProductBySerialNumber(newValue.trim());
                barcodeInput.setText("");
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to start barcode server: " + e.getMessage());
            alert.showAndWait();
        }


        newStage.setOnCloseRequest(event -> {
            barcodeServer.stopServer();
            tglBtnScan.setSelected(false);

        });

        System.out.println("Scan button clicked. Implement barcode scanning logic here.");
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

        InventoryItemDTO selectedItem = tblProductInventory.getSelectionModel().getSelectedItem();
        String itemName = txtItemName.getText().trim();
        String serialNumber = txtSerialNumber.getText().trim();
        String warrantyText = txtWarranty.getText().trim();
        String priceText = txtPrice.getText().trim();

        if (!tblProductInventory.getSelectionModel().isEmpty()) {

            if (!selectedItem.getSerialNumber().equalsIgnoreCase(serialNumber)
                    || !selectedItem.getProductName().equalsIgnoreCase(itemName)
                    || selectedItem.getCustomerWarranty() != Integer.parseInt(warrantyText.isEmpty() ? "0" : warrantyText)
                    || selectedItem.getItemPrice() != Double.parseDouble(priceText)) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You ar modifying the selected item in the inventory."
                        + " Do you want add modified item to cart.", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    handleAddToCartFromFields();
                } else {
                    handleAddToCartFromTbl();
                }

            } else {
                handleAddToCartFromTbl();
            }

        } else if (!txtItemName.getText().trim().isEmpty() &&
                !txtSerialNumber.getText().trim().isEmpty() &&
                !txtPrice.getText().trim().isEmpty()) {

            handleAddToCartFromFields();

        } else {
            new Alert(Alert.AlertType.WARNING, "Please select a product to add to the cart.").show();
        }
    }

    @FXML
    private void handleRemoveItemAction() {
        ItemCartDTO selectedCartItem = tblCart.getSelectionModel().getSelectedItem();
        if (selectedCartItem == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an item to remove from the cart.").show();
            return;
        }

        cartItemList.remove(selectedCartItem);
        tblCart.getSelectionModel().clearSelection();
        calculateTotals();
    }

    @FXML
    private void handleCheckoutAction() {
        handleCustomerAction();
        // This is where you OPEN THE POPUP
        System.out.println("Opening Payment Modal...");

        // openPaymentModal(currentTotal);
    }


    private void handleAddToCartFromTbl() {
        InventoryItemDTO selectedItem = tblProductInventory.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a product to add to the cart.").show();
            return;
        }

        String discount = txtDiscount.getText().trim() == null || txtDiscount.getText().trim().isEmpty() ? "0" : txtDiscount.getText().trim();


        for (ItemCartDTO item : cartItemList) {
            if (item.getSerialNo().equalsIgnoreCase(selectedItem.getSerialNumber())) {
                new Alert(Alert.AlertType.WARNING, "This item is already in the cart.").show();
                return;
            }
        }
        cartItemList.add(new ItemCartDTO(
                selectedItem.getItemId(),
                selectedItem.getProductName(),
                selectedItem.getSerialNumber(),
                selectedItem.getItemPrice(),
                Double.parseDouble(discount),
                selectedItem.getItemPrice() - Double.parseDouble(discount)
        ));


        calculateTotals();
    }

    private void handleAddToCartFromFields() {
        String itemName = txtItemName.getText().trim();
        String serialNumber = txtSerialNumber.getText().trim();
        String warrantyText = txtWarranty.getText().trim();
        String priceText = txtPrice.getText().trim();
        String discountText = txtDiscount.getText().trim();

        if (itemName.isEmpty() || serialNumber.isEmpty() || priceText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please fill in all item fields before adding to the cart.").show();
            return;
        }

        double price;
        double discount = 0;
        int warranty = 0;

        try {
            price = Double.parseDouble(priceText);
            warranty = Integer.parseInt(warrantyText.isEmpty() ? "0" : warrantyText);

            if (!discountText.isEmpty()) {
                discount = Double.parseDouble(discountText);
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Please enter valid numbers for price and discount.").show();
            return;
        }

        InventoryItemDTO newItem = new InventoryItemDTO(
                -1,
                itemName,
                serialNumber,
                warranty,
                price,
                "Available"
        );

        boolean itemExists = false;
        // Check if serial number already exists in inventory
        for (InventoryItemDTO item : inventoryItemsList) {
            if (item.getSerialNumber().equalsIgnoreCase(serialNumber)) {
                itemExists = true;
                break;
            }
        }
        if (!itemExists) {
            // Serial number is unique, proceed ,add to inventory
            inventoryItemsList.add(newItem);
            // In real application, you would also save this new item to the database
            // For now, we just add it to the local list
            // and get that item id from database
            // newItem.setItemId(database.saveNewInventoryItem(newItem));           *******************************
            // For demo, we assign a random id
            newItem.setItemId(inventoryItemsList.size() + 100); // Dummy ID
        }

        for (ItemCartDTO item : cartItemList) {
            if (item.getSerialNo().equalsIgnoreCase(newItem.getSerialNumber())) {
                new Alert(Alert.AlertType.WARNING, "This item is already in the cart.").show();
                return;
            }
        }

        cartItemList.add(new ItemCartDTO(
                newItem.getItemId(),
                newItem.getProductName(),
                newItem.getSerialNumber(),
                newItem.getItemPrice(),
                discount,
                newItem.getItemPrice() - discount
        ));
        calculateTotals();
    }

    private void handleCustomerAction() {

        CustomerDTO selectedCustomer = null;
        if (comboCustomer.getValue() != null) {
            String selectedKey = comboCustomer.getValue();

            selectedCustomer = customerList.stream()
                    .filter(customer -> String.valueOf(customer.getId()).equals(selectedKey))
                    .findFirst()
                    .orElse(null);
        }
        int customerId = handleCustomerFromFields(selectedCustomer);

        if (customerId == -1) {
            System.out.println("Walk-in Customer or Error in Customer Details.");
        } else if (customerId == -2) {
            // Error in customer details
            return;
        } else {
            System.out.println("Customer ID for Transaction: " + customerId);
        }
    }

    private int handleCustomerFromFields(CustomerDTO selectedCustomer) {

        String inputName = txtCusName.getText() == null ? "" : txtCusName.getText().trim();
        String inputContact = txtCusContact.getText() == null ? "" : txtCusContact.getText().trim();
        String inputEmail = txtCusEmail.getText() == null ? "" : txtCusEmail.getText().trim();
        String inputAddress = txtCusAddress.getText() == null ? "" : txtCusAddress.getText().trim();


        if ((inputName.isEmpty())
                && (!inputContact.isEmpty() || !inputEmail.isEmpty() || !inputAddress.isEmpty())) {
            new Alert(Alert.AlertType.WARNING, "Customer name is required when other details are provided.").showAndWait();
            return -2;
        }
        if (inputName.isEmpty()) {
            // Walk-in Customer
            return -1;
        }

        boolean isIdentityChanged = selectedCustomer != null && (
                !inputName.equalsIgnoreCase(selectedCustomer.getName())
        );

        if (selectedCustomer == null || isIdentityChanged) {

            // get new customer id from db and return it ***********************************************
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You are adding a new customer. Do you want to proceed?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                return saveNewCustomer(inputName, inputContact, inputEmail, inputAddress);
            } else {
                return selectedCustomer == null ? -1 : selectedCustomer.getId();
            }


        } else {

            String currentEmail = selectedCustomer.getEmailAddress() == null ? "" : selectedCustomer.getEmailAddress();
            String currentAddress = selectedCustomer.getAddress() == null ? "" : selectedCustomer.getAddress();
            String currentContact = selectedCustomer.getNumber() == null ? "" : selectedCustomer.getNumber();

            boolean isDetailsChanged = !inputEmail.equalsIgnoreCase(currentEmail) ||
                    !inputAddress.equalsIgnoreCase(currentAddress) ||
                    !inputContact.equalsIgnoreCase(currentContact);

            if (isDetailsChanged) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You are modifying the selected customer's details."
                        + " Do you want to update the customer?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    updateExistingCustomer(selectedCustomer, inputEmail, inputAddress);
                    return selectedCustomer.getId();
                } else {
                    return selectedCustomer.getId();
                }

            } else {
                // Optional: Logic for when nothing changed at all
                System.out.println("No changes detected.");
                return selectedCustomer.getId();
            }
        }
    }

    private int saveNewCustomer(String name, String contact, String email, String address) {

        CustomerDTO newCustomer = new CustomerDTO(0, name, contact, email, address);
        try {
            int newCustomerId = customersModel.insertCustomerAndGetId(newCustomer);
            loadCustomers();// Reload Customers from DB
            setupCusCmbBox(); // Refresh ComboBox
            comboCustomer.setValue(String.valueOf(newCustomerId)); // Select the newly added customer


            System.out.println("New Customer Added: " + name);
            return newCustomerId;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -2; // Indicate failure
    }

    private void updateExistingCustomer(CustomerDTO customer, String newEmail, String newAddress) {
        // 1. Update the object
        customer.setEmailAddress(newEmail);
        customer.setAddress(newAddress);

        // 2. Database Update Logic Here

        try {
            customersModel.updateCustomer(customer);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to update customer: " + e.getMessage()).show();
            return;
        } finally {
            loadCustomers();// Reload Customers from DB
        }

        System.out.println("Customer Updated: " + customer.getName() + " (ID: " + customer.getId() + ")");
    }

    private void calculateTotals() {
        double subTotal = cartItemList.stream()
                .mapToDouble(ItemCartDTO::getUnitPrice)
                .sum();

        double totalDiscount = cartItemList.stream()
                .mapToDouble(ItemCartDTO::getDiscount)
                .sum();


        double grandTotal = subTotal - totalDiscount;

        lblSubTotal.setText(String.format("%.2f", subTotal));
        lblDiscount.setText(String.format("%.2f", totalDiscount));
        lblGrandTotal.setText(String.format("%.2f", grandTotal));
    }

    private void setupTableColumns() {
        colItemId.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSerial.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));
        colWarranty.setCellValueFactory(new PropertyValueFactory<>("customerWarranty"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colCartItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCartSerial.setCellValueFactory(new PropertyValueFactory<>("serialNo"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void loadProducts() {
        // Load data from Database
    }

    private void loadCustomers() {
        // Load data from Database
        customerList = new ArrayList<>();
        try {
            customerList = customersModel.getAllCustomers();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load customers: " + e.getMessage()).show();
            return;
        }

        for (CustomerDTO c : customerList) {
            customerMap.put(String.valueOf(c.getId()), c.getName());
        }

    }

    private void loadProductItems() {
        try {
            // Load from DB
            inventoryItemsList.clear();
            inventoryItemsList.addAll(salesModel.getAllAvailableItems());
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load inventory items: " + e.getMessage()).show();
        }
    }

    private void setupListeners() {
        // Initial Button States
        btnRemoveItem.setDisable(true);

        // Search Field Listener
        txtSearchProduct.textProperty().addListener((observable, oldValue, newValue) -> getFilteredProducts(newValue.trim()));

        // Cart Table Selection Listener
        tblCart.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemoveItem.setDisable(newValue == null);
        });

        // Discount Percentage Field Formatter
        txtDisPercentage.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            String regex = "^([0-9]{0,2}(\\.)?(\\d?)?)?$";
            if (newText.matches(regex)) {
                return change;
            } else {
                return null;
            }
        }));

        // Customer Details Toggle Button Initial State
        tglBtnCusAdd.setText("DETAILS ▼");
        vboxCustomerDetails.setVisible(false);
        vboxCustomerDetails.setManaged(false);

        // Customer Details Toggle Button Listener
        tglBtnCusAdd.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tglBtnCusAdd.setText("DETAILS ▲");
                vboxCustomerDetails.setVisible(true);
                vboxCustomerDetails.setManaged(true);

            } else {
                tglBtnCusAdd.setText("DETAILS ▼");
                vboxCustomerDetails.setVisible(false);
                vboxCustomerDetails.setManaged(false);
            }
        });

        // Item Name and Serial Number Enter Key Listeners
        txtItemName.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                getProductByName(txtItemName.getText().trim());
            }
        });
        // Serial Number Enter Key Listener
        txtSerialNumber.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                getProductBySerialNumber(txtSerialNumber.getText().trim());
            }
        });

        // Customer Name Enter Key Listener
        txtCusName.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                getCustomerByName(txtCusName.getText().trim());
            }
        });

        // Barcode Scan Toggle Button Listener
        tglBtnScan.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Button is ON (Selected)

                tglBtnScan.setText("SCANNING ACTIVE...");
                handleScanAction();
            } else {
                // Button is OFF (Unselected)
                tglBtnScan.setText("START SCAN");
                barcodeServer.stopServer();
                newStage.close();

            }
        });

        // Listener to stop barcode server when view is removed
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            // If newScene is null, it means this view was removed/swapped out
            if (newScene == null) {
                barcodeServer.stopServer();
                newStage.close();
            }
        });

    }

    private void getProductBySerialNumber(String serialNumber) {
        InventoryItemDTO item = inventoryItemsList.stream()
                .filter(i -> i.getSerialNumber().equalsIgnoreCase(serialNumber))
                .findFirst()
                .orElse(null);
        if (item != null) {
            populateItemFields(item);
        } else {
            new Alert(Alert.AlertType.WARNING, "No product found with the given serial number.").show();
        }
    }

    private void getProductByName(String productName) {
        InventoryItemDTO item = inventoryItemsList.stream()
                .filter(i -> i.getProductName().equalsIgnoreCase(productName))
                .findFirst()
                .orElse(null);
        if (item != null) {
            populateItemFields(item);
        } else {
            new Alert(Alert.AlertType.WARNING, "No product found with the given name.").show();
        }
    }

    private void getCustomerByName(String customerName) {
        CustomerDTO customer = customerList.stream()
                .filter(c -> c.getName().equalsIgnoreCase(customerName))
                .findFirst()
                .orElse(null);
        if (customer != null) {
            populateCusField(customer);
        } else {
            System.out.println("No customer found with the given name.");
        }
    }

    private void populateItemFields(InventoryItemDTO item) {

        txtPrice.setText(String.valueOf(item.getItemPrice()));
        txtItemName.setText(item.getProductName());
        txtSerialNumber.setText(item.getSerialNumber());
        txtWarranty.setText(String.valueOf(item.getCustomerWarranty()));
    }

    private void setupCusCmbBox() {

        comboCustomer.getItems().setAll(customerMap.keySet());

        comboCustomer.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String key) {
                return customerMap.get(key);
            }

            @Override
            public String fromString(String string) {
                return null;
            }
        });
        comboCustomer.setOnAction(event -> {
            String selectedKey = comboCustomer.getValue();

            filterCustomers(selectedKey);
            System.out.println("Selected Customer: " + selectedKey);
        });
    }

    private void filterCustomers(String id) {
        CustomerDTO c = customerList.stream()
                .filter(customer -> String.valueOf(customer.getId()).equals(id))
                .findFirst()
                .orElse(null);
        populateCusField(c);
    }

    private void populateCusField(CustomerDTO c) {
        if (c == null) {
            txtCusName.setText("");
            txtCusContact.setText("");
            txtCusEmail.setText("");
            txtCusAddress.setText("");
            return;
        }
        txtCusName.setText(c.getName());
        txtCusContact.setText(c.getNumber());
        txtCusEmail.setText(c.getEmailAddress() == null ? "" : c.getEmailAddress());
        txtCusAddress.setText(c.getAddress());
    }

    private void formatTxtFieldAsNumber(TextField textField, boolean allowDecimal) {
        String regex = allowDecimal ? "\\d*(\\.\\d{0,2})?" : "\\d*";

        textField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches(regex)) {
                return change;
            } else {
                return null;
            }
        }));
    }

    private void setupDiscountFieldListener() {
        txtPrice.textProperty().addListener((observable, oldValue, newValue) -> {

            txtDiscount.setText("0.0");
        });
        txtDiscount.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateFinalPrice();

            if (!txtDisPercentage.isFocused()) {
                try {
                    double price = Double.parseDouble(txtPrice.getText().trim());
                    double discount = 0;

                    if (!txtDiscount.getText().isEmpty()) {
                        discount = Double.parseDouble(txtDiscount.getText().trim());
                    }

                    double percentage = (discount / price) * 100;
                    String formattedPercentage = String.format("%.1f", percentage);
                    txtDisPercentage.setText(formattedPercentage);

                } catch (NumberFormatException e) {
                    // If errors, just clear the final price
                    txtDisPercentage.setText("");
                }
            }
        });
        txtDisPercentage.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!txtDiscount.isFocused()) {
                try {
                    double price = Double.parseDouble(txtPrice.getText().trim());
                    double percentage = 0;


                    if (!txtDisPercentage.getText().isEmpty()) {
                        percentage = Double.parseDouble(txtDisPercentage.getText().trim());
                    }

                    double discount = (price * percentage) / 100;
                    String formattedDiscount = String.format("%.2f", discount);
                    txtDiscount.setText(formattedDiscount);

                } catch (NumberFormatException e) {
                    // If errors, just clear the final price
                    txtDiscount.setText("");
                }
            }
        });

    }

    private void calculateFinalPrice() {
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            double discount = 0;

            if (!txtDiscount.getText().isEmpty()) {
                discount = Double.parseDouble(txtDiscount.getText().trim());
            }

            double finalPrice = price - discount;
            txtFinalPrice.setText(String.valueOf(finalPrice));

        } catch (NumberFormatException e) {
            // If errors, just clear the final price
            txtFinalPrice.setText("");
        }
    }

    public void getFilteredProducts(String searchText) {

        String finalSearch = (searchText == null) ? "" : searchText.toLowerCase();

        List<InventoryItemDTO> filteredList = inventoryItemsList.stream()
                // 1. Filter by Name
                .filter(p -> p.getProductName().toLowerCase().contains(finalSearch))
                .collect(Collectors.toList());

        if (finalSearch.isEmpty()) {
            tblProductInventory.setItems(FXCollections.observableArrayList(inventoryItemsList));
            return;
        }
        tblProductInventory.setItems(FXCollections.observableArrayList(filteredList));
    }

    @FXML
    private void clearItemFields() {
        txtPrice.setText("");
        txtItemName.setText("");
        txtSerialNumber.setText("");
        txtWarranty.setText("");
    }

    @FXML
    private void clearCustomerFields() {
        setupCusCmbBox();
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
