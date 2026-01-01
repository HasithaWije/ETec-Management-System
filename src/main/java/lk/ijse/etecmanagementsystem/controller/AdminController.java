package lk.ijse.etecmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import lk.ijse.etecmanagementsystem.util.BackupUtil;

import java.io.File;

public class AdminController {

    @FXML
    void handleBackup(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Location to Save Backup");


        javafx.scene.Node source = (javafx.scene.Node) event.getSource();

        File selectedDirectory = directoryChooser.showDialog(source.getScene().getWindow());

        if (selectedDirectory != null) {

            boolean isSuccess = BackupUtil.backupDatabase(
                    "ETec", // DB Name
                    "root",                 // Username
                    "mysql",                 // Password
                    selectedDirectory.getAbsolutePath()
            );

            if (isSuccess) {
                new Alert(Alert.AlertType.INFORMATION, "Backup created successfully!").show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Backup Failed. Please check database settings.").show();
            }
        }
    }
}
