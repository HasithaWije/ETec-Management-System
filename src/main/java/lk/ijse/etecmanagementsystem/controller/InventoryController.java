package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.TilePane;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.component.ProductCard;
import lk.ijse.etecmanagementsystem.component.SkeletonCard;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.service.InventoryService;
import lk.ijse.etecmanagementsystem.service.ThreadService;
import lk.ijse.etecmanagementsystem.service.MenuBar; // Assuming you have this
import lk.ijse.etecmanagementsystem.util.ProductCondition;

import java.util.ArrayList;
import java.util.List;


public class InventoryController {

    // --- FXML UI Elements ---
    @FXML
    private TilePane productGrid;
    @FXML
    private TableView<ProductDTO> productTable;
    @FXML
    private TableColumn<ProductDTO, String> colId, colName, colCategory;
    @FXML
    private TableColumn<ProductDTO, Double> colSellPrice;
    @FXML
    private TableColumn<ProductDTO, Integer> colWarrantyMonth, colQty;

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private Button btnLoadMore, gridViewButton, tableViewButton;
    @FXML
    private Label lblPageInfo;

    @FXML
    private Button btnDashboard, btnInventory, btnRepairs, btnSuppliers, btnCustomers, btnTransactions, btnWarranty, btnSettings, btnUser;

    @FXML
    private ComboBox<ProductCondition> cmbCondition;

    @FXML
    private Button btnAddProduct;


    // --- State Management ---
    private final InventoryService inventoryService = new InventoryService();
    private final MenuBar menuBar = new MenuBar();

    // Data Containers
    private List<ProductDTO> allFetchedData = new ArrayList<>(); // Stores ALL results from DB
    private ObservableList<ProductDTO> tableDataList = FXCollections.observableArrayList();

    // Pagination / Threading
    private Task<List<ProductDTO>> currentLoadTask;
    private int currentGridLimit = 10; // Start with 10 items
    private final int BATCH_SIZE = 10; // Load 10 more on click
    private final int moreButtonThreshold = 48;
    private boolean isGridView = true; // Track current view mode

    @FXML
    public void initialize() {
        setupMenu();
        setupTableColumns();

        // 1. Setup Controls
        cmbCategory.setItems(FXCollections.observableArrayList("All Categories", "Electronics", "Accessories", "Parts"));
        cmbCategory.getSelectionModel().selectFirst();
        cmbCondition.getItems().setAll(ProductCondition.values());
        btnLoadMore.setVisible(false); // Hide until data loads

        // 2. Setup Listeners (Debouncing could be added here for optimization)
        txtSearch.textProperty().addListener((obs, old, newVal) -> refreshData());
        cmbCategory.valueProperty().addListener((obs, old, newVal) -> refreshData());

        // 3. Initial View State
        switchToGridView();
    }

    @FXML
    private void addProduct() {
        // Logic to open Add Product dialog or navigate to Add Product page
        System.out.println("Add Product button clicked.");

        openSecondaryWindow();
    }

    private void openSecondaryWindow() {
        try {
            // Changed title from "Secondary Window" to "Product Management"
            App.setupSecondaryStageScene("product", "Product Management");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void refreshData() {

        if (currentLoadTask != null && currentLoadTask.isRunning()) {
            currentLoadTask.cancel();
        }

        currentGridLimit = BATCH_SIZE;

        if (isGridView) {
            productGrid.getChildren().clear();
            for (int i = 0; i < 10; i++) {
                productGrid.getChildren().add(new SkeletonCard());
            }
            btnLoadMore.setVisible(false);
        }


        currentLoadTask = new Task<>() {
            @Override
            protected List<ProductDTO> call() throws Exception {
                // Simulate network/DB delay (Remove this in production)
                Thread.sleep(600);

                // Fetch ALL matching data from Service
                return inventoryService.getFilteredProducts(txtSearch.getText(), cmbCategory.getValue());
            }
        };


        currentLoadTask.setOnSucceeded(event -> {
            allFetchedData = currentLoadTask.getValue(); // Store master list

            if (isGridView) {
                renderGrid(); // Render cards
            } else {
                renderTable(); // Render table rows
            }
        });

        currentLoadTask.setOnFailed(event -> {
            Throwable e = currentLoadTask.getException();
            e.printStackTrace(); // Log for developer

            if (isGridView) productGrid.getChildren().clear();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Load Error");
            alert.setHeaderText("Could not load inventory.");
            alert.setContentText("Please check your database connection.\nDetails: " + e.getMessage());
            alert.showAndWait();
        });


        ThreadService.setInventoryLoadingThread(new Thread(currentLoadTask));
        ThreadService.getInventoryLoadingThread().start();
    }

    private void renderGrid() {
        productGrid.getChildren().clear(); // Clear skeletons

        if (allFetchedData.isEmpty()) {
            Label placeholder = new Label("No products found.");
            placeholder.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
            productGrid.getChildren().add(placeholder);
            btnLoadMore.setVisible(false);
            return;
        }

        int limit = Math.min(currentGridLimit, allFetchedData.size());

        for (int i = 0; i < limit; i++) {

            ProductDTO p = allFetchedData.get(i);
            productGrid.getChildren().add(new ProductCard(p));
        }

        if (limit < allFetchedData.size()) {
            btnLoadMore.setVisible(true);
        } else {
            btnLoadMore.setVisible(false);
        }

        if(productGrid.getChildren().size() >= moreButtonThreshold) {
            btnLoadMore.setVisible(false);
        }
    }

    private void renderTable() {
        tableDataList.setAll(allFetchedData);
        productTable.setItems(tableDataList);
        btnLoadMore.setVisible(false);
    }

    @FXML
    private void handleLoadMore() {
        // Increase limit
        currentGridLimit = moreButtonThreshold;
        btnLoadMore.setVisible(false);
        renderGrid();
    }

    @FXML
    private void switchToGridView() {
        isGridView = true;

        productGrid.setVisible(true);
        productGrid.setManaged(true);
        productTable.setVisible(false);
        productTable.setManaged(false);

        gridViewButton.setDisable(true);
        tableViewButton.setDisable(false);

        refreshData(); // First load

    }

    @FXML
    private void switchToTableView() {
        isGridView = false;

        productGrid.setVisible(false);
        productGrid.setManaged(false);
        productTable.setVisible(true);
        productTable.setManaged(true);

        tableViewButton.setDisable(true);
        gridViewButton.setDisable(false);

        // Render existing data
        renderTable();

    }


    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSellPrice.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        colWarrantyMonth.setCellValueFactory(new PropertyValueFactory<>("warrantyMonth"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
    }

    private void setupMenu() {
        menuBar.setActive(btnInventory);

        menuBar.setupButton(btnDashboard);
        menuBar.setupButton(btnInventory);
        menuBar.setupButton(btnRepairs);
        menuBar.setupButton(btnSuppliers);
        menuBar.setupButton(btnCustomers);
        menuBar.setupButton(btnTransactions);
        menuBar.setupButton(btnWarranty);
        menuBar.setupButton(btnSettings);
        menuBar.setupButton(btnUser);
    }
}



