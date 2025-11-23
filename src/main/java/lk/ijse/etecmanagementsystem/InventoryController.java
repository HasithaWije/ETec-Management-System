package lk.ijse.etecmanagementsystem;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lk.ijse.etecmanagementsystem.service.Login;

import java.util.ArrayList;
import java.util.List;

public class InventoryController {
    @FXML
    private Button btnDashboard;
    @FXML private Button btnInventory;
    @FXML private Button btnRepairs;
    @FXML private Button btnSuppliers;
    @FXML private Button btnCustomers;
    @FXML private Button btnPayments;
    @FXML private Button btnWarranty;
    @FXML private Button btnSettings;
    @FXML private Button btnUser;

    // We need a list to easily loop through them
    private List<Button> menuButtons = new ArrayList<>();

    @FXML
    public void initialize() {
//         Add all buttons to the list
        menuButtons.add(btnDashboard);
        menuButtons.add(btnInventory);
        menuButtons.add(btnRepairs);
        menuButtons.add(btnSuppliers);
        menuButtons.add(btnCustomers);
        menuButtons.add(btnPayments);
        menuButtons.add(btnWarranty);
        menuButtons.add(btnSettings);
        menuButtons.add(btnUser);


        String username = Login.getUserName();
        btnUser.setText(username);

        MenuBar menuBar = new MenuBar();// Create MenuBar instance

        // Apply logic to EVERY button
        for (Button btn : menuButtons) {
            MenuBar.setupButton(btn);
        }

        // Set Default Active Button (e.g., Dashboard)
        MenuBar.setActive(btnInventory);

    }
}
