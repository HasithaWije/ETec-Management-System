package lk.ijse.etecmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    private static Scene scene;

    private static Stage primaryStage;


    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        setupLoginStageScene("login");
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        // keep login fixed-size, allow resizing for other scenes
        if (primaryStage != null) {
            primaryStage.setResizable(!"login".equals(fxml));
        }
    }
    public static void setupLoginStageScene(String fxmlFileName) throws IOException {

        Scene sceneLogin = new Scene(loadFXML(fxmlFileName), 1000, 600);
        primaryStage.setResizable(false);
        primaryStage.setMaximized(false);
        primaryStage.setTitle("ETec Management System - Login");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(App.class.getResourceAsStream("images/logo.png"))));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(sceneLogin);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    public static void setupPrimaryStageScene(String fxmlFileName) throws Exception {

        scene = new Scene(loadFXML(fxmlFileName), 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ETec Management System");
        primaryStage.setResizable(true);
        primaryStage.setMinHeight(656);
        primaryStage.setMinWidth(1016);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/"+fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static Scene getScene() {
        return  scene;
    }

    public static  Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}

