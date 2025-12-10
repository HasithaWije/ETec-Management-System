package lk.ijse.etecmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lk.ijse.etecmanagementsystem.util.Login;
import lk.ijse.etecmanagementsystem.service.MenuBar;

import java.util.ArrayList;
import java.util.List;

public class UserController {

    @FXML
    private Label userPageTitle;


    // We need a list to easily loop through them
    private List<Button> menuButtons = new ArrayList<>();




    @FXML
    public void initialize() {




        String username = Login.getUserName();
        userPageTitle.setText(username);

    }
}