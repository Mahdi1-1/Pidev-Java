// src/main/java/tests/Main.java
package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = getClass().getResource("/fxml/Main.fxml");
        if (location == null) {
            throw new RuntimeException("Impossible de trouver /Main.fxml dans les ressources");
        }
        Parent root = FXMLLoader.load(location);
        primaryStage.setTitle("Gestion Médicale");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}