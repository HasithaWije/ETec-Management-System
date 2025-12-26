package lk.ijse.etecmanagementsystem.controller;

import javafx.application.Platform;
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
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpdateRepairTicketController {

    @FXML private Label lblJobId;

    // --- CUSTOMER SEARCH & PREVIEW ---
    @FXML private ComboBox<String> cmbCustomer;
    @FXML private Label lblCusName;
    @FXML private Label lblCusContact;
    @FXML private Label lblCusId;
    @FXML private Label lblCusEmail;
    @FXML private Label lblCusAddress;

    // --- DEVICE FIELDS ---
    @FXML private TextField txtDeviceName;
    @FXML private TextField txtSerial;
    @FXML private TextArea txtProblem;

    // Data handling
    private RepairJobTM currentJob;
    private RepairDashboardController mainController;
    private final Map<String, CustomerDTO> customerMap = new HashMap<>();
    private final ObservableList<String> originalList = FXCollections.observableArrayList();
    private int selectedCustomerId = -1;
    private boolean isCodeUpdate = false;

    // =========================================================
    // INITIALIZATION & DATA LOADING
    // =========================================================

    @FXML
    public void initialize() {
        loadCustomerData(); // Load list for search
        setupSearchFilter(); // Attach listener
    }

    public void setJobData(RepairJobTM job, RepairDashboardController mainController) {
        this.currentJob = job;
        this.mainController = mainController;

        // UPDATED: Using Standard Getters (POJO Style)
        lblJobId.setText("JOB #" + job.getRepairId());

        // 1. FILL DEVICE DETAILS
        txtDeviceName.setText(job.getDeviceName());
        txtSerial.setText(job.getSerialNumber());
        txtProblem.setText(job.getProblemDescription());

        // 2. FILL CURRENT CUSTOMER (PREVIEW)
        lblCusName.setText(job.getCustomerName());
        lblCusContact.setText(job.getContactNumber());

        // If your TM/DTO has ID, set it.
        if (job.getOriginalDto() != null) {
            selectedCustomerId = job.getOriginalDto().getCusId();
            lblCusId.setText(String.valueOf(selectedCustomerId));

            // Set ComboBox Text to current name
            isCodeUpdate = true;
            cmbCustomer.getEditor().setText(job.getCustomerName());
            isCodeUpdate = false;
        }
    }

    // =========================================================
    // SEARCH LOGIC (SAME AS ADD TICKET)
    // =========================================================

    private void loadCustomerData() {
        customerMap.clear();
        originalList.clear();

        // MOCK DATA (Replace with DB)
        addMockCustomer(1, "Kamal Perera", "0771234567", "kamal@gmail.com", "Colombo 5");
        addMockCustomer(2, "Kamal Perera", "0718889999", "kamal.p@yahoo.com", "Kandy");
        addMockCustomer(3, "Nimal Siripala", "0751112222", "nimal@test.com", "Galle");

        cmbCustomer.setItems(FXCollections.observableArrayList(originalList));
    }

    private void addMockCustomer(int id, String name, String contact, String email, String address) {
        CustomerDTO customer = new CustomerDTO(id, name, contact, email, address);
        String key = name + " | " + contact;
        customerMap.put(key, customer);
        originalList.add(key);
    }

    private void setupSearchFilter() {
        cmbCustomer.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (isCodeUpdate) return;
            if (newVal == null || newVal.isEmpty()) {
                isCodeUpdate = true;
                try {
                    cmbCustomer.setItems(FXCollections.observableArrayList(originalList));
                    cmbCustomer.hide();
                } finally { isCodeUpdate = false; }
                return;
            }

            String lowerVal = newVal.toLowerCase();
            ObservableList<String> filtered = originalList.stream()
                    .filter(item -> item.toLowerCase().contains(lowerVal))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            isCodeUpdate = true;
            try {
                if (cmbCustomer.getItems().size() != filtered.size() || !cmbCustomer.getItems().containsAll(filtered)) {
                    cmbCustomer.setItems(filtered);
                }
                cmbCustomer.getEditor().setText(newVal);
                if (newVal.length() > 0) cmbCustomer.getEditor().positionCaret(newVal.length());
                if (!filtered.isEmpty()) cmbCustomer.show(); else cmbCustomer.hide();
            } finally { isCodeUpdate = false; }
        });

        cmbCustomer.setOnAction(e -> handleCustomerSelection());
    }

    @FXML
    private void handleCustomerSelection() {
        if (isCodeUpdate) return;

        String selectedKey = cmbCustomer.getSelectionModel().getSelectedItem();
        if (selectedKey == null) selectedKey = cmbCustomer.getEditor().getText();

        if (selectedKey != null && customerMap.containsKey(selectedKey)) {
            CustomerDTO selectedCus = customerMap.get(selectedKey);

            selectedCustomerId = selectedCus.getId();
            lblCusName.setText(selectedCus.getName());
            lblCusContact.setText(selectedCus.getNumber());
            lblCusId.setText(String.valueOf(selectedCus.getId()));
            lblCusAddress.setText(selectedCus.getAddress());
            lblCusEmail.setText(selectedCus.getEmailAddress());
        }
    }

    // =========================================================
    // BUTTON ACTIONS
    // =========================================================

    @FXML
    private void handleAddNewCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Customers.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh search list
            loadCustomerData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleUpdate() {
        if (selectedCustomerId == -1 || txtDeviceName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Customer and Device are required.");
            return;
        }

        try {
            RepairJobDTO jobDTO = new RepairJobDTO();
            // UPDATED: Using Standard Getter
            jobDTO.setRepairId(currentJob.getRepairId());

            // UPDATE: Use the selected ID (User might have changed it via Search)
            jobDTO.setCusId(selectedCustomerId);

            jobDTO.setDeviceName(txtDeviceName.getText());
            // UPDATED: CamelCase
            jobDTO.setDeviceSn(txtSerial.getText());
            jobDTO.setProblemDesc(txtProblem.getText());

            // TODO: Call RepairJobModel.update(jobDTO)
            boolean success = true;

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Details Updated Successfully.");
                mainController.refreshList();
                closeWindow();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Update Failed.");
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        // UPDATED: Using Standard Getter
        alert.setHeaderText("Delete Job #" + currentJob.getRepairId() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Call DAO Delete
            mainController.refreshList();
            closeWindow();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) lblJobId.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}