package lk.ijse.etecmanagementsystem;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;



public class LoginController {


    private Scene scene = App.getScene();

    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;

@FXML
private void initialize() {
    // In your Controller's initialize method
    URL cssUrl = getClass().getResource("button_glow.css");
    if (cssUrl != null) {
        scene.getStylesheets().add(cssUrl.toExternalForm());
    }

}


    @FXML
    private void onLoginButtonClick() {
        String username = userNameField.getText();
        String password = passwordField.getText();

        // Simple authentication logic (replace with real authentication)
        if ("admin".equals(username) && "password".equals(password)) {
            try {
                App.setRoot("dashboard"); // Navigate to dashboard on successful login
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Handle invalid login
            System.out.println("Invalid username or password");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username or password. Please try again.");
            alert.showAndWait();
        }
    }



}

