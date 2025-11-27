package lk.ijse.etecmanagementsystem.controller;


import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lk.ijse.etecmanagementsystem.service.ThreadService;
import lk.ijse.etecmanagementsystem.util.MenuBar;
import lk.ijse.etecmanagementsystem.service.Login;

import java.util.ArrayList;
import java.util.List;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lk.ijse.etecmanagementsystem.dto.ProductDTO; // Import your model

import java.util.Comparator;
import java.util.stream.Collectors;

public class InventoryController {
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnInventory;
    @FXML
    private Button btnRepairs;
    @FXML
    private Button btnSuppliers;
    @FXML
    private Button btnCustomers;
    @FXML
    private Button btnTransactions;
    @FXML
    private Button btnWarranty;
    @FXML
    private Button btnSettings;
    @FXML
    private Button btnUser;

    @FXML
    private TilePane productGrid;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private Button btnLoadMore;


    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;
    @FXML
    private Label lblPageInfo;

    @FXML
    private TableView productTable;
    @FXML
    private TableColumn colId;
    @FXML
    private TableColumn colName;
    @FXML
    private TableColumn colCategory;
    @FXML
    private TableColumn colSellPrice;
    @FXML
    private TableColumn colWarrantyMonth;
    @FXML
    private TableColumn colQty;


    private int currentPage = 0;
    private final int ITEMS_PER_PAGE = 8;

    private final List<ProductDTO> allProductDTOS = new ArrayList<>(); // Master list
    private List<ProductDTO> displayedList = new ArrayList<>(); // List after filtering

    private final List<FadeTransition> runningAnimations = new ArrayList<>();

    private int currentLimit = 10;
    private final int BATCH_SIZE = 10;
    private final int moreButtonThreshold = 48;

    MenuBar menuBar = new MenuBar();


    @FXML
    public void initialize() {

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

        String username = Login.getUserName();
        btnUser.setText(username);



        // 1. Initialize Dummy Data (Or load from DB)
        loadDummyData();

        // 2. Setup Category Dropdown
        cmbCategory.setItems(FXCollections.observableArrayList("All Categories", "Electronics", "Accessories", "Parts"));
        cmbCategory.getSelectionModel().selectFirst();

        // 3. Setup Listeners (When user types or selects category)
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterAndRender());
        cmbCategory.valueProperty().addListener((observable, oldValue, newValue) -> filterAndRender());

        // 4. Initial Render
        filterAndRender();


