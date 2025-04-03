package tests;

import entities.Analyse;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AnalyseDetailsController {
    @FXML private Label idLabel;
    @FXML private Label dossierIdLabel;
    @FXML private Label typeLabel;
    @FXML private Label dateLabel;
    @FXML private Label donneesLabel;
    @FXML private Label diagnosticLabel;

    public void setAnalyse(Analyse analyse) {
        idLabel.setText("ID: " + analyse.getId());
        dossierIdLabel.setText("Dossier ID: " + analyse.getDossierId());
        typeLabel.setText("Type: " + analyse.getType());
        dateLabel.setText("Date: " + analyse.getDateAnalyse().toString());
        donneesLabel.setText("Données: " + analyse.getDonneesAnalyse());
        diagnosticLabel.setText("Diagnostic: " + analyse.getDiagnostic());
    }
}