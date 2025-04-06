package tests.Doctor;

import entities.DossierMedical;
import entities.Prediction;
import services.ServicePrediction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tests.AnalyseListController;

import java.io.IOException;
import java.sql.SQLException;

public class DossierMedicalDetailsDoctorController {
    @FXML private Label utilisateurIdLabel;
    @FXML private Label dateLabel;
    @FXML private Label fichierLabel;
    @FXML private Label uniteLabel;
    @FXML private Label mesureLabel;

    @FXML private Label hypertensionLabel;
    @FXML private Label heartDiseaseLabel;
    @FXML private Label smokingHistoryLabel;
    @FXML private Label bmiLabel;
    @FXML private Label hbA1cLevelLabel;
    @FXML private Label bloodGlucoseLevelLabel;
    @FXML private Label diabeteLabel;

    private Stage stage;
    private DossierMedical dossier;

    public void setDossier(DossierMedical dossier) {
        this.dossier = dossier;

        // Vérification des labels
        if (utilisateurIdLabel == null || dateLabel == null || fichierLabel == null ||
                uniteLabel == null || mesureLabel == null || diabeteLabel == null) {
            showAlert("Erreur FXML", "Un ou plusieurs labels n'ont pas été correctement injectés depuis le FXML.");
            return;
        }

        utilisateurIdLabel.setText("Utilisateur ID: " + dossier.getUtilisateurId());
        dateLabel.setText("Date: " + dossier.getDate().toString());
        fichierLabel.setText("Fichier: " + dossier.getFichier());
        uniteLabel.setText("Unité: " + dossier.getUnite());
        mesureLabel.setText("Mesure: " + dossier.getMesure());

        try {
            ServicePrediction servicePrediction = new ServicePrediction();
            Prediction prediction = servicePrediction.getByDossierId(dossier.getId()).stream().findFirst().orElse(null);
            if (prediction != null) {
                diabeteLabel.setText("Risque de Diabète: " + (prediction.isDiabete() ? "Oui" : "Non"));
            } else {
                diabeteLabel.setText("Risque de Diabète: Aucune prédiction");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la récupération de la prédiction : " + e.getMessage());
        }
    }

    @FXML
    private void showPredictionList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/PredictionListDoctor.fxml"));
            Parent root = loader.load();

            PredictionListDoctorController controller = loader.getController();
            controller.setDossierId(dossier.getId());

            Stage stage = new Stage();
            stage.setTitle("Liste des Prédictions");
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(true);
            stage.showAndWait();

            // Rafraîchir les prédictions affichées après la gestion
            setDossier(dossier);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des prédictions : " + e.getMessage());
        }
    }

    @FXML
    private void showAnalyseList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/AnalyseListDoctor.fxml"));
            Parent root = loader.load();
            AnalyseListController controller = loader.getController();
            controller.filterByDossierId(dossier.getId());

            Stage analyseStage = new Stage();
            analyseStage.setTitle("Liste des Analyses pour le Dossier " + dossier.getId());
            analyseStage.setScene(new Scene(root, 600, 400));
            analyseStage.setResizable(true);
            analyseStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des analyses : " + e.getMessage());
        }
    }

    @FXML
    private void closeDetails() {
        stage = (Stage) utilisateurIdLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}