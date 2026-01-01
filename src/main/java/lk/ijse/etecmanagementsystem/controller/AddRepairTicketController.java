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
import lk.ijse.etecmanagementsystem.db.DBConnection;
import lk.ijse.etecmanagementsystem.dto.CustomerDTO;
import lk.ijse.etecmanagementsystem.dto.RepairJobDTO;
import lk.ijse.etecmanagementsystem.model.CustomersModel; // NEW IMPORT
import lk.ijse.etecmanagementsystem.model.RepairJobModel; // NEW IMPORT
import lk.ijse.etecmanagementsystem.util.RepairStatus;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AddRepairTicketController {

    @FXML
    private ComboBox<String> cmbCustomer;

    @FXML
    private Label lblCusName;
    @FXML
    private Label lblCusContact;
    @FXML
    private Label lblCusId;
    @FXML
    private Label lblCusAddress;

    @FXML
    private TextField txtDeviceName;
    @FXML
    private TextField txtSerial;
    @FXML
    private TextArea txtProblem;

    private final Map<String, CustomerDTO> customerMap = new HashMap<>();
    private final ObservableList<String> originalList = FXCollections.observableArrayList();

    private int selectedCustomerId = -1;
    private RepairDashboardController mainController;

    private final CustomersModel customersModel = new CustomersModel();
    private final RepairJobModel repairJobModel = new RepairJobModel();

    public void setMainController(RepairDashboardController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadCustomerData();
        setupSearchFilter();
    }

    private void loadCustomerData() {
        customerMap.clear();
        originalList.clear();

        try {
            // FETCH REAL DATA FROM DB
            List<CustomerDTO> customers = customersModel.getAllCustomers();

            for (CustomerDTO customer : customers) {
                // Key format: "Name | Contact" for easy searching
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
        }
    }

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

            loadCustomerData(); // Refresh list after adding
            cmbCustomer.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Customer Manager");
        }
    }

    @FXML
    private void handleSaveTicket() {
        // Validation
        if (selectedCustomerId == -1) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Please select a valid customer.");
            return;
        }
        if (txtDeviceName.getText().isEmpty() || txtProblem.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Device Name and Problem are required.");
            return;
        }

        try {
            // 1. Create DTO
            RepairJobDTO newJob = new RepairJobDTO();
            newJob.setCusId(selectedCustomerId);
            newJob.setDeviceName(txtDeviceName.getText());
            newJob.setDeviceSn(txtSerial.getText());
            newJob.setProblemDesc(txtProblem.getText());

            newJob.setStatus(RepairStatus.PENDING);
            newJob.setDateIn(new Date());

            newJob.setUserId(1);

            boolean isSaved = repairJobModel.saveRepairJob(newJob);

            if (isSaved) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ticket saved successfully with ID: " + newJob.getRepairId());
                alert.setTitle("Success");
                alert.setHeaderText("Repair Ticket Saved");
                alert.showAndWait();
                if (mainController != null) mainController.refreshList();
                closeWindow();

                generateIntakeReceipt(newJob.getRepairId());

            } else {
                showAlert(Alert.AlertType.ERROR, "Failure", "Ticket could not be saved.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    public void generateIntakeReceipt(int repairId) {
        try {

            String path = "reports/repairTicket.jasper";

            InputStream reportStream = App.class.getResourceAsStream(path);

            if (reportStream == null) {
                System.err.println("Error: Could not find repairTicket.jasper at " + path);
                return;
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

            Map<String, Object> parameters = new HashMap<>();

            parameters.put("repairId", repairId);

            Connection connection = DBConnection.getInstance().getConnection();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            JasperViewer.viewReport(jasperPrint, false); // false = Don't close app on exit

        } catch (JRException | java.sql.SQLException e) {
            e.printStackTrace();
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