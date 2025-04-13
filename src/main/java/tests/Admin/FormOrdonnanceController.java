package tests.Admin;

import entities.Consultation;
import entities.Ordonnance;
import services.ServiceConsultation;
import services.ServiceOrdonnance;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class FormOrdonnanceController {
    @FXML private ComboBox<Consultation> consultationComboBox;
    @FXML private TextArea descriptionTextArea;
    @FXML private TextField signatureTextField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Ordonnance ordonnance;
    private boolean isEditMode = false;
    private ServiceOrdonnance serviceOrdonnance;
    private ServiceConsultation serviceConsultation;
    private Consultation preselectedConsultation;
    
    @FXML
    public void initialize() {
        try {
            serviceOrdonnance = new ServiceOrdonnance();
            serviceConsultation = new ServiceConsultation();
            
            // Charger les consultations disponibles
            loadConsultations();
            
            // Configurer l'affichage des consultations dans le ComboBox
            consultationComboBox.setCellFactory(param -> new ListCell<Consultation>() {
                @Override
                protected void updateItem(Consultation item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("ID: " + item.getId() + " - Patient: " + item.getPatient().getNom() + " " + 
                               item.getPatient().getPrenom() + " - Date: " + item.getDateC().toString().replace("T", " "));
                    }
                }
            });
            
            consultationComboBox.setButtonCell(new ListCell<Consultation>() {
                @Override
                protected void updateItem(Consultation item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("ID: " + item.getId() + " - Patient: " + item.getPatient().getNom() + " " + 
                               item.getPatient().getPrenom() + " - Date: " + item.getDateC().toString().replace("T", " "));
                    }
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation : " + e.getMessage());
        }
    }
    
    private void loadConsultations() throws SQLException {
        // Récupérer toutes les consultations
        List<Consultation> consultations = serviceConsultation.afficherConsultation();
        
        // Filtrer pour ne garder que les consultations complétées sans ordonnance
      //  consultations.removeIf(c -> !c.getStatus().equals(Consultation.STATUS_COMPLETED) || c.getOrdonnance() != null);
        
        consultationComboBox.setItems(FXCollections.observableArrayList(consultations));
        if (!consultations.isEmpty()) {
            consultationComboBox.setValue(consultations.get(0));
        }
    }
    
    public void setOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
        this.isEditMode = true;
        
        // Remplir les champs avec les données de l'ordonnance
        descriptionTextArea.setText(ordonnance.getDescription());
        signatureTextField.setText(ordonnance.getSignature());
        
        // Sélectionner la consultation associée
        Consultation consultation = ordonnance.getConsultation();
        for (Consultation c : consultationComboBox.getItems()) {
            if (c.getId().equals(consultation.getId())) {
                consultationComboBox.setValue(c);
                break;
            }
        }
        
        // En mode édition, on ne peut pas changer la consultation
        consultationComboBox.setDisable(true);
    }
    
    @FXML
    private void saveOrdonnance() {
        try {
            // Valider les entrées
            if (consultationComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez sélectionner une consultation.");
                return;
            }
            
            if (descriptionTextArea.getText().trim().isEmpty()) {
                showAlert("Erreur", "Veuillez saisir une description.");
                return;
            }
            
            if (signatureTextField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Veuillez saisir une signature.");
                return;
            }
            
            // Créer ou mettre à jour l'ordonnance
            if (!isEditMode) {
                ordonnance = new Ordonnance();
            }
            
            ordonnance.setDescription(descriptionTextArea.getText().trim());
            ordonnance.setSignature(signatureTextField.getText().trim());
            ordonnance.setConsultation(consultationComboBox.getValue());
            
            if (isEditMode) {
                serviceOrdonnance.modifier(ordonnance);
            } else {
                serviceOrdonnance.ajouter(ordonnance);
            }
            
            // Fermer la fenêtre
            closeForm();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }
    
    @FXML
    private void cancel() {
        closeForm();
    }
    
    private void closeForm() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Définit une consultation spécifique pour la création d'une nouvelle ordonnance
     * @param consultation La consultation à associer à l'ordonnance
     */
    public void setConsultation(Consultation consultation) {
        this.preselectedConsultation = consultation;
        
        // Sélectionner la consultation dans le ComboBox
        if (consultation != null && consultationComboBox != null) {
            for (Consultation c : consultationComboBox.getItems()) {
                if (c.getId().equals(consultation.getId())) {
                    consultationComboBox.setValue(c);
                    break;
                }
            }
            
            // Désactiver le ComboBox puisque la consultation est déjà sélectionnée
            consultationComboBox.setDisable(true);
        }
    }
}