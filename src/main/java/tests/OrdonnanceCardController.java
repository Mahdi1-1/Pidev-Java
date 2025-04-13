package tests;

import entities.Ordonnance;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.ServiceOrdonnance;
import java.time.format.DateTimeFormatter;

public class OrdonnanceCardController {
    @FXML private Label idLabel;
    @FXML private Label consultationIdLabel;
    @FXML private Label signatureLabel;
    @FXML private Label dateLabel;
    @FXML private Label patientLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button detailsButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Ordonnance ordonnance;
    private ServiceOrdonnance serviceOrdonnance;
    private Runnable onDelete;
    private Runnable onEdit;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        try {
            serviceOrdonnance = new ServiceOrdonnance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize button actions
        deleteButton.setOnAction(e -> handleDelete());
        editButton.setOnAction(e -> handleEdit());
        detailsButton.setOnAction(e -> handleDetails());
    }

    public void setData(Ordonnance ordonnance, Runnable onDelete, Runnable onEdit) {
        this.ordonnance = ordonnance;
        this.onDelete = onDelete;
        this.onEdit = onEdit;

        // Update UI with ordonnance data
        idLabel.setText("ID: " + ordonnance.getId());
        consultationIdLabel.setText("Consultation: " + ordonnance.getConsultation().getId());
        signatureLabel.setText(ordonnance.getSignature());
        dateLabel.setText(ordonnance.getConsultation().getDateC().format(DATE_FORMATTER));
        patientLabel.setText(ordonnance.getConsultation().getPatient().getNom() + " " + 
                           ordonnance.getConsultation().getPatient().getPrenom());
        descriptionTextArea.setText(ordonnance.getDescription());
    }

    private void handleDelete() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette ordonnance ?");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                serviceOrdonnance.supprimer(ordonnance.getId());
                if (onDelete != null) {
                    onDelete.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de la suppression de l'ordonnance.");
        }
    }

    private void handleEdit() {
        if (onEdit != null) {
            onEdit.run();
        }
    }

    private void handleDetails() {
        // Show ordonnance details in a dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'ordonnance");
        dialog.setHeaderText(null);

        // Create the content
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("ID: " + ordonnance.getId()),
            new Label("Consultation ID: " + ordonnance.getConsultation().getId()),
            new Label("Date: " + ordonnance.getConsultation().getDateC().format(DATE_FORMATTER)),
            new Label("Patient: " + ordonnance.getConsultation().getPatient().getNom() + " " + 
                     ordonnance.getConsultation().getPatient().getPrenom()),
            new Label("Médecin: Dr. " + ordonnance.getConsultation().getMedecin().getNom() + " " + 
                     ordonnance.getConsultation().getMedecin().getPrenom()),
            new Label("Signature: " + ordonnance.getSignature())
        );

        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea(ordonnance.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        content.getChildren().addAll(descLabel, descArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}