        // Set up the product table
//        setProductTable();
        loadProductData();

    }

    private void loadDummyData() {
        for (int i = 1; i <= 3000; i++) {
            String cat = (i % 3 == 0) ? "Electronics" : (i % 2 == 0) ? "Accessories" : "Parts";
            allProductDTOS.add(new ProductDTO("Item " + i, 1000 + (i * 50), cat, "placeholder.png"));
        }

        allProductDTOS.add(new ProductDTO("Apple iPhone22222222222222222222", 250000, "Electronics", "placeholder.png"));
        allProductDTOS.add(new ProductDTO("Zebra Cable", 500, "Accessories", "0.png"));
    }

    private void filterAndRender() {
        String searchText = txtSearch.getText().toLowerCase();
        String selectedCategory = cmbCategory.getValue();

        displayedList = allProductDTOS.stream().filter(p -> p.getName().toLowerCase().contains(searchText)) // Search Name
                .filter(p -> {
                    if (selectedCategory == null || selectedCategory.equals("All Categories")) return true;
                    return p.getCategory().equals(selectedCategory); // Filter Category
                }).sorted(Comparator.comparing(ProductDTO::getName)) // Sort A-Z (Ascending)
                .collect(Collectors.toList());

        currentLimit = BATCH_SIZE;

        if (displayedList.size() <= moreButtonThreshold)
            btnLoadMore.setVisible(false);
        else {
            btnLoadMore.setVisible(true);
        }

        System.out.println("is loadingThead deamon: "+ThreadService.getInventoryLoadingThread().isDaemon());
        System.out.println("is loadingThead alive: "+ThreadService.getInventoryLoadingThread().isAlive());

        renderGrid();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            renderGrid(); // Re-draw the grid
        }
    }

    @FXML
    private void handleNextPage() {
        int maxPage = (int) Math.ceil((double) displayedList.size() / ITEMS_PER_PAGE) - 1;
        if (currentPage < maxPage) {
            currentPage++;
            renderGrid(); // Re-draw the grid
        }
    }

    @FXML
    private void handleLoadMore() {
        // Increase the limit and re-render
        currentLimit = moreButtonThreshold;
        btnLoadMore.setVisible(false);
        renderGrid();
    }

    private void renderGrid() {
        productGrid.getChildren().clear();

        loadProductGrid();
    }

    private void loadProductGrid() {
        // 1. STOP OLD ANIMATIONS (Fixes Memory Leak)
        for (FadeTransition fade : runningAnimations) {
            fade.stop();
        }
        runningAnimations.clear();

        // 2. CLEAR GRID & SHOW NEW SKELETONS
        productGrid.getChildren().clear();

        // Add 10 dummy cards
        for (int i = 0; i < 10; i++) {
            productGrid.getChildren().add(createSkeletonCard());
        }

        ThreadService.setInventoryLoadingThread(new Thread(() -> {
            try {
                Thread.sleep(5); // Reduced for better UX

                javafx.application.Platform.runLater(() -> {
                    // Stop Skeleton Animations before removing them
                    for (FadeTransition fade : runningAnimations) {
                        fade.stop();
                    }
                    runningAnimations.clear();

                    // Clear Skeletons
                    productGrid.getChildren().clear();

                    // Determine max items
                    int maxItems = Math.min(currentLimit, displayedList.size());

                    for (int i = 0; i < maxItems; i++) {
                        ProductDTO p = displayedList.get(i);
                        productGrid.getChildren().add(createProductCard(p));
                    }
                });

            } catch (InterruptedException e) {
                // Thread was interrupted (e.g. user typed new search), just stop.
            } catch (Exception e) {
                e.printStackTrace();
            }
        })
        );

        ThreadService.getInventoryLoadingThread().setDaemon(true);
        ThreadService.getInventoryLoadingThread().start();
    }

    private VBox createProductCard(ProductDTO p) {
// 1. The Main Card Container
        VBox card = new VBox(5); // Spacing of 5 between elements
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setPrefWidth(180);
        card.setPrefHeight(220); // Force a FIXED TOTAL HEIGHT for the card
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-cursor: hand;");

        // --- SECTION 1: THE IMAGE (Fixed 100x100 Box) ---
        // We wrap the ImageView in a StackPane.
        // This forces the "Image Area" to be 100px tall even if the image is missing.
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(100, 100);
        imageContainer.setMinSize(100, 100);
        imageContainer.setMaxSize(100, 100);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        // Image Loading Logic
        String imagePath = "/lk/ijse/etecmanagementsystem/images/" + p.getImagePath();
        try {
            // 1. Check if resource exists BEFORE trying to load it
            if (getClass().getResource(imagePath) != null) {
                String url = getClass().getResource(imagePath).toExternalForm();
                imageView.setImage(new Image(url));
            } else {
                // 2. Resource not found? Load a default/error image
//                System.out.println("Image missing in resources: " + imagePath);
//                imageView.setImage(new Image("/images/placeholder.png")); // Ensure this file exists!
                imageView.setVisible(false); // Ensure this file exists!

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageContainer.getChildren().add(imageView);

        // --- SECTION 2: THE NAME (Fixed 45px Tall) ---
        Label lblName = new Label(p.getName());
        lblName.setWrapText(true);
        lblName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        lblName.setAlignment(Pos.CENTER); // Vertically Center the text in its box

        // CRITICAL: Force this label to always be exactly 45px tall
        lblName.setMinHeight(45);
        lblName.setPrefHeight(45);
        lblName.setMaxHeight(45);

        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");

        // --- SECTION 3: THE PRICE & STOCK ---
        Label lblPrice = new Label("LKR " + String.format("%,.2f", p.getPrice()));
        lblPrice.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblStock = new Label("In Stock: 50");
        lblStock.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Add everything to card
        card.getChildren().addAll(imageContainer, lblName, lblPrice, lblStock);

        return card;
    }

    private VBox createSkeletonCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // 1. Fake Image (Gray Box)
        Rectangle imgPlaceholder = new Rectangle(100, 100, Color.web("#e4e4e4"));
        imgPlaceholder.setArcWidth(10);
        imgPlaceholder.setArcHeight(10);

        // 2. Fake Text Lines (Gray Bars)
        Rectangle textLine1 = new Rectangle(140, 15, Color.web("#e4e4e4"));
        textLine1.setArcWidth(10);
        textLine1.setArcHeight(10);

        Rectangle textLine2 = new Rectangle(80, 15, Color.web("#e4e4e4"));
        textLine2.setArcWidth(10);
        textLine2.setArcHeight(10);

        card.getChildren().addAll(imgPlaceholder, textLine1, textLine2);

        // Add "Pulse" Animation
        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), card);
        fade.setFromValue(1.0);
        fade.setToValue(0.5);
        fade.setAutoReverse(true);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.play();

        // !!! IMPORTANT: Add to list so we can stop it later !!!
        runningAnimations.add(fade);

        return card;
    }

    private void setProductTable(){




    }
    private void loadProductData() {
        // Example data (replace with actual database data)
        ObservableList<ProductDTO> products = FXCollections.observableArrayList(
                new ProductDTO("P001", "Laptop Pro", "Electronics", 1250.00, 12, 15),
                new ProductDTO("P002", "Mechanical Keyboard", "Accessories", 150.99, 6, 22),
                new ProductDTO("P003", "Mousepad XXL", "Accessories", 25.50, 0, 50)
        );

        // Set the data into the TableView
        productTable.setItems(products);
    }
}
