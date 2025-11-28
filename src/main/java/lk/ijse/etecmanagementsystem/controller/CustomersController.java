package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lk.ijse.etecmanagementsystem.service.MenuBar;
import lk.ijse.etecmanagementsystem.util.Login;

import java.util.ArrayList;
import java.util.List;

public class CustomersController {

    // Inject all buttons from FXML
    @FXML private Button btnDashboard;
    @FXML private Button btnInventory;
    @FXML private Button btnRepairs;
    @FXML private Button btnSuppliers;
    @FXML private Button btnCustomers;
    @FXML private Button btnTransactions;
    @FXML private Button btnWarranty;
    @FXML private Button btnSettings;
    @FXML private Button btnUser;


    // We need a list to easily loop through them
    private List<Button> menuButtons = new ArrayList<>();

    private MenuBar menuBar = new MenuBar();

    @FXML
    public void initialize() {

        menuBar.setActive(btnCustomers);

        menuBar.setupButton(btnDashboard);
        menuBar.setupButton(btnInventory);
        menuBar.setupButton(btnRepairs);
        menuBar.setupButton(btnSuppliers);
        menuBar.setupButton(btnCustomers);
        menuBar.setupButton(btnTransactions);
        menuBar.setupButton(btnWarranty);
        menuBar.setupButton(btnSettings);
        menuBar.setupButton(btnUser);

        String username = Login.getUserName();
        btnUser.setText(username);

    }
}