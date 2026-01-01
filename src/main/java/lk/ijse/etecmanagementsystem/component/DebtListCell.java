package lk.ijse.etecmanagementsystem.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lk.ijse.etecmanagementsystem.dto.tm.DebtTM;

public class DebtListCell extends ListCell<DebtTM> {

    @Override
    protected void updateItem(DebtTM item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            setStyle("-fx-background-color: transparent;");
        } else {
            HBox hBox = new HBox(15);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setStyle("-fx-padding: 5 0 5 0;");

            // 1. Icon (S for Sale, R for Repair)
            Label icon = new Label(item.getType().substring(0, 1));
            String iconColor = item.getType().equals("SALE") ? "#3498db" : "#9b59b6";
            icon.setStyle("-fx-background-color: " + iconColor + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10; -fx-font-weight: bold;");

            // 2. Customer Name & ID
            VBox vBox = new VBox(3);
            String cusName = item.getCustomer() == null ? " - " : item.getCustomer();
            String cusName2 = cusName.length() > 20 ? cusName.substring(0, 17) + "..." : cusName;
            Label nameLbl = new Label(cusName2 + " (#" + item.getId() + ")");
            nameLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            nameLbl.setTextFill(Color.web("#2c3e50"));

            Label typeLbl = new Label(item.getType() + " Payment");
            typeLbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");
            vBox.getChildren().addAll(nameLbl, typeLbl);

            // 3. Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // 4. Amount Due (Bold Red)
            Label amountLbl = new Label(String.format("%.2f", item.getAmount()));
            amountLbl.setTextFill(Color.web("#c0392b"));
            amountLbl.setFont(Font.font("System", FontWeight.BOLD, 14));

            hBox.getChildren().addAll(icon, vBox, spacer, amountLbl);

            setGraphic(hBox);
        }
    }
}