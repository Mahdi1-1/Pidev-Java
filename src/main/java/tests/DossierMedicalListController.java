package tests;

import entities.DossierMedical;
import services.ServiceDossierMedical;
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

public class DossierMedicalListController {
    @FXML private TableView<DossierMedical> dossierTable;
    @FXML private TableColumn<DossierMedical, Integer> dossierIdColumn;
    @FXML private TableColumn<DossierMedical, Integer> dossierUtilisateurIdColumn;
    @FXML private TableColumn<DossierMedical, LocalDate> dossierDateColumn;
    @FXML private TableColumn<DossierMedical, String> dossierFichierColumn;
    @FXML private TableColumn<DossierMedical, String> dossierUniteColumn;
    @FXML private TableColumn<DossierMedical, Double> dossierMesureColumn;
    @FXML private Button addDossierButton;
    @FXML private Button editDossierButton;
    @FXML private Button deleteDossierButton;

    // Champs du formulaire
    @FXML private TextField utilisateurIdField;
    @FXML private TextField dateField;
    @FXML private TextField fichierField;
    @FXML private TextField uniteField;
    @FXML private TextField mesureField;

    private ServiceDossierMedical serviceDossier = new ServiceDossierMedical();
    private ObservableList<DossierMedical> dossierList = FXCollections.observableArrayList();
    private Stage formStage;
    private DossierMedical selectedDossier;

    @FXML
    public void initialize() {
        dossierIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dossierUtilisateurIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        dossierDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dossierFichierColumn.setCellValueFactory(new PropertyValueFactory<>("fichier"));
        dossierUniteColumn.setCellValueFactory(new PropertyValueFactory<>("unite"));
        dossierMesureColumn.setCellValueFactory(new PropertyValueFactory<>("mesure"));

        loadDossiers();
    }

    private void loadDossiers() {
        try {
            dossierList.setAll(serviceDossier.afficher());
            dossierTable.setItems(dossierList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les dossiers : " + e.getMessage());
        }
    }

    @FXML
    private void addDossier() {
        selectedDossier = null;
        showDossierForm("Ajouter Dossier Médical");
    }

    @FXML
    private void editDossier() {
        selectedDossier = dossierTable.getSelectionModel().getSelectedItem();
        if (selectedDossier != null) {
            showDossierForm("Modifier Dossier Médical");
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un dossier à modifier.");
        }
    }

    private void showDossierForm(String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormDossierMedical.fxml"));
            Parent root = loader.load();
            DossierMedicalListController controller = loader.getController();

            formStage = new Stage();
            formStage.setTitle(title);
            formStage.initModality(Modality.APPLICATION_MODAL);
            formStage.setScene(new Scene(root));
            formStage.setResizable(false);

            if (selectedDossier != null) {
                utilisateurIdField.setText(String.valueOf(selectedDossier.getUtilisateurId()));
                dateField.setText(selectedDossier.getDate().toString());
                fichierField.setText(selectedDossier.getFichier());
                uniteField.setText(selectedDossier.getUnite());
                mesureField.setText(String.valueOf(selectedDossier.getMesure()));
            } else {
                clearDossierFields();
            }

            formStage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void saveDossier() {
        try {
            int utilisateurId = Integer.parseInt(utilisateurIdField.getText());
            LocalDate date = LocalDate.parse(dateField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            String fichier = fichierField.getText();
            String unite = uniteField.getText();
            double mesure = Double.parseDouble(mesureField.getText());

            if (selectedDossier == null) {
                DossierMedical newDossier = new DossierMedical(utilisateurId, date, fichier, unite, mesure);
                serviceDossier.ajouter(newDossier);
            } else {
                selectedDossier.setUtilisateurId(utilisateurId);
                selectedDossier.setDate(date);
                selectedDossier.setFichier(fichier);
                selectedDossier.setUnite(unite);
                selectedDossier.setMesure(mesure);
                serviceDossier.modifier(selectedDossier);
            }

            loadDossiers();
            formStage.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'enregistrement : " + e.getMessage());
        } catch (NumberFormatException | DateTimeParseException e) {
            showAlert("Erreur", "Veuillez vérifier les formats des champs (ex: utilisateur ID et mesure doivent être des nombres, date au format YYYY-MM-DD).");
        }
    }

    @FXML
    private void deleteDossier() {
        DossierMedical selected = dossierTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                serviceDossier.supprimer(selected.getId());
                loadDossiers();
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression : " + e.getMessage());
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un dossier à supprimer.");
        }
    }

    @FXML
    private void showDossierDetails() {
        DossierMedical selected = dossierTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DossierMedicalDetails.fxml"));
                Parent root = loader.load();
                DossierMedicalDetailsController controller = loader.getController();
                controller.setDossier(selected);

                Stage stage = new Stage();
                stage.setTitle("Détails du Dossier Médical");
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

    private void clearDossierFields() {
        utilisateurIdField.clear();
        dateField.clear();
        fichierField.clear();
        uniteField.clear();
        mesureField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}