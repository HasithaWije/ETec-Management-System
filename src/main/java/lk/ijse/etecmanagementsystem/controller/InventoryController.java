package lk.ijse.etecmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.component.ProductCard;
import lk.ijse.etecmanagementsystem.component.SkeletonCard;
import lk.ijse.etecmanagementsystem.dao.CategoryDAOImpl;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.model.CategoryModel;
import lk.ijse.etecmanagementsystem.model.InventoryModel;
import lk.ijse.etecmanagementsystem.util.InventoryUtil;
import lk.ijse.etecmanagementsystem.util.ThreadService;
import lk.ijse.etecmanagementsystem.util.Category;
import lk.ijse.etecmanagementsystem.util.ProductCondition;
import javafx.scene.input.MouseEvent;
import lk.ijse.etecmanagementsystem.util.Stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class InventoryController {

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
    private ComboBox<ProductCondition> cmbCondition;

    @FXML
    private ComboBox<Stock> cmbStock;

    @FXML
    private Button btnProductManager;

    @FXML
    private Button btnReset;


    private final InventoryUtil inventoryUtil = new InventoryUtil();

    private final ObservableList<ProductDTO> productDataList = FXCollections.observableArrayList();
    private List<ProductDTO> allFetchedData = new ArrayList<>(); // Stores ALL results from DB
    private ObservableList<ProductDTO> tableDataList = FXCollections.observableArrayList();


    private Task<List<ProductDTO>> currentLoadTask;
    private int currentGridLimit = 10;
    private final int BATCH_SIZE = 10;
    private final int moreButtonThreshold = 48;
    private boolean isGridView = true;


    private final InventoryModel inventoryModel = new InventoryModel();


    @FXML
    public void initialize() {

        loadProducts();
        loadCategories();

        setupTableColumns();
        setupControls();
        setupCategoryComboBox();

        setupListeners();


        switchToGridView();

    }


    private void loadProducts() {
        try {

            List<ProductDTO> rawData = inventoryModel.findAll();
            if (rawData != null) {
//                ProductUtil.productCache.setAll(rawData);
                productDataList.clear();
                productDataList.setAll(rawData);
            } else {
                productDataList.clear();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "No products found in the database.");
                alert.show();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading products: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void manageUnite() {

        System.out.println("Unit Manager button clicked.");

        try {


            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Unit Management");
            stage.setScene(new Scene(App.loadFXML("unitManagement"), 1000, 700));
            stage.showAndWait();


        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Unit Management window: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void manageProduct() {

        System.out.println("Product Manager button clicked.");

        try {


            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Product Management");
            stage.setScene(new Scene(App.loadFXML("product"), 1000, 700));
            stage.showAndWait();


        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Product Management window: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void categoryManagement() {

        System.out.println("Category Management button clicked.");
        setCategoryStage();
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
                Thread.sleep(100);

                // Fetch ALL matching data from Service
                return inventoryUtil.getFilteredProducts(productDataList, txtSearch.getText(),
                        cmbCategory.getValue(), cmbCondition.getValue(), cmbStock.getValue());
            }
        };


        currentLoadTask.setOnSucceeded(event -> {
            if (currentLoadTask.valueProperty().get() != null) {
                allFetchedData = currentLoadTask.getValue(); // Store master list
            }

            if (isGridView) {
                renderGrid(); // Render cards
            } else {
                renderTable(); // Render table rows
            }
        });

        currentLoadTask.setOnFailed(event -> {
            Throwable e = currentLoadTask.getException();
            System.out.println(" Error loading inventory: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Error loading inventory: " + e.getMessage()).show();

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

        if (productGrid.getChildren().size() >= moreButtonThreshold) {
            btnLoadMore.setVisible(false);
        }
    }

    private void renderTable() {
        tableDataList.setAll(allFetchedData);
        productTable.setItems(tableDataList);
        btnLoadMore.setVisible(false);
    }

    private void setupControls() {

        cmbCondition.getItems().setAll(ProductCondition.values());
        cmbCondition.getSelectionModel().select(ProductCondition.BOTH);
        cmbStock.getItems().setAll(Stock.values());
        cmbStock.getSelectionModel().select(Stock.ALL);
        btnLoadMore.setVisible(false);


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

    private void setupListeners() {
        // 2. Setup Listeners (Debouncing could be added here for optimization)
        txtSearch.textProperty().addListener((obs, old, newVal) -> refreshData());
        cmbCategory.valueProperty().addListener((obs, old, newVal) -> refreshData());
        cmbCondition.valueProperty().addListener((obs, old, newVal) -> refreshData());
        cmbStock.valueProperty().addListener((obs, old, newVal) -> refreshData());

    }

    @FXML
    private void getTableSelectedItem(MouseEvent event) {
        if (event.getClickCount() == 2) {

            ProductDTO selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                System.out.println("Selected Product: " + selectedProduct);

                try {
                    // 1. Create the Loader manually (Don't use App.loadFXML)
                    FXMLLoader loader = new FXMLLoader(App.class.getResource("view/product.fxml"));
                    Parent root = loader.load();

                    // 2. Get the Controller from the loader
                    ProductController controller = loader.getController();

                    // 3. Pass the data
                    System.out.println("Passing selected product to ProductController: " + selectedProduct);
                    controller.populateFields(selectedProduct);

                    // 4. Show the window
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root, 1000, 700));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Product Management");
                    stage.show();


                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Product Management navigated window: " + e.getMessage());
                    alert.showAndWait();
                }

            }
        }

    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSellPrice.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        colWarrantyMonth.setCellValueFactory(new PropertyValueFactory<>("warrantyMonth"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
    }

    private void loadCategories() {

        CategoryDAOImpl categoryDAO = new CategoryDAOImpl();
        Category.getCategories().clear();
        try {
            List<String> list = categoryDAO.getAllCategories();
            if (!list.isEmpty()) {
                Category.getCategories().setAll(list);

                System.out.println("Categories loaded from DB: ");
            } else {
                System.out.println("No categories found in the database.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load categories: " + e.getMessage());
        }

    }

    private void setupCategoryComboBox() {
        List<String> dbCategories = Category.getCategories();

        ObservableList<String> listData = FXCollections.observableArrayList();

        listData.add("All Categories");

        listData.addAll(dbCategories);

        cmbCategory.setItems(listData);

        cmbCategory.getSelectionModel().select("All Categories");
    }


    @FXML
    private void handleReset() {
        txtSearch.clear();
        setupCategoryComboBox();
        cmbCondition.getSelectionModel().select(ProductCondition.BOTH);
        cmbStock.getSelectionModel().select(Stock.ALL);
        loadProducts();
        loadCategories();
        refreshData();
    }

}



