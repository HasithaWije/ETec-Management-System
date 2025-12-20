package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import lk.ijse.etecmanagementsystem.service.MenuBar;

import java.net.URL;


public class DashboardController {
    
    @FXML
    private Pane rootPane;

    @FXML
    public void initialize() {
        setBackground();
    }
    public void setBackground() {
        // 1. Load the image from resources (Portable path)
        // This looks inside 'src/main/resources' for the path
        String imagePath = "/lk/ijse/etecmanagementsystem/images/Background03.png";
        URL imageUrl = getClass().getResource(imagePath);

        // Safety check to prevent crashing if path is wrong
        if (imageUrl == null) {
            System.out.println("Error: Image not found at " + imagePath);
            return;
        }
        Image image = new Image(imageUrl.toExternalForm());


        // 2. Define the "Cover" behavior
        // Width=1.0, Height=1.0, AsPercentage=true, Contain=false, Cover=true
        // width=1.0 (100%), height=1.0 (100%), asPercentage=true, contain=false, cover=false
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);

        // 3. Create the BackgroundImage
        BackgroundImage bgImage = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,  // Don't repeat horizontally
                BackgroundRepeat.NO_REPEAT,  // Don't repeat vertically
                BackgroundPosition.CENTER,   // Center the image
                backgroundSize
        );

        // 4. Apply it to your pane (e.g., anchorPane, stackPane)
        rootPane.setBackground(new Background(bgImage));
    }
}