package tests.Patient;

import entities.Consultation;
import entities.TypeConsultation;
import entities.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ServiceConsultation;
import services.ServiceUtilisateur;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PatientConsultationController implements Initializable {

    @FXML private TableView<Consultation> tableConsultations;
    @FXML private TableColumn<Consultation, Integer> colId;
    @FXML private TableColumn<Consultation, String> colType;
    @FXML private TableColumn<Consultation, String> colStatus;
    @FXML private TableColumn<Consultation, String> colDate;
    @FXML private TableColumn<Consultation, String> colMedecin;
    
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private DatePicker filterDate;
    @FXML private TextField searchField;
    
    @FXML private Button btnSchedule;
    @FXML private Button btnCancel;
    @FXML private Button btnView;
    
    private ObservableList<Consultation> consultationsList = FXCollections.observableArrayList();
    private ServiceConsultation serviceConsultation;
    private Utilisateur currentUser;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            serviceConsultation = new ServiceConsultation();
            ServiceUtilisateur su = new ServiceUtilisateur();
            // TODO FIX IN INTEGRATION
            this.currentUser = su.getById(1);
            loadConsultations();
                    // Configure table columns
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().getDisplayName()));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colDate.setCellValueFactory(data -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(data.getValue().getDateC().format(formatter));
            });
            colMedecin.setCellValueFactory(data -> {
                Utilisateur medecin = data.getValue().getMedecin();
                if (medecin != null) {
                    return new SimpleStringProperty(medecin.getNom() + " " + medecin.getPrenom());
                }
                return new SimpleStringProperty("");
            });
            
            // Initialize filter ComboBoxes
            initializeFilters();
            
            // Apply filters when values change
            filterStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            filterType.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            filterDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            
            // Disable buttons initially
            btnCancel.setDisable(true);
            btnView.setDisable(true);
            
            // Enable buttons when a row is selected
            tableConsultations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                btnView.setDisable(!hasSelection);
                
                // Only enable cancel for pending consultations
                if (hasSelection && "pending".equals(newSelection.getStatus())) {
                    btnCancel.setDisable(false);
                } else {
                    btnCancel.setDisable(true);
                }
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion à la base de données", e.getMessage());
        }
    }
    
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        loadConsultations();
    }
    
    private void loadConsultations() {
        try {
            if (currentUser != null) {
                String status = filterStatus.getValue();
                String type = filterType.getValue();
                LocalDateTime date = filterDate.getValue() != null ? 
                    filterDate.getValue().atStartOfDay() : null;
                String search = searchField.getText();
                
                // Load consultations for the current patient with filters
                List<Consultation> consultations = serviceConsultation.filterConsultations(
                    status, type, date, search, String.valueOf(currentUser.getId()), 
                    currentPage, PAGE_SIZE);
                
                consultationsList.clear();
                consultationsList.addAll(consultations);
                tableConsultations.setItems(consultationsList);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des consultations", e.getMessage());
        }
    }
    
    private void initializeFilters() {
        try {
            // Add an empty option
            filterStatus.getItems().add("");
            filterStatus.getItems().addAll(serviceConsultation.getDistinctStatuses());
            
            filterType.getItems().add("");
            filterType.getItems().addAll(serviceConsultation.getDistinctTypes());
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation des filtres", e.getMessage());
        }
    }
    
    private void applyFilters() {
        currentPage = 1; // Reset to first page when applying filters
        loadConsultations();
    }
    
    @FXML
    private void handleScheduleAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_form.fxml"));
            Parent root = loader.load();
            
            PatientConsultationFormController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setMode("create");
            
            // On successful save, reload the consultations list
            controller.setOnSaveCallback(() -> loadConsultations());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Planifier Consultation");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelAction(ActionEvent event) {
        Consultation selectedConsultation = tableConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null && "pending".equals(selectedConsultation.getStatus())) {
            // Ask for confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
                "Êtes-vous sûr de vouloir annuler cette consultation ?", 
                ButtonType.YES, ButtonType.NO);
            confirmation.showAndWait();
            
            if (confirmation.getResult() == ButtonType.YES) {
                try {
                    // Update the status to cancelled
                    selectedConsultation.setStatus(Consultation.STATUS_CANCELLED);
                    serviceConsultation.modifier(selectedConsultation);
                    
                    // Reload consultations
                    loadConsultations();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Consultation annulée", "La consultation a été annulée avec succès.");
                    
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Erreur lors de l'annulation", e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void handleViewAction(ActionEvent event) {
        Consultation selectedConsultation = tableConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/consultation_details.fxml"));
                Parent root = loader.load();
                
                PatientConsultationDetailsController controller = loader.getController();
                controller.setConsultation(selectedConsultation);
                
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Détails de la Consultation");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture des détails", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleNextPage() {
        try {
            String status = filterStatus.getValue();
            String type = filterType.getValue();
            LocalDateTime date = filterDate.getValue() != null ? 
                filterDate.getValue().atStartOfDay() : null;
            String search = searchField.getText();
            
            int totalCount = serviceConsultation.countConsultations(
                status, type, date, search, String.valueOf(currentUser.getId()));
            
            int maxPage = (int) Math.ceil((double) totalCount / PAGE_SIZE);
            
            if (currentPage < maxPage) {
                currentPage++;
                loadConsultations();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de pagination", e.getMessage());
        }
    }
    
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadConsultations();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 