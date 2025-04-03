package tests;

import entities.DossierMedical;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DossierMedicalDetailsController {
    @FXML private Label idLabel;
    @FXML private Label utilisateurIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label fichierLabel;
    @FXML private Label uniteLabel;
    @FXML private Label mesureLabel;

    private DossierMedical dossier;

    public void setDossier(DossierMedical dossier) {
        this.dossier = dossier;
        idLabel.setText("ID: " + dossier.getId());
        utilisateurIdLabel.setText("Utilisateur ID: " + dossier.getUtilisateurId());
        dateLabel.setText("Date: " + dossier.getDate().toString());
        fichierLabel.setText("Fichier: " + dossier.getFichier());
        uniteLabel.setText("Unité: " + dossier.getUnite());
        mesureLabel.setText("Mesure: " + dossier.getMesure());
    }

    @FXML
    private void viewAnalyses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnalyseList.fxml"));
            Parent root = loader.load();
            AnalyseListController controller = loader.getController();
            controller.filterByDossierId(dossier.getId());

            Stage stage = new Stage();
            stage.setTitle("Analyses du Dossier " + dossier.getId());
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            // Fermer la fenêtre des détails
            Stage currentStage = (Stage) idLabel.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}