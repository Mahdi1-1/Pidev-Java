package tests.Admin;

import entities.Consultation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.function.Consumer;

public class ConsultationCardController {
    @FXML private AnchorPane rootPane;
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
    private Consumer<Consultation> onDetailsAction;
    private Consumer<Consultation> onEditAction;
    private Consumer<Consultation> onDeleteAction;
    
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        
        // Remplir les données
        idLabel.setText("ID: " + consultation.getId());
        statusLabel.setText(consultation.getStatus());
        
        // Appliquer la classe CSS en fonction du statut
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.getStyleClass().add("status-" + consultation.getStatus());
        
        typeLabel.setText(consultation.getType().toString());
        dateLabel.setText(consultation.getDateC().toString().replace("T", " "));
        medecinLabel.setText("Dr. " + consultation.getMedecin().getNom() + " " + consultation.getMedecin().getPrenom());
        patientLabel.setText(consultation.getPatient().getNom() + " " + consultation.getPatient().getPrenom());
        commentaireTextArea.setText(consultation.getCommentaire());
    }
    
    public void setOnDetailsAction(Consumer<Consultation> onDetailsAction) {
        this.onDetailsAction = onDetailsAction;
        detailsButton.setOnAction(event -> {
            if (onDetailsAction != null) {
                onDetailsAction.accept(consultation);
            }
        });
    }
    
    public void setOnEditAction(Consumer<Consultation> onEditAction) {
        this.onEditAction = onEditAction;
        editButton.setOnAction(event -> {
            if (onEditAction != null) {
                onEditAction.accept(consultation);
            }
        });
    }
    
    public void setOnDeleteAction(Consumer<Consultation> onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
        deleteButton.setOnAction(event -> {
            if (onDeleteAction != null) {
                onDeleteAction.accept(consultation);
            }
        });
    }
    
    public AnchorPane getRootPane() {
        return rootPane;
    }
}