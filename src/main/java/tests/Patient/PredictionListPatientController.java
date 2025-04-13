package tests.Patient;

import entities.Prediction;
import javafx.scene.control.Alert;
import services.ServicePrediction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class PredictionListPatientController {
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