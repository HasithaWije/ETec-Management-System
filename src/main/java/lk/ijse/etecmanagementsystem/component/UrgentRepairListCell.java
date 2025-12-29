package lk.ijse.etecmanagementsystem.component; // Adjust package as needed

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lk.ijse.etecmanagementsystem.dto.tm.UrgentRepairTM;

public class UrgentRepairListCell extends ListCell<UrgentRepairTM> {

    @Override
    protected void updateItem(UrgentRepairTM item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            setStyle("-fx-background-color: transparent;");
        } else {
            // Main Container
            HBox hBox = new HBox(15);
            hBox.setAlignment(Pos.TOP_LEFT);
            hBox.setStyle("-fx-padding: 5 0 5 0; -fx-background-color: transparent;");

            // 1. The Badge (Orange Circle with ID)
            StackPane badge = new StackPane();
            Circle circle = new Circle(15, Color.web("#e67e22"));
            Label Lbl = new Label("R");
            Lbl.setTextFill(Color.WHITE);
            Lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            badge.getChildren().addAll(circle, Lbl);

            // 2. Middle Text (Device Name & Status)
            VBox vBox = new VBox(3);
            String repairID = ("#"+String.valueOf(item.getId()));
            Label deviceLbl = new Label(item.getDevice()+" " + repairID);
            deviceLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            deviceLbl.setTextFill(Color.web("#2c3e50"));

            Label statusLbl = new Label(item.getStatus());
            statusLbl.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
            vBox.getChildren().addAll(deviceLbl, statusLbl);

            // 3. Spacer (pushes Date to right)
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // 4. Right Side (Date)
            Label dateLbl = new Label(item.getDate());
            dateLbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");

            hBox.getChildren().addAll(badge, vBox, spacer, dateLbl);

            setGraphic(hBox);
        }
    }
}