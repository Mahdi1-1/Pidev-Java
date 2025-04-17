package tests.Doctor;

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
import javafx.scene.chart.PieChart;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DoctorConsultationController implements Initializable {

    @FXML private TableView<Consultation> tableConsultations;
    @FXML private TableColumn<Consultation, Integer> colId;
    @FXML private TableColumn<Consultation, String> colType;
    @FXML private TableColumn<Consultation, String> colStatus;
    @FXML private TableColumn<Consultation, String> colDate;
    @FXML private TableColumn<Consultation, String> colPatient;
    
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterType;
    @FXML private DatePicker filterDate;
    @FXML private TextField searchField;
    
    @FXML private Button btnAccept;
    @FXML private Button btnReject;
    @FXML private Button btnComplete;
    @FXML private Button btnView;
    @FXML private Button btnOrdonnance;
    
    @FXML private PieChart statsPieChart;
    @FXML private Label statsTotalLabel;
    @FXML private Label statsPendingLabel;
    @FXML private Label statsTodayLabel;
    
    private ObservableList<Consultation> consultationsList = FXCollections.observableArrayList();
    private ServiceConsultation serviceConsultation;
    private Utilisateur currentUser;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            ServiceUtilisateur su = new ServiceUtilisateur();
            this.currentUser = su.getById(1);
            serviceConsultation = new ServiceConsultation();
            loadConsultations();
            // Configure table columns
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().getDisplayName()));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colDate.setCellValueFactory(data -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(data.getValue().getDateC().format(formatter));
            });
            colPatient.setCellValueFactory(data -> {
                Utilisateur patient = data.getValue().getPatient();
                if (patient != null) {
                    return new SimpleStringProperty(patient.getNom() + " " + patient.getPrenom());
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
            btnAccept.setDisable(true);
            btnReject.setDisable(true);
            btnComplete.setDisable(true);
            btnView.setDisable(true);
            btnOrdonnance.setDisable(true);
            
            // Enable buttons when a row is selected
            tableConsultations.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                btnView.setDisable(!hasSelection);
                
                if (hasSelection) {
                    String status = newSelection.getStatus();
                    btnAccept.setDisable(!Consultation.STATUS_PENDING.equals(status));
                    btnReject.setDisable(!Consultation.STATUS_PENDING.equals(status));
                    btnComplete.setDisable(!Consultation.STATUS_CONFIRMED.equals(status));
                    btnOrdonnance.setDisable(!(Consultation.STATUS_CONFIRMED.equals(status) || 
                                             Consultation.STATUS_COMPLETED.equals(status)));
                } else {
                    btnAccept.setDisable(true);
                    btnReject.setDisable(true);
                    btnComplete.setDisable(true);
                    btnOrdonnance.setDisable(true);
                }
            });
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion à la base de données", e.getMessage());
        }
    }
    
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        loadConsultations();
        updateDashboardStats();
    }
    
    private void loadConsultations() {
        try {
            if (currentUser != null) {
                String status = filterStatus.getValue();
                String type = filterType.getValue();
                LocalDateTime date = filterDate.getValue() != null ? 
                    filterDate.getValue().atStartOfDay() : null;
                String search = searchField.getText();
                
                // Load consultations for the current doctor with filters
                List<Consultation> consultations = serviceConsultation.filterConsultations(
                    status, type, date, search, null, 
                    currentPage, PAGE_SIZE);
                
                // Filter to only show consultations for the current doctor
                consultations = consultations.stream()
                    .filter(c -> c.getMedecin().getId() == currentUser.getId())
                    .collect(Collectors.toList());
                
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
    
    private void updateDashboardStats() {
        try {
            if (currentUser != null) {
                // Get all consultations for this doctor
                List<Consultation> allConsultations = serviceConsultation.getByMedecinId(currentUser.getId());
                
                // Count by status
                Map<String, Integer> countByStatus = new HashMap<>();
                allConsultations.forEach(c -> {
                    countByStatus.put(c.getStatus(), countByStatus.getOrDefault(c.getStatus(), 0) + 1);
                });
                
                // Count today's consultations
                LocalDate today = LocalDate.now();
                long todayCount = allConsultations.stream()
                    .filter(c -> c.getDateC().toLocalDate().equals(today))
                    .count();
                
                // Update labels
                statsTotalLabel.setText("Total: " + allConsultations.size());
                statsPendingLabel.setText("En attente: " + countByStatus.getOrDefault(Consultation.STATUS_PENDING, 0));
                statsTodayLabel.setText("Aujourd'hui: " + todayCount);
                
                // Update chart
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                countByStatus.forEach((status, count) -> pieChartData.add(new PieChart.Data(status, count)));
                statsPieChart.setData(pieChartData);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour des statistiques", e.getMessage());
        }
    }
    
    @FXML
    private void handleAcceptAction(ActionEvent event) {
        Consultation selectedConsultation = tableConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null && Consultation.STATUS_PENDING.equals(selectedConsultation.getStatus())) {
            try {
                // Update the status to confirmed
                selectedConsultation.setStatus(Consultation.STATUS_CONFIRMED);
                serviceConsultation.modifier(selectedConsultation);
                
                // Reload consultations and stats
                loadConsultations();
                updateDashboardStats();
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Consultation confirmée", "La consultation a été confirmée avec succès.");
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de la confirmation", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRejectAction(ActionEvent event) {
        Consultation selectedConsultation = tableConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null && Consultation.STATUS_PENDING.equals(selectedConsultation.getStatus())) {
            // Ask for confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
                "Êtes-vous sûr de vouloir refuser cette consultation ?", 
                ButtonType.YES, ButtonType.NO);
            confirmation.showAndWait();
            
            if (confirmation.getResult() == ButtonType.YES) {
                try {
                    // Update the status to cancelled
                    selectedConsultation.setStatus(Consultation.STATUS_CANCELLED);
                    serviceConsultation.modifier(selectedConsultation);
                    
                    // Reload consultations and stats
                    loadConsultations();
                    updateDashboardStats();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Consultation refusée", "La consultation a été refusée avec succès.");
                    
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Erreur lors du refus", e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void handleCompleteAction(ActionEvent event) {
        Consultation selectedConsultation = tableConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null && Consultation.STATUS_CONFIRMED.equals(selectedConsultation.getStatus())) {
            try {
                // Update the status to completed
                selectedConsultation.setStatus(Consultation.STATUS_COMPLETED);
                serviceConsultation.modifier(selectedConsultation);
                
                // Reload consultations and stats
                loadConsultations();
                updateDashboardStats();
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                    "Consultation terminée", "La consultation a été marquée comme terminée avec succès.");
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de la finalisation", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleViewAction(ActionEvent event) {
        Consultation selectedConsultation = tableConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/consultation_details.fxml"));
                Parent root = loader.load();
                
                DoctorConsultationDetailsController controller = loader.getController();
                controller.setConsultation(selectedConsultation);
                controller.setOnUpdateCallback(() -> {
                    loadConsultations();
                    updateDashboardStats();
                });
                
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Détails de la Consultation");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture des détails", e.getMessage());
                System.out.println(e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleOrdonnanceAction(ActionEvent event) {
        Consultation selectedConsultation = tableConsultations.getSelectionModel().getSelectedItem();
        if (selectedConsultation != null) {
            if (Consultation.STATUS_CONFIRMED.equals(selectedConsultation.getStatus()) || 
                Consultation.STATUS_COMPLETED.equals(selectedConsultation.getStatus())) {
                
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/ordonnance_form.fxml"));
                    Parent root = loader.load();
                    
                    DoctorOrdonnanceFormController controller = loader.getController();
                    controller.setConsultation(selectedConsultation);
                    controller.setOnSaveCallback(() -> {
                        loadConsultations();
                        updateDashboardStats();
                    });
                    
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Ordonnance");
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                    
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture du formulaire d'ordonnance", e.getMessage());
                }
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
                status, type, date, search, null);
            
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
    
    @FXML
    private void handleRefresh() {
        loadConsultations();
        updateDashboardStats();
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 