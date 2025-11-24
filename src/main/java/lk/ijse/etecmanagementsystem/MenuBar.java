package lk.ijse.etecmanagementsystem;

import javafx.scene.control.Button;

import java.io.IOException;

public class MenuBar {


    // Track which button is currently active
    private static Button currentActiveButton = null;

    // Define your colors here so you don't type hex codes everywhere
    private static final String IDLE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #cfcfcf; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    private static final String HOVER_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    private static final String ACTIVE_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent transparent transparent #3498db; -fx-border-width: 0 0 0 4;";

    public static void setupButton(Button btn) {
        // 1. Mouse Enter (Hover Effect)
        btn.setOnMouseEntered(e -> {
            // Only change color if this button is NOT the active one
            if (btn != currentActiveButton) {
                btn.setStyle(HOVER_STYLE);
            }
        });

        // 2. Mouse Exit (Stop Hover Effect)
        btn.setOnMouseExited(e -> {
            // If this is NOT the active button, go back to transparent
            if (btn != currentActiveButton) {
                btn.setStyle(IDLE_STYLE);
            }
        });

        // 3. Click Action
        btn.setOnAction(e -> {
           Button pBtn = currentActiveButton;
            handleNavClicks(btn);

            // 4. (Optional) Load different content based on button
            String key = btn.getId() != null ? btn.getId() : btn.getText();
            if(btn != pBtn) {
                System.out.println(key);
                String btnText = btn.getText().toLowerCase();
                if(key.equals("btnUser") ) {
                    btnText = "user";
                }
                try {
                    App.setRoot(btnText);
                    System.gc();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
//            switch (key) {
//                case "btnDashboard":
//                    System.out.println("Load Dashboard View");
//
//                    break;
//                case "btnInventory":
//                    System.out.println("Load Inventory View");
//                    try {
//                        App.setRoot("inventory");
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                    break;
//                case "btnRepairs":
//                    System.out.println("Load Repairs View");
//                    break;
//                case "btnSuppliers":
//                    System.out.println("Load Suppliers View");
//                    break;
//                case "btnCustomers":
//                    System.out.println("Load Customers View");
//                    break;
//                case "btnPayments":
//                    System.out.println("Load Payments View");
//                    break;
//                case "btnWarranty":
//                    System.out.println("Load Warranty View");
//                    break;
//                case "btnSettings":
//                    System.out.println("Load Settings View");
//                    break;
//                case "btnUser":
//                    System.out.println("Load User View");
//                    break;
//                default:
//                    System.out.println("Unknown navigation target: " + key);
//            }

        });
    }

    public static void handleNavClicks(Button clickedButton) {
        // 1. Reset the OLD active button to IDLE style
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(IDLE_STYLE);
        }

        // 2. Set the NEW button to ACTIVE style
        clickedButton.setStyle(ACTIVE_STYLE);

        // 3. Update the reference
        currentActiveButton = clickedButton;
    }

    // Just a helper if you linked 'onAction' in FXML,
    // though the code above handles it via setOnAction.
    public static void setActive(Button btn) {
        handleNavClicks(btn);
    }
}
