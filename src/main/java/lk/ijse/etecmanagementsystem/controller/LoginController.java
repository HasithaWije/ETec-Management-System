package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lk.ijse.etecmanagementsystem.util.Login;
import lk.ijse.etecmanagementsystem.service.StageManager;
import lk.ijse.etecmanagementsystem.service.ButtonStyle;


public class LoginController {

    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;

    ButtonStyle buttonStyle = new ButtonStyle();


    @FXML
    private void initialize() {
        loginButtonStyle();
    }


    @FXML
    private void onLoginButtonClick() {
        String username = userNameField.getText();
        String password = passwordField.getText();

        if ("admin".equals(username) && "password".equals(password)) {

            Login.setUserName(username);
            try {
                StageManager.setupPrimaryStageScene("dashboard");
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

