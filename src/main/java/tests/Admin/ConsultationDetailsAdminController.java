package tests.Admin;

import entities.Consultation;
import entities.Ordonnance;
import services.ServiceConsultation;
import services.ServiceOrdonnance;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ConsultationDetailsAdminController {
    @FXML private Label idLabel;
    @FXML private Label typeLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private Label medecinLabel;
    @FXML private Label patientLabel;
    @FXML private TextArea commentaireTextArea;
    @FXML private Label meetLinkLabel;
    @FXML private Label ordonnanceLabel;
    
    private Stage stage;
    private Consultation consultation;
    
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        
        // Vérification des labels
        if (idLabel == null || typeLabel == null || statusLabel == null ||
                dateLabel == null || medecinLabel == null || patientLabel == null ||
                commentaireTextArea == null || meetLinkLabel == null || ordonnanceLabel == null) {
            showAlert("Erreur FXML", "Un ou plusieurs éléments n'ont pas été correctement injectés depuis le FXML.");
            return;
        }
        
        // Remplir les informations de la consultation
        idLabel.setText("ID: " + consultation.getId());
        typeLabel.setText("Type: " + consultation.getType());
        statusLabel.setText("Statut: " + consultation.getStatus());
        dateLabel.setText("Date: " + consultation.getDateC().toString().replace("T", " "));
        medecinLabel.setText("Médecin: Dr. " + consultation.getMedecin().getNom() + " " + consultation.getMedecin().getPrenom());
        patientLabel.setText("Patient: " + consultation.getPatient().getNom() + " " + consultation.getPatient().getPrenom());
        commentaireTextArea.setText(consultation.getCommentaire());
        meetLinkLabel.setText("Lien de réunion: " + (consultation.getMeetLink() != null ? consultation.getMeetLink() : "Non défini"));
        
        // Vérifier si une ordonnance est associée
        if (consultation.getOrdonnance() != null) {
            ordonnanceLabel.setText("Ordonnance: Disponible");
        } else {
            ordonnanceLabel.setText("Ordonnance: Non disponible");
        }
    }
    
    @FXML
    private void showOrdonnance() {
        try {
            if (consultation.getOrdonnance() != null) {
                // Afficher l'ordonnance existante
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/OrdonnanceDetailsAdmin.fxml"));
                Parent root = loader.load();
                
                OrdonnanceDetailsAdminController controller = loader.getController();
                controller.setOrdonnance(consultation.getOrdonnance());
                
                Stage stage = new Stage();
                stage.setTitle("Détails de l'Ordonnance");
                stage.setScene(new Scene(root, 500, 400));
                stage.setResizable(true);
                stage.showAndWait();
            } else {
                // Proposer de créer une nouvelle ordonnance
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Ordonnance non disponible");
                confirmDialog.setHeaderText(null);
                confirmDialog.setContentText("Aucune ordonnance n'est associée à cette consultation. Voulez-vous en créer une ?");
                
                confirmDialog.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        createOrdonnance();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails de l'ordonnance : " + e.getMessage());
        }
    }
    
    private void createOrdonnance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormOrdonnance.fxml"));
            Parent root = loader.load();
            
            FormOrdonnanceController controller = loader.getController();
            controller.setConsultation(consultation);
            
            Stage stage = new Stage();
            stage.setTitle("Créer une Ordonnance");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Rafraîchir les informations après la création
            refreshConsultation();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ordonnance : " + e.getMessage());
        }
    }
    
    private void refreshConsultation() {
        try {
            ServiceConsultation serviceConsultation = new ServiceConsultation();
            consultation = serviceConsultation.getById(consultation.getId());
            setConsultation(consultation);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du rafraîchissement des données : " + e.getMessage());
        }
    }
    
    @FXML
    private void editConsultation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormConsultation.fxml"));
            Parent root = loader.load();
            
            FormConsultationController controller = loader.getController();
            controller.setConsultation(consultation);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier la Consultation");
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(false);
            stage.showAndWait();
            
            refreshConsultation();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage());
        }
    }
    
    @FXML
    private void deleteConsultation() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette consultation ?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ServiceConsultation serviceConsultation = new ServiceConsultation();
                    serviceConsultation.supprimer(consultation.getId());
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