package lk.ijse.etecmanagementsystem;


import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lk.ijse.etecmanagementsystem.service.Login;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lk.ijse.etecmanagementsystem.service.Product; // Import your model

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    // Data Storage
    private final List<Product> allProducts = new ArrayList<>(); // Master list
    private List<Product> displayedList = new ArrayList<>(); // List after filtering

    // Keep track of running animations to stop them later
    private List<FadeTransition> runningAnimations = new ArrayList<>();

    // Pagination Variables
    private int currentLimit = 10; // Start by showing 10 items
    private final int BATCH_SIZE = 10; // Load 10 more on click

    // We need a list to easily loop through them
    private final List<Button> menuButtons = new ArrayList<>();

    @FXML
    public void initialize() {
//         Add all buttons to the list
        menuButtons.add(btnDashboard);
        menuButtons.add(btnInventory);
        menuButtons.add(btnRepairs);
        menuButtons.add(btnSuppliers);
        menuButtons.add(btnCustomers);
        menuButtons.add(btnTransactions);
        menuButtons.add(btnWarranty);
        menuButtons.add(btnSettings);
        menuButtons.add(btnUser);


        String username = Login.getUserName();
        btnUser.setText(username);


        // Apply logic to EVERY button
        for (Button btn : menuButtons) {
            MenuBar.setupButton(btn);
        }

        // Set Default Active Button (e.g., Dashboard)
        MenuBar.setActive(btnInventory);

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

    }

    private void loadDummyData() {
        // Create 30 dummy items to test scrolling and pagination
        for (int i = 1; i <= 3000; i++) {
            String cat = (i % 3 == 0) ? "Electronics" : (i % 2 == 0) ? "Accessories" : "Parts";
            allProducts.add(new Product("Item " + i, 1000 + (i * 50), cat, "0.jpg"));
        }

        allProducts.add(new Product("Apple iPhone", 250000, "Electronics", "placeholder.png"));
        allProducts.add(new Product("Zebra Cable", 500, "Accessories", "0.png"));
    }

    // --- MAIN LOGIC: FILTER, SORT, AND UPDATE GRID ---
    private void filterAndRender() {
        String searchText = txtSearch.getText().toLowerCase();
        String selectedCategory = cmbCategory.getValue();

        // 1. Filter the Master List
        displayedList = allProducts.stream().filter(p -> p.getName().toLowerCase().contains(searchText)) // Search Name
                .filter(p -> {
                    if (selectedCategory == null || selectedCategory.equals("All Categories")) return true;
                    return p.getCategory().equals(selectedCategory); // Filter Category
                }).sorted(Comparator.comparing(Product::getName)) // Sort A-Z (Ascending)
                .collect(Collectors.toList());

        // 2. Reset Pagination
        currentLimit = BATCH_SIZE;

        // 3. Update UI
        renderGrid();
    }

    @FXML
    private void handleLoadMore() {
        // Increase the limit and re-render
        currentLimit += BATCH_SIZE;
        renderGrid();
    }

    private void renderGrid() {
        productGrid.getChildren().clear();


        // Show/Hide "View More" button
        if (currentLimit >= displayedList.size()) {
            btnLoadMore.setVisible(false);
        } else {
            btnLoadMore.setVisible(true);
        }

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

        // 3. USE A DAEMON THREAD (Or JavaFX Task)
        // Using a Daemon thread ensures it dies if the app closes.
        Thread loadingThread = new Thread(() -> {
            try {
                // Simulate delay
                Thread.sleep(500); // Reduced for better UX

                // 4. UPDATE UI SAFELY
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

                    // Loop and Create Real Cards
                    for (int i = 0; i < maxItems; i++) {
                        Product p = displayedList.get(i);
                        productGrid.getChildren().add(createProductCard(p));
                    }
                });

            } catch (InterruptedException e) {
                // Thread was interrupted (e.g. user typed new search), just stop.
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    // --- UI GENERATOR FOR SINGLE CARD ---
    // This looks exactly like the FXML dummy card
    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-cursor: hand;");

//        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

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
                imageView.setImage(null); // Ensure this file exists!

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Labels
        Label lblName = new Label(p.getName());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");

        Label lblPrice = new Label("LKR " + String.format("%,.2f", p.getPrice()));
        lblPrice.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");

        Label lblQuantity = new Label("In Stock: 50");
        lblQuantity.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        card.getChildren().addAll(imageView, lblName, lblPrice, lblQuantity);
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
}
