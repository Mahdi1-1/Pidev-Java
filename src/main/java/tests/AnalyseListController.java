package tests;

import entities.Analyse;
import services.ServiceAnalyse;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AnalyseListController {
    @FXML private TableView<Analyse> analyseTable;
    @FXML private TableColumn<Analyse, Integer> analyseIdColumn;
    @FXML private TableColumn<Analyse, Integer> analyseDossierIdColumn;
    @FXML private TableColumn<Analyse, String> analyseTypeColumn;
    @FXML private TableColumn<Analyse, LocalDate> analyseDateColumn;
    @FXML private TableColumn<Analyse, String> analyseDonneesColumn;
    @FXML private TableColumn<Analyse, String> analyseDiagnosticColumn;
    @FXML private Button addAnalyseButton;
    @FXML private Button editAnalyseButton;
    @FXML private Button deleteAnalyseButton;

    // Champs du formulaire
    @FXML private TextField dossierIdField;
    @FXML private TextField typeField;
    @FXML private TextField dateAnalyseField;
    @FXML private TextField donneesAnalyseField;
    @FXML private TextField diagnosticField;

    private ServiceAnalyse serviceAnalyse = new ServiceAnalyse();
    private ObservableList<Analyse> analyseList = FXCollections.observableArrayList();
    private Stage formStage;
    private Analyse selectedAnalyse;
    private Integer filterDossierId = null;

    @FXML
    public void initialize() {
        analyseIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        analyseDossierIdColumn.setCellValueFactory(new PropertyValueFactory<>("dossierId"));
        analyseTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        analyseDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateAnalyse"));
        analyseDonneesColumn.setCellValueFactory(new PropertyValueFactory<>("donneesAnalyse"));
        analyseDiagnosticColumn.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));

        loadAnalyses();
    }

    public void filterByDossierId(int dossierId) {
        this.filterDossierId = dossierId;
        loadAnalyses();
    }

    private void loadAnalyses() {
        try {
            if (filterDossierId != null) {
                analyseList.setAll(serviceAnalyse.afficher().stream()
                        .filter(analyse -> analyse.getDossierId() == filterDossierId)
                        .toList());
            } else {
                analyseList.setAll(serviceAnalyse.afficher());
            }
            analyseTable.setItems(analyseList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les analyses : " + e.getMessage());
        }
    }

    @FXML
    private void addAnalyse() {
        selectedAnalyse = null;
        showAnalyseForm("Ajouter Analyse");
    }

    @FXML
    private void editAnalyse() {
        selectedAnalyse = analyseTable.getSelectionModel().getSelectedItem();
        if (selectedAnalyse != null) {
            showAnalyseForm("Modifier Analyse");
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une analyse à modifier.");
        }
    }

    private void showAnalyseForm(String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormAnalyse.fxml"));
            Parent root = loader.load();
            AnalyseListController controller = loader.getController();

            formStage = new Stage();
            formStage.setTitle(title);
            formStage.initModality(Modality.APPLICATION_MODAL);
            formStage.setScene(new Scene(root));
            formStage.setResizable(false);

            if (selectedAnalyse != null) {
                dossierIdField.setText(String.valueOf(selectedAnalyse.getDossierId()));
                typeField.setText(selectedAnalyse.getType());
                dateAnalyseField.setText(selectedAnalyse.getDateAnalyse().toString());
                donneesAnalyseField.setText(selectedAnalyse.getDonneesAnalyse());
                diagnosticField.setText(selectedAnalyse.getDiagnostic());
            } else {
                clearAnalyseFields();
            }

            formStage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void saveAnalyse() {
        try {
            int dossierId = Integer.parseInt(dossierIdField.getText());
            String type = typeField.getText();
            LocalDate dateAnalyse = LocalDate.parse(dateAnalyseField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            String donneesAnalyse = donneesAnalyseField.getText();
            String diagnostic = diagnosticField.getText();

            if (selectedAnalyse == null) {
                Analyse newAnalyse = new Analyse(dossierId, type, dateAnalyse, donneesAnalyse, diagnostic);
                serviceAnalyse.ajouter(newAnalyse);
            } else {
                selectedAnalyse.setDossierId(dossierId);
                selectedAnalyse.setType(type);
                selectedAnalyse.setDateAnalyse(dateAnalyse);
                selectedAnalyse.setDonneesAnalyse(donneesAnalyse);
                selectedAnalyse.setDiagnostic(diagnostic);
                serviceAnalyse.modifier(selectedAnalyse);
            }

            loadAnalyses();
            formStage.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'enregistrement : " + e.getMessage());
        } catch (NumberFormatException | DateTimeParseException e) {
            showAlert("Erreur", "Veuillez vérifier les formats des champs (ex: dossier ID doit être un nombre, date au format YYYY-MM-DD).");
        }
    }

    @FXML
    private void deleteAnalyse() {
        Analyse selected = analyseTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                serviceAnalyse.supprimer(selected.getId());
                loadAnalyses();
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression : " + e.getMessage());
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une analyse à supprimer.");
        }
    }

    @FXML
    private void showAnalyseDetails() {
        Analyse selected = analyseTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnalyseDetails.fxml"));
                Parent root = loader.load();
                AnalyseDetailsController controller = loader.getController();
                controller.setAnalyse(selected);

                Stage stage = new Stage();
                stage.setTitle("Détails de l'Analyse");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.showAndWait();
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de charger les détails : " + e.getMessage());
            }
        }
    }

    @FXML
    private void cancelForm() {
        formStage.close();
    }

    private void clearAnalyseFields() {
        dossierIdField.clear();
        typeField.clear();
        dateAnalyseField.clear();
        donneesAnalyseField.clear();
        diagnosticField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}