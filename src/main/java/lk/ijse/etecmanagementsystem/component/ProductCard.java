package lk.ijse.etecmanagementsystem.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;

//public class ProductCard extends VBox {
//
//    public ProductCard(ProductDTO p) {
//        // 1. Setup Main Card Layout
//        this.setSpacing(5);
//        this.setAlignment(Pos.CENTER);
//        this.setPadding(new Insets(10));
//        this.setPrefSize(180, 220);
//        this.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
//                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
//                "-fx-cursor: hand;");
//
//        // 2. Image Area
//        StackPane imageContainer = new StackPane();
//        imageContainer.setPrefSize(100, 100);
//
//        ImageView imageView = new ImageView();
//        imageView.setFitWidth(100);
//        imageView.setFitHeight(100);
//        imageView.setPreserveRatio(true);
//
//        try {
//            String imagePath = "/lk/ijse/etecmanagementsystem/images/" + p.getImagePath();
//            if (getClass().getResource(imagePath) != null) {
//                imageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
//            }
//        } catch (Exception e) {
//            // e.printStackTrace(); // Optional: Log missing images
//        }
//        imageContainer.getChildren().add(imageView);
//
//        // 3. Name Label
//        Label lblName = new Label(p.getName());
//        lblName.setWrapText(true);
//        lblName.setTextAlignment(TextAlignment.CENTER);
//        lblName.setAlignment(Pos.CENTER);
//        lblName.setPrefHeight(45); // Fixed height to keep alignment
//        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
//
//        // 4. Price Label
//        Label lblPrice = new Label("LKR " + String.format("%,.2f", p.getPrice()));
//        lblPrice.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 13px;");
//
//        // 5. Stock Label
//        Label lblStock = new Label("In Stock: " + p.getQty()); // Assuming ProductDTO has getQty()
//        lblStock.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 12px;");
//
//        // 6. Add all to VBox
//        this.getChildren().addAll(imageContainer, lblName, lblPrice, lblStock);
//    }
//}


//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.control.Label;
//import javafx.scene.effect.DropShadow;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.TextAlignment;
//import lk.ijse.etecmanagementsystem.dto.ProductDTO;
//
//public class ProductCard extends StackPane {
//
//    public ProductCard(ProductDTO p) {
//        // --- 1. Main Card Container (The White Box) ---
//        VBox cardContent = new VBox();
//        cardContent.setSpacing(5);
//        cardContent.setAlignment(Pos.TOP_CENTER);
//        cardContent.setPadding(new Insets(15, 10, 15, 10));
//
//        // Styling the white card background
//        cardContent.setStyle("-fx-background-color: white; " +
//                "-fx-background-radius: 8; " +
//                "-fx-border-color: #e2e8f0; " +
//                "-fx-border-radius: 8; " +
//                "-fx-border-width: 1;");
//
//        // Add a subtle shadow
//        DropShadow shadow = new DropShadow();
//        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
//        shadow.setRadius(5);
//        shadow.setOffsetY(3);
//        this.setEffect(shadow);
//
//        // Set Card Size
//        this.setPrefSize(220, 320);
//        this.setMaxSize(220, 320);
//
//        // --- 2. Product Image ---
//        ImageView productImageView = new ImageView();
//        productImageView.setFitWidth(140);
//        productImageView.setFitHeight(120);
//        productImageView.setPreserveRatio(true);
//
//        try {
//            String imagePath = "/lk/ijse/etecmanagementsystem/images/" + p.getImagePath();
//            if (getClass().getResource(imagePath) != null) {
//                productImageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
//            }
//        } catch (Exception e) {
//            // Placeholder if image fails
//        }
//
//        // --- 3. Your Custom Logo (Below Product Image) ---
//        HBox logoContainer = new HBox();
//        logoContainer.setAlignment(Pos.CENTER_LEFT);
//        logoContainer.setPadding(new Insets(0, 0, 0, 10)); // Indent slightly
//
//        ImageView logoView = new ImageView();
//        logoView.setFitHeight(20); // Adjust size for your logo
//        logoView.setPreserveRatio(true);
//
//        try {
//            // TODO: REPLACE THIS WITH YOUR LOGO PATH
//            String myLogoPath = "/lk/ijse/etecmanagementsystem/assets/my_logo.png";
//            if (getClass().getResource(myLogoPath) != null) {
//                logoView.setImage(new Image(getClass().getResource(myLogoPath).toExternalForm()));
//            }
//        } catch (Exception e) { }
//        logoContainer.getChildren().add(logoView);
//
//        // --- 4. Product Name ---
//        Label lblName = new Label(p.getName().toUpperCase());
//        lblName.setWrapText(true);
//        lblName.setTextAlignment(TextAlignment.CENTER);
//        lblName.setAlignment(Pos.CENTER);
//        lblName.setPrefHeight(40);
//        lblName.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 11px; -fx-text-fill: #555555;");
//
//        // --- 5. Item Code ---
//        // Assuming your DTO has an ID or Code.
//        Label lblCode = new Label("ITEM CODE : " + p.getId());
//        lblCode.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: black;");
//
//        // --- 6. Standard Price (Red) ---
//        Label lblPrice = new Label("Rs. " + String.format("%,.2f", p.getPrice()));
//        lblPrice.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 14px;");
//
//        // --- 7. Cash Price (Green) ---
//        // Assuming cash price is same as price, or calculate discount here
//        Label lblCashPrice = new Label("Cash Price : Rs. " + String.format("%,.2f", p.getPrice()));
//        lblCashPrice.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 13px;");
//
//        // Add elements to vertical box
//        cardContent.getChildren().addAll(productImageView, logoContainer, lblName, lblCode, lblPrice, lblCashPrice);
//
//        // --- 8. BADGES (Overlay) ---
//
//        // "IN STOCK" Ribbon (Top Left)
//        Label lblInStock = new Label("IN STOCK");
//        lblInStock.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 20 3 20;");
//        lblInStock.setRotate(-45);
//        lblInStock.setTranslateX(-25); // Move left
//        lblInStock.setTranslateY(15);  // Move down
//
//        // Container to clip the ribbon if needed, or just place it in StackPane
//        StackPane ribbonContainer = new StackPane(lblInStock);
//        ribbonContainer.setAlignment(Pos.TOP_LEFT);
//        ribbonContainer.setPickOnBounds(false); // Let clicks pass through empty areas
//
//        // "NEW ARRIVAL" Badge (Top Right)
//        // You can use an image or a styled label. Here is a yellow badge style.
//        Label lblNew = new Label("NEW\nARRIVAL");
//        lblNew.setTextAlignment(TextAlignment.CENTER);
//        lblNew.setStyle("-fx-background-color: #facc15; -fx-text-fill: #a16207; -fx-font-weight: bold; -fx-font-size: 8px; -fx-background-radius: 20; -fx-padding: 5;");
//        lblNew.setRotate(15);
//
////        StackPane badgeContainer = new StackPane(lblNew);
////        badgeContainer.setAlignment(Pos.TOP_RIGHT);
////        badgeContainer.setPadding(new Insets(10));
////        badgeContainer.setPickOnBounds(false);
////
////        // --- Final Assembly ---
//        this.getChildren().addAll(cardContent, ribbonContainer);
//
//        // Interactive Hover Effect
//        this.setOnMouseEntered(e -> this.setScaleX(1.02)); // Slight zoom on hover
//        this.setOnMouseEntered(e -> this.setScaleY(1.02));
//        this.setOnMouseExited(e -> this.setScaleX(1.0));
//        this.setOnMouseExited(e -> this.setScaleY(1.0));
//        this.setStyle("-fx-cursor: hand;");
//    }
//}



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;

