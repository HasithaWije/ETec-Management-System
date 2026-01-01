package lk.ijse.etecmanagementsystem.controller;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.dto.tm.ManualTransactionResult;

import java.util.function.Consumer;

public class ManualTransactionController {

    @FXML
    private ComboBox<String> cmbType;
    @FXML
    private TextField txtAmount;
    @FXML
    private ComboBox<String> cmbMethod;
    @FXML
    private TextField txtNote;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Consumer<ManualTransactionResult> saveHandler;

    @FXML
    public void initialize() {

        cmbType.setItems(FXCollections.observableArrayList("EXPENSE", "SUPPLIER_PAYMENT", "OTHER_INCOME"));
        cmbType.getSelectionModel().select("EXPENSE");

        cmbMethod.setItems(FXCollections.observableArrayList("CASH", "CARD", "TRANSFER"));
        cmbMethod.getSelectionModel().select("CASH");


        txtAmount.textProperty().addListener((observable, oldValue, newValue) -> validateForm());
    }


    public void setOnSave(Consumer<ManualTransactionResult> handler) {
        this.saveHandler = handler;
    }

    private void validateForm() {
        boolean isValid = false;
        String amountText = txtAmount.getText().trim();

        if (!amountText.isEmpty()) {
            try {
                Double.parseDouble(amountText);
                isValid = true;
            } catch (NumberFormatException e) {
                // Not a number
            }
        }
        btnSave.setDisable(!isValid);
    }

    @FXML
    private void handleSave() {
        if (saveHandler != null) {
            // Create the result object
            ManualTransactionResult result = new ManualTransactionResult(
                    cmbType.getValue(),
                    Double.parseDouble(txtAmount.getText()),
                    cmbMethod.getValue(),
                    txtNote.getText()
            );

            saveHandler.accept(result);

            closeWindow();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}