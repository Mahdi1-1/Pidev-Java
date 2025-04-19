package Controllers.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import service.EvenementService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherEvent extends SceneSwitch {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    @FXML
    private TextField searchField; // Champ de recherche

    @FXML
    private ComboBox<String> sortComboBox; // Menu déroulant pour le tri

    private final EvenementService service = new EvenementService();
    private List<Evenement> allEvents; // Liste complète des événements pour filtrage

    // Constantes de style pour éviter la duplication
    private static final String LABEL_STYLE = "-fx-font-size: 14px; -fx-text-fill: #333;";
    private static final String TITLE_LABEL_STYLE = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000;";
    private static final String BUTTON_STYLE = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 10px; -fx-font-size: 14px;";
    private static final String BUTTON_HOVER_STYLE = "-fx-background-color: #1976D2; -fx-text-fill: white;";

    @FXML
    public void initialize() {
        try {
            // Initialiser la liste complète des événements
            allEvents = service.recuperer();
            cardsContainer.getChildren().clear(); // Nettoyer le conteneur

            // Configurer le ComboBox pour le tri
            sortComboBox.getItems().addAll("Titre (A-Z)", "Titre (Z-A)", "Date de début (croissant)", "Date de début (décroissant)", "Statut");
            sortComboBox.setValue("Titre (A-Z)"); // Valeur par défaut
            sortComboBox.setOnAction(event -> updateEventDisplay());

            // Configurer la recherche dynamique
            searchField.textProperty().addListener((observable, oldValue, newValue) -> updateEventDisplay());

            // Afficher les événements initiaux
            updateEventDisplay();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEventDisplay() {
        try {
            cardsContainer.getChildren().clear(); // Nettoyer le conteneur

            // Filtrer les événements selon la recherche
            String searchText = searchField.getText().toLowerCase();
            List<Evenement> filteredEvents = allEvents.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(searchText) ||
                            e.getDescription().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            // Trier les événements selon l'option sélectionnée
            String sortOption = sortComboBox.getValue();
            filteredEvents.sort(getComparator(sortOption));

            // Afficher les événements filtrés et triés
            for (Evenement evenement : filteredEvents) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
                Node eventCard = loader.load();
                EvenementDetails controller = loader.getController();
                controller.setEvenement(evenement);
                cardsContainer.getChildren().add(eventCard);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Comparator<Evenement> getComparator(String sortOption) {
        switch (sortOption) {
            case "Titre (A-Z)":
                return Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER);
            case "Titre (Z-A)":
                return Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Date de début (croissant)":
                return Comparator.comparing(Evenement::getDateDebut);
            case "Date de début (décroissant)":
                return Comparator.comparing(Evenement::getDateDebut).reversed();
            case "Statut":
                return Comparator.comparing(Evenement::getStatut, String.CASE_INSENSITIVE_ORDER);
            default:
                return Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER);
        }
    }

    private boolean validateEvent(Evenement evenement) {
        // Vérification des champs essentiels
        if (evenement.getTitre() == null || evenement.getTitre().isEmpty()) {
            System.out.println("Titre manquant pour l'événement.");
            return false;
        }
        if (evenement.getDescription() == null || evenement.getDescription().isEmpty()) {
            System.out.println("Description manquante pour l'événement.");
            return false;
        }
        if (evenement.getDateDebut() == null || evenement.getDateFin() == null) {
            System.out.println("Dates manquantes pour l'événement.");
            return false;
        }
        if (evenement.getDateDebut().isAfter(evenement.getDateFin())) {
            System.out.println("La date de début ne peut pas être après la date de fin.");
            return false;
        }
        return true;
    }

    private HBox createEventBox(Evenement evenement) {
        HBox evenementBox = new HBox(15);
        evenementBox.setStyle("-fx-padding: 15px; -fx-background-color: #ffffff; -fx-border-radius: 12px; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 5, 5, 0);");

        VBox detailsBox = createDetailsBox(evenement);
        evenementBox.getChildren().add(detailsBox);

        return evenementBox;
    }

    private VBox createDetailsBox(Evenement evenement) {
        VBox detailsBox = new VBox(10);
        detailsBox.setStyle("-fx-pref-width: 300px; -fx-padding: 10px;");

        Label evenementLabel = new Label(evenement.getTitre());
        evenementLabel.setStyle(TITLE_LABEL_STYLE);

        Label descriptionLabel = new Label(evenement.getDescription());
        descriptionLabel.setStyle(LABEL_STYLE);

        Label lieuLabel = new Label("Lieu: " + evenement.getLieu());
        lieuLabel.setStyle(LABEL_STYLE);

        Label dateDebutLabel = new Label("Début: " + evenement.getDateDebut().toString());
        dateDebutLabel.setStyle(LABEL_STYLE);

        Label dateFinLabel = new Label("Fin: " + evenement.getDateFin().toString());
        dateFinLabel.setStyle(LABEL_STYLE);

        Label statutLabel = new Label("Statut: " + evenement.getStatut());
        statutLabel.setStyle(LABEL_STYLE);

        Label categorieLabel = new Label("Catégorie: " + evenement.getCategorieId());
        categorieLabel.setStyle(LABEL_STYLE);

        Button voirDetailsButton = new Button("Voir détails");
        voirDetailsButton.setStyle(BUTTON_STYLE);
        voirDetailsButton.setOnMouseEntered(e -> voirDetailsButton.setStyle(BUTTON_HOVER_STYLE));
        voirDetailsButton.setOnMouseExited(e -> voirDetailsButton.setStyle(BUTTON_STYLE));

        detailsBox.getChildren().addAll(
                evenementLabel,
                descriptionLabel,
                lieuLabel,
                dateDebutLabel,
                dateFinLabel,
                statutLabel,
                categorieLabel,
                voirDetailsButton
        );

        return detailsBox;
    }
}