public class ProductCard extends StackPane {

    public ProductCard(ProductDTO p) {
        // --- 1. Root StackPane Settings (Matches "mainContainer") ---
        this.setPrefSize(238, 349);
        this.setMaxSize(238, 349);
        this.setStyle("-fx-cursor: hand;");

        // --- 2. The Main Card Box (VBox) ---
        VBox cardContent = new VBox();
        cardContent.setAlignment(Pos.TOP_CENTER);
        cardContent.setSpacing(5);
        cardContent.setStyle("-fx-padding: 8 5 10 5 "); // Top, Right, Bottom, Left
        cardContent.setPrefSize(238, 349);
        cardContent.setMaxSize(238, 349);

        // Styling matches your FXML VBox
        cardContent.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; ");



        // --- 3. Internal Elements ---

        // A. Title Label (Matches "lblName1") - e.g. "HP OMNIBOOK 5 FLIP"
        Label lblTitle = new Label(p.getName());
        lblTitle.setAlignment(Pos.CENTER);
        lblTitle.setPrefWidth(226);
        lblTitle.setPrefHeight(41);
        lblTitle.setTextAlignment(TextAlignment.CENTER);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: black; -fx-font-size: 14px;");
        lblTitle.setWrapText(true);

        // B. Product Image (Matches "imgProduct")
        ImageView imgProduct = new ImageView();
        imgProduct.setFitWidth(200);
        imgProduct.setFitHeight(200);
        imgProduct.setPreserveRatio(false);

        try {
            String imagePath = "/lk/ijse/etecmanagementsystem/images/" + p.getImagePath();
            if (getClass().getResource(imagePath) != null) {
                imgProduct.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
            }
        } catch (Exception e) {
            // Optional: Set default image here
        }

        // C. Description Label (Matches "lblName") - The long text
        // Assuming your DTO has a description, or we construct it
        String descriptionText = p.getName() + " " + p.getDescription();
        Label lblName = new Label(descriptionText);
        lblName.setAlignment(Pos.CENTER);
        lblName.setPrefWidth(217);
        lblName.setPrefHeight(51);
        lblName.setTextAlignment(TextAlignment.CENTER);
        lblName.setStyle("-fx-font-size: 10px; -fx-text-fill: #555555;");
        lblName.setWrapText(true);


        // D. Item Code Label (Matches "lblCode")
        Label lblCode = new Label("ITEM CODE : " + p.getId());
        lblCode.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: black;");

        // E. Price Label (Matches "lblPrice")
        Label lblPrice = new Label("Rs. " + String.format("%,.2f", p.getPrice()));
        lblPrice.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Add all elements to the VBox
        cardContent.getChildren().addAll(lblTitle, imgProduct, lblName, lblCode, lblPrice);

        // --- 4. The "In Stock" Overlay (Matches the second StackPane/Label) ---

        // This container aligns the label to the Top-Left of the card initially
        StackPane overlayContainer = new StackPane();
        overlayContainer.setAlignment(Pos.TOP_LEFT);
        overlayContainer.setPickOnBounds(false); // Allows clicking through empty space

        // The Stock Label
        Label lblStock = new Label("IN STOCK : " + p.getQty());
        lblStock.setPrefWidth(127);
        lblStock.setPrefHeight(24);
        lblStock.setAlignment(Pos.CENTER);
        lblStock.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 3 20 3 20;");

        lblStock.setTranslateX(110);
        lblStock.setTranslateY(170);

        overlayContainer.getChildren().add(lblStock);

        // --- 5. Add VBox and Overlay to Root ---
        this.getChildren().addAll(cardContent, overlayContainer);
    }
}