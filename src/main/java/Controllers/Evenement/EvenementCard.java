package Controllers.Evenement;

import service.CategorieEvService;  // Incorrectpackage Controllers.Evenement;

import entities.CategorieEv;
import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import service.CategorieEvService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;

public class EvenementCard extends VBox {

    @FXML
    private Label dateDebutLabel;

    @FXML
    private Label dateFinLabel;

    @FXML
    private Label lieuLabel;

    @FXML
    private Label statutLabel;

    @FXML
    private Label categorieLabel;

    @FXML
    private ImageView eventImage;

    @FXML
    private Label titreLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Button voirDetailsButton;

    private Evenement evenement;
    private final CategorieEvService categorieService = new CategorieEvService();

    public EvenementCard() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/EvenementCard.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        titreLabel.setText(evenement.getTitre());
        descriptionLabel.setText(evenement.getDescription());
        lieuLabel.setText("Lieu: " + evenement.getLieu());
        dateDebutLabel.setText("Début: " + evenement.getDateDebut().toString());
        dateFinLabel.setText("Fin: " + evenement.getDateFin().toString());
        statutLabel.setText("Statut: " + evenement.getStatut());

        // Récupérer et afficher le nom de la catégorie
        try {
            System.out.println("Récupération de la catégorie avec l'ID: " + evenement.getCategorieId());
            CategorieEv categorie = categorieService.getById(evenement.getCategorieId());
            if (categorie != null) {
                System.out.println("Catégorie trouvée: " + categorie.getNom());
                categorieLabel.setText("Catégorie: " + categorie.getNom());
            } else {
                System.out.println("Aucune catégorie trouvée pour l'ID: " + evenement.getCategorieId());
                categorieLabel.setText("Catégorie: Non définie");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la catégorie: " + e.getMessage());
            e.printStackTrace();
            categorieLabel.setText("Catégorie: Erreur");
        }

        // Chargement sécurisé de l’image
        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            try {
                Image image = new Image("file:" + evenement.getImage());
                eventImage.setImage(image);
            } catch (Exception e) {
                System.out.println("Image loading failed: " + e.getMessage());
                eventImage.setImage(new Image("/images/default-event.jpg"));
            }
        } else {
            eventImage.setImage(new Image("/images/default-event.jpg"));
        }
    }

    @FXML
    private void handleVoirDetails() {
        SceneSwitch.switchScene((Pane) this.getParent(), "/EvenementDetails.fxml");
    }

    public void setData(Evenement evenement) {
        this.titreLabel.setText(evenement.getTitre()); // Afficher le titre de l'événement
        this.dateDebutLabel.setText("Début: " + evenement.getDateDebut().toString()); // Afficher la date de début
        this.dateFinLabel.setText("Fin: " + evenement.getDateFin().toString()); // Afficher la date de fin
        this.lieuLabel.setText("Lieu: " + evenement.getLieu()); // Afficher le lieu
        this.statutLabel.setText("Statut: " + evenement.getStatut()); // Afficher le statut

        // Récupérer et afficher le nom de la catégorie
        try {
            CategorieEv categorie = categorieService.getById(evenement.getCategorieId());
            if (categorie != null) {
                this.categorieLabel.setText("Catégorie: " + categorie.getNom());
            } else {
                this.categorieLabel.setText("Catégorie: Non définie");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la catégorie: " + e.getMessage());
            this.categorieLabel.setText("Catégorie: Erreur");
        }

        // Si l'image n'est pas vide ou nulle, l'afficher
        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            try {
                Image image = new Image(evenement.getImage()); // Charger l'image depuis l'URL ou le chemin
                this.eventImage.setImage(image); // Mettre l'image dans l'interface
            } catch (Exception e) {
                System.out.println("Image loading failed: " + e.getMessage());
                this.eventImage.setImage(new Image("/images/default-event.jpg"));
            }
        } else {
            this.eventImage.setImage(new Image("/images/default-event.jpg")); // Image par défaut
        }
    }
}
