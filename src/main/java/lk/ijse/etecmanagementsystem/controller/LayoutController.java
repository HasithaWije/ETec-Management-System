package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.util.MenuBar;
import lk.ijse.etecmanagementsystem.util.ThreadService;
import lk.ijse.etecmanagementsystem.util.LoginUtil;

import java.io.IOException;
import java.util.Objects;

public class LayoutController {


    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnSales;
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
    private Button btnReports;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnUser;
    @FXML
    private Button btnAdmin;

    @FXML
    private StackPane contentArea;

    MenuBar menuBar = new MenuBar();

    @FXML
    public void initialize() {

        btnDashboardClicked();

        setupMenuBar();


        String username = LoginUtil.getUserName();
        String userRole = LoginUtil.getUserRole();
        btnAdmin.setText(username);

        if (userRole.equals("ADMIN")) {

            btnUser.setVisible(true);
            btnAdmin.setVisible(true);

        } else if(userRole.equals("MANAGER")) {

            btnAdmin.setVisible(true);
            btnUser.setVisible(false);

        }else {
            btnUser.setVisible(false);
            btnAdmin.setVisible(false);
        }

        System.out.println("is loadingThead deamon: " + ThreadService.getInventoryLoadingThread().isDaemon());
        System.out.println("is loadingThead alive: " + ThreadService.getInventoryLoadingThread().isAlive());
    }

    @FXML
    private void btnDashboardClicked() {
        menuBar.setActive(btnDashboard);
        try {
            contentArea.getChildren().clear();
            contentArea.getChildren().setAll(App.loadFXML("dashboard"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void btnSalesClicked() {
        menuBar.setActive(btnSales);
        try {
            contentArea.getChildren().clear();
            contentArea.getChildren().setAll(App.loadFXML("sales"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    private void btnInventoryClicked() {
        menuBar.setActive(btnInventory);
        try {
            contentArea.getChildren().setAll(App.loadFXML("inventory"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void btnRepairsClicked() {
        menuBar.setActive(btnRepairs);
        try {
            contentArea.getChildren().setAll(App.loadFXML("repairDashboard"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnSuppliersClicked() {
        menuBar.setActive(btnSuppliers);
        try {
            contentArea.getChildren().setAll(App.loadFXML("suppliers"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnCustomersClicked() {
        menuBar.setActive(btnCustomers);
        try {
            contentArea.getChildren().setAll(App.loadFXML("customers"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnTransactionsClicked() {
        menuBar.setActive(btnTransactions);
        try {
            contentArea.getChildren().setAll(App.loadFXML("transactions"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnReportsClicked() {
        menuBar.setActive(btnReports);
        try {
            contentArea.getChildren().setAll(App.loadFXML("reports"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnAdminClicked() {
        menuBar.setActive(btnAdmin);
        try {
            contentArea.getChildren().setAll(App.loadFXML("admin"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnUserClicked() {
        menuBar.setActive(btnUser);
        try {
            contentArea.getChildren().setAll(App.loadFXML("user"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnLogoutClicked() {
        menuBar.setActive(btnLogout);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setGraphic(new ImageView(new Image(Objects.requireNonNull(App.class.getResourceAsStream("images/logout_icon.png")))));
        alert.showAndWait();
        if (alert.getResult() != ButtonType.YES) {
            return;
        }

        try {
            App.setupLoginStageScene("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setupMenuBar() {

        menuBar.setupButton(btnDashboard);
        menuBar.setupButton(btnSales);
        menuBar.setupButton(btnInventory);
        menuBar.setupButton(btnRepairs);
        menuBar.setupButton(btnSuppliers);
        menuBar.setupButton(btnCustomers);
        menuBar.setupButton(btnTransactions);
        menuBar.setupButton(btnReports);
        menuBar.setupButton(btnLogout);
        menuBar.setupButton(btnUser);
        menuBar.setupButton(btnAdmin);
    }
}