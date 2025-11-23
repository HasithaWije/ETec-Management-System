package lk.ijse.etecmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("login"), 1060, 720);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // lock size for login
        primaryStage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        // keep login fixed-size, allow resizing for other scenes
        if (primaryStage != null) {
            primaryStage.setResizable("login".equals(fxml) ? false : true);
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static Scene getScene() {
        return  scene;
    }

    public static void main(String[] args) {
        launch();
    }
}

