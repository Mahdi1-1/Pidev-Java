package Controllers.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import service.EvenementService;

public class AfficherEvenement{
    @FXML
    private Label titreLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label lieuLabel;
    @FXML
    private Button voirDetailsButton;
    @FXML
    private Button participerButton;

    private Evenement evenement;
    private EvenementService evenementService;
    private NotificationService notificationService;

    public void initialize() {
        evenementService = new EvenementService();
        notificationService = new NotificationService();
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        afficherEvenement();
    }

    private void afficherEvenement() {
        titreLabel.setText(evenement.getTitre());
        descriptionLabel.setText(evenement.getDescription());
        dateLabel.setText(evenement.getDateDebut().toString() + " - " + evenement.getDateFin().toString());
        lieuLabel.setText(evenement.getLieu());
    }

    @FXML
    private void handleVoirDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsEvenement.fxml"));
            Parent root = loader.load();
            
            DetailsEvenementController controller = loader.getController();
            controller.setEvenement(evenement);
            
            Stage stage = new Stage();
            stage.setTitle("Détails de l'événement");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleParticiper() {
        try {
            // Créer une notification de participation
            String titre = "Demande de participation - " + evenement.getTitre();
            String message = "Je souhaite participer à l'événement : " + evenement.getTitre();
            notificationService.creerNotification(titre, message, "participation");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Demande envoyée");
            alert.setHeaderText(null);
            alert.setContentText("Votre demande de participation a été envoyée à l'administrateur.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        Stage stage = (Stage) titreLabel.getScene().getWindow();
        stage.close();
    }
} 