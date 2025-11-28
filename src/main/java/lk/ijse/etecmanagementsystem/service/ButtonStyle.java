package lk.ijse.etecmanagementsystem.service;

import javafx.scene.control.Button;

public class ButtonStyle {
    public final String IDLE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #cfcfcf; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    public final String HOVER_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
    public final String ACTIVE_STYLE = "-fx-background-color: #2C3545; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-border-color: transparent transparent transparent #3498db; -fx-border-width: 0 0 0 4;";

    private String currentStyle = IDLE_STYLE;
    private String hoverStyle = HOVER_STYLE;
    private String activeStyle = ACTIVE_STYLE;

    private String backgroundColor;  // Dark Navy
    private String textColor;
    private String alignment;
    private String accentColor;
    private String borderWidth;


    public ButtonStyle() {
    }

    public ButtonStyle(String hoverStyle, String currentStyle) {
        this.hoverStyle = hoverStyle;
        this.currentStyle = currentStyle;
    }

    public ButtonStyle(String currentStyle, String hoverStyle, String activeStyle) {
        this.currentStyle = currentStyle;
        this.hoverStyle = hoverStyle;
        this.activeStyle = activeStyle;
    }

    public void setCurrentStyle(String backgroundColor, String textColor, String alignment, String accentColor, String borderWidth) {
        this.currentStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-alignment: %s; " +
                        "-fx-border-color: transparent transparent transparent %s; " +
                        "-fx-border-width: %s;",
                backgroundColor, textColor, alignment, accentColor, borderWidth
        );
    }

    public void setHoverStyle(String backgroundColor, String textColor, String alignment, String accentColor, String borderWidth) {
        this.hoverStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-alignment: %s; " +
                        "-fx-border-color: transparent transparent transparent %s; " +
                        "-fx-border-width: %s;",
                backgroundColor, textColor, alignment, accentColor, borderWidth
        );
    }

    public void setActiveStyle(String backgroundColor, String textColor, String alignment, String accentColor, String borderWidth) {
        this.activeStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-alignment: %s; " +
                        "-fx-border-color: transparent transparent transparent %s; " +
                        "-fx-border-width: %s;",
                backgroundColor, textColor, alignment, accentColor, borderWidth
        );
    }

    public String getCurrentStyle() {
        return currentStyle;
    }

    public String getHoverStyle() {
        return hoverStyle;
    }

    public String getActiveStyle() {
        return activeStyle;
    }

    public void setCurrentStyle(String currentStyle) {
        this.currentStyle = currentStyle;
    }

    public void setHoverStyle(String hoverStyle) {
        this.hoverStyle = hoverStyle;
    }

    public void setActiveStyle(String activeStyle) {
        this.activeStyle = activeStyle;
    }

    public void onMouseAction(Button btn) {
        btn.setOnMouseEntered(event -> {
            btn.setStyle(hoverStyle);
        });

        btn.setOnMouseExited(event -> {
            btn.setStyle(currentStyle);
        });

        btn.setOnMousePressed(event -> {
            btn.setStyle(activeStyle);
        });

    }
    public void onMouseAction(Button btn, String currentStyle, String hoverStyle) {
        btn.setOnMouseEntered(event -> {
            btn.setStyle(hoverStyle);
        });

        btn.setOnMouseExited(event -> {
            btn.setStyle(currentStyle);
        });

    }
}
