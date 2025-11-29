package lk.ijse.etecmanagementsystem.service;

import javafx.scene.control.Button;
import lk.ijse.etecmanagementsystem.App;

public class MenuBar {

    private static Button currentActiveButton = null;

    // Define your colors here so you don't type hex codes everywhere
    private  final String IDLE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #cfcfcf; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    private  final String HOVER_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    private  final String ACTIVE_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent transparent transparent #3498db; -fx-border-width: 0 0 0 4;";



    public  void setupButton(Button btn) {

        btn.setOnMouseEntered(event -> {
            if (btn != currentActiveButton) {
                btn.setStyle(HOVER_STYLE);
            }
        });

        btn.setOnMouseExited(event -> {
            if (btn != currentActiveButton) {
                btn.setStyle(IDLE_STYLE);
            }
        });

        btn.setOnAction(e -> {
            Button pBtn = currentActiveButton;


            String key = btn.getId() != null ? btn.getId() : btn.getText();

            if (btn != pBtn) {
                String btnText = btn.getText().toLowerCase();
                System.out.println(key+"-"+btnText);

                if (key.equals("btnUser")) {
                    btnText = "user";
                }
                if (!btnText.equals("inventory")) {
                    System.out.println(ThreadService.getInventoryLoadingThread().isAlive());
                }

                try {
                    App.setRoot(btnText);
                    System.gc();

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public  void setActive(Button btn) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(IDLE_STYLE);
        }

        btn.setStyle(ACTIVE_STYLE);

        currentActiveButton = btn;
    }
}
