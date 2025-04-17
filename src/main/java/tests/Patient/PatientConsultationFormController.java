package tests.Patient;

import entities.Consultation;
import entities.TypeConsultation;
import entities.Utilisateur;
import exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceConsultation;
import services.ServiceUtilisateur;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class PatientConsultationFormController implements Initializable {

    @FXML private ComboBox<TypeConsultation> comboType;
    @FXML private ComboBox<Utilisateur> comboMedecin;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboHour;
    @FXML private TextArea textCommentaire;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private ServiceConsultation serviceConsultation;
    private ServiceUtilisateur serviceUtilisateur;
    private Utilisateur currentUser;
    private String mode = "create"; // "create" or "edit"
    private Consultation currentConsultation;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            serviceConsultation = new ServiceConsultation();
            serviceUtilisateur = new ServiceUtilisateur();
            
            // Initialize Type ComboBox
            comboType.setItems(FXCollections.observableArrayList(TypeConsultation.values()));
            comboType.setConverter(new javafx.util.StringConverter<TypeConsultation>() {
                @Override
                public String toString(TypeConsultation object) {
                    return object != null ? object.getDisplayName() : "";
                }

                @Override
                public TypeConsultation fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            
            // Initialize hours ComboBox (8:00 to 18:00)
            ObservableList<String> hours = FXCollections.observableArrayList();
            for (int i = 8; i <= 18; i++) {
                hours.add(String.format("%02d:00", i));
                if (i < 18) {
                    hours.add(String.format("%02d:30", i));
                }
            }
            comboHour.setItems(hours);
            loadMedecins();
            // Set today as default date
            datePicker.setValue(LocalDate.now());
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation", e.getMessage());
        }
    }
    
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        loadMedecins();
    }
    
    public void setMode(String mode) {
        this.mode = mode;
        updateUI();
    }
    
    public void setConsultation(Consultation consultation) {
        this.currentConsultation = consultation;
        
        if (consultation != null) {
            comboType.setValue(consultation.getType());
            comboMedecin.setValue(consultation.getMedecin());
            datePicker.setValue(consultation.getDateC().toLocalDate());
            
            // Format time to match ComboBox format
            int hour = consultation.getDateC().getHour();
            int minute = consultation.getDateC().getMinute();
            String timeStr = String.format("%02d:%02d", hour, minute);
            comboHour.setValue(timeStr);
            
            textCommentaire.setText(consultation.getCommentaire());
            
            updateUI();
        }
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    private void loadMedecins() {
        try {
            // Load doctors (users with role ROLE_MEDECIN)
            List<Utilisateur> medecins = serviceUtilisateur.afficher();
            System.out.println("setting combomedecin");
            comboMedecin.setItems(FXCollections.observableArrayList(medecins));
            System.out.println(comboMedecin.getValue());

            // Set display for doctors (Full name)
            comboMedecin.setConverter(new javafx.util.StringConverter<Utilisateur>() {
                @Override
                public String toString(Utilisateur user) {
                    return user != null ? user.getNom() + " " + user.getPrenom() : "";
                }

                @Override
                public Utilisateur fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des médecins", e.getMessage());
        }
    }
    
    private void updateUI() {
        if ("edit".equals(mode)) {
            btnSave.setText("Modifier");
        } else {
            btnSave.setText("Planifier");
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            // Validate input
            if (comboType.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner un type de consultation");
            }
            if (comboMedecin.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner un médecin");
            }
            if (datePicker.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner une date");
            }
            if (comboHour.getValue() == null) {
                throw new ValidationException("Veuillez sélectionner une heure");
            }
            
            // Check if the date is not in the past
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate.isBefore(LocalDate.now())) {
                throw new ValidationException("Vous ne pouvez pas planifier une consultation dans le passé");
            }
            
            // Parse time from ComboBox
            String[] timeParts = comboHour.getValue().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // Create LocalDateTime for the consultation
            LocalDateTime dateTime = LocalDateTime.of(
                selectedDate,
                LocalTime.of(hour, minute)
            );
            
            // Check if the date/time is already past for today
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new ValidationException("Vous ne pouvez pas planifier une consultation à une heure déjà passée");
            }
            
            if ("create".equals(mode)) {
                // Create new consultation
                Consultation consultation = new Consultation();
                consultation.setType(comboType.getValue());
                consultation.setStatus(Consultation.STATUS_PENDING);
                consultation.setCommentaire(textCommentaire.getText());
                consultation.setDateC(dateTime);
                consultation.setMedecin(comboMedecin.getValue());
                // TODO MAKE IT DYNAMIC
                Utilisateur user = serviceUtilisateur.getById(1);
                consultation.setPatient(user);
                
                // Set a meet link for virtual consultations
                if (consultation.getType() == TypeConsultation.VIRTUELLE) {
                    consultation.setMeetLink("https://meet.tbibi.tn/" + System.currentTimeMillis());
                }
                
                // Save to database
                serviceConsultation.ajouter(consultation);
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Consultation planifiée", 
                    "Votre demande de consultation a été soumise avec succès. Veuillez attendre la confirmation du médecin.");
                
            } else {
                // Update existing consultation
                currentConsultation.setType(comboType.getValue());
                currentConsultation.setCommentaire(textCommentaire.getText());
                currentConsultation.setDateC(dateTime);
                currentConsultation.setMedecin(comboMedecin.getValue());
                
                // Update meet link if type changes to virtual
                if (currentConsultation.getType() == TypeConsultation.VIRTUELLE && 
                    (currentConsultation.getMeetLink() == null || currentConsultation.getMeetLink().isEmpty())) {
                    currentConsultation.setMeetLink("https://meet.tbibi.tn/" + System.currentTimeMillis());
                }
                
                // Save changes
                serviceConsultation.modifier(currentConsultation);
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Consultation modifiée", 
                    "Votre consultation a été modifiée avec succès.");
            }
            
            // Call the callback if provided
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            // Close the form
            ((Stage) btnSave.getScene().getWindow()).close();
            
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", e.getMessage(), null);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue s'est produite", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 