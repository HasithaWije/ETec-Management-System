package lk.ijse.etecmanagementsystem.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.dto.ProductDTO;
import lk.ijse.etecmanagementsystem.util.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class ProductCard extends StackPane {

    public ProductCard(ProductDTO p) {
        this.setPrefSize(215, 349);
        this.setMaxSize(215, 349);
        this.setStyle("-fx-cursor: hand;");

        VBox cardContent = new VBox();
        cardContent.setAlignment(Pos.TOP_CENTER);
        cardContent.setSpacing(2);
        cardContent.setStyle("-fx-padding: 5 5 5 5 ; -fx-background-color: white; -fx-border-color: #e2e8f0;"); // Top, Right, Bottom, Left
        cardContent.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);


        Label lblTitle = new Label(p.getName());
        lblTitle.setAlignment(Pos.CENTER);
        lblTitle.setPrefWidth(210);
        lblTitle.setPrefHeight(41);
        lblTitle.setTextAlignment(TextAlignment.CENTER);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: black; -fx-font-size: 14px;");
        lblTitle.setWrapText(true);

        ImageView imgProduct = new ImageView();
        imgProduct.setFitWidth(200);
        imgProduct.setFitHeight(200);
        imgProduct.setPreserveRatio(false);

        try {
            // 1. Construct the full path to the file on disk
            String fullPath = ImageUtils.getImagesDirectoryPath() + p.getImagePath();
            File file = new File(fullPath);

            // 2. Check if the file actually exists on the computer
            if (file.exists()) {
                // 3. Convert the file path to a URL format JavaFX understands (file:///C:/...)
                String imageUri = file.toURI().toString();

                // 4. Set the image
                imgProduct.setImage(new Image(imageUri));

                System.out.println("Loaded image from: " + fullPath);
            } else {
                // If the file is missing from Documents, throw exception to trigger the catch block
                // or handle the placeholder logic here directly.
                System.out.println("File not found: " + fullPath);
                throw new IOException("File not found");
            }

        } catch (Exception e) {
            // Keep your existing catch block for the placeholder
            String placeholderPath = "/lk/ijse/etecmanagementsystem/images/placeholder.png";
            if (getClass().getResource(placeholderPath) != null) {
                imgProduct.setImage(new Image(getClass().getResource(placeholderPath).toExternalForm()));
            }
        }


        String descriptionText = p.getName() + " " + p.getDescription();
        Label lblName = new Label(descriptionText);
        lblName.setAlignment(Pos.CENTER);
        lblName.setPrefWidth(210);
        lblName.setPrefHeight(51);
        lblName.setTextAlignment(TextAlignment.CENTER);
        lblName.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");
        lblName.setWrapText(true);



        Label lblCode = new Label("STOCK ID : " + p.getId());
        lblCode.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: black;");


        Label lblPrice = new Label("Rs. " + String.format("%,.2f", p.getSellPrice()));
        lblPrice.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 14px;");


        cardContent.getChildren().addAll(lblTitle, imgProduct, lblName, lblCode, lblPrice);

        StackPane overlayContainer = new StackPane();
        overlayContainer.setAlignment(Pos.TOP_LEFT);
        overlayContainer.setPickOnBounds(false);


        Label lblStock = getLblStock(p);

        overlayContainer.getChildren().add(lblStock);

        this.getChildren().addAll(cardContent, overlayContainer);
    }

    private static Label getLblStock(ProductDTO p) {
        Label lblStock = new Label("IN STOCK : " + p.getQty());
        lblStock.setPrefWidth(127);
        lblStock.setPrefHeight(24);
        lblStock.setAlignment(Pos.CENTER);
        lblStock.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 3 20 3 20;");

        if (p.getQty() < 1) {
            lblStock.setText("OUT OF STOCK");
            lblStock.setStyle("-fx-background-color: #f87171; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 3 20 3 20;");
        }
        lblStock.setTranslateX(90);
        lblStock.setTranslateY(220);
        return lblStock;
    }
}