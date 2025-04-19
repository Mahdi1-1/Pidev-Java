package Controllers.Evenement;

import entities.CategorieEv;
import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.CategorieEvService;
import service.EvenementService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterEvenement implements Initializable {

    @FXML private VBox rootVBox;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField lieuField;
    @FXML private Label imagePathLabel;
    @FXML private ComboBox<CategorieEv> categorieComboBox;
    @FXML private ComboBox<String> statutComboBox;

    private File selectedImageFile;
    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvService categorieEvService = new CategorieEvService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        setupDateValidation();
        setupStatutComboBox();
    }

    private void loadCategories() {
        try {
            categorieComboBox.getItems().clear();
            categorieComboBox.getItems().addAll(categorieEvService.afficher());

            categorieComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(CategorieEv categorie, boolean empty) {
                    super.updateItem(categorie, empty);
                    setText(empty || categorie == null ? null : categorie.getNom());
                }
            });

            categorieComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(CategorieEv categorie, boolean empty) {
                    super.updateItem(categorie, empty);
                    setText(empty || categorie == null ? null : categorie.getNom());
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    private void setupStatutComboBox() {
        statutComboBox.getItems().addAll("actif", "inactif", "annulé", "complet");
        statutComboBox.setValue("actif");
    }

    private void setupDateValidation() {
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebutPicker.getValue() != null && newVal.isBefore(dateDebutPicker.getValue())) {
                dateFinPicker.setValue(dateDebutPicker.getValue());
            }
        });
    }

//    @FXML
//    void chooseImage(ActionEvent event) {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Choisir une image");
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
//        );
//
//        File file = fileChooser.showOpenDialog(null);
//        if (file != null) {
//            copyImageToProjectDirectory(file);
//        }
//    }
//
//    private void copyImageToProjectDirectory(File file) {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Choisir une image");
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
//        );
//        File file1 = fileChooser.showOpenDialog(null); // Affiche le sélecteur de fichiers
//
//        if (file1 != null) {
//            selectedImageFile = file1;
//
//            // Construire le chemin compatible avec JavaFX (file:/)
//            String filePath = "file:" + file.getAbsolutePath().replace("\\", "/");
//            // Afficher le chemin dans le label
//            imagePathLabel.setText(filePath);
//
//            // Si tu veux, tu peux même afficher l'image directement dans un ImageView :
//            // Image image = new Image(filePath);
//            // imageView.setImage(image);
//        }
//    }
@FXML
void chooseImage(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choisir une image");
    fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
    );

    File file = fileChooser.showOpenDialog(null);

    if (file != null) {
        selectedImageFile = file;
        // Ajouter le préfixe "file:/"
        String filePath = "file:" + file.getAbsolutePath().replace("\\", "/"); // Remplacer les antislashs par des slashes
        imagePathLabel.setText(filePath); // Afficher le chemin avec file:/ pour l'utilisateur
    }
}

    @FXML
    void removeImage(ActionEvent event) {
        selectedImageFile = null;
        imagePathLabel.setText("Aucune image choisie");
    }

    @FXML
    void ajouterEvenement(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            Evenement evenement = new Evenement();
            evenement.setTitre(titreField.getText());
            evenement.setDescription(descriptionField.getText());
            evenement.setDateDebut(dateDebutPicker.getValue());
            evenement.setDateFin(dateFinPicker.getValue());
            evenement.setLieu(lieuField.getText());
            evenement.setStatut(statutComboBox.getValue());
            evenement.setImage(selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "");
            evenement.setCategorieId(categorieComboBox.getValue().getId());
            // Définir l'image avec le préfixe file:/ si une image est sélectionnée
            if (selectedImageFile != null) {
                String imagePath = "file:" + selectedImageFile.getAbsolutePath().replace("\\", "/");
                evenement.setImage(imagePath); // Utiliser le chemin formaté avec file:/
            } else {
                evenement.setImage("file:/default.png"); // Image par défaut
            }
            evenementService.ajouter(evenement);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'événement a été ajouté avec succès.");
            onBackClick(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de l'événement : " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (titreField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() ||
                dateDebutPicker.getValue() == null ||
                dateFinPicker.getValue() == null ||
                lieuField.getText().isEmpty() ||
                statutComboBox.getValue() == null ||
                categorieComboBox.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Dates invalides", "La date de fin doit être après la date de début");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventBack.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers la liste des événements.");
        }
    }

    @FXML
    void ajouter(ActionEvent actionEvent) {
        ajouterEvenement(actionEvent);
    }

    public void annuler(ActionEvent actionEvent) {
        onBackClick(actionEvent);
    }
}
