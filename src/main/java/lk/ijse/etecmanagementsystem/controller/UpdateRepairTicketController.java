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
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.model.CustomersModel; // Model Import
import lk.ijse.etecmanagementsystem.model.RepairJobModel; // Model Import

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class UpdateRepairTicketController {

    @FXML
    private Label lblJobId;
    @FXML
    private ComboBox<String> cmbCustomer;
    @FXML
    private Label lblCusName;
    @FXML
    private Label lblCusContact;
    @FXML
    private Label lblCusId;
    @FXML
    private Label lblCusEmail;
    @FXML
    private Label lblCusAddress;
    @FXML
    private TextField txtDeviceName;
    @FXML
    private TextField txtSerial;
    @FXML
    private TextArea txtProblem;

    // Data handling
    private RepairJobTM currentJob;
    private RepairDashboardController mainController;
    private final Map<String, CustomerDTO> customerMap = new HashMap<>();
    private final ObservableList<String> originalList = FXCollections.observableArrayList();
    private int selectedCustomerId = -1;

    private final CustomersModel customersModel = new CustomersModel();
    private final RepairJobModel repairJobModel = new RepairJobModel();

    @FXML
    public void initialize() {
        loadCustomerData(); // Load list for search
        setupSearchFilter(); // Attach listener
    }

    public void setJobData(RepairJobTM job, RepairDashboardController mainController) {
        this.currentJob = job;
        this.mainController = mainController;

        lblJobId.setText("JOB #" + job.getRepairId());

        txtDeviceName.setText(job.getDeviceName());
        txtSerial.setText(job.getSerialNumber());
        txtProblem.setText(job.getProblemDescription());

        lblCusName.setText(job.getCustomerName());
        lblCusContact.setText(job.getContactNumber());

        if (job.getOriginalDto() != null) {
            selectedCustomerId = job.getOriginalDto().getCusId();
            lblCusId.setText(String.valueOf(selectedCustomerId));

            populateCustomerDetailsById(selectedCustomerId);

            cmbCustomer.getEditor().setText(job.getCustomerName());

        }
    }

    private void populateCustomerDetailsById(int cusId) {
        for (CustomerDTO dto : customerMap.values()) {
            if (dto.getId() == cusId) {
                lblCusEmail.setText(dto.getEmailAddress());
                lblCusAddress.setText(dto.getAddress());
                break;
            }
        }
    }

    private void loadCustomerData() {
        customerMap.clear();
        originalList.clear();

        try {
            // FETCH REAL DATA FROM DB
            List<CustomerDTO> customers = customersModel.getAllCustomers();

            for (CustomerDTO customer : customers) {
                String key = customer.getName() + " | " + customer.getNumber();
                customerMap.put(key, customer);
                originalList.add(key);
            }
            cmbCustomer.setItems(FXCollections.observableArrayList(originalList));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load customers.");
        }
    }

    private void setupSearchFilter() {
        cmbCustomer.getEditor().textProperty().addListener((obs, old, newVal) -> {

            if (newVal == null) return;

            if (cmbCustomer.getSelectionModel().getSelectedItem() != null &&
                    Objects.equals(cmbCustomer.getSelectionModel().getSelectedItem(), newVal)) {
                return;
            }

            cmbCustomer.getSelectionModel().clearSelection();


            ObservableList<String> filteredList = originalList.stream()
                    .filter(item -> item.toLowerCase().contains(newVal.trim().toLowerCase()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            if (!filteredList.isEmpty()) {
                cmbCustomer.setItems(filteredList);

                cmbCustomer.getEditor().setText(newVal);
                cmbCustomer.getEditor().positionCaret(newVal.length());

                if (!cmbCustomer.isShowing()) {
                    cmbCustomer.show();
                }
            } else {

                cmbCustomer.hide();
            }
        });


        cmbCustomer.setOnAction(e -> {
            String key = cmbCustomer.getSelectionModel().getSelectedItem();

            if (key == null) key = cmbCustomer.getEditor().getText();

            if (key != null && customerMap.containsKey(key)) {
                handleCustomerSelection();
            }
        });
    }

    @FXML
    private void handleCustomerSelection() {
        String selectedKey = cmbCustomer.getSelectionModel().getSelectedItem();

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedCustomerId == -1 || txtDeviceName.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Customer and Device are required.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Update details for Job #" + currentJob.getRepairId() + "?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() != ButtonType.YES) {
            return;
        }

        try {
            RepairJobDTO jobDTO = new RepairJobDTO();
            jobDTO.setRepairId(currentJob.getRepairId());


            jobDTO.setCusId(selectedCustomerId);
            jobDTO.setDeviceName(txtDeviceName.getText());
            jobDTO.setDeviceSn(txtSerial.getText());
            jobDTO.setProblemDesc(txtProblem.getText());

            // CALL MODEL
            boolean success = repairJobModel.updateRepairJob(jobDTO);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Details Updated Successfully.");
                mainController.refreshList(); // Reload main dashboard
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update record.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete Job #" + currentJob.getRepairId() + "?");
        alert.setContentText("This action cannot be undone.");

        try {
            List<RepairPartTM> associatedParts = repairJobModel.getUsedParts(currentJob.getRepairId());

            if (associatedParts != null && !associatedParts.isEmpty()) {
                StringBuilder partsList = new StringBuilder("The following parts are associated with this job:\n");
                for (RepairPartTM part : associatedParts) {
                    partsList.append("- ").append(part.getItemName()).append(" (Qty: ").append(1).append(")\n");
                }
                partsList.append("Deleting this job will also remove these associated parts.");

                Alert partsAlert = new Alert(Alert.AlertType.WARNING);
                partsAlert.setTitle("Associated Parts Found");
                partsAlert.setHeaderText("Cannot delete job with associated parts. you can delete it manually first.");
                partsAlert.setContentText(partsList.toString());
                partsAlert.showAndWait();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to check associated parts.");
            return;
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = repairJobModel.deleteRepairJob(currentJob.getRepairId());

                if (success) {
                    mainController.refreshList();
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete record.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

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