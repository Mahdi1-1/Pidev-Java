package Controllers.Evenement;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.EvenementService;

import java.sql.SQLException;

public class ModifierEvenement {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField lieuField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private Button mapsButton;

    private Evenement evenement;
    private final EvenementService evenementService = new EvenementService();

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        titreField.setText(evenement.getTitre());
        descriptionField.setText(evenement.getDescription());
        dateDebutPicker.setValue(evenement.getDateDebut());
        dateFinPicker.setValue(evenement.getDateFin());

        // Parse lieu to extract location name and coordinates
        String lieu = evenement.getLieu();
        String lieuName = lieu;
        if (lieu.contains("(") && lieu.endsWith(")")) {
            int startIndex = lieu.lastIndexOf("(");
            lieuName = lieu.substring(0, startIndex).trim();
            String coords = lieu.substring(startIndex + 1, lieu.length() - 1);
            String[] latLon = coords.split(",");
            try {
                latitudeField.setText(latLon[0].trim());
                longitudeField.setText(latLon[1].trim());
            } catch (Exception e) {
                latitudeField.setText("0.0");
                longitudeField.setText("0.0");
            }
        } else {
            latitudeField.setText("0.0");
            longitudeField.setText("0.0");
        }
        lieuField.setText(lieuName);

        statutComboBox.setValue(evenement.getStatut());
        // Populate statutComboBox with options
        statutComboBox.getItems().addAll("actif", "inactif", "annulé", "complet");
    }

    @FXML
    void onMapsClick(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sélectionner un emplacement");
        dialog.setHeaderText("Entrez les coordonnées de l'emplacement");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField latInput = new TextField(latitudeField.getText());
        latInput.setPromptText("Latitude");
        TextField lonInput = new TextField(longitudeField.getText());
        lonInput.setPromptText("Longitude");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Latitude:"), latInput,
                new Label("Longitude:"), lonInput
        );
        dialogPane.setContent(content);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    double latitude = Double.parseDouble(latInput.getText());
                    double longitude = Double.parseDouble(lonInput.getText());

                    if (latitude < -90 || latitude > 90) {
                        showAlert(Alert.AlertType.WARNING, "Latitude invalide", "La latitude doit être entre -90 et 90.");
                        return;
                    }
                    if (longitude < -180 || longitude > 180) {
                        showAlert(Alert.AlertType.WARNING, "Longitude invalide", "La longitude doit être entre -180 et 180.");
                        return;
                    }

                    latitudeField.setText(String.valueOf(latitude));
                    longitudeField.setText(String.valueOf(longitude));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Entrée invalide", "Les valeurs de latitude et longitude doivent être des nombres valides.");
                }
            }
        });
    }

    @FXML
    void modifierEvenement(ActionEvent event) {
        try {
            if (!validateInputs()) {
                return;
            }

            evenement.setTitre(titreField.getText());
            evenement.setDescription(descriptionField.getText());
            evenement.setDateDebut(dateDebutPicker.getValue());
            evenement.setDateFin(dateFinPicker.getValue());

            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());
            String lieuWithCoords = String.format("%s (%.6f, %.6f)", lieuField.getText(), latitude, longitude);
            evenement.setLieu(lieuWithCoords);

            evenement.setStatut(statutComboBox.getValue());

            evenementService.modifier(evenement);

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification de l'événement : " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Entrée invalide", "Les valeurs de latitude et longitude doivent être des nombres valides.");
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        if (titreField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() ||
                dateDebutPicker.getValue() == null ||
                dateFinPicker.getValue() == null ||
                lieuField.getText().isEmpty() ||
                statutComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Dates invalides", "La date de fin doit être après la date de début");
            return false;
        }

        try {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());
            if (latitude < -90 || latitude > 90) {
                showAlert(Alert.AlertType.WARNING, "Latitude invalide", "La latitude doit être entre -90 et 90.");
                return false;
            }
            if (longitude < -180 || longitude > 180) {
                showAlert(Alert.AlertType.WARNING, "Longitude invalide", "La longitude doit être entre -180 et 180.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Entrée invalide", "Les valeurs de latitude et longitude doivent être des nombres valides.");
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
}