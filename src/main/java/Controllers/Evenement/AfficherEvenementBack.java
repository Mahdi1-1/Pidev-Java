package Controllers.Evenement;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.EvenementService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherEvenementBack extends SceneSwitch {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    private final EvenementService service = new EvenementService();

    @FXML
    public void initialize() {
        loadEvenementCards();
    }

    /**
     * Charge tous les événements depuis la base de données et les affiche dans le FlowPane.
     */
    @FXML
    public void loadEvenementCards() {
        cardsContainer.getChildren().clear();

        try {
            List<Evenement> evenements = service.recuperer();

            for (Evenement evenement : evenements) {
                cardsContainer.getChildren().add(createEvenementCard(evenement));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des événements : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée une carte (VBox) visuelle pour un événement donné.
     */
    private VBox createEvenementCard(Evenement evenement) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMinWidth(250);
        card.setMaxWidth(250);

        // Titre de l'événement
        Label titleLabel = new Label(evenement.getTitre());
        titleLabel.setStyle("-fx-text-fill: #555;");

        // Lieu (ajuste selon tes données)
        Label lieuLabel = new Label("Lieu: " + evenement.getLieu());
        lieuLabel.setStyle("-fx-text-fill: #555;");

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button modifyButton = new Button("Modifier");
        modifyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        modifyButton.setOnAction(e -> modifyEvenement(evenement));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteEvenement(evenement));

        buttonsBox.getChildren().addAll(modifyButton, deleteButton);

        // Ajout à la carte
        card.getChildren().addAll(titleLabel, lieuLabel, buttonsBox);

        return card;
    }

    /**
     * Ouvre une nouvelle scène pour ajouter un événement.
     */
    @FXML
    void onAddClick(ActionEvent event) throws IOException {
        switchScene(rootVBox, "/AjouterEvent.fxml");
        loadEvenementCards();
    }

    /**
     * Ouvre une nouvelle fenêtre pour modifier un événement.
     */
    private void modifyEvenement(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
            VBox modifierRoot = loader.load();

            ModifierEvenement controller = loader.getController();
            controller.setEvenement(evenement);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Événement");
            stage.setScene(new javafx.scene.Scene(modifierRoot));
            stage.showAndWait();

            loadEvenementCards();
        } catch (IOException e) {
            System.err.println("Erreur lors de la modification de l'événement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supprime l’événement sélectionné.
     */
    private void deleteEvenement(Evenement evenement) {
        try {
            service.supprimer(evenement.getId());
            loadEvenementCards();
            System.out.println("Événement supprimé : " + evenement.getTitre());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'événement : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
