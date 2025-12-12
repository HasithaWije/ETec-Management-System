package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;


import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.*;
import lk.ijse.etecmanagementsystem.server.BarcodeServer;
import lk.ijse.etecmanagementsystem.util.ProductCondition;
import lk.ijse.etecmanagementsystem.util.ProductUtil;
import lk.ijse.etecmanagementsystem.util.Stock;

import java.io.IOException;
import java.sql.Date;
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
    private Button btnScan;
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
    private ToggleButton tglBtnAdd;

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

    public HashMap<String, String> customerMap = new HashMap<>();
    private List<CustomerDTO> customerList = new ArrayList<>();
    private final ObservableList<InventoryItemDTO> inventoryItemsList = FXCollections.observableArrayList();
    private final ObservableList<ItemCartDTO> cartItemList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        loadCustomers();
        loadProductItems();

        tblProductInventory.setItems(inventoryItemsList);
        tblCart.setItems(cartItemList);
        setupTableColumns();

        setupCusCmbBox();

        formatTxtFieldAsNumber(txtDiscount, true);
        formatTxtFieldAsNumber(txtCusContact, false);
        formatTxtFieldAsNumber(txtWarranty, false);


        setupListeners();

        setupDiscountFieldListener();

    }

    @FXML
    private void handleScanAction() {
        TextField barcodeInput = new TextField();
        BarcodeServer barcodeServer = BarcodeServer.getBarcodeServerInstance(txtSerialNumber);
        barcodeServer.startServer();
        Stage newStage = new Stage();

        try {


            newStage.setTitle("Barcode Scanner");
            newStage.setScene(new Scene(App.loadFXML("barcode")));
            newStage.setResizable(false);
            newStage.show();



        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to start barcode server: " + e.getMessage());
            alert.showAndWait();
        }
        txtSerialNumber.setText(barcodeInput.getText());
        newStage.setOnCloseRequest(event -> barcodeServer.stopServer());





        // Start the server and pass the text field to it


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

    @FXML
    private void handleRemoveItemAction() {
        ItemCartDTO selectedCartItem = tblCart.getSelectionModel().getSelectedItem();
        if (selectedCartItem == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an item to remove from the cart.").show();
            return;
        }

        cartItemList.remove(selectedCartItem);
        calculateTotals();
    }

    @FXML
    private void handleCheckoutAction() {
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
        }else  if(customerId == -2){
            // Error in customer details
            return;
        } else {
            System.out.println("Customer ID for Transaction: " + customerId);
        }
        // This is where you OPEN THE POPUP
        System.out.println("Opening Payment Modal...");

        // openPaymentModal(currentTotal);
    }


    private int handleCustomerFromFields(CustomerDTO selectedCustomer) {

        String inputName = txtCusName.getText() == null ? "" : txtCusName.getText().trim();
        String inputContact = txtCusContact.getText() == null ? "" : txtCusContact.getText().trim();
        String inputEmail = txtCusEmail.getText() == null ? "" : txtCusEmail.getText().trim();
        String inputAddress = txtCusAddress.getText() == null ? "" : txtCusAddress.getText().trim();


        if ((inputName.isEmpty())
        && ( !inputContact.isEmpty() || !inputEmail.isEmpty() || !inputAddress.isEmpty()) ) {
            new Alert(Alert.AlertType.WARNING, "Customer name is required when other details are provided.").showAndWait();
            return -2;
        }
        if( inputName.isEmpty()){
            // Walk-in Customer
            return -1;
        }

        boolean isIdentityChanged = selectedCustomer != null && (
                !inputName.equalsIgnoreCase(selectedCustomer.getName()) ||
                        !inputContact.equalsIgnoreCase(selectedCustomer.getNumber())
        );

        if (selectedCustomer == null || isIdentityChanged) {

               // get new customer id from db and return it ***********************************************
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You are adding a new customer. Do you want to proceed?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                return saveNewCustomer(inputName, inputContact, inputEmail, inputAddress);
            }else {
                return selectedCustomer == null ? -1 : selectedCustomer.getId();
            }


        } else {

            String currentEmail = selectedCustomer.getEmailAddress() == null ? "" : selectedCustomer.getEmailAddress();
            String currentAddress = selectedCustomer.getAddress() == null ? "" : selectedCustomer.getAddress();

            boolean isDetailsChanged = !inputEmail.equalsIgnoreCase(currentEmail) ||
                    !inputAddress.equalsIgnoreCase(currentAddress);

            if (isDetailsChanged) {

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You are modifying the selected customer's details."
                            + " Do you want to update the customer?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        updateExistingCustomer(selectedCustomer, inputEmail, inputAddress);
                        return selectedCustomer.getId();
                    }else {
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
        // 1. Database Insert Logic Here
        // int newId = customerService.add(...);


        // 2. UI Update Logic (Dummy code based on your snippet)
        int newCustomerId = customerList.size() + 1;
        CustomerDTO newCustomer = new CustomerDTO(newCustomerId, name, contact, email, address);

        customerList.add(newCustomer);
        customerMap.put(String.valueOf(newCustomerId), name);
        comboCustomer.getItems().add(String.valueOf(newCustomerId));
        comboCustomer.setValue(String.valueOf(newCustomerId));


        System.out.println("New Customer Added: " + name);
        return newCustomerId;
    }

    private void updateExistingCustomer(CustomerDTO customer, String newEmail, String newAddress) {
        // 1. Update the object
        customer.setEmailAddress(newEmail);
        customer.setAddress(newAddress);

        // 2. Database Update Logic Here
        // customerService.update(customer);

        System.out.println("Customer Updated: " + customer.getName());
    }

//    private  void handleCustomerFromFields(CustomerDTO customer){
//        String cusName = txtCusName.getText().trim() == null ? "" : txtCusName.getText().trim();
//        String cusContact = txtCusContact.getText().trim() == null ? "" : txtCusContact.getText().trim();
//        String cusEmail = txtCusEmail.getText().trim() == null ? "" : txtCusEmail.getText().trim();
//        String cusAddress =  txtCusAddress.getText().trim() == null ? "" : txtCusAddress.getText().trim();
//
//        if (!cusName.isEmpty() && !cusContact.isEmpty()) {
//            if (customer == null || !customer.getName().equalsIgnoreCase(cusName)
//                    || !customer.getNumber().equalsIgnoreCase(cusContact)
//                    || (customer.getEmailAddress() == null ? "" : customer.getEmailAddress()).equalsIgnoreCase(cusEmail)
//                    || (customer.getAddress() == null ? "" : customer.getAddress()).equalsIgnoreCase(cusAddress)) {
//
//                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You are modifying the selected customer details."
//                        + " Do you want to add modified customer?", ButtonType.YES, ButtonType.NO);
//                alert.showAndWait();
//                if(customer != null && !customer.getName().equalsIgnoreCase(cusName)
//                || !customer.getNumber().equalsIgnoreCase(cusContact)){
//
//                }
//
//                if (alert.getResult() == ButtonType.YES) {
//                    // Add or update customer in database
//                    // If new customer, get generated ID from database
//                    int newCustomerId = customerList.size() + 1; // Dummy ID for demo
//                    CustomerDTO newCustomer = new CustomerDTO(
//                            newCustomerId,
//                            cusName,
//                            cusContact,
//                            cusEmail,
//                            cusAddress
//                    );
//                    customerList.add(newCustomer);
//                    customerMap.put(String.valueOf(newCustomerId), cusName);
//                    comboCustomer.getItems().add(String.valueOf(newCustomerId));
//                    comboCustomer.setValue(String.valueOf(newCustomerId));
//                }
//            }
//
//        }
//
//
//        // Open Customer Details Modal
//        System.out.println("Opening Customer Details Modal...");
//    }

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
        customerList.add(new CustomerDTO(1, "John Doe", "1234567890", "", "123 Main St"));
        customerList.add(new CustomerDTO(2, "Jane Smith", "0987654321", "", "456 Elm St"));
        customerList.add(new CustomerDTO(3, "Bob Johnson", "5555555555", "", "789 Oak St"));

        for (CustomerDTO c : customerList) {
            customerMap.put(String.valueOf(c.getId()), c.getName());
        }

    }

    private void loadProductItems() {
        // Load data from Database

        inventoryItemsList.addAll(
                new InventoryItemDTO(1, "Laptop", "SN12345", 12, 1500.00, "Available"),
                new InventoryItemDTO(2, "Smartphone", "SN67890", 24, 800.00, "Available"),
                new InventoryItemDTO(3, "Tablet", "SN54321", 18, 600.00, "Sold")
        );
    }

    private void setupListeners() {
        btnRemoveItem.setDisable(true);
        txtSearchProduct.textProperty().addListener((observable, oldValue, newValue) -> getFilteredProducts(newValue.trim()));
        tblCart.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemoveItem.setDisable(newValue == null);
//            if(!tblCart.isFocused()){
//                tblCart.getSelectionModel().clearSelection();
//            }

        });

        txtDisPercentage.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            String regex = "^([0-9]{0,2}(\\.)?(\\d?)?)?$";
            if (newText.matches(regex)) {
                return change;
            } else {
                return null;
            }
        }));
    }

    private void populateItemFields(InventoryItemDTO item) {

        txtPrice.setText(String.valueOf(item.getItemPrice()));
        txtItemName.setText(item.getProductName());
        txtSerialNumber.setText(item.getSerialNumber());
        txtWarranty.setText(String.valueOf(item.getCustomerWarranty()));
    }

    private void setupCusCmbBox() {

        comboCustomer.getItems().addAll(customerMap.keySet());

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

}
