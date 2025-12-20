package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import lk.ijse.etecmanagementsystem.App;
import lk.ijse.etecmanagementsystem.service.MenuBar;
import lk.ijse.etecmanagementsystem.service.ThreadService;
import lk.ijse.etecmanagementsystem.util.LoginUtil;

import java.io.IOException;

public class LayoutController {


    @FXML
    private Button btnDashboard;
    @FXML private Button btnSales;
    @FXML private Button btnInventory;
    @FXML private Button btnRepairs;
    @FXML private Button btnSuppliers;
    @FXML private Button btnCustomers;
    @FXML private Button btnTransactions;
    @FXML private Button btnWarranty;
    @FXML private Button btnSettings;
    @FXML private Button btnUser;

    @FXML private StackPane contentArea;

    MenuBar menuBar = new MenuBar();

    @FXML
    public void initialize() {

        btnDashboardClicked();

        setupMenuBar();



        String username = LoginUtil.getUserName();
        btnUser.setText(username);

        System.out.println("is loadingThead deamon: "+ ThreadService.getInventoryLoadingThread().isDaemon());
        System.out.println("is loadingThead alive: "+ThreadService.getInventoryLoadingThread().isAlive());
    }

    @FXML
    private void btnDashboardClicked()  {
        menuBar.setActive(btnDashboard);
        try {
            contentArea.getChildren().setAll(App.loadFXML("dashboard"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void btnSalesClicked(){
        menuBar.setActive(btnSales);
        try {
            contentArea.getChildren().setAll(App.loadFXML("sales"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    private void btnInventoryClicked()  {
        menuBar.setActive(btnInventory);
        try {
            contentArea.getChildren().setAll(App.loadFXML("inventory"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void btnRepairsClicked()  {
        menuBar.setActive(btnRepairs);
        try {
            contentArea.getChildren().setAll(App.loadFXML("repairs"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnSuppliersClicked()  {
        menuBar.setActive(btnSuppliers);
        try {
            contentArea.getChildren().setAll(App.loadFXML("suppliers"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnCustomersClicked()  {
        menuBar.setActive(btnCustomers);
        try {
            contentArea.getChildren().setAll(App.loadFXML("customers"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnTransactionsClicked()  {
        menuBar.setActive(btnTransactions);
        try {
            contentArea.getChildren().setAll(App.loadFXML("transactions"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnWarrantyClicked()  {
        menuBar.setActive(btnWarranty);
        try {
            contentArea.getChildren().setAll(App.loadFXML("warranty"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void btnSettingsClicked()  {
        menuBar.setActive(btnSettings);
        try {
            contentArea.getChildren().setAll(App.loadFXML("settings"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void btnUserClicked()  {
        menuBar.setActive(btnUser);
        try {
            contentArea.getChildren().setAll(App.loadFXML("user"));
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
        menuBar.setupButton(btnWarranty);
        menuBar.setupButton(btnSettings);
        menuBar.setupButton(btnUser);
    }



}