package tests.Admin;

import entities.DossierMedical;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.function.Consumer;

public class DossierMedicalCardController {
    @FXML private AnchorPane rootPane;
    @FXML private Label idLabel;
    @FXML private Label dateLabel;
    @FXML private Label utilisateurLabel;
    @FXML private Label uniteLabel;
    @FXML private Label mesureLabel;
    @FXML private Label fichierLabel;
    @FXML private Button detailsButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    private DossierMedical dossierMedical;
    private Consumer<DossierMedical> onDetailsAction;
    private Consumer<DossierMedical> onEditAction;
    private Consumer<DossierMedical> onDeleteAction;
    
    public void setDossierMedical(DossierMedical dossierMedical) {
        this.dossierMedical = dossierMedical;
        
        // Remplir les données
        idLabel.setText("ID: " + dossierMedical.getId());
        dateLabel.setText(dossierMedical.getDate().toString());
        utilisateurLabel.setText("ID: " + dossierMedical.getUtilisateurId());
        uniteLabel.setText(dossierMedical.getUnite());
        mesureLabel.setText(String.valueOf(dossierMedical.getMesure()));
        fichierLabel.setText(dossierMedical.getFichier());
    }
    
    public void setOnDetailsAction(Consumer<DossierMedical> onDetailsAction) {
        this.onDetailsAction = onDetailsAction;
        detailsButton.setOnAction(event -> {
            if (onDetailsAction != null) {
                onDetailsAction.accept(dossierMedical);
            }
        });
    }
    
    public void setOnEditAction(Consumer<DossierMedical> onEditAction) {
        this.onEditAction = onEditAction;
        editButton.setOnAction(event -> {
            if (onEditAction != null) {
                onEditAction.accept(dossierMedical);
            }
        });
    }
    
    public void setOnDeleteAction(Consumer<DossierMedical> onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
        deleteButton.setOnAction(event -> {
            if (onDeleteAction != null) {
                onDeleteAction.accept(dossierMedical);
            }
        });
    }
    
    public AnchorPane getRootPane() {
        return rootPane;
    }
}