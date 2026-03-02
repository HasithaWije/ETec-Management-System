package lk.ijse.etecmanagementsystem.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.etecmanagementsystem.App;

import java.io.IOException;

public class CategoryScene {

    private static final ObservableList<String> categories;

    static {
        categories = FXCollections.observableArrayList();
    }

    public static ObservableList<String> getCategories() {
        return categories;
    }

}
