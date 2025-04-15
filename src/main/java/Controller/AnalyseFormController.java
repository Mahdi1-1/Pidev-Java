package Controller;

import entities.Analyse;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.ServiceAnalyse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AnalyseFormController {

    @FXML private TextField dossierIdField;
    @FXML private TextField typeField;
    @FXML private TextField dateAnalyseField;
    @FXML private TextField donneesAnalyseField;
    @FXML private TextField diagnosticField;

    private Analyse analyse;
    private AnalyseListController listController;
    private ServiceAnalyse serviceAnalyse;

    public AnalyseFormController() throws SQLException {
        this.serviceAnalyse = new ServiceAnalyse();
    }

    @FXML
    public void initialize() {
        // Rien à faire ici pour le moment
        // La logique de pré-remplissage sera déplacée dans setAnalyse
    }

    public void setAnalyse(Analyse analyse) {
        this.analyse = analyse;
        // Pré-remplir les champs si on modifie une analyse existante
        if (analyse != null) {
            dossierIdField.setText(String.valueOf(analyse.getDossierId()));
            typeField.setText(analyse.getType());
            dateAnalyseField.setText(analyse.getDateAnalyse().toString());
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
            // Valider les champs
            int dossierId = Integer.parseInt(dossierIdField.getText().trim());
            String type = typeField.getText().trim();
            String dateStr = dateAnalyseField.getText().trim();
            String donnees = donneesAnalyseField.getText().trim();
            String diagnostic = diagnosticField.getText().trim();

            if (type.isEmpty() || dateStr.isEmpty() || donnees.isEmpty() || diagnostic.isEmpty()) {
                showAlert("Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Valider et parser la date
            LocalDate dateAnalyse;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                dateAnalyse = LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                showAlert("Erreur", "La date doit être au format YYYY-MM-DD.");
                return;
            }

            // Créer ou mettre à jour l'analyse
            if (analyse == null) {
                analyse = new Analyse();
            }
            analyse.setDossierId(dossierId);
            analyse.setType(type);
            analyse.setDateAnalyse(dateAnalyse);
            analyse.setDonneesAnalyse(donnees);
            analyse.setDiagnostic(diagnostic);

            if (analyse.getId() == 0) {
                serviceAnalyse.ajouter(analyse);
            } else {
                serviceAnalyse.modifier(analyse);
            }

            // Fermer la fenêtre et rafraîchir la liste
            Stage stage = (Stage) dossierIdField.getScene().getWindow();
            stage.close();

            if (listController != null) {
                listController.loadAnalyses();
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "L'ID du dossier doit être un nombre valide.");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}