package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.model.LoginModel;
import lk.ijse.etecmanagementsystem.util.ETecAlerts;
import lk.ijse.etecmanagementsystem.util.EmailService;
import lk.ijse.etecmanagementsystem.util.LoginUtil;
import lk.ijse.etecmanagementsystem.util.ButtonStyle;

import javafx.scene.input.KeyEvent;
import java.sql.SQLException;
import java.util.Optional;


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
    LoginModel loginModel = new LoginModel();


    @FXML
    private void initialize() {


        userNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Text field IS focused -> Add the glow class to parent
                userNameBox.getStyleClass().remove("input-box-error");
                userNameBox.getStyleClass().add("input-box-focused");
            } else {
                // Text field LOST focus -> Remove the glow class from parent
                userNameBox.getStyleClass().remove("input-box-focused");
            }
        });

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Text field IS focused -> Add the glow class to parent
                passwordBox.getStyleClass().remove("input-box-error");
                passwordBox.getStyleClass().add("input-box-focused");
            } else {
                // Text field LOST focus -> Remove the glow class from parent
                passwordBox.getStyleClass().remove("input-box-focused");
            }
        });

    }

    @FXML
    private void handleMoveToPasswordField(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleEnterKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onLoginButtonClick();
        }
    }

    @FXML
    private void onLoginButtonClick() {
        String username = userNameField.getText() == null ? "" : userNameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        int userId = -1;
        String role;

        if (checkIsEmpty(username, userNameBox)) return;
        if (checkIsEmpty(password, passwordBox)) return;


        try {


            if (loginModel.validateCredentials(username, password)) {

                LoginUtil.setUserName(username);
                userId = loginModel.getUserId(username);
                role = loginModel.getUserRole(username);


                if (userId == -1) {
                    ETecAlerts.showAlert(Alert.AlertType.ERROR, "Login Error", "Unable to retrieve user ID. Please contact support.");
                    return;
                }
                if (role == null || role.isEmpty()) {
                    ETecAlerts.showAlert(Alert.AlertType.ERROR, "Login Error", "Unable to retrieve user role. Please contact support.");
                    return;
                }

                LoginUtil.setUserId(userId);
                LoginUtil.setUserRole(role);

                System.out.println("Login successful for user: " + username + " with ID: " + userId);

                try {
                    App.setupPrimaryStageScene("layout");
                } catch (Exception e) {
                    System.out.println("Error loading main layout: " + e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Loading Error");
                    alert.setHeaderText(null);
                    alert.setContentText("An error occurred while loading the main layout. Please try again later.");
                    alert.showAndWait();
                }
            } else {

                ETecAlerts.showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
                userNameField.clear();
                passwordField.clear();
                userNameField.requestFocus();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while connecting to the database. Please try again later.");
            alert.showAndWait();
        }


    }

    private boolean checkIsEmpty(String username, HBox userNameBox) {
        if (username.isEmpty()) {
            userNameBox.getStyleClass().add("input-box-error");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input Required");
            alert.setHeaderText(null);
            alert.setContentText("Please enter both username and password.");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    @FXML
    private void handleForgotPassword() {

        String username = userNameField.getText() == null ? "" : userNameField.getText().trim();
        if (username.isEmpty()) {
            ETecAlerts.showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter your username before proceeding.");
            return;
        }

        if (!checkUserNameExists(username)) {
            ETecAlerts.showAlert(Alert.AlertType.ERROR, "Email Error", "Invalid username. Please enter a registered username.");
            return;
        }

        // 1. Show a dialog to get the user's email
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Get Password to Email");
        dialog.setHeaderText("Forgot your password?");
        dialog.setContentText("Please enter your registered email:");


        Optional<String> result = dialog.showAndWait();

        if (!result.isPresent()) {
            System.out.println("User closed the dialog.");
            return;
        }

        String email = result.get().trim();

        if (email.isEmpty()) {
            ETecAlerts.showAlert(Alert.AlertType.WARNING, "Input Required", "Email input cannot be empty.");
            return;
        }

        System.out.println("User entered: " + email);


        if (isValidEmailInDatabase(username, email)) {

            String userPassword = getUserPassword(username);
            if (userPassword == null) {
                return;
            }
            // Send the password to the user's email
            boolean isSent = EmailService.sendUserPasswordToEmail(email, userPassword);
            if (!isSent) {
                return;
            }

            ETecAlerts.showAlert(Alert.AlertType.INFORMATION, "Email Successfully Sent", "Email Sent");

        } else {
            ETecAlerts.showAlert(Alert.AlertType.ERROR, "Email Error", "The provided email does not match our records for the given username.");
        }

    }

    private boolean checkUserNameExists(String username) {
        try {
            return loginModel.validateUserName(username);
        } catch (SQLException e) {
            ETecAlerts.showAlert(Alert.AlertType.INFORMATION, "Database Error", "An error occurred while accessing the database. Please try again later.");
            return false;
        }
    }

    private String getUserPassword(String username) {
        try {
            return loginModel.getUserPassword(username);
        } catch (SQLException e) {
            ETecAlerts.showAlert(Alert.AlertType.INFORMATION, "Database Error", "An error occurred while accessing the database. Please try again later.");
            return null;
        }
    }

    private boolean isValidEmailInDatabase(String userName, String email) {

        try {
            return loginModel.validateUserEmail(userName, email);
        } catch (SQLException e) {
            ETecAlerts.showAlert(Alert.AlertType.INFORMATION, "Database Error", "An error occurred while accessing the database. Please try again later.");
            return false;
        }
    }
}

