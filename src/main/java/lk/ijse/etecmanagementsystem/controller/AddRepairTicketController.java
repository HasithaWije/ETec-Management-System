package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AddRepairTicketController {

    // --- FXML INJECTIONS ---
    @FXML private ComboBox<String> cmbCustomer;

    // Details Labels
    @FXML private Label lblCusName;
    @FXML private Label lblCusContact;
    @FXML private Label lblCusId;
    @FXML private Label lblCusAddress;

    @FXML private TextField txtDeviceName;
    @FXML private TextField txtSerial;
    @FXML private TextArea txtProblem;

    // --- DATA HANDLING ---
    private final Map<String, CustomerDTO> customerMap = new HashMap<>();
    private final ObservableList<String> originalList = FXCollections.observableArrayList();

    private int selectedCustomerId = -1;
    private RepairDashboardController mainController;

    // --- OPTIMIZATION FLAGS ---
    private boolean isCodeUpdate = false;

    public void setMainController(RepairDashboardController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadCustomerData();
        setupSearchFilter();
    }

    private void loadCustomerData() {
        // Clear existing data to prevent memory duplicates if called multiple times
        customerMap.clear();
        originalList.clear();

        // Mock Data (Ideally replace with CustomerModel.getAll())
        addMockCustomer(1, "Kamal Perera", "0771234567", "kamal@gmail.com", "Colombo 5");
        addMockCustomer(2, "Kamal Perera", "0718889999", "kamal.p@yahoo.com", "Kandy");
        addMockCustomer(3, "Nimal Siripala", "0751112222", "nimal@test.com", "Galle");

        // Initial Set
        cmbCustomer.setItems(FXCollections.observableArrayList(originalList));
    }

    private void addMockCustomer(int id, String name, String contact, String email, String address) {
        CustomerDTO customer = new CustomerDTO(id, name, contact, email, address);
        String key = name + " | " + contact;
        customerMap.put(key, customer);
        originalList.add(key);
    }

    // --- FIX: OPTIMIZED SEARCH LOGIC ---
    private void setupSearchFilter() {
        cmbCustomer.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {

            // 1. STOP RECURSION: If we are updating via code, ignore this event
            if (isCodeUpdate) return;

            // 2. Handle empty text
            if (newVal == null || newVal.isEmpty()) {
                isCodeUpdate = true;
                try {
                    cmbCustomer.setItems(FXCollections.observableArrayList(originalList));
                    cmbCustomer.hide();
                } finally {
                    isCodeUpdate = false;
                }
                return;
            }

            // 3. Filter Logic
            String lowerVal = newVal.toLowerCase();
            ObservableList<String> filtered = originalList.stream()
                    .filter(item -> item.toLowerCase().contains(lowerVal))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            // 4. Update UI safely
            isCodeUpdate = true; // Lock the listener
            try {
                // Only update if the list size actually changed to save resources
                if (cmbCustomer.getItems().size() != filtered.size() || !cmbCustomer.getItems().containsAll(filtered)) {
                    cmbCustomer.setItems(filtered);
                }

                // Restore text and caret position (because setItems clears text)
                cmbCustomer.getEditor().setText(newVal);

                // Move caret to end of text
                if (newVal.length() > 0) {
                    cmbCustomer.getEditor().positionCaret(newVal.length());
                }

                // Show/Hide dropdown
                if (!filtered.isEmpty()) {
                    cmbCustomer.show();
                } else {
                    cmbCustomer.hide();
                }
            } finally {
                isCodeUpdate = false; // Unlock the listener
            }
        });

        // Handle Enter Key or Click
        cmbCustomer.setOnAction(e -> handleCustomerSelection());
    }

    @FXML
    private void handleCustomerSelection() {
        // Prevent Logic if it's just a typing update
        if (isCodeUpdate) return;

        String selectedKey = cmbCustomer.getSelectionModel().getSelectedItem();

        // Fallback: if selection model is empty, try getting text (User might have pasted text)
        if (selectedKey == null) {
            selectedKey = cmbCustomer.getEditor().getText();
        }

        if (selectedKey != null && customerMap.containsKey(selectedKey)) {
            CustomerDTO selectedCus = customerMap.get(selectedKey);

            selectedCustomerId = selectedCus.getId();
            lblCusName.setText(selectedCus.getName());
            lblCusContact.setText(selectedCus.getNumber());
            lblCusId.setText(String.valueOf(selectedCus.getId()));
            lblCusAddress.setText(selectedCus.getAddress());
        }
    }

    // --- ADD NEW CUSTOMER ---
    @FXML
    private void handleAddNewCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Customers.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Customer Management");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh Data
            loadCustomerData();
            cmbCustomer.requestFocus();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Customer Manager");
        }
    }

    @FXML
    private void handleSaveTicket() {
        if (selectedCustomerId == -1) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Please select a valid customer.");
            return;
        }
        if (txtDeviceName.getText().isEmpty() || txtProblem.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Device details are required.");
            return;
        }

        try {
            RepairJobDTO newJob = new RepairJobDTO();
            newJob.setCusId(selectedCustomerId);
            newJob.setDeviceName(txtDeviceName.getText());
            // UPDATED: CamelCase to match DTO
            newJob.setDeviceSn(txtSerial.getText());
            newJob.setProblemDesc(txtProblem.getText());

            newJob.setStatus(RepairStatus.PENDING);
            newJob.setDateIn(new Date());
            newJob.setUserId(1);

            // TODO: Call RepairJobModel.save(newJob)
            boolean isSaved = true;

            if (isSaved) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Ticket Created Successfully!");
                if(mainController != null) mainController.refreshList();
                closeWindow();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save ticket.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtDeviceName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}