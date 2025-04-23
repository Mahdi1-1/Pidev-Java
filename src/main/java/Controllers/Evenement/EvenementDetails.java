package Controllers.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.CategorieEvService;
import service.EvenementService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EvenementDetails implements javafx.fxml.Initializable {
    @FXML
    private Label titreLabel;

    @FXML
    private Label dateDebutLabel;

    @FXML
    private Label dateFinLabel;

    @FXML
    private Label lieuLabel;

    @FXML
    private Label statutLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private Button backButton;

    @FXML
    private Button uploadImageButton;

    @FXML
    private FlowPane categorieContainer;

    @FXML
    private Label descriptionLabel;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvService categorieEvService = new CategorieEvService();

    private Evenement evenement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialisation du contrôleur EvenementDetails");
        categorieContainer.setHgap(15);
        categorieContainer.setVgap(15);
        categorieContainer.setPrefWrapLength(700);
    }

    public void setEvenement(Evenement evenement) {
        System.out.println("Setting evenement: " + evenement.getId() + " - " + evenement.getTitre());
        this.evenement = evenement;

        // Mettre à jour tous les champs de l'UI
        titreLabel.setText(evenement.getTitre());
        descriptionLabel.setText(evenement.getDescription());
        lieuLabel.setText(evenement.getLieu());
        dateDebutLabel.setText(evenement.getDateDebut().toString());
        dateFinLabel.setText(evenement.getDateFin().toString());
        statutLabel.setText(evenement.getStatut());

        // Chargement de l'image de l'événement
        String imagePath = evenement.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Nettoyer le chemin de l'image pour enlever le préfixe "file:" s'il existe
                if (imagePath.startsWith("file:")) {
                    imagePath = imagePath.replace("file:", "");
                }

                Image image;
                if (imagePath.startsWith("http")) {
                    // Gestion des images basées sur une URL
                    image = new Image(imagePath);
                } else {
                    // Gestion des chemins locaux de fichiers
                    image = new Image(new File(imagePath).toURI().toString());
                }
                if (image.isError()) {
                    System.out.println("Erreur de chargement de l'image: " + image.getException());
                    loadDefaultImage();
                } else {
                    imageView.setImage(image);
                    System.out.println("Image chargée avec succès: " + imagePath);
                }
            } catch (Exception e) {
                System.out.println("Erreur de chargement de l'image: " + e.getMessage());
                loadDefaultImage();
            }
        } else {
            System.out.println("Aucune image disponible pour l'événement");
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            System.out.println("Chargement de l'image par défaut");
            Image defaultImage = new Image(getClass().getResourceAsStream("/assets/images/default-event.jpg"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("Erreur de chargement de l'image par défaut: " + e.getMessage());
            imageView.setImage(null);
        }
    }

    @FXML
    private void onBackClick() {
        try {
            // Charger la vue CalendarView.fxml (ou AfficherEvent.fxml selon ton besoin)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CalendarView.fxml"));
            Parent calendarView = loader.load();

            // Obtenir la scène actuelle et passer à la vue précédente
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(calendarView);
            stage.setScene(scene);
            stage.setTitle("Calendrier des événements");
            stage.show();

            // Réinitialiser la liste des événements dans CalendarViewController
            CalendarViewController controller = loader.getController();
            controller.setEventList(evenementService.recuperer());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la vue précédente.");
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de récupérer les événements.");
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image pour l'événement");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Enregistrement du chemin absolu du fichier
                String newImagePath = selectedFile.getAbsolutePath();
                evenement.setImage(newImagePath);

                // Mise à jour dans la base de données
                evenementService.modifier(evenement);

                // Mise à jour de l'image dans l'UI
                Image newImage = new Image(selectedFile.toURI().toString());
                imageView.setImage(newImage);
                System.out.println("Image chargée avec succès: " + newImagePath);
            } catch (SQLException e) {
                System.out.println("Erreur lors de la mise à jour de l'image dans la base de données: " + e.getMessage());
                showErrorAlert("Erreur", "Impossible de sauvegarder l'image dans la base de données.");
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de l'image: " + e.getMessage());
                showErrorAlert("Erreur", "Impossible de charger l'image sélectionnée.");
            }
        }
    }

    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}