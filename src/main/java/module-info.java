module lk.ijse.etecmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    requires org.controlsfx.controls;
    requires java.sql;
    requires java.sql.rowset;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;
    requires jdk.httpserver;
    requires net.sf.jasperreports.core;
    requires java.mail;


    exports lk.ijse.etecmanagementsystem;
    opens lk.ijse.etecmanagementsystem to javafx.fxml;

    exports lk.ijse.etecmanagementsystem.controller;
    opens lk.ijse.etecmanagementsystem.controller to javafx.fxml;

    exports lk.ijse.etecmanagementsystem.util;
    opens lk.ijse.etecmanagementsystem.util to javafx.fxml;

    opens lk.ijse.etecmanagementsystem.dto to java.base;
    exports lk.ijse.etecmanagementsystem.dto;

    opens lk.ijse.etecmanagementsystem.dto.tm to java.base;
    exports lk.ijse.etecmanagementsystem.dto.tm;

}