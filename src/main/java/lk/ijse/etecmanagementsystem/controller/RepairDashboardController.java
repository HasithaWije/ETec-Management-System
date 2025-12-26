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
import java.util.Optional;

public class RepairDashboardController {

    // =========================================================
    // FXML INJECTIONS
    // =========================================================

    @FXML private TextField txtSearch;
    @FXML private ComboBox<RepairStatus> comboStatusFilter;
    @FXML private Button btnAddTicket;
    @FXML private ListView<RepairJobTM> listRepairJobs;
    @FXML private VBox detailsPane;

    // Labels
    @FXML private Label lblJobId;
    @FXML private Label lblStatusBadge;
    @FXML private Label lblDate;
    @FXML private Label lblCustomerName;
    @FXML private Label lblContact;
    @FXML private Label lblDeviceName;
    @FXML private Label lblSerial;

    @FXML private ProgressBar progressWorkflow;

    @FXML private TextArea txtIntake;
    @FXML private TextArea txtDiagnosis;
    @FXML private TextArea txtResolution;

    // --- PARTS TABLE INJECTIONS ---
    @FXML private TableView<RepairPartTM> tblParts;
    @FXML private TableColumn<RepairPartTM, String> colPartName;
    @FXML private TableColumn<RepairPartTM, Double> colPartPrice;
    @FXML private TableColumn<RepairPartTM, String> colPartSN; // Used for Serial
    @FXML private TableColumn<RepairPartTM, String> colPartCondition;



    @FXML private Button btnUpdateJob;

    // =========================================================
    // DATA & INITIALIZATION
    // =========================================================

    private final ObservableList<RepairJobTM> masterData = FXCollections.observableArrayList();
    private FilteredList<RepairJobTM> filteredData;
    private RepairJobTM currentSelection;


    // --- DATA LISTS ---
    private final ObservableList<RepairPartTM> usedPartsList = FXCollections.observableArrayList(); // Visible in Table
    private final List<RepairPartTM> partsToReturnList = new ArrayList<>(); // Hidden list for removed items (Restocking)


    // Model Instance
    private final RepairJobModel repairModel = new RepairJobModel();

    @FXML
    public void initialize() {
        // 1. Setup Status Filter
        comboStatusFilter.getItems().setAll(RepairStatus.values());

        // 2. Setup List View Appearance
        setupListView();

        // 3. Load Data from DB
        loadDataFromDB();

        // 4. Setup Filtering
        filteredData = new FilteredList<>(masterData, p -> true);
        listRepairJobs.setItems(filteredData);
        setupListeners();

        // --- LINK DATA ---
        tblParts.setItems(usedPartsList);

        // --- ENABLE RIGHT-CLICK REMOVE ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Remove Part");
        deleteItem.setOnAction(event -> handleRemovePart()); // Links to the remove method
        contextMenu.getItems().add(deleteItem);
        tblParts.setContextMenu(contextMenu);
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

    // Called by SelectPartController
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

    // =========================================================
    // SAVE CHANGES LOGIC (The "Update" Feature)
    // =========================================================

    @FXML
    private void handleSaveChanges() {
        // 1. Validation
        if (currentSelection == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a repair job first.");
            return;
        }

        try {
            // --- GATHER DATA ---
            int repairId = currentSelection.getRepairId();
            String intake = txtIntake.getText();
            String diagnosis = txtDiagnosis.getText();
            String resolution = txtResolution.getText();

            // --- CALCULATE COSTS ---
            // Get base labor cost (from DTO)
            double laborCost = currentSelection.getOriginalDto().getLaborCost();

            // Sum part costs from the table
            double partsCost = 0.0;
            for (RepairPartTM part : usedPartsList) {
                partsCost += part.getUnitPrice();
            }

            double totalAmount = laborCost + partsCost;

            // --- CALL MODEL (TRANSACTION) ---
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

            // --- UPDATE UI ON SUCCESS ---
            if (isSuccess) {
                // Update Memory Object so list updates without refresh
                currentSelection.setProblemDescription(intake);
                currentSelection.setDiagnosisDescription(diagnosis);
                currentSelection.setRepairResults(resolution);

                // Update DTO Financials
                currentSelection.getOriginalDto().setPartsCost(partsCost);
                currentSelection.getOriginalDto().setTotalAmount(totalAmount);

                // Clear Return Queue (DB has processed them)
                partsToReturnList.clear();

                showAlert(Alert.AlertType.INFORMATION, "Saved Successfully",
                        "Job Updated.\nParts Cost: " + partsCost + "\nTotal: " + totalAmount);
            } else {
                showAlert(Alert.AlertType.ERROR, "Save Failed", "Database update failed.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    // =========================================================
    // DATABASE LOADING
    // =========================================================

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
        // Clear selection details if needed
        detailsPane.setVisible(false);
        currentSelection = null;
    }

    // =========================================================
    // LOGIC & EVENTS
    // =========================================================

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
    }

    private void showDetails(RepairJobTM job) {
        this.currentSelection = job;
        detailsPane.setVisible(true);

        btnUpdateJob.setDisable(job.getStatus() != RepairStatus.PENDING);

        lblJobId.setText("Job #" + job.getRepairId());
        lblDate.setText(job.getDateInFormatted());
        lblCustomerName.setText(job.getCustomerName());
        lblContact.setText(job.getContactNumber());
        lblDeviceName.setText(job.getDeviceName());
        lblSerial.setText(job.getSerialNumber());
        txtIntake.setText(job.getProblemDescription());   // Tab 1
        txtDiagnosis.setText(job.getDiagnosisDescription()); // Tab 2
        txtResolution.setText(job.getRepairResults());       // Tab 3

        refreshStatusUI(job.getStatus());

        // --- LOAD PARTS FROM DB ---
        try {
            // 1. Clear previous selection data
            usedPartsList.clear();
            partsToReturnList.clear();

            // 2. Fetch saved parts for this specific job
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
            // 1. Add to return list (so Model knows to set it back to 'AVAILABLE')
            partsToReturnList.add(selectedPart);

            // 2. Remove from UI immediately
            usedPartsList.remove(selectedPart);
        }
    }

    @FXML
    private void handleNextStatus() {
        if (currentSelection == null) return;

        RepairStatus current = currentSelection.getStatus();
        int nextOrdinal = current.ordinal() + 1;

        if (nextOrdinal < RepairStatus.values().length) {
            RepairStatus nextStatus = RepairStatus.values()[nextOrdinal];
            if (nextStatus != RepairStatus.CANCELLED) {
                updateStatus(nextStatus);
            }
        }
    }

    @FXML
    private void handlePrevStatus() {
        if (currentSelection == null) return;
        int prevOrdinal = currentSelection.getStatus().ordinal() - 1;
        if (prevOrdinal >= 0) {
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
                refreshStatusUI(newStatus);
                listRepairJobs.refresh();
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
            loadDataFromDB();
            if(currentSelection != null) showDetails(currentSelection);

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // =========================================================
    // UI HELPERS
    // =========================================================

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

        double max = RepairStatus.DELIVERED.ordinal();
        double current = status.ordinal();
        if(current > max) current = 0;
        progressWorkflow.setProgress(current / max);
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
                    Label name = new Label(item.getCustomerName() + " - " + item.getDeviceName());
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
        // Clear all fields and reset state
        listRepairJobs.getSelectionModel().clearSelection();
        usedPartsList.clear();
    }
}