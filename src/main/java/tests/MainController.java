package tests;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private Button goToDossierButton; // Ajout d'une référence au bouton

    @FXML
    private void goToDossierList() {
        try {
            // Charger la nouvelle fenêtre (liste des dossiers médicaux)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DossierMedicalList.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Dossiers Médicaux");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            // Fermer la fenêtre principale (Main.fxml)
            Stage currentStage = (Stage) goToDossierButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}