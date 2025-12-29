package lk.ijse.etecmanagementsystem.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.util.ImageUtils;

import java.io.File;
import java.io.IOException;

public class ImagePopupController {

    @FXML
    private Pane paneImageBack;

    @FXML
    private ImageView imageViewBack;

    @FXML
    private ImageView imgPreview;

    private File selectedFile;
    private String productName;
    private ProductController productController;


    @FXML
    public void initialize() {
        paneImageBack.setVisible(false);
    }

    public void setProductDetails(String productName, ProductController productController) {
        this.productController = productController;
        this.productName = productName;
    }


    @FXML
    protected void onProcessImageClick() {
        // 1. Get the Image (User selects a file)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Get the current stage (window) to show the dialog
        Stage stage = new Stage(); // Or retrieve your actual main stage
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Display the selected image in the ImageView
            imageViewBack.setImage(new javafx.scene.image.Image(selectedFile.toURI().toString()));
            BoxBlur boxBlur = new BoxBlur();
            boxBlur.setWidth(500);
            boxBlur.setHeight(340);
            boxBlur.setIterations(3);
            imageViewBack.setEffect(boxBlur);
            paneImageBack.setVisible(true);
            imgPreview.setImage(new javafx.scene.image.Image(selectedFile.toURI().toString()));
        }


    }

    @FXML
    protected void handleConfirmButtonClick() {
        if (selectedFile != null) {
            try {
                // Define your new settings

                //String destinationPath = "C:/MyAppData/Images/"; // New Location
                // This gets "C:\Users\HASITHA" automatically
                String userHome = System.getProperty("user.home");

                // Combine it with the rest of your path
                // Result: C:\Users\HASITHA\Documents\ETec Management System\images\
                String destinationPath = userHome + File.separator + "Documents" + File.separator + "ETec Management System" + File.separator + "images" + File.separator;
                String newName = productName + "_" + System.currentTimeMillis(); // New Name (Renaming)
                int newWidth = 200;  // Resize Width
                int newHeight = 200; // Resize Height

                // Call the utility method
                ImageUtils.resizeAndSave(selectedFile, destinationPath, newName, newWidth, newHeight);
                // Notify the ProductController about the new image
                if (productController != null) {
//                    productController.updateProductImage(destinationPath + newName + ".png");
                    System.out.println("Image processed and saved successfully.");
                    System.out.println("New Image Path: " + destinationPath + newName + ".png");
                    productController.setSelectedImagePath(newName + ".png");

                    new Alert(Alert.AlertType.INFORMATION, "Image saved successfully!").show();
                    closeWindow();
                }

            } catch (IOException e) {
                e.printStackTrace();
                // Handle error (e.g., show an Alert to the user)
            }
        }
    }

    @FXML
    private void handleBtnCancel() {
        closeWindow();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) imgPreview.getScene().getWindow();
        stage.close();
    }

}