package Controller;

import entities.Analyse;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AnalyseDetailsController {
    @FXML private Label dossierIdLabel;
    @FXML private Label typeLabel;
    @FXML private Label dateAnalyseLabel;
    @FXML private Label donneesAnalyseLabel;
    @FXML private Label diagnosticLabel;

    private Stage stage;

    public void setAnalyse(Analyse analyse) {
        dossierIdLabel.setText("Dossier ID: " + analyse.getDossierId());
        typeLabel.setText("Type: " + analyse.getType());
        dateAnalyseLabel.setText("Date Analyse: " + analyse.getDateAnalyse().toString());
        donneesAnalyseLabel.setText("Données Analyse: " + analyse.getDonneesAnalyse());
        diagnosticLabel.setText("Diagnostic: " + analyse.getDiagnostic());
    }

    @FXML
    private void closeDetails() {
        stage = (Stage) dossierIdLabel.getScene().getWindow();
        stage.close();
    }
}