package Controllers.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import service.CategorieEvService;
import service.EvenementService;
import utils.SceneSwitch;

import java.io.File;
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
    private Button uploadImageButton; // Nouveau bouton pour télécharger l'image

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
        System.out.println("Setting collection: " + evenement.getId() + " - " + evenement.getTitle());
        this.evenement = evenement;

        // Set collection details
        titreLabel.setText(evenement.getTitle());
        lieuLabel.setText(evenement.getLocation());
        dateDebutLabel.setText(evenement.getDateDebut().toString());
        dateFinLabel.setText(evenement.getDateFin().toString());


        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            try {
                String imagePath = evenement.getImage();
                // Si c'est un chemin local sans "file:/", on le convertit
                if (!imagePath.startsWith("file:") && !imagePath.startsWith("http")) {
                    imagePath = new File(imagePath).toURI().toString();
                }
                Image image = new Image(imagePath);
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }

    }

    private void updateUI() {
        titreLabel.setText(evenement.getTitre());
        descriptionLabel.setText(evenement.getDescription());
        lieuLabel.setText(evenement.getLieu());
        dateDebutLabel.setText(evenement.getDateDebut().toString());
        dateFinLabel.setText(evenement.getDateFin().toString());
        statutLabel.setText(evenement.getStatut());

        // Chargement de l'image de l'événement
        String imagePath = evenement.getImage(); // Récupération de l'image
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
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
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");
        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/AfficherEvent.fxml");
            System.out.println("Retour à la vue des événements réussi");
        } else {
            System.out.println("Impossible de trouver mainRouter pour la navigation");
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
