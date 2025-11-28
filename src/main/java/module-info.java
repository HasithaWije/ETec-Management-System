module lk.ijse.etecmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    requires org.controlsfx.controls;



    opens lk.ijse.etecmanagementsystem to javafx.fxml;
    exports lk.ijse.etecmanagementsystem;
    exports lk.ijse.etecmanagementsystem.controller;
    opens lk.ijse.etecmanagementsystem.controller to javafx.fxml;
    exports lk.ijse.etecmanagementsystem.util;
    opens lk.ijse.etecmanagementsystem.util to javafx.fxml;
    opens lk.ijse.etecmanagementsystem.dto to java.base;
    exports  lk.ijse.etecmanagementsystem.dto;
    exports lk.ijse.etecmanagementsystem.service;
    opens lk.ijse.etecmanagementsystem.service to javafx.fxml;

}