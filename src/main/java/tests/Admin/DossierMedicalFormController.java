package tests.Admin;

import entities.DossierMedical;
import services.ServiceDossierMedical;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class DossierMedicalFormController {
    @FXML private TextField utilisateurIdField;
    @FXML private DatePicker datePicker;
    @FXML private TextField fichierField;
    @FXML private TextField uniteField;
    @FXML private TextField mesureField;

    private Stage stage;
    private DossierMedical dossier;
    private DossierMedicalListAdminController listController;
    private ServiceDossierMedical serviceDossier;

    public DossierMedicalFormController() {
        try {
            serviceDossier = new ServiceDossierMedical();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDossier(DossierMedical dossier) {
        this.dossier = dossier;
        if (dossier != null) {
            utilisateurIdField.setText(String.valueOf(dossier.getUtilisateurId()));
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
    private void saveDossier() {
        try {
            int utilisateurId = Integer.parseInt(utilisateurIdField.getText());
            LocalDate date = datePicker.getValue();
            String fichier = fichierField.getText();
            String unite = uniteField.getText();
            double mesure = Double.parseDouble(mesureField.getText());

            if (dossier == null) {
                dossier = new DossierMedical();
                dossier.setUtilisateurId(utilisateurId);
                dossier.setDate(date);
                dossier.setFichier(fichier);
                dossier.setUnite(unite);
                dossier.setMesure(mesure);
                serviceDossier.ajouter(dossier);
            } else {
                dossier.setUtilisateurId(utilisateurId);
                dossier.setDate(date);
                dossier.setFichier(fichier);
                dossier.setUnite(unite);
                dossier.setMesure(mesure);
                serviceDossier.modifier(dossier);
            }

            stage = (Stage)utilisateurIdField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'enregistrement : " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides pour l'ID utilisateur et la mesure.");
        }
    }

    @FXML
    private void cancel() {
        stage = (Stage) utilisateurIdField.getScene().getWindow();
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