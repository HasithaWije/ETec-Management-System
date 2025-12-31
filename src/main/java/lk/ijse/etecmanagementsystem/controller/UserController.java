package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.etecmanagementsystem.dto.UserDTO;
import lk.ijse.etecmanagementsystem.model.UserModel;
import lk.ijse.etecmanagementsystem.util.FieldsValidation;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserController {

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAddress;
    @FXML private TextField txtUserName;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole; // Changed to ComboBox for Role

    @FXML private TextField txtSearchByID;
    @FXML private TextField txtSearch;

    @FXML private TableView<UserDTO> tblUser;
    @FXML private TableColumn<UserDTO, Integer> colId;
    @FXML private TableColumn<UserDTO, String> colName;
    @FXML private TableColumn<UserDTO, String> colContact;
    @FXML private TableColumn<UserDTO, String> colEmail;
    @FXML private TableColumn<UserDTO, String> colAddress;
    @FXML private TableColumn<UserDTO, String> colUserName;
    @FXML private TableColumn<UserDTO, String> colRole;

    private final UserModel userModel = new UserModel();
    private final ObservableList<UserDTO> userObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setCellValueFactories();
        loadUsers();

        // Initialize Role ComboBox
        ObservableList<String> roles = FXCollections.observableArrayList("ADMIN", "MANAGER", "STAFF", "TECHNICIAN");
        cmbRole.setItems(roles);

        tblUser.setItems(userObservableList);

        tblUser.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
    }

    private void setCellValueFactories() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        try {
            UserDTO user = new UserDTO(
                    0,
                    txtName.getText().trim(),
                    txtContact.getText().trim(),
                    txtAddress.getText().trim(),
                    txtEmail.getText().trim(),
                    txtUserName.getText().trim(),
                    txtPassword.getText().trim(), // In a real app, hash this password!
                    cmbRole.getValue()
            );

            boolean isSaved = userModel.saveUser(user);
            if (isSaved) {
                showAlert(Alert.AlertType.INFORMATION, "User Saved Successfully!");
                handleReset();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to Save User.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (txtId.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select a user to update.");
            return;
        }
        if (!validateFields()) return;
        //TextField txtName, TextField txtContact,TextField txtEmail, TextField txtAddress,TextField txtId, TextField txtUserName, TextField txtPassword
        if(FieldsValidation.validateUserFields(txtName, txtContact,txtEmail, txtAddress, txtId, txtUserName, txtPassword)) {
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText().trim());
            UserDTO user = new UserDTO(
                    id,
                    txtName.getText().trim(),
                    txtContact.getText().trim(),
                    txtAddress.getText().trim(),
                    txtEmail.getText().trim(),
                    txtUserName.getText().trim(),
                    txtPassword.getText().trim(),
                    cmbRole.getValue()
            );

            boolean isUpdated = userModel.updateUser(user);
            if (isUpdated) {
                showAlert(Alert.AlertType.INFORMATION, "User Updated Successfully!");
                handleReset();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to Update User.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (txtId.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select a user to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this user?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                boolean isDeleted = userModel.deleteUser(id);
                if (isDeleted) {
                    showAlert(Alert.AlertType.INFORMATION, "User Deleted Successfully!");
                    handleReset();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed to Delete User.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error: " + e.getMessage());
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
        txtUserName.setText("");
        txtPassword.setText("");
        cmbRole.getSelectionModel().clearSelection();
        tblUser.getSelectionModel().clearSelection();
        txtSearch.setText("");
        loadUsers();
        tblUser.setItems(userObservableList);
    }

    private void loadUsers() {
        try {
            List<UserDTO> rawData = userModel.getAllUsers();
            userObservableList.clear();
            if (rawData != null) {
                userObservableList.addAll(rawData);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading Users: " + e.getMessage());
        }
    }

    private void populateFields(UserDTO u) {
        txtId.setText(String.valueOf(u.getUserId()));
        txtName.setText(u.getName());
        txtContact.setText(u.getContact());
        txtEmail.setText(u.getEmail());
        txtAddress.setText(u.getAddress());
        txtUserName.setText(u.getUserName());
        txtPassword.setText(u.getPassword());
        cmbRole.setValue(u.getRole());
    }

    // Basic internal validation - You can move this to your FieldsValidation class
    private boolean validateFields() {
        if (txtName.getText().isEmpty() || txtContact.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || txtUserName.getText().isEmpty() ||
                txtPassword.getText().isEmpty() || cmbRole.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "All fields are required!");
            return false;
        }
        // Add regex checks here similar to your Customer validation
        return true;
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }

    @FXML
    private void handleSearch() {
        String search = txtSearch.getText().toLowerCase();
        if (!search.isEmpty() ) {
            ObservableList<UserDTO> filteredList = userObservableList.stream()
                    .filter(u -> u.getName().toLowerCase().contains(search) ||
                            u.getContact().contains(search) ||
                            u.getUserName().toLowerCase().contains(search))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            tblUser.setItems(filteredList);
        } else {
            tblUser.setItems(userObservableList);
        }
    }
}