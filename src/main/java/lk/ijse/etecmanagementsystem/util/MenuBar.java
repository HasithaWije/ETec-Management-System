package lk.ijse.etecmanagementsystem.util;

import javafx.scene.control.Button;

public class MenuBar {

    private static Button currentActiveButton = null;

    // Define your colors here so you don't type hex codes everywhere
    private final String IDLE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #cfcfcf; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    private final String HOVER_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    private final String ACTIVE_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent transparent transparent #3498db; -fx-border-width: 0 0 0 4;";


    public void setupButton(Button btn) {

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

    }

    public void setActive(Button btn) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(IDLE_STYLE);
        }

        btn.setStyle(ACTIVE_STYLE);

        currentActiveButton = btn;
    }
}
