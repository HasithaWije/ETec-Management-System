package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;


import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import lk.ijse.etecmanagementsystem.dto.*;
import lk.ijse.etecmanagementsystem.util.ProductCondition;
import lk.ijse.etecmanagementsystem.util.ProductUtil;
import lk.ijse.etecmanagementsystem.util.Stock;

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
    private TextField txtName;
    @FXML
    private TextField txtContact;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtAddress;


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
        formatTxtFieldAsNumber(txtContact, false);

        txtPrice.setText("10000");

        setupListeners();

       setupDiscountFieldListener();

    }

    @FXML
    private  void handleProductTableClick() {
        InventoryItemDTO selectedItem = tblProductInventory.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            populateItemFields(selectedItem);
        }
    }

    @FXML
    private void handleAddToCartAction() {
        if(!tblProductInventory.getSelectionModel().isEmpty()) {
            handleAddToCartFromTbl();

        }else {
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

    private void handleCheckoutAction() {
        // This is where you OPEN THE POPUP
        System.out.println("Opening Payment Modal...");
        // openPaymentModal(currentTotal);
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
        txtSearchProduct.textProperty().addListener((observable, oldValue, newValue) -> getFilteredProducts(newValue));
        tblCart.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemoveItem.setDisable(newValue == null);

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
            txtName.setText("");
            txtContact.setText("");
            txtEmail.setText("");
            txtAddress.setText("");
            return;
        }
        txtName.setText(c.getName());
        txtContact.setText(c.getNumber());
        txtEmail.setText(c.getEmailAddress() == null ? "" : c.getEmailAddress());
        txtAddress.setText(c.getAddress());
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
