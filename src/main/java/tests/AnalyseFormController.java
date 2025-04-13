package tests;

import entities.Analyse;
import services.ServiceAnalyse;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class AnalyseFormController {
    @FXML private TextField dossierIdField;
    @FXML private TextField typeField;
    @FXML private DatePicker dateAnalysePicker;
    @FXML private TextField donneesAnalyseField;
    @FXML private TextField diagnosticField;

    private Stage stage;
    private Analyse analyse;
    private AnalyseListController listController;
    private ServiceAnalyse serviceAnalyse;

    public AnalyseFormController() {
        try {
            serviceAnalyse = new ServiceAnalyse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAnalyse(Analyse analyse) {
        this.analyse = analyse;
        if (analyse != null) {
            dossierIdField.setText(String.valueOf(analyse.getDossierId()));
            typeField.setText(analyse.getType());
            dateAnalysePicker.setValue(analyse.getDateAnalyse());
            donneesAnalyseField.setText(analyse.getDonneesAnalyse());
            diagnosticField.setText(analyse.getDiagnostic());
        }
    }

    public void setListController(AnalyseListController listController) {
        this.listController = listController;
    }

    @FXML
    private void saveAnalyse() {
        try {
            int dossierId = Integer.parseInt(dossierIdField.getText());
            String type = typeField.getText();
            LocalDate dateAnalyse = dateAnalysePicker.getValue();
            String donneesAnalyse = donneesAnalyseField.getText();
            String diagnostic = diagnosticField.getText();

            if (analyse == null) {
                analyse = new Analyse();
                analyse.setDossierId(dossierId);
                analyse.setType(type);
                analyse.setDateAnalyse(dateAnalyse);
                analyse.setDonneesAnalyse(donneesAnalyse);
                analyse.setDiagnostic(diagnostic);
                serviceAnalyse.ajouter(analyse);
            } else {
                analyse.setDossierId(dossierId);
                analyse.setType(type);
                analyse.setDateAnalyse(dateAnalyse);
                analyse.setDonneesAnalyse(donneesAnalyse);
                analyse.setDiagnostic(diagnostic);
                serviceAnalyse.modifier(analyse);
            }

            stage = (Stage) dossierIdField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'enregistrement : " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un ID de dossier valide.");
        }
    }

    @FXML
    private void cancel() {
        stage = (Stage) dossierIdField.getScene().getWindow();
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