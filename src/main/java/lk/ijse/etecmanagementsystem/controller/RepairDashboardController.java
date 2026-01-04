package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.tm.RepairJobTM;
import lk.ijse.etecmanagementsystem.dto.tm.RepairPartTM;
import lk.ijse.etecmanagementsystem.model.RepairJobModel;
import lk.ijse.etecmanagementsystem.util.RepairStatus;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class RepairDashboardController {

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<RepairStatus> comboStatusFilter;
    @FXML
    private Button btnAddTicket;
    @FXML
    private ListView<RepairJobTM> listRepairJobs;
    @FXML
    private VBox detailsPane;
    @FXML
    private Button btnReset;


    @FXML
    private Label lblJobId;
    @FXML
    private Label lblStatusBadge;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblCustomerName;
    @FXML
    private Label lblContact;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblAddress;
    @FXML
    private Label lblDeviceName;
    @FXML
    private Label lblSerial;

    @FXML
    private ProgressBar progressWorkflow;

    @FXML
    private TextArea txtIntake;
    @FXML
    private TextArea txtDiagnosis;
    @FXML
    private TextArea txtResolution;
    @FXML
    private TextField txtLaborCost;
    @FXML
    private TextField txtTotalCost;

    // --- PARTS TABLE INJECTIONS ---
    @FXML
    private TableView<RepairPartTM> tblParts;
    @FXML
    private TableColumn<RepairPartTM, String> colPartName;
    @FXML
    private TableColumn<RepairPartTM, Double> colPartPrice;
    @FXML
    private TableColumn<RepairPartTM, String> colPartSN; // Used for Serial
    @FXML
    private TableColumn<RepairPartTM, String> colPartCondition;


    @FXML
    private Button btnUpdateJob;
    @FXML
    private Button btnSaveChanges;
    @FXML
    private Button btnUnclaimed;
    @FXML
    private Button btnCheckout;
    @FXML
    private Button btnRemovePart;

    private final ObservableList<RepairJobTM> masterData = FXCollections.observableArrayList();
    private FilteredList<RepairJobTM> filteredData;
    private RepairJobTM currentSelection;

    private final ObservableList<RepairPartTM> usedPartsList = FXCollections.observableArrayList(); // Visible in Table
    private final List<RepairPartTM> partsToReturnList = new ArrayList<>(); // Hidden list for removed items (Restocking)

    private final RepairJobModel repairModel = new RepairJobModel();

    @FXML
    public void initialize() {

        comboStatusFilter.getItems().setAll(RepairStatus.values());

        setupListView();

        loadDataFromDB();

        filteredData = new FilteredList<>(masterData, p -> true);
        listRepairJobs.setItems(filteredData);
        setupListeners();

        tblParts.setItems(usedPartsList);

        detailsPane.setVisible(false);

        btnRemovePart.setVisible(false);
        btnRemovePart.setDisable(true);
        tblParts.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnRemovePart.setVisible(newValue != null);// Show button only when a part is selected
            btnRemovePart.setDisable(newValue == null);// Disable button when no part is selected
        });
    }

    @FXML
    private void handleCheckout() {
        if (currentSelection == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a job.");
            return;
        }

        // Optional: Block checkout if status is already delivered
        if (currentSelection.getStatus() == RepairStatus.DELIVERED) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "This job is already delivered.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/RepairCheckout.fxml"));
            Parent root = loader.load();

            RepairCheckoutController controller = loader.getController();
            controller.setInvoiceData(currentSelection, usedPartsList, this);

            Stage stage = new Stage();
            stage.setTitle("Repair Checkout");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Blocks main window
            stage.setResizable(false);
            stage.showAndWait();

            setupListView();
            resetSelectedJob();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "UI Error", "Could not open checkout window.");
        }
    }

    @FXML
    private void handleAddPart() {
        if (currentSelection == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/SelectRepairPart.fxml"));
            Parent root = loader.load();

            SelectRepairPartController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Part");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPartToTable(RepairPartTM part) {
        // Check for duplicates
        for (RepairPartTM p : usedPartsList) {
            if (p.getItemId() == part.getItemId()) {
                showAlert(Alert.AlertType.WARNING, "Duplicate", "This item is already added.");
                return;
            }
        }
        usedPartsList.add(part);
    }

    @FXML
    private void handleSaveChanges() {
        // 1. Validation
        if (currentSelection == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a repair job first.");
            return;
        }

        try {
            int repairId = currentSelection.getRepairId();
            String intake = txtIntake.getText();
            String diagnosis = txtDiagnosis.getText();
            String resolution = txtResolution.getText();

            double laborCost = 0.0;
            try {
                if (!txtLaborCost.getText().isEmpty()) {
                    laborCost = Double.parseDouble(txtLaborCost.getText());
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Labor Cost must be a number.");
                return;
            }

            // Sum part costs from the table
            double partsCost = 0.0;
            for (RepairPartTM part : usedPartsList) {
                partsCost += part.getUnitPrice();
            }

            double totalAmount = laborCost + partsCost;

            boolean isSuccess = repairModel.updateRepairJobDetails(
                    repairId,
                    intake,
                    diagnosis,
                    resolution,
                    laborCost,
                    partsCost,
                    totalAmount,
                    new ArrayList<>(usedPartsList), // Active Parts (Link & Mark Sold)
                    partsToReturnList               // Returned Parts (Unlink & Mark Available)
            );

            if (isSuccess) {

                currentSelection.setProblemDescription(intake);
                currentSelection.setDiagnosisDescription(diagnosis);
                currentSelection.setRepairResults(resolution);

                currentSelection.getOriginalDto().setLaborCost(laborCost); // <--- Update Memory
                currentSelection.getOriginalDto().setPartsCost(partsCost);
                currentSelection.getOriginalDto().setTotalAmount(totalAmount);
                txtTotalCost.setText(Double.toString(totalAmount));

                partsToReturnList.clear();

                if (currentSelection.getStatus() == RepairStatus.PENDING) {
                    updateStatus(RepairStatus.DIAGNOSIS);

                    showAlert(Alert.AlertType.INFORMATION, "Saved & Updated",
                            "Details saved.\nStatus automatically moved to 'DIAGNOSIS'.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Saved Successfully",
                            "Job Updated.\nParts Cost: " + partsCost + "\nTotal: " + totalAmount);
                }


                setupListView();
                resetSelectedJob();

            } else {
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Database update failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadDataFromDB() {
        try {
            List<RepairJobTM> dbList = repairModel.getAllRepairJobs();
            masterData.clear();
            masterData.addAll(dbList);
            listRepairJobs.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load repair jobs: " + e.getMessage());
        }
    }

    public void refreshList() {
        loadDataFromDB();

        detailsPane.setVisible(false);
        currentSelection = null;
    }

    private void setupListeners() {
        // Search
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterList());
        comboStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterList());

        // Selection
        listRepairJobs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
//                resetDetailForm();
                showDetails(newVal);
            } else {
                detailsPane.setVisible(false);
            }
        });
        txtLaborCost.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                double laborCost = 0.0;
                try {
                    if (!newVal.isEmpty()) {
                        laborCost = Double.parseDouble(newVal);
                    }
                } catch (NumberFormatException e) {
                    txtLaborCost.setText(oldVal); // Revert to old value
                    return;
                }

                double partsCost = 0.0;
                for (RepairPartTM part : usedPartsList) {
                    partsCost += part.getUnitPrice();
                }

                double totalAmount = laborCost + partsCost;
                txtTotalCost.setText(String.valueOf(totalAmount));
            }
        });
    }

    private void filterList() {
        String filterText = txtSearch.getText().toLowerCase();
        RepairStatus statusFilter = comboStatusFilter.getValue();

        filteredData.setPredicate(job -> {
            boolean matchesText = job.getCustomerName().toLowerCase().contains(filterText) ||
                    job.getDeviceName().toLowerCase().contains(filterText) ||
                    String.valueOf(job.getRepairId()).contains(filterText);

            boolean matchesStatus = (statusFilter == null) || job.getStatus() == statusFilter;

            return matchesText && matchesStatus;
        });
        setupListView();

    }

    private void showDetails(RepairJobTM job) {
        this.currentSelection = job;
        detailsPane.setVisible(true);

        btnUpdateJob.setDisable(job.getStatus() != RepairStatus.PENDING);

        lblJobId.setText("Job #" + job.getRepairId());
        lblDate.setText(job.getDateInFormatted());
        lblCustomerName.setText(job.getCustomerName());
        lblContact.setText(job.getContactNumber());
        lblEmail.setText(job.getEmail());
        lblAddress.setText(job.getAddress());
        lblDeviceName.setText(job.getDeviceName());
        lblSerial.setText(job.getSerialNumber());
        txtIntake.setText(job.getProblemDescription());   // Tab 1
        txtDiagnosis.setText(job.getDiagnosisDescription()); // Tab 2
        txtResolution.setText(job.getRepairResults());       // Tab 3

        txtLaborCost.setText(String.valueOf(job.getOriginalDto().getLaborCost()));
        txtTotalCost.setText(String.valueOf(job.getOriginalDto().getTotalAmount()));

        refreshStatusUI(job.getStatus());

        try {

            usedPartsList.clear();
            partsToReturnList.clear();

            List<RepairPartTM> dbParts = repairModel.getUsedParts(job.getRepairId());
            usedPartsList.addAll(dbParts);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load parts: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemovePart() {
        RepairPartTM selectedPart = tblParts.getSelectionModel().getSelectedItem();
        if (selectedPart == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove " + selectedPart.getItemName() + "?\n(Item will be restocked upon Save)",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            partsToReturnList.add(selectedPart);

            usedPartsList.remove(selectedPart);


        }
        tblParts.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleNextStatus() {
        if (currentSelection == null) return;


        RepairStatus current = currentSelection.getStatus();
        int nextOrdinal = current.ordinal() + 1;

        if (nextOrdinal < RepairStatus.values().length) {
            RepairStatus nextStatus = RepairStatus.values()[nextOrdinal];
            if (nextStatus != RepairStatus.CANCELLED) {

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to advance the status?",
                        ButtonType.YES, ButtonType.NO);
                confirmAlert.showAndWait();
                if (confirmAlert.getResult() != ButtonType.YES) {
                    return; // User cancelled
                }

                if (nextStatus == RepairStatus.DELIVERED) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Marking as DELIVERED is final. Are you sure?" +
                                    "Do you want to check out now?",
                            ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        handleCheckout();
                        return;
                    } else {
                        return; // User cancelled
                    }
                }

                updateStatus(nextStatus);
            }
        }
    }

    @FXML
    private void handlePrevStatus() {
        if (currentSelection == null) return;

        int prevOrdinal = currentSelection.getStatus().ordinal() - 1;
        if (prevOrdinal >= 0) {
            if (currentSelection.getStatus().equals(RepairStatus.DELIVERED)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Action", "Cannot move back from DELIVERED status.");
                return;
            } else if (currentSelection.getStatus().equals(RepairStatus.PENDING)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Action", "Cannot move back from PENDING status.");
                return;
            } else if (currentSelection.getStatus().equals(RepairStatus.CANCELLED)) {
                updateStatus(RepairStatus.values()[prevOrdinal - 1]);
                return;
            }
            updateStatus(RepairStatus.values()[prevOrdinal]);
        }
    }

    @FXML
    private void handleUnclaimed() {
        if (currentSelection == null) return;
        updateStatus(RepairStatus.CANCELLED);
    }

    private void updateStatus(RepairStatus newStatus) {
        try {
            boolean isUpdated = repairModel.updateStatus(currentSelection.getRepairId(), newStatus);
            if (isUpdated) {
                currentSelection.setStatus(newStatus);

                setupListView();
                resetSelectedJob();
//                refreshStatusUI(newStatus);
//                listRepairJobs.refresh();

            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status in Database");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update status: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateJob() {
        if (currentSelection == null || currentSelection.getStatus() != RepairStatus.PENDING) return;
        openUpdateWindow();
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        if (currentSelection == null) return;
        if (currentSelection.getStatus() == RepairStatus.PENDING) {
            openUpdateWindow();
        }
    }

    private void openUpdateWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/UpdateRepairTicket.fxml"));
            Parent root = loader.load();

            UpdateRepairTicketController controller = loader.getController();
            controller.setJobData(currentSelection, this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh list in case data changed
            setupListView();
            resetSelectedJob();

            if (currentSelection != null) showDetails(currentSelection);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewTicket() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/AddRepairTicket.fxml"));
            Parent root = loader.load();

            AddRepairTicketController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh list after adding new ticket
            setupListView();
            resetSelectedJob();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshStatusUI(RepairStatus status) {
        lblStatusBadge.setText(status.toString());
        lblStatusBadge.getStyleClass().removeAll("status-pending", "status-warn", "status-done", "status-danger");


        switch (status) {
            case COMPLETED:
            case DELIVERED:
                lblStatusBadge.getStyleClass().add("status-done");
                break;
            case PENDING:
                lblStatusBadge.getStyleClass().add("status-warn");
                break;
            case CANCELLED:
                lblStatusBadge.getStyleClass().add("status-danger");
                break;
            default:
                lblStatusBadge.getStyleClass().add("status-pending");
        }

        updateButtonStates(status);


        double max = RepairStatus.DELIVERED.ordinal();
        double current = status.ordinal();
        if (current > max) current = 0;
        progressWorkflow.setProgress(current / max);
    }

    private void updateButtonStates(RepairStatus status) {
        // 1. Reset: Enable everything by default
        btnUpdateJob.setDisable(false);
        btnSaveChanges.setDisable(false);
        btnUnclaimed.setDisable(false);
        if (btnCheckout != null) btnCheckout.setDisable(false);

        // 2. Apply Rules
        switch (status) {
            case PENDING:
                // All Active
                break;

            case DIAGNOSIS:
            case WAITING_PARTS:
            case COMPLETED:
                btnUpdateJob.setDisable(true);
                break;

            case DELIVERED:
            case CANCELLED:
                btnUpdateJob.setDisable(true);
                btnSaveChanges.setDisable(true);
                btnUnclaimed.setDisable(true);

                if (btnCheckout != null) btnCheckout.setDisable(true);
                break;
        }
    }

    private void setupListView() {

        listRepairJobs.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(RepairJobTM item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vBox = new VBox(3);
                    switch (item.getStatus()) {
                        case COMPLETED:
                            setStyle("-fx-background-color: #d1ecf1;"); // Light blue
                            break;
                        case DIAGNOSIS, WAITING_PARTS:
                            setStyle("-fx-background-color: #f0f0f0;"); // Light gray
                            break;
                        case DELIVERED:
                            setStyle("-fx-background-color: #d4edda;"); // Light green

                            break;
                        case PENDING:
                            setStyle("-fx-background-color: #fff3cd;"); // Light yellow

                            break;
                        case CANCELLED:
                            setStyle("-fx-background-color: #f8d7da;"); // Light red
                            break;
                        default:
                            setStyle(""); // Default
                    }
                    Label name = new Label("#" + item.getRepairId() + "_" + item.getCustomerName() + " - " + item.getDeviceName());
                    name.setStyle("-fx-font-weight: bold;");

                    Label status = new Label("Status: " + item.getStatus());
                    status.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");


                    vBox.getChildren().addAll(name, status);
                    setGraphic(vBox);
                }
            }
        });

        colPartName.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        colPartCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));

        colPartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        colPartSN.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));

        tblParts.setItems(usedPartsList);
    }


    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }

    private void resetDetailForm() {
        listRepairJobs.getSelectionModel().clearSelection();
        usedPartsList.clear();
    }

    private void resetSelectedJob() {
        RepairJobTM selectedJob = listRepairJobs.getSelectionModel().getSelectedItem();
        if (selectedJob != null) {
            loadDataFromDB();
            listRepairJobs.getSelectionModel().clearSelection();

            listRepairJobs.getSelectionModel().select(
                    masterData.stream()
                            .filter(job -> job.getRepairId() == selectedJob.getRepairId())
                            .findFirst()
                            .orElse(null)
            );
        }
    }

    @FXML
    private void resetSelectedJobSimple() {
        listRepairJobs.getSelectionModel().clearSelection();
        comboStatusFilter.getSelectionModel().clearSelection();
        setupListView();
        txtSearch.clear();
    }
}