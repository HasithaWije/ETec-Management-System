package lk.ijse.etecmanagementsystem.util;

import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.net.URL;

public class BackgroundImageUtil {

    public void setBackground(Region rootPane) {
        // 1. Load the image from resources (Portable path)
        String imagePath = "/lk/ijse/etecmanagementsystem/images/Background03.png";
        URL imageUrl = getClass().getResource(imagePath);

        if (imageUrl == null) {
            System.out.println("Error: Image not found at " + imagePath);
            return;
        }

        Image image = new Image(imageUrl.toExternalForm());

        // 2. Define the "Cover" behavior
        // Width=1.0, Height=1.0, AsPercentage=true, Contain=false, Cover=true
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, true);

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
