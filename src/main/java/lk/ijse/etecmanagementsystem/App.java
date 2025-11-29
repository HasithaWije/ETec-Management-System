package lk.ijse.etecmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Scene scene;
    private static Stage primaryStage;
    private static Scene secondaryScene;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("login"), 998, 645);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // lock size for login
        primaryStage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        // keep login fixed-size, allow resizing for other scenes
        if (primaryStage != null) {
            primaryStage.setResizable(!"login".equals(fxml));
        }
    }
    public static void setupPrimaryStageScene(String fxmlFileName) throws Exception {

        scene = new Scene(loadFXML(fxmlFileName), 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinHeight(656);
        primaryStage.setMinWidth(1016);
        primaryStage.show();
    }

    public static void setupSecondaryStageScene(String fxmlFileName,String title) throws Exception {
        Stage secondaryStage = new Stage();
        secondaryScene = new Scene(loadFXML(fxmlFileName), 800, 600);
        secondaryStage.setScene(secondaryScene);
        secondaryStage.setTitle(title);
        secondaryStage.setResizable(false);
        secondaryStage.setAlwaysOnTop(true);
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        secondaryStage.initOwner(primaryStage);
        secondaryStage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource( fxml + ".fxml"));
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

