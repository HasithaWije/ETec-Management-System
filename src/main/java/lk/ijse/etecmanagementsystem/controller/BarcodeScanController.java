package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.barcodescanner.BarcodeServer;

import java.net.URL;
import java.util.ResourceBundle;

public class BarcodeScanController {



    private BarcodeServer barcodeServer;
    private Stage newStage;

    @FXML
    private ImageView imageViewScan;

    private BorderPane rootPane;



    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        this.barcodeServer.startServer();


    }

    public void setBarcodeInput(TextField barcodeInput) {
        this.barcodeServer = BarcodeServer.getBarcodeServerInstance(barcodeInput);
    }





}
