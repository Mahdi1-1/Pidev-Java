package tests.Admin;

import entities.Ordonnance;
import services.ServiceOrdonnance;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class OrdonnanceDetailsAdminController {
    @FXML private Label idLabel;
    @FXML private Label consultationIdLabel;
    @FXML private Label consultationDateLabel;
    @FXML private Label patientLabel;
    @FXML private Label medecinLabel;
    @FXML private Label signatureLabel;
    @FXML private TextArea descriptionTextArea;
    
    private Stage stage;
    private Ordonnance ordonnance;
    
    public void setOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        
        // Vérification des labels
        if (idLabel == null || consultationIdLabel == null || consultationDateLabel == null ||
                patientLabel == null || medecinLabel == null || signatureLabel == null ||
                descriptionTextArea == null) {
            showAlert("Erreur FXML", "Un ou plusieurs éléments n'ont pas été correctement injectés depuis le FXML.");
            return;
        }
        
        // Remplir les informations de l'ordonnance
        idLabel.setText("ID: " + ordonnance.getId());
        consultationIdLabel.setText("Consultation ID: " + ordonnance.getConsultation().getId());
        consultationDateLabel.setText("Date de consultation: " + ordonnance.getConsultation().getDateC().toString().replace("T", " "));
        patientLabel.setText("Patient: " + ordonnance.getConsultation().getPatient().getNom() + " " + ordonnance.getConsultation().getPatient().getPrenom());
        medecinLabel.setText("Médecin: Dr. " + ordonnance.getConsultation().getMedecin().getNom() + " " + ordonnance.getConsultation().getMedecin().getPrenom());
        signatureLabel.setText("Signature: " + ordonnance.getSignature());
        descriptionTextArea.setText(ordonnance.getDescription());
    }
    
    @FXML
    private void showConsultation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/ConsultationDetailsAdmin.fxml"));
            Parent root = loader.load();
            
            ConsultationDetailsAdminController controller = loader.getController();
            controller.setConsultation(ordonnance.getConsultation());
            
            Stage stage = new Stage();
            stage.setTitle("Détails de la Consultation");
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(true);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails de la consultation : " + e.getMessage());
        }
    }
    
    @FXML
    private void editOrdonnance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormOrdonnance.fxml"));
            Parent root = loader.load();
            
            FormOrdonnanceController controller = loader.getController();
            controller.setOrdonnance(ordonnance);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier l'Ordonnance");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Rafraîchir les informations après la modification
            refreshOrdonnance();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage());
        }
    }
    
    private void refreshOrdonnance() {
        try {
            ServiceOrdonnance serviceOrdonnance = new ServiceOrdonnance();
            ordonnance = serviceOrdonnance.getById(ordonnance.getId());
            setOrdonnance(ordonnance);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du rafraîchissement des données : " + e.getMessage());
        }
    }
    
    @FXML
    private void deleteOrdonnance() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette ordonnance ?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ServiceOrdonnance serviceOrdonnance = new ServiceOrdonnance();
                    serviceOrdonnance.supprimer(ordonnance.getId());
                    closeDetails();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void closeDetails() {
        stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}