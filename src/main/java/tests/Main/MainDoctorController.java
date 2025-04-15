package tests.Main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDoctorController {

    @FXML private ImageView backgroundImageView;
    @FXML private javafx.scene.control.Button forumButton;
    @FXML private javafx.scene.control.Button produitButton;
    @FXML private javafx.scene.control.Button evenementButton;
    @FXML private javafx.scene.control.Button suiviMedicalButton;
    @FXML private javafx.scene.control.Button consultationButton;
    @FXML private javafx.scene.control.Button connexionButton;

    @FXML
    private void goToForum() throws IOException {
        loadScene("/fxml/Forum.fxml", "Forum");
    }

    @FXML
    private void goToProduit() throws IOException {
        loadScene("/fxml/Produit.fxml", "Produit");
    }

    @FXML
    private void goToEvenement() throws IOException {
        loadScene("/fxml/Evenement.fxml", "Événement");
    }

    @FXML
    private void goToDossierList() throws IOException {
        // Redirection vers la liste des dossiers médicaux pour un médecin (vue admin)
        loadScene("/fxml/Doctor/DossierMedicalListDoctor.fxml", "Liste des Dossiers Médicaux");
    }

    @FXML
    private void goToConsultation() throws IOException {
        loadScene("/fxml/Consultation.fxml", "Consultation");
    }

    @FXML
    private void goToConnexion() throws IOException {
        loadScene("/fxml/Connexion.fxml", "Connexion");
    }

    private void loadScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) suiviMedicalButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }
}