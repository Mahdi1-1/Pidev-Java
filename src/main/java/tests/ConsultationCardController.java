package tests;

import entities.Consultation;
import entities.TypeConsultation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.ServiceConsultation;
import java.time.format.DateTimeFormatter;

public class ConsultationCardController {
    @FXML private Label idLabel;
    @FXML private Label statusLabel;
    @FXML private Label typeLabel;
    @FXML private Label dateLabel;
    @FXML private Label medecinLabel;
    @FXML private Label patientLabel;
    @FXML private TextArea commentaireTextArea;
    @FXML private Button detailsButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Consultation consultation;
    private ServiceConsultation serviceConsultation;
    private Runnable onDelete;
    private Runnable onEdit;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void initialize() {
        try {
            serviceConsultation = new ServiceConsultation();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize button actions
        deleteButton.setOnAction(e -> handleDelete());
        editButton.setOnAction(e -> handleEdit());
        detailsButton.setOnAction(e -> handleDetails());
    }

    public void setData(Consultation consultation, Runnable onDelete, Runnable onEdit) {
        this.consultation = consultation;
        this.onDelete = onDelete;
        this.onEdit = onEdit;

        // Update UI with consultation data
        idLabel.setText("ID: " + consultation.getId());
        statusLabel.setText(consultation.getStatus());
        typeLabel.setText(consultation.getType().getDisplayName());
        dateLabel.setText(consultation.getDateC().format(DATE_FORMATTER));
        medecinLabel.setText("Dr. " + consultation.getMedecin().getNom() + " " + consultation.getMedecin().getPrenom());
        patientLabel.setText(consultation.getPatient().getNom() + " " + consultation.getPatient().getPrenom());
        commentaireTextArea.setText(consultation.getCommentaire());

        // Update status label style
        updateStatusStyle();

        // Disable edit and delete buttons for completed consultations
        boolean isCompleted = Consultation.STATUS_COMPLETED.equals(consultation.getStatus());
        editButton.setDisable(isCompleted);
        deleteButton.setDisable(isCompleted);
    }

    private void updateStatusStyle() {
        // Remove all existing status styles
        statusLabel.getStyleClass().removeAll(
            "status-pending",
            "status-confirmed",
            "status-completed",
            "status-cancelled"
        );

        // Add appropriate style class based on status
        switch (consultation.getStatus()) {
            case Consultation.STATUS_PENDING:
                statusLabel.getStyleClass().add("status-pending");
                break;
            case Consultation.STATUS_CONFIRMED:
                statusLabel.getStyleClass().add("status-confirmed");
                break;
            case Consultation.STATUS_COMPLETED:
                statusLabel.getStyleClass().add("status-completed");
                break;
            case Consultation.STATUS_CANCELLED:
                statusLabel.getStyleClass().add("status-cancelled");
                break;
        }
    }

    private void handleDelete() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette consultation ?");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                serviceConsultation.supprimer(consultation.getId());
                if (onDelete != null) {
                    onDelete.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Une erreur est survenue lors de la suppression de la consultation.");
            errorAlert.showAndWait();
        }
    }

    private void handleEdit() {
        if (onEdit != null) {
            onEdit.run();
        }
    }

    private void handleDetails() {
        // Show consultation details in a dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la consultation");
        dialog.setHeaderText(null);

        // Create the content
        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("ID: " + consultation.getId()),
            new Label("Type: " + consultation.getType().getDisplayName()),
            new Label("Status: " + consultation.getStatus()),
            new Label("Date: " + consultation.getDateC().format(DATE_FORMATTER)),
            new Label("Médecin: Dr. " + consultation.getMedecin().getNom() + " " + consultation.getMedecin().getPrenom()),
            new Label("Patient: " + consultation.getPatient().getNom() + " " + consultation.getPatient().getPrenom())
        );

        if (consultation.getMeetLink() != null && !consultation.getMeetLink().isEmpty()) {
            content.getChildren().add(new Label("Lien Meet: " + consultation.getMeetLink()));
        }

        Label commentLabel = new Label("Commentaire:");
        TextArea commentArea = new TextArea(consultation.getCommentaire());
        commentArea.setEditable(false);
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(3);
        content.getChildren().addAll(commentLabel, commentArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
}