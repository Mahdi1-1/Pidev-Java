package tests.Patient;

import entities.Question;
import enums.Specialite;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import services.QuestionService;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class EditQuestionController {

    // Constantes pour les contrôles de saisie
    private static final int TITLE_MIN_LENGTH = 10;
    private static final int TITLE_MAX_LENGTH = 100;
    private static final int CONTENT_MIN_LENGTH = 20;
    private static final int CONTENT_MAX_LENGTH = 1000;
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[\\w\\séèàçùêîôûäëïöüâãõñ'-]+$");

    @FXML private TextField titleField;
    @FXML private ComboBox<Specialite> specialiteCombo;
    @FXML private TextArea contentArea;
    @FXML private CheckBox visibleCheck;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private QuestionService questionService = new QuestionService();
    private Question currentQuestion;

    @FXML
    public void initialize() {
        specialiteCombo.getItems().setAll(Specialite.values());

        // Styles initiaux
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");

        // Contrôles de saisie dynamiques
        setupInputValidation();
    }

    private void setupInputValidation() {
        // Validation du titre
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > TITLE_MAX_LENGTH) {
                titleField.setText(oldVal);
            }
        });

        // Validation du contenu
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > CONTENT_MAX_LENGTH) {
                contentArea.setText(oldVal);
            }
        });
    }

    public void setQuestion(Question question) {
        this.currentQuestion = question;
        populateFields();
    }

    private void populateFields() {
        if (currentQuestion != null) {
            titleField.setText(currentQuestion.getTitre());
            contentArea.setText(currentQuestion.getContenu());
            specialiteCombo.getSelectionModel().select(currentQuestion.getSpecialite());
            visibleCheck.setSelected(currentQuestion.isVisible());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            updateQuestionFromFields();
            questionService.modifier(currentQuestion);
            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        Specialite specialite = specialiteCombo.getValue();

        // Validation du titre
        if (title.isEmpty()) {
            showAlert("Erreur", "Champ manquant", "Le titre ne peut pas être vide", Alert.AlertType.ERROR);
            titleField.requestFocus();
            return false;
        }

        if (title.length() < TITLE_MIN_LENGTH) {
            showAlert("Erreur", "Titre trop court",
                    "Le titre doit contenir au moins " + TITLE_MIN_LENGTH + " caractères",
                    Alert.AlertType.ERROR);
            titleField.requestFocus();
            return false;
        }

        if (!TITLE_PATTERN.matcher(title).matches()) {
            showAlert("Erreur", "Caractères invalides",
                    "Le titre contient des caractères non autorisés",
                    Alert.AlertType.ERROR);
            titleField.requestFocus();
            return false;
        }

        // Validation du contenu
        if (content.isEmpty()) {
            showAlert("Erreur", "Champ manquant", "Le contenu ne peut pas être vide", Alert.AlertType.ERROR);
            contentArea.requestFocus();
            return false;
        }

        if (content.length() < CONTENT_MIN_LENGTH) {
            showAlert("Erreur", "Contenu trop court",
                    "Le contenu doit contenir au moins " + CONTENT_MIN_LENGTH + " caractères",
                    Alert.AlertType.ERROR);
            contentArea.requestFocus();
            return false;
        }

        // Validation de la spécialité
        if (specialite == null) {
            showAlert("Erreur", "Champ manquant", "Veuillez sélectionner une spécialité", Alert.AlertType.ERROR);
            specialiteCombo.requestFocus();
            return false;
        }

        return true;
    }

    private void updateQuestionFromFields() {
        currentQuestion.setTitre(titleField.getText().trim());
        currentQuestion.setContenu(contentArea.getText().trim());
        currentQuestion.setSpecialite(specialiteCombo.getValue());
        currentQuestion.setVisible(visibleCheck.isSelected());
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        titleField.getScene().getWindow().hide();
    }

    // Méthodes pour les effets de survol
    @FXML
    private void onCancelHover(MouseEvent event) {
        cancelButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
    }

    @FXML
    private void onCancelExit(MouseEvent event) {
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
    }

    @FXML
    private void onSaveHover(MouseEvent event) {
        saveButton.setStyle("-fx-background-color: #219150; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
    }

    @FXML
    private void onSaveExit(MouseEvent event) {
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}