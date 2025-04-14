package Controller.Admin;

import entities.DossierMedical;
import entities.Utilisateur;
import services.ServiceDossierMedical;
import services.ServiceUtilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DossierMedicalFormController {

    @FXML private ChoiceBox<String> utilisateurChoiceBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField fichierField;
    @FXML private TextField uniteField;
    @FXML private TextField mesureField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage stage;
    private DossierMedical dossier;
    private DossierMedicalListAdminController listController;
    private ServiceDossierMedical serviceDossier;
    private ServiceUtilisateur serviceUtilisateur;
    private Map<String, Integer> emailToUserIdMap; // Map to store email-to-ID mapping

    public DossierMedicalFormController() {
        try {
            serviceDossier = new ServiceDossierMedical();
            serviceUtilisateur = new ServiceUtilisateur();
            emailToUserIdMap = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Populate the ChoiceBox with users who don't have a dossier medical
        try {
            List<Utilisateur> users = serviceUtilisateur.getUsersWithoutDossierMedical();
            for (Utilisateur user : users) {
                utilisateurChoiceBox.getItems().add(user.getEmail());
                emailToUserIdMap.put(user.getEmail(), user.getId());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    public void setDossier(DossierMedical dossier) {
        this.dossier = dossier;
        if (dossier != null) {
            try {
                // When editing, fetch the user associated with this dossier
                Utilisateur user = serviceUtilisateur.getById(dossier.getUtilisateurId());
                if (user != null) {
                    // Add the user's email to the ChoiceBox if it's not already there
                    if (!emailToUserIdMap.containsKey(user.getEmail())) {
                        utilisateurChoiceBox.getItems().add(user.getEmail());
                        emailToUserIdMap.put(user.getEmail(), user.getId());
                    }
                    utilisateurChoiceBox.setValue(user.getEmail());
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'utilisateur : " + e.getMessage());
            }
            datePicker.setValue(dossier.getDate());
            fichierField.setText(dossier.getFichier());
            uniteField.setText(dossier.getUnite());
            mesureField.setText(String.valueOf(dossier.getMesure()));
        }
    }

    public void setListController(DossierMedicalListAdminController listController) {
        this.listController = listController;
    }

    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Documents", "*.txt", "*.doc", "*.docx")
        );

        File selectedFile = fileChooser.showOpenDialog(fichierField.getScene().getWindow());
        if (selectedFile != null) {
            fichierField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void saveDossier() {
        try {
            // Validate input
            if (utilisateurChoiceBox.getValue() == null ||
                    datePicker.getValue() == null ||
                    fichierField.getText().trim().isEmpty() ||
                    uniteField.getText().trim().isEmpty() ||
                    mesureField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Get the user ID from the selected email
            String selectedEmail = utilisateurChoiceBox.getValue();
            int utilisateurId = emailToUserIdMap.get(selectedEmail);
            LocalDate date = datePicker.getValue();
            String fichier = fichierField.getText().trim();
            String unite = uniteField.getText().trim();
            double mesure = Double.parseDouble(mesureField.getText().trim());

            if (dossier == null) {
                dossier = new DossierMedical();
                dossier.setUtilisateurId(utilisateurId);
                dossier.setDate(date);
                dossier.setFichier(fichier);
                dossier.setUnite(unite);
                dossier.setMesure(mesure);
                serviceDossier.ajouter(dossier);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dossier médical ajouté avec succès !");
            } else {
                dossier.setUtilisateurId(utilisateurId);
                dossier.setDate(date);
                dossier.setFichier(fichier);
                dossier.setUnite(unite);
                dossier.setMesure(mesure);
                serviceDossier.modifier(dossier);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Dossier médical modifié avec succès !");
            }

            // Refresh the list in DossierMedicalListAdminController
            if (listController != null) {
                listController.refreshDossiers();
            }

            // Close the window
            stage = (Stage) fichierField.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement : " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs valides pour la mesure.");
        }
    }

    @FXML
    private void cancel() {
        stage = (Stage) fichierField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/MedicalStyle.css").toExternalForm());
        alert.showAndWait();
    }
}