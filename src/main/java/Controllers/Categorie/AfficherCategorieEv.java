package Controllers.Categorie;

import entities.CategorieEv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import service.CategorieEvService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCategorieEv extends SceneSwitch {

    @FXML private FlowPane cardsContainer;
    @FXML private VBox rootVBox;
    @FXML private ScrollPane scrollPane;

    private final CategorieEvService categorieEvService = new CategorieEvService();

    @FXML
    public void initialize() {
        loadCategoryCards();
    }

    private void loadCategoryCards() {
        cardsContainer.getChildren().clear();

        try {
            List<CategorieEv> categories = categorieEvService.afficher();

            for (CategorieEv categorieEv : categories) {
                VBox card = createCategoryCard(categorieEv);
                cardsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories: " + e.getMessage());
        }
    }

    private VBox createCategoryCard(CategorieEv categorieEv) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMinWidth(250);
        card.setMaxWidth(250);

        Label nameLabel = new Label(categorieEv.getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label(categorieEv.getDescription());
        descLabel.setWrapText(true);

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editBtn.setOnAction(e -> handleEdit(categorieEv));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDelete(categorieEv));

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(nameLabel, descLabel, buttonsBox);
        return card;
    }

    @FXML
    private void handleAdd(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCategorieEv.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Ajouter une catégorie");
        stage.setScene(new Scene(root));
        stage.showAndWait();

        // Rafraîchir la liste après ajout
        loadCategoryCards();
    }

    private void handleEdit(CategorieEv categorieEv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCategorieEv.fxml"));
            VBox modifierRoot = loader.load();

            Controllers.Categorie.ModifierCategorieEv controller = loader.getController();
            controller.setCategorieEv(categorieEv);

            Stage stage = new Stage();
            stage.setTitle("Modifier la catégorie");
            stage.setScene(new Scene(modifierRoot));
            stage.showAndWait();

            loadCategoryCards();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface de modification");
        }
    }

    private void handleDelete(CategorieEv categorieEv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + categorieEv.getNom() + "' ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categorieEvService.supprimer(categorieEv.getId());
                    cardsContainer.getChildren().removeIf(node -> {
                        if (node instanceof VBox card && card.getChildren().get(0) instanceof Label label) {
                            return label.getText().equals(categorieEv.getNom());
                        }
                        return false;
                    });
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onAddClick(ActionEvent actionEvent) {
        SceneSwitch.switchScene(rootVBox, "/AjouterCategorieEv.fxml");
    }
}
