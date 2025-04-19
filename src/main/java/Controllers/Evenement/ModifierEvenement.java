package Controllers.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.EvenementService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierEvenement implements Initializable {
    @FXML
    private TextField titreField;  // Renommé de titleField

    @FXML
    private DatePicker dateDebutPicker;  // Renommé de startingDatePicker

    @FXML
    private DatePicker dateFinPicker;  // Renommé de endingDatePicker

    @FXML
    private TextField lieuField;

    @FXML
    private TextField statutField;

    @FXML
    private TextField imageField;

    @FXML
    private ComboBox<Integer> categorieComboBox;  // Nouveau ComboBox pour la catégorie

    private Evenement evenementAModifier;
    private final EvenementService evenementService = new EvenementService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation si nécessaire
    }

    public void setEvenement(Evenement evenement) {
        this.evenementAModifier = evenement;
        remplirChamps();
    }

    private void remplirChamps() {
        titreField.setText(evenementAModifier.getTitre());
        dateDebutPicker.setValue(evenementAModifier.getDateDebut());
        dateFinPicker.setValue(evenementAModifier.getDateFin());
        lieuField.setText(evenementAModifier.getLieu());
        statutField.setText(evenementAModifier.getStatut());
        imageField.setText(evenementAModifier.getImage());
        // Ici vous devrez initialiser le ComboBox des catégories
        // categorieComboBox.setValue(evenementAModifier.getCategorieId());
    }

    @FXML
    void sauvegarderModifications() {
        evenementAModifier.setTitre(titreField.getText());
        evenementAModifier.setDateDebut(dateDebutPicker.getValue());
        evenementAModifier.setDateFin(dateFinPicker.getValue());
        evenementAModifier.setLieu(lieuField.getText());
        evenementAModifier.setStatut(statutField.getText());
        evenementAModifier.setImage(imageField.getText());
        // evenementAModifier.setCategorieId(categorieComboBox.getValue());

        try {
            evenementService.modifier(evenementAModifier);
            fermerFenetre();
        } catch (SQLException e) {
            afficherErreur("Erreur lors de la modification", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void annulerModification() {  // Renommé de cancelEdit
        fermerFenetre();
    }

    private void fermerFenetre() {  // Renommé de closeWindow
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}