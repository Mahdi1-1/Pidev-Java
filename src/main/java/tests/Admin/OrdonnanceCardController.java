package tests.Admin;

import entities.Ordonnance;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.function.Consumer;

public class OrdonnanceCardController {
    @FXML private AnchorPane rootPane;
    @FXML private Label idLabel;
    @FXML private Label consultationIdLabel;
    @FXML private Label signatureLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button detailsButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    private Ordonnance ordonnance;
    private Consumer<Ordonnance> onDetailsAction;
    private Consumer<Ordonnance> onEditAction;
    private Consumer<Ordonnance> onDeleteAction;
    
    public void setOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        
        // Remplir les données
        idLabel.setText("ID: " + ordonnance.getId());
        consultationIdLabel.setText("Consultation: " + ordonnance.getConsultation().getId());
        signatureLabel.setText(ordonnance.getSignature());
        descriptionTextArea.setText(ordonnance.getDescription());
    }
    
    public void setOnDetailsAction(Consumer<Ordonnance> onDetailsAction) {
        this.onDetailsAction = onDetailsAction;
        detailsButton.setOnAction(event -> {
            if (onDetailsAction != null) {
                onDetailsAction.accept(ordonnance);
            }
        });
    }
    
    public void setOnEditAction(Consumer<Ordonnance> onEditAction) {
        this.onEditAction = onEditAction;
        editButton.setOnAction(event -> {
            if (onEditAction != null) {
                onEditAction.accept(ordonnance);
            }
        });
    }
    
    public void setOnDeleteAction(Consumer<Ordonnance> onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
        deleteButton.setOnAction(event -> {
            if (onDeleteAction != null) {
                onDeleteAction.accept(ordonnance);
            }
        });
    }
    
    public AnchorPane getRootPane() {
        return rootPane;
    }
}