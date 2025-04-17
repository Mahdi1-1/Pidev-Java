package Controller;

import entities.Analyse;
import entities.DossierMedical;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ServiceAnalyse;
import services.ServiceDossierMedical;
import utils.UtilisateurStatique;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

public class AnalyseFormController {

    @FXML private ChoiceBox<String> typeChoiceBox; // Remplacé TextField par ChoiceBox
    @FXML private DatePicker dateAnalyseField; // Déjà un DatePicker
    @FXML private TextField donneesAnalyseField;
    @FXML private TextField diagnosticField;

    private Analyse analyse;
    private AnalyseListController listController;
    private ServiceAnalyse serviceAnalyse;
    private ServiceDossierMedical serviceDossierMedical;
    private Integer dossierId;

    public AnalyseFormController() throws SQLException {
        this.serviceAnalyse = new ServiceAnalyse();
        this.serviceDossierMedical = new ServiceDossierMedical();
    }

    @FXML
    public void initialize() {
        // Initialiser la ChoiceBox avec une liste de types d'analyses
        typeChoiceBox.setItems(FXCollections.observableArrayList(
                "Analyse de sang",
                "Analyse d'urine",
                "Radiographie",
                "Échographie",
                "Test de glycémie"
        ));
        typeChoiceBox.setValue("Analyse de sang"); // Valeur par défaut

        // Récupérer automatiquement le dossierId à partir de l'utilisateur statique
        try {
            int utilisateurId = UtilisateurStatique.getUtilisateurId();
            if (utilisateurId == -1) {
                showAlert("Erreur", "Aucun utilisateur connecté défini.");
                return;
            }
            DossierMedical dossier = serviceDossierMedical.getByUtilisateurId(utilisateurId);
            if (dossier != null) {
                dossierId = dossier.getId();
            } else {
                showAlert("Erreur", "Aucun dossier médical trouvé pour l'utilisateur connecté.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la récupération du dossier médical : " + e.getMessage());
        }
    }


    public void setAnalyse(Analyse analyse) {
        this.analyse = analyse;
        if (analyse != null) {
            typeChoiceBox.setValue(analyse.getType());
            dateAnalyseField.setValue(analyse.getDateAnalyse());
            donneesAnalyseField.setText(analyse.getDonneesAnalyse());
            diagnosticField.setText(analyse.getDiagnostic());
            dossierId = analyse.getDossierId();
        }
    }

    public void setListController(AnalyseListController listController) {
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

        File selectedFile = fileChooser.showOpenDialog(donneesAnalyseField.getScene().getWindow());
        if (selectedFile != null) {
            donneesAnalyseField.setText(selectedFile.getAbsolutePath());
        }
    }
    @FXML
    private void saveAnalyse() {
        try {
            // Valider les champs
            String type = typeChoiceBox.getValue(); // Récupérer la valeur sélectionnée
            LocalDate dateAnalyse = dateAnalyseField.getValue(); // Récupérer directement la date
            String donnees = donneesAnalyseField.getText().trim();
            String diagnostic = diagnosticField.getText().trim();

            if (type == null || dateAnalyse == null || donnees.isEmpty() || diagnostic.isEmpty()) {
                showAlert("Erreur", "Tous les champs doivent être remplis.");
                return;
            }

            // Vérifier que dossierId a été récupéré
            if (dossierId == null) {
                showAlert("Erreur", "Impossible de déterminer le dossier médical associé.");
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
            Stage stage = (Stage) typeChoiceBox.getScene().getWindow();
            stage.close();

            if (listController != null) {
                listController.loadAnalyses();
            }
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