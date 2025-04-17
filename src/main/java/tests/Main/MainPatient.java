package tests.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

public class MainPatient extends Application {
    private static final String PATIENT_ID = "1";

    @Override
    public void start(Stage primaryStage) throws Exception {
        //URL location = getClass().getResource("/fxml/Patient/consultation_list.fxml");
        URL location = getClass().getResource("/fxml/Doctor/consultation_list.fxml");

        if (location == null) {
            throw new RuntimeException("Impossible de trouver /fxml/Admin/DossierMedicalListAdmin.fxml dans les ressources");
        }
        Parent root = FXMLLoader.load(location);

        // Créer une scène avec un fond transparent
        Scene scene = new Scene(root, 849, 552);
        scene.setFill(null); // Rendre la scène transparente

        // Configurer la fenêtre
        primaryStage.setTitle("Gestion Médicale - Admin");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}