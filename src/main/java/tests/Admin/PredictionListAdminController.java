package tests.Admin;

import entities.Prediction;
import services.ServicePrediction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tests.Doctor.FormPredictionController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PredictionListAdminController {
    @FXML private Label titleLabel;
    @FXML private TableView<Prediction> predictionTable;
    @FXML private TableColumn<Prediction, Integer> idColumn;
    @FXML private TableColumn<Prediction, Integer> dossierIdColumn;
    @FXML private TableColumn<Prediction, Boolean> hypertensionColumn;
    @FXML private TableColumn<Prediction, Boolean> heartDiseaseColumn;
    @FXML private TableColumn<Prediction, String> smokingHistoryColumn;
    @FXML private TableColumn<Prediction, Float> bmiColumn;
    @FXML private TableColumn<Prediction, Float> hbA1cLevelColumn;
    @FXML private TableColumn<Prediction, Float> bloodGlucoseLevelColumn;
    @FXML private TableColumn<Prediction, Boolean> diabeteColumn;
    @FXML private HBox buttonBox;
    @FXML private Button ajouterButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;

    private int dossierId;
    private ObservableList<Prediction> predictionList;

    public void setDossierId(int dossierId) {
        this.dossierId = dossierId;
        titleLabel.setText("Liste des Prédictions (Dossier " + dossierId + ")");
        chargerPredictions();
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dossierIdColumn.setCellValueFactory(new PropertyValueFactory<>("dossierId"));
        hypertensionColumn.setCellValueFactory(new PropertyValueFactory<>("hypertension"));
        heartDiseaseColumn.setCellValueFactory(new PropertyValueFactory<>("heartDisease"));
        smokingHistoryColumn.setCellValueFactory(new PropertyValueFactory<>("smokingHistory"));
        bmiColumn.setCellValueFactory(new PropertyValueFactory<>("bmi"));
        hbA1cLevelColumn.setCellValueFactory(new PropertyValueFactory<>("hbA1cLevel"));
        bloodGlucoseLevelColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGlucoseLevel"));
        diabeteColumn.setCellValueFactory(new PropertyValueFactory<>("diabete"));

        predictionList = FXCollections.observableArrayList();
        predictionTable.setItems(predictionList);
    }

    private void chargerPredictions() {
        try {
            ServicePrediction service = new ServicePrediction();
            List<Prediction> predictions = service.getByDossierId(dossierId);
            predictionList.clear();
            predictionList.addAll(predictions);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des prédictions : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterPrediction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/FormPrediction.fxml"));
            Parent root = loader.load();

            FormPredictionController controller = loader.getController();
            controller.setDossierId(dossierId);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Prédiction");
            stage.setScene(new Scene(root, 400, 500));
            stage.setResizable(false);
            stage.showAndWait();

            chargerPredictions();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void modifierPrediction() {
        Prediction selectedPrediction = predictionTable.getSelectionModel().getSelectedItem();
        if (selectedPrediction == null) {
            showAlert("Erreur", "Veuillez sélectionner une prédiction à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/FormPrediction.fxml"));
            Parent root = loader.load();

            FormPredictionController controller = loader.getController();
            controller.setDossierId(dossierId);
            controller.setPrediction(selectedPrediction);

            Stage stage = new Stage();
            stage.setTitle("Modifier une Prédiction");
            stage.setScene(new Scene(root, 400, 500));
            stage.setResizable(false);
            stage.showAndWait();

            chargerPredictions();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerPrediction() {
        Prediction selectedPrediction = predictionTable.getSelectionModel().getSelectedItem();
        if (selectedPrediction == null) {
            showAlert("Erreur", "Veuillez sélectionner une prédiction à supprimer.");
            return;
        }

        try {
            ServicePrediction service = new ServicePrediction();
            service.supprimer(selectedPrediction.getId());
            showAlert("Succès", "Prédiction supprimée avec succès !");
            chargerPredictions();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la suppression de la prédiction : " + e.getMessage());
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) predictionTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}