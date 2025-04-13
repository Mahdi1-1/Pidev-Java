package tests.Admin;

import entities.Consultation;
import entities.TypeConsultation;
import entities.Utilisateur;
import services.ServiceConsultation;
import services.ServiceUtilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FormConsultationController {
    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private ChoiceBox<String> statusChoiceBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureComboBox;
    @FXML private ComboBox<String> minuteComboBox;
    @FXML private ComboBox<Utilisateur> medecinComboBox;
    @FXML private ComboBox<Utilisateur> patientComboBox;
    @FXML private TextField meetLinkTextField;
    @FXML private TextArea commentaireTextArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Consultation consultation;
    private boolean isEditMode = false;
    private ServiceConsultation serviceConsultation;
    private ServiceUtilisateur serviceUtilisateur;
    
    @FXML
    public void initialize() {
        try {
            serviceConsultation = new ServiceConsultation();
            serviceUtilisateur = new ServiceUtilisateur();
            
            // Initialize type choices using enum names
            typeChoiceBox.setItems(FXCollections.observableArrayList(
                TypeConsultation.PHYSIQUE.name(),
                TypeConsultation.VIRTUELLE.name()
            ));
            typeChoiceBox.setValue(TypeConsultation.PHYSIQUE.name());
            
            // Initialize status choices
            statusChoiceBox.setItems(FXCollections.observableArrayList(
                Consultation.STATUS_PENDING,
                Consultation.STATUS_CONFIRMED,
                Consultation.STATUS_COMPLETED,
                Consultation.STATUS_CANCELLED
            ));
            statusChoiceBox.setValue(Consultation.STATUS_PENDING);
            
            // Initialize hours (8:00 - 18:00)
            List<String> hours = new ArrayList<>();
            for (int i = 8; i <= 18; i++) {
                hours.add(String.format("%02d", i));
            }
            heureComboBox.setItems(FXCollections.observableArrayList(hours));
            heureComboBox.setValue("09");
            
            // Initialize minutes (00, 15, 30, 45)
            List<String> minutes = new ArrayList<>();
            for (int i = 0; i < 60; i += 15) {
                minutes.add(String.format("%02d", i));
            }
            minuteComboBox.setItems(FXCollections.observableArrayList(minutes));
            minuteComboBox.setValue("00");
            
            // Set default date to today
            datePicker.setValue(LocalDate.now());
            
            // Load doctors and patients
            loadMedecinsAndPatients();
            
            // Configure meet link field visibility based on consultation type
            typeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                boolean isVirtual = TypeConsultation.VIRTUELLE.name().equals(newValue);
                meetLinkTextField.setDisable(!isVirtual);
                if (!isVirtual) {
                    meetLinkTextField.setText("");
                }
            });
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation : " + e.getMessage());
        }
    }
    
    private void loadMedecinsAndPatients() throws SQLException {
        // Load doctors (users with MEDECIN role)
        List<Utilisateur> medecins = serviceUtilisateur.getByRole("MEDECIN");
        medecinComboBox.setItems(FXCollections.observableArrayList(medecins));
        
        // Load patients (users with PATIENT role)
        List<Utilisateur> patients = serviceUtilisateur.getByRole("PATIENT");
        patientComboBox.setItems(FXCollections.observableArrayList(patients));
        
        // Configure display for doctors
        medecinComboBox.setCellFactory(param -> new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Dr. " + item.getNom() + " " + item.getPrenom());
                }
            }
        });
        
        medecinComboBox.setButtonCell(new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Dr. " + item.getNom() + " " + item.getPrenom());
                }
            }
        });
        
        // Configure display for patients
        patientComboBox.setCellFactory(param -> new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });
        
        patientComboBox.setButtonCell(new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });
    }
    
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        this.isEditMode = true;
        
        // Fill fields with consultation data
        typeChoiceBox.setValue(consultation.getType().name());
        statusChoiceBox.setValue(consultation.getStatus());
        datePicker.setValue(consultation.getDateC().toLocalDate());
        heureComboBox.setValue(String.format("%02d", consultation.getDateC().getHour()));
        minuteComboBox.setValue(String.format("%02d", consultation.getDateC().getMinute()));
        meetLinkTextField.setText(consultation.getMeetLink() != null ? consultation.getMeetLink() : "");
        commentaireTextArea.setText(consultation.getCommentaire());
        
        // Select doctor and patient
        for (Utilisateur medecin : medecinComboBox.getItems()) {
            if (medecin.getId() == consultation.getMedecin().getId()) {
                medecinComboBox.setValue(medecin);
                break;
            }
        }
        
        for (Utilisateur patient : patientComboBox.getItems()) {
            if (patient.getId() == consultation.getPatient().getId()) {
                patientComboBox.setValue(patient);
                break;
            }
        }
    }
    
    @FXML
    private void saveConsultation() {
        try {
            // Validate inputs
            if (datePicker.getValue() == null) {
                showAlert("Erreur", "Veuillez sélectionner une date.");
                return;
            }
            
            if (medecinComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez sélectionner un médecin.");
                return;
            }
            
            if (patientComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez sélectionner un patient.");
                return;
            }
            
            // Create or update Consultation object
            if (!isEditMode) {
                consultation = new Consultation();
            }
            
            // Set properties
            consultation.setType(TypeConsultation.valueOf(typeChoiceBox.getValue()));
            consultation.setStatus(statusChoiceBox.getValue());
            
            // Build date and time
            LocalTime time = LocalTime.of(
                Integer.parseInt(heureComboBox.getValue()),
                Integer.parseInt(minuteComboBox.getValue())
            );
            LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), time);
            consultation.setDateC(dateTime);
            
            consultation.setMeetLink(meetLinkTextField.getText().trim().isEmpty() ? null : meetLinkTextField.getText().trim());
            consultation.setCommentaire(commentaireTextArea.getText());
            consultation.setMedecin(medecinComboBox.getValue());
            consultation.setPatient(patientComboBox.getValue());
            
            // Save consultation
            if (isEditMode) {
                serviceConsultation.modifier(consultation);
            } else {
                serviceConsultation.ajouter(consultation);
            }
            
            // Close window
            cancel();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }
    
    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}