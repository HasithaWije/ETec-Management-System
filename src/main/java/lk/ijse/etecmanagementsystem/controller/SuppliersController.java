package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lk.ijse.etecmanagementsystem.dto.SupplierDTO;
import lk.ijse.etecmanagementsystem.model.SuppliersModel;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SuppliersController {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtContact;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtSearchByID;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;

    @FXML
    private TableView<SupplierDTO> tblSuppliers;
    @FXML
    private TableColumn<SupplierDTO, Integer> colId;
    @FXML
    private TableColumn<SupplierDTO, String> colName;
    @FXML
    private TableColumn<SupplierDTO, String> colContact;
    @FXML
    private TableColumn<SupplierDTO, String> colEmail;
    @FXML
    private TableColumn<SupplierDTO, String> colAddress;

    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnReset;

    private final SuppliersModel suppliersModel = new SuppliersModel();
    private final ObservableList<SupplierDTO> suppliersObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setCellValueFactories();
        loadProducts();
        tblSuppliers.setItems(suppliersObservableList);

        // Listener to handle selection changes
        tblSuppliers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
    }

    private void setCellValueFactories() {

        colId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailAddress"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        try {

            SupplierDTO supplier = new SupplierDTO(
                    0,
                    txtName.getText().trim(),
                    txtContact.getText().trim(),
                    txtEmail.getText().trim(),
                    txtAddress.getText().trim()
            );

            boolean isSaved = suppliersModel.saveSuppliers(supplier);
            if (isSaved) {
                new Alert(Alert.AlertType.INFORMATION, "Supplier Saved Successfully!").show();
                handleReset();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to Save Supplier.").show();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleUpdate() {
        if (txtId.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Please select a supplier to update.").show();
            return;
        }
        if (!validateFields()) return;

        try {
            int id = Integer.parseInt(txtId.getText().trim());
            SupplierDTO supplier = new SupplierDTO(
                    id,
                    txtName.getText(),
                    txtContact.getText(),
                    txtEmail.getText(),
                    txtAddress.getText()
            );

            boolean isUpdated = suppliersModel.updateSuppliers(supplier);
            if (isUpdated) {
                new Alert(Alert.AlertType.INFORMATION, "Supplier Updated Successfully!").show();
                handleReset();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to Update Supplier.").show();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid ID format.").show();
        }
    }

    @FXML
    private void handleDelete() {
        if (txtId.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Please select a supplier to delete.").show();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this supplier?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                boolean isDeleted = suppliersModel.deleteSuppliers(id);
                if (isDeleted) {
                    new Alert(Alert.AlertType.INFORMATION, "Supplier Deleted Successfully!").show();
                    handleReset();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Failed to Delete Supplier.").show();
                }
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage()).show();
            }
        }
    }

    @FXML
    private void handleEnterNav(KeyEvent event) {

        final String ID_REGEX = "^\\d+$";

        if (event.getCode() == KeyCode.ENTER) {
            String idText = txtSearchByID.getText().trim();

            if (idText.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Please enter supplier ID").show();
                return;
            }
            if (!idText.matches(ID_REGEX)) {
                new Alert(Alert.AlertType.ERROR, "Invalid ID: Must be a number (Integer) only.").show();
                return;
            }

            try {
                int sID = Integer.parseInt(idText);

                SupplierDTO s = suppliersModel.getSupplierById(sID);

                if (s != null) {
                    populateFields(s);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Supplier ID does not exist").show();
                }

            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid ID. Please enter a number.").show();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Something went wrong! " + e.getMessage()).show();
            }
        }
    }

    @FXML
    private void handleReset() {
        txtId.setText("");
        txtName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtSearchByID.setText("");
        txtSearch.setText("");
        reloadTable();
    }

    @FXML
    private void handleSearch() {
        String search = txtSearch.getText().toLowerCase();
        if (!search.isEmpty()) {
            ObservableList<SupplierDTO> filteredList = filterData(search);
            tblSuppliers.setItems(filteredList);
        } else {
            tblSuppliers.setItems(suppliersObservableList);
        }
    }


    @FXML
    private void handleTableClick() {
        SupplierDTO selectedItem = tblSuppliers.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            populateFields(selectedItem);
        }
    }


    private boolean validateFields() {

        final String ID_REGEX = "^\\d+$";
        final String NAME_REGEX = "^[a-zA-Z0-9\\s.\\-&]+$";
        final String CONTACT_REGEX = "^0\\d{9}$";
        final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$";
        final String ADDRESS_REGEX = "^[A-Za-z0-9, ./-]{4,}$";


        String idText = txtId.getText();
        if (!(idText == null || idText.isEmpty()) && !idText.matches(ID_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid ID: Must be a number (Integer) only.").show();
            txtId.requestFocus();
            return false;
        }


        String nameText = txtName.getText();
        if (nameText == null || nameText.isEmpty() || !nameText.matches(NAME_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Name: Must contain at least 3 letters.").show();
            txtName.requestFocus();
            return false;
        }

        // 3. Contact Validation (THIS WAS CAUSING THE ERROR)
        String contactText = txtContact.getText();
        // Added 'contactText == null' check
        if (contactText == null || contactText.isEmpty() || !contactText.matches(CONTACT_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Contact: Must start with 0 and have exactly 10 digits.").show();
            txtContact.requestFocus();
            return false;
        }

        // 4. Email Validation
        String emailText = txtEmail.getText();
        if (!(emailText == null || emailText.isEmpty()) && !emailText.matches(EMAIL_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Email: Please enter a valid email address.").show();
            txtEmail.requestFocus();
            return false;
        }

        // 5. Address Validation
        String addressText = txtAddress.getText();
        if (addressText == null || addressText.isEmpty() || !addressText.matches(ADDRESS_REGEX)) {
            new Alert(Alert.AlertType.ERROR, "Invalid Address: Must be at least 4 characters long.").show();
            txtAddress.requestFocus();
            return false;
        }

        return true;
    }

    private void reloadTable() {
        loadProducts();
        tblSuppliers.setItems(suppliersObservableList);
    }

    private void loadProducts() {
        try {
            List<SupplierDTO> rawData = suppliersModel.getAllSuppliers();
            suppliersObservableList.clear(); // Always clear before adding
            if (rawData != null) {
                suppliersObservableList.addAll(rawData);
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading Suppliers: " + e.getMessage()).show();
        }
    }

    private ObservableList<SupplierDTO> filterData(String search) {
        String searchLower = search.toLowerCase();

        return suppliersObservableList.stream()
                .filter(s -> {
                    boolean matchesName = s.getSupplierName() != null && s.getSupplierName().toLowerCase().contains(searchLower);
                    boolean matchesContact = s.getContactNumber() != null && s.getContactNumber().contains(search);
                    boolean matchesEmail = s.getEmailAddress() != null && s.getEmailAddress().toLowerCase().contains(searchLower);

                    return matchesName || matchesContact || matchesEmail;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private void populateFields(SupplierDTO s) {
        txtId.setText(String.valueOf(s.getSupplierId()));
        txtName.setText(s.getSupplierName());
        txtContact.setText(s.getContactNumber());
        txtEmail.setText(s.getEmailAddress());
        txtAddress.setText(s.getAddress());
    }
}