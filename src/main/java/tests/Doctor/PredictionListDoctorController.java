package tests.Doctor;

import entities.Prediction;
import services.ServicePrediction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PredictionListDoctorController {
    @FXML private Label titleLabel;
    @FXML private Label dossierIdLabel;
    @FXML private TableView<Prediction> predictionTable;
    @FXML private TableColumn<Prediction, Integer> idColumn;
    @FXML private TableColumn<Prediction, Boolean> hypertensionColumn;
    @FXML private TableColumn<Prediction, Boolean> heartDiseaseColumn;
    @FXML private TableColumn<Prediction, String> smokingHistoryColumn;
    @FXML private TableColumn<Prediction, Float> bmiColumn;
    @FXML private TableColumn<Prediction, Float> hbA1cLevelColumn;
    @FXML private TableColumn<Prediction, Float> bloodGlucoseLevelColumn;
    @FXML private TableColumn<Prediction, Boolean> diabeteColumn;
    // Removed datePredictionColumn since the field is missing in Prediction class

    @FXML private Button ajouterButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;


    private ObservableList<Prediction> predictionList;
    private Integer dossierId;

    private final ServicePrediction servicePrediction = new ServicePrediction();

    public PredictionListDoctorController() throws SQLException {
    }

    @FXML
    public void initialize() {
        // Bind the TableView columns to the Prediction properties
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        hypertensionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isHypertension()).asObject());
        heartDiseaseColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isheart_disease()).asObject());
        smokingHistoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getsmoking_history()));
        bmiColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getBmi()).asObject());
        hbA1cLevelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().gethbA1c_level()).asObject());
        bloodGlucoseLevelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleFloatProperty(cellData.getValue().getBloodGlucoseLevel()).asObject());
        diabeteColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isDiabete()).asObject());

        // Enable buttons only when a row is selected
        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
        predictionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            modifierButton.setDisable(newSelection == null);
            supprimerButton.setDisable(newSelection == null);
        });
    }

    public void setDossierId(Integer dossierId) {
        this.dossierId = dossierId;
        dossierIdLabel.setText("Dossier ID: " + dossierId);
        loadPredictions();
    }

    private void loadPredictions() {
        try {
            List<Prediction> predictions = servicePrediction.getByDossierId(dossierId);
            predictionList = FXCollections.observableArrayList(predictions);
            predictionTable.setItems(predictionList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des prédictions : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterPrediction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/FormPrediction.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter une Prédiction");

            FormPredictionController controller = loader.getController();
            controller.setDossierId(dossierId);
            stage.showAndWait();
            loadPredictions(); // Refresh the table after adding
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void modifierPrediction() {
        Prediction selectedPrediction = predictionTable.getSelectionModel().getSelectedItem();
        if (selectedPrediction != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/FormPrediction.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Modifier une Prédiction");

                FormPredictionController controller = loader.getController();
                controller.setPrediction(selectedPrediction);
                stage.showAndWait();
                loadPredictions(); // Refresh the table after modifying
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'ouverture du formulaire : " + e.getMessage());
            }
        }
    }

    @FXML
    private void supprimerPrediction() {
        Prediction selectedPrediction = predictionTable.getSelectionModel().getSelectedItem();
        if (selectedPrediction != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette prédiction ?");
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        servicePrediction.supprimer(selectedPrediction.getId());
                        predictionList.remove(selectedPrediction);
                        showAlert("Succès", "Prédiction supprimée avec succès !");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Erreur lors de la suppression de la prédiction : " + e.getMessage());
                    }
                }
            });
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
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