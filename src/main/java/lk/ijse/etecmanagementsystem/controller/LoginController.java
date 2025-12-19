package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.util.Login;
import lk.ijse.etecmanagementsystem.service.ButtonStyle;

import java.net.URL;


public class LoginController {

    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private HBox userNameBox;
    @FXML
    private HBox passwordBox;


    @FXML
    private AnchorPane rootNode;

    ButtonStyle buttonStyle = new ButtonStyle();


    @FXML
    private void initialize() {


        userNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Text field IS focused -> Add the glow class to parent
                userNameBox.getStyleClass().add("input-box-focused");
            } else {
                // Text field LOST focus -> Remove the glow class from parent
                userNameBox.getStyleClass().remove("input-box-focused");
            }
        });

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Text field IS focused -> Add the glow class to parent
                passwordBox.getStyleClass().add("input-box-focused");
            } else {
                // Text field LOST focus -> Remove the glow class from parent
                passwordBox.getStyleClass().remove("input-box-focused");
            }
        });

//        backgroundImage.setVisible(false);



//        setBackground((rootNode));



//        loginButtonStyle();
    }

    public void setBackground(Region rootPane) {
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


    @FXML
    private void onLoginButtonClick() {
        String username = userNameField.getText();
        String password = passwordField.getText();

        if ("admin".equals(username) && "password".equals(password)) {

            Login.setUserName(username);
            try {
                App.setupPrimaryStageScene("layout");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            System.out.println("Invalid username or password");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username or password. Please try again.");
            alert.showAndWait();

            userNameField.clear();
            passwordField.clear();
        }
    }

    private void loginButtonStyle() {
        final String neonColor = "#21E3FF";

        final String idleStyle = "-fx-background-color: transparent;"
                + " -fx-text-fill: white;"
                + " -fx-padding: 1px;"
                + " -fx-border-color: white; "
                + " -fx-border-width: 1; "
                + " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 0);";

        final String hoverStyle = "-fx-background-color:  #1e293b;"
                + " -fx-text-fill: white;"
                + " -fx-padding: 1px;"
                + " -fx-border-color: " + neonColor + "; "
                + " -fx-border-width: 1;"
                + " -fx-effect: dropshadow(three-pass-box, " + neonColor + ", 5, 0.5, 0, 0);"
                + " -fx-cursor: hand;";

        final String activeStyle = "-fx-background-color: #1e293b; "
                + "-fx-text-fill: white; "
                + "-fx-padding: 1px; "
                + "-fx-border-color: " + neonColor + "; "
                + "-fx-border-width: 0.5; "
                + "-fx-effect: dropshadow(three-pass-box, " + neonColor + ", 2.5, 0.2, 0, 0);"
                + " -fx-cursor: hand;";

        buttonStyle.setCurrentStyle(idleStyle);
        buttonStyle.setHoverStyle(hoverStyle);
        buttonStyle.setActiveStyle(activeStyle);

        buttonStyle.onMouseAction(loginBtn);
    }


}

