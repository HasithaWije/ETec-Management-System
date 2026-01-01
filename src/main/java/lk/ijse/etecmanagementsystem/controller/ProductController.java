package lk.ijse.etecmanagementsystem.controller;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.model.CategoryModel;
import lk.ijse.etecmanagementsystem.model.ProductModel;
import lk.ijse.etecmanagementsystem.model.UnitManagementModel;
import lk.ijse.etecmanagementsystem.util.Category;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.util.FieldsValidation;
import lk.ijse.etecmanagementsystem.util.ProductCondition;
import lk.ijse.etecmanagementsystem.util.ProductUtil;

import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ProductController implements Initializable {


    @FXML
    private TextField txtId;
    @FXML
    private TextArea txtName;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private ComboBox<ProductCondition> cmbCondition;
    @FXML
    private TextField txtSellPrice;
    @FXML
    private TextField txtBuyPrice;
    @FXML
    private TextField txtWarranty;
    @FXML
    private TextField txtQty;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtImagePath;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnNewCategory;
    @FXML
    private Button btnOpenImagePopup;

    @FXML
    private TableView<ProductDTO> tableProducts;
    @FXML
    private TableColumn<ProductDTO, String> colId;
    @FXML
    private TableColumn<ProductDTO, String> colName;
    @FXML
    private TableColumn<ProductDTO, String> colCategory;
    @FXML
    private TableColumn<ProductDTO, String> colCondition;
    @FXML
    private TableColumn<ProductDTO, Double> colSellPrice;
    @FXML
    private TableColumn<ProductDTO, Double> colBuyPrice;
    @FXML
    private TableColumn<ProductDTO, Integer> colWarranty;
    @FXML
    private TableColumn<ProductDTO, Integer> colQty;
    @FXML
    private TableColumn<ProductDTO, String> colDesc;


    private final ObservableList<ProductDTO> productList = FXCollections.observableArrayList();
    private String selectedImagePath = "";

    private final ProductModel productModel = new ProductModel();

    private final String NAME_REGEX = "^[ -~]{3,30}$"; // Alphanumeric and special characters, 3-50 chars

    private final String DESCRIPTION_REGEX = "^[a-zA-Z0-9.,\\-: ]{3,150}$";
    // Price is generally good, but \\d is cleaner than [0-9]
    private final String PRICE_REGEX = "^\\d+(\\.\\d{1,2})?$";
    // Warranty and Qty are fine as is
    private final String WARRANTY_REGEX = "^\\d{1,2}$";
    private final String QTY_REGEX = "^\\d{1,5}$";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Load Products from Database
        loadProducts();

        tableProducts.setItems(productList);

        setCellValueFactories();

        initComboBoxes();

        FieldsValidation.formatTxtFieldAsNumber(txtId, false);
        FieldsValidation.formatTxtFieldAsNumber(txtSellPrice, true);
        FieldsValidation.formatTxtFieldAsNumber(txtBuyPrice, true);
        FieldsValidation.formatTxtFieldAsNumber(txtWarranty, false);
        FieldsValidation.formatTxtFieldAsNumber(txtQty, false);


        // 4. Add Listener for Table Selection
        tableProducts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFields(newValue);
            }
        });

        // 5. Setup Button Actions
        setupButtonActions();
    }

    private void setCellValueFactories() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colSellPrice.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        colBuyPrice.setCellValueFactory(new PropertyValueFactory<>("buyPrice"));
        colWarranty.setCellValueFactory(new PropertyValueFactory<>("warrantyMonth"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void initComboBoxes() {

        cmbCondition.getItems().setAll(ProductCondition.values());
        cmbCategory.setItems(Category.getCategories());

    }

    private void setupButtonActions() {
        btnAdd.setOnAction(event -> saveProduct());
        btnUpdate.setOnAction(event -> updateProduct());
        btnDelete.setOnAction(event -> deleteProduct());
        btnClear.setOnAction(event -> clearForm());

        // Logic for the "+" button next to Category
        btnNewCategory.setOnAction(event -> {
            setCategoryStage();
            loadCategories();
            cmbCategory.setItems(Category.getCategories());

        });

        // Logic for Image Popup
        btnOpenImagePopup.setOnAction(event -> openImagePopup());
    }

    private void loadCategories() {

        CategoryModel categoryModel = new CategoryModel();
        Category.getCategories().clear();
        try {
            List<String> list = categoryModel.getAllCategories();
            if (!list.isEmpty()) {
                Category.getCategories().setAll(list);

                System.out.println("Categories loaded from DB: " + list);
            } else {
                System.out.println("No categories found in the database.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load categories: " + e.getMessage());
        }

    }

    private void saveProduct() {
        txtId.setText("");
        if (validateFields()) return;

        // Create the DTO
        ProductDTO newProduct = new ProductDTO(
                txtName.getText().trim(),
                txtDescription.getText().trim(),
                Double.parseDouble(txtSellPrice.getText().trim()),
                cmbCategory.getValue(),
                cmbCondition.getValue(),
                Double.parseDouble(txtBuyPrice.getText().trim()),
                Integer.parseInt(txtWarranty.getText()),
                Integer.parseInt(txtQty.getText()), // e.g., 10
                selectedImagePath
        );

        try {
            // 1. Save Product and get the new ID (e.g., ID = 50)
            int newStockId = productModel.saveProductAndGetId(newProduct);

            if (newStockId > 0) {

                showAlert(Alert.AlertType.INFORMATION, "Success", "Product Saved with " + newProduct.getQty() + " empty slots created!");
                reFresh();

            } else {
                showAlert(Alert.AlertType.ERROR, "Failure", "Failed to save product.");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("UNIQUE constraint failed: Product.name")) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Product with the same name already exists.");
                return;
            }
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save product: ");
            System.out.println(e.getMessage());
        }
    }

    private void updateProduct() {
        // 1. Validation checks (ID existence, regex fields)
        if (txtId.getText() == null || txtId.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a product to update.");
            return;
        }
        if (!isProductIdExist()) return;

        tableProducts.getSelectionModel().clearSelection();

        if (validateFields()) return;

        // 2. Prepare Data
        int stockId = Integer.parseInt(txtId.getText().trim());
        int newQty = Integer.parseInt(txtQty.getText().trim());


        try {

            int realItemCount = productModel.getRealItemCount(stockId);

            if (newQty < realItemCount) {
                showAlert(Alert.AlertType.ERROR, "Quantity Error",
                        "Cannot reduce Quantity to " + newQty + ".\n" +
                                "You have " + realItemCount + " physical items registered.\n" +
                                "You must manually delete specific items first.");

                txtQty.setText(String.valueOf(realItemCount));
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You are updating the product details and quantity to " + newQty + ".\n" +
                    "Click OK to proceed.", ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle("Confirm Update");
            alert.showAndWait();
            if (alert.getResult() != ButtonType.OK) {
                return;
            }

            ProductDTO selected = new ProductDTO(
                    txtId.getText().trim(),
                    txtName.getText().trim(),
                    txtDescription.getText().trim(),
                    Double.parseDouble(txtSellPrice.getText()),
                    cmbCategory.getValue(),
                    cmbCondition.getValue(),
                    Double.parseDouble(txtBuyPrice.getText()),
                    Integer.parseInt(txtWarranty.getText()),
                    newQty,
                    selectedImagePath
            );

            boolean result = productModel.updateProductWithQtySync(selected);

            if (result) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product and Quantity Updated Successfully!");
                reFresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failure", "Failed to update product.");
            }

        } catch (Exception e) {

            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("UNIQUE constraint failed: Product.name")) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Product with the same name already exists.");
                return;
            }
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product: ");
            System.out.println(e.getMessage());

        }
    }

    private void deleteProduct() {

        ProductDTO selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {

            if (txtId.getText() == null || txtId.getText().isEmpty()) {

                showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a product to delete.");
                return;

            } else {

                if (!isProductIdExist()) return;

                selected = new ProductDTO();
                selected.setId(txtId.getText().trim());
                selected.setName("Product ID: " + txtId.getText()); // Placeholder name
            }
        } else {

            populateFields(selected);

        }

        try {

            ProductModel.ItemDeleteStatus status = productModel.checkItemStatusForDelete(selected.getId());

            if (status.restrictedCount > 0) {
                showAlert(Alert.AlertType.ERROR, "Deletion Blocked",
                        "Cannot delete this product.\n\n" +
                                "Reason: It has " + status.restrictedCount + " items with history (SOLD, RMA, or DAMAGED).\n" +
                                "You cannot delete records that are part of business history.");
                return;
            }

            if (status.realAvailableCount > 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Deletion of Physical Stock");
                alert.setHeaderText("Warning: Physical Items Found");
                alert.setContentText(
                        "This product has " + status.realAvailableCount + " REAL items currently in stock.\n\n" +
                                "Deleting this product will remove these items from the database permanently.\n" +
                                "Are you sure you want to proceed?");


                if (alert.showAndWait().get() != ButtonType.OK) {
                    return; // User cancelled
                }
            } else {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to delete " + selected.getName() + "?\n(Only empty slots will be removed)",
                        ButtonType.YES, ButtonType.NO);
                if (alert.showAndWait().get() != ButtonType.YES) {
                    return;
                }
            }

            boolean result = productModel.deleteById(selected.getId());

            if (result) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product Deleted Successfully!");
                reFresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failure", "Failed to delete product.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete product: " + e.getMessage());
        }
    }


    @FXML
    private void getEnterKeyNav(KeyEvent Event) {
        if (Event.getCode() == KeyCode.ENTER) {
            String id = txtId.getText();
            tableProducts.getSelectionModel().clearSelection();

            try {
                if (id == null || id.isEmpty()) {
//                    new Alert(Alert.AlertType.WARNING, "Please enter a Product ID to search.").show();
                    return;
                }

                ResultSet product = productModel.findById(id);
                ProductDTO p;

                if (product.next()) {
                    p = new ProductDTO(
                            product.getString("stock_id"),
                            product.getString("name"),
                            product.getString("description"),
                            product.getDouble("sell_price"),
                            product.getString("category"),
                            fromConditionString(product.getString("p_condition")),
                            product.getDouble("buy_price"),
                            product.getInt("warranty_months"),
                            product.getInt("qty"),
                            product.getString("image_path")
                    );
                    populateFields(p);
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "No product found with ID: " + id).showAndWait();
                    clearForm();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error retrieving product: " + e.getMessage()).show();
            }
        }
    }

    private void loadProducts() {
        try {

            List<ProductDTO> rawData = productModel.findAll();
            if (rawData != null) {
//                ProductUtil.productCache.setAll(rawData);
                productList.setAll(rawData);
            } else {
                productList.clear();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No products found in the database.");
                alert.show();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading products: " + e.getMessage());
            alert.show();
        }
    }

    private void reFresh() {
        loadProducts();
        clearForm();
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        cmbCategory.getSelectionModel().select(null);
        cmbCondition.getSelectionModel().select(null);
        txtSellPrice.setText("");
        txtBuyPrice.setText("");
        txtWarranty.setText("");
        txtQty.setText("");
        txtDescription.setText("");
        selectedImagePath = "";
        txtImagePath.setText("");
        tableProducts.getSelectionModel().clearSelection();
    }

    // --- Helper Methods ---

    void populateFields(ProductDTO p) {
        txtId.setText(p.getId());
        txtName.setText(p.getName());
        cmbCategory.setValue(p.getCategory());
        cmbCondition.setValue(p.getCondition());
        txtSellPrice.setText(String.valueOf(p.getSellPrice()));
        txtBuyPrice.setText(String.valueOf(p.getBuyPrice()));
        txtWarranty.setText(String.valueOf(p.getWarrantyMonth()));
        txtQty.setText(String.valueOf(p.getQty()));
        txtDescription.setText(p.getDescription());
        selectedImagePath = p.getImagePath();
        txtImagePath.setText(selectedImagePath != null ? selectedImagePath : "@...");
    }

    private boolean validateFields() {

        if ((txtName.getText() == null) || !(txtName.getText().matches(NAME_REGEX))) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Name must be 3-50 alphanumeric characters.");
            return true;
        }
        if ((txtDescription.getText() == null) || !txtDescription.getText().matches(DESCRIPTION_REGEX)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Desc must be 3-150 alphabetic characters.");
            return true;
        }
        if ((txtSellPrice.getText() == null || txtBuyPrice.getText() == null)
                || !txtSellPrice.getText().matches(PRICE_REGEX) || !txtBuyPrice.getText().matches(PRICE_REGEX)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid  Price format.");
            return true;
        }
        if (txtWarranty.getText() == null || !txtWarranty.getText().matches(WARRANTY_REGEX)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Warranty must be 1-2 digit number.");
            return true;
        }
        if (txtQty.getText() == null || !txtQty.getText().matches(QTY_REGEX)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Qty must be 1-5 digit number.");
            return true;
        }
        if (cmbCategory.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a Category.");
            return true;
        }
        if (cmbCondition.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a Condition.");
            return true;
        }
        return false;
    }

    private boolean isProductIdExist() {
        String id = txtId.getText();
        try {
            ResultSet product = productModel.findById(id);
            if (!product.next()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Product with ID " + id + " does not exist.");
                return false;
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error checking product ID: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    private void openImagePopup() {
        if (txtName.getText() == null || txtName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter the Product Name before selecting an image.");
            return;
        }
        try {

            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/imagePopup.fxml"));
            Parent root = loader.load();

            ImagePopupController controller = loader.getController();
            controller.setProductDetails(txtName.getText(), this);


            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Select Image");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Logic to retrieve the selected file would go here if using a shared model/controller logic

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open image popup.");
        }
    }

    protected void setSelectedImagePath(String path) {
        this.selectedImagePath = path;
        if (this.selectedImagePath != null) {
            txtImagePath.setText(this.selectedImagePath);
        } else {
            selectedImagePath = "";
            txtImagePath.setText("null");
        }
    }

    private void setCategoryStage() {
        try {
            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.setTitle("Category");

            newStage.setScene(new Scene(App.loadFXML("category"), 400, 200));
            newStage.setResizable(false);
            newStage.showAndWait();


        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Category window: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private ProductCondition fromConditionString(String s) {
        if (s == null) return null;
        try {
            if (s.equals("Used")) {
                return ProductCondition.USED;
            } else if (s.equals("Brand New")) {
                return ProductCondition.BRAND_NEW;
            }
            return ProductCondition.BOTH;
        } catch (IllegalArgumentException ex) {
            return ProductCondition.BOTH; // unknown condition value
        }
    }
}
