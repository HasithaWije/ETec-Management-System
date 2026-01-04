package lk.ijse.etecmanagementsystem.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.util.BarcodeScanner;

import javax.swing.border.Border;
import java.net.URL;
import java.util.ResourceBundle;

public class BarcodeScanController {

    // Barcode Scanning

    private  BarcodeScanner barcodeScanner;
    private Stage newStage;

    private BorderPane rootPane;



    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        barcodeScanner.startScan();

    }

    public void setBarcodeInput(TextField barcodeInput) {
        this.barcodeScanner = BarcodeScanner.getInstance(barcodeInput);
    }





}
