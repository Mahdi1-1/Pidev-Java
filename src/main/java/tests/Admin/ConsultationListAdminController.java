package tests.Admin;

import entities.Consultation;
import services.ServiceConsultation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ConsultationListAdminController {
    @FXML private FlowPane consultationsContainer;
    @FXML private ChoiceBox<String> statusFilterChoiceBox;
    @FXML private ChoiceBox<String> typeFilterChoiceBox;
    @FXML private DatePicker dateFilterPicker;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    
    private ObservableList<Consultation> consultationList;
    private int currentPage = 1;
    private int pageSize = 6;
    private int totalPages = 1;
    
    private ServiceConsultation serviceConsultation;
    
    @FXML
    public void initialize() {
        try {
            serviceConsultation = new ServiceConsultation();
            consultationList = FXCollections.observableArrayList();
            
            // Initialiser les filtres
            initializeFilters();
            
            // Configurer la recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                loadConsultations();
            });
            
            // Charger les consultations
            loadConsultations();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation : " + e.getMessage());
        }
    }
    
    private void initializeFilters() {
        try {
            // Initialiser le filtre de statut
            List<String> statuses = serviceConsultation.getDistinctStatuses();
            statuses.add(0, "Tous");
            statusFilterChoiceBox.setItems(FXCollections.observableArrayList(statuses));
            statusFilterChoiceBox.setValue("Tous");
            statusFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                loadConsultations();
            });
            
            // Initialiser le filtre de type
            List<String> types = serviceConsultation.getDistinctTypes();
            types.add(0, "Tous");
            typeFilterChoiceBox.setItems(FXCollections.observableArrayList(types));
            typeFilterChoiceBox.setValue("Tous");
            typeFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                loadConsultations();
            });
            
            // Initialiser le filtre de date
            dateFilterPicker.setValue(null);
            dateFilterPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                loadConsultations();
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation des filtres : " + e.getMessage());
        }
    }
    
    private void loadConsultations() {
        try {
            // Récupérer les valeurs des filtres
            String statusFilter = statusFilterChoiceBox.getValue().equals("Tous") ? null : statusFilterChoiceBox.getValue();
            String typeFilter = typeFilterChoiceBox.getValue().equals("Tous") ? null : typeFilterChoiceBox.getValue();
            LocalDateTime dateFilter = dateFilterPicker.getValue() != null ? dateFilterPicker.getValue().atStartOfDay() : null;
            String searchText = searchField.getText().trim().isEmpty() ? null : searchField.getText().trim();
            
            // For admin view, pass null as patientId to see all consultations
            String patientId = null;
            
            // Compter le nombre total de consultations pour la pagination
            int totalConsultations = serviceConsultation.countConsultations(statusFilter, typeFilter, dateFilter, searchText, patientId);
            totalPages = (int) Math.ceil((double) totalConsultations / pageSize);
            
            // Mettre à jour le label de pagination
            pageLabel.setText("Page " + currentPage + " / " + totalPages);
            
            // Activer/désactiver les boutons de pagination
            prevPageButton.setDisable(currentPage <= 1);
            nextPageButton.setDisable(currentPage >= totalPages);
            
            // Récupérer les consultations filtrées et paginées
            List<Consultation> consultations = serviceConsultation.filterConsultations(
                statusFilter, typeFilter, dateFilter, searchText, patientId, currentPage, pageSize
            );
            consultationList.clear();
            consultationList.addAll(consultations);
            
            // Afficher les consultations dans le conteneur
            displayConsultations();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des consultations : " + e.getMessage());
        }
    }
    
    private void displayConsultations() {
        consultationsContainer.getChildren().clear();
        
        for (Consultation consultation : consultationList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/consultationcard.fxml"));
                loader.setController(new ConsultationCardController());
                AnchorPane consultationCard = loader.load();
                
                // Récupérer le contrôleur et configurer la carte
                ConsultationCardController cardController = loader.getController();
                cardController.setConsultation(consultation);
                
                // Configurer les actions des boutons
                cardController.setOnDetailsAction(this::showDetails);
                cardController.setOnEditAction(this::editConsultation);
                cardController.setOnDeleteAction(this::deleteConsultation);
                
                consultationsContainer.getChildren().add(consultationCard);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'affichage d'une consultation : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadConsultations();
        }
    }
    
    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadConsultations();
        }
    }
    
    @FXML
    private void addConsultation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormConsultation.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Consultation");
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(false);
            stage.showAndWait();
            
            loadConsultations();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout : " + e.getMessage());
        }
    }
    
    private void showDetails(Consultation consultation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/ConsultationDetailsAdmin.fxml"));
            Parent root = loader.load();
            
            ConsultationDetailsAdminController controller = loader.getController();
            controller.setConsultation(consultation);
            
            Stage stage = new Stage();
            stage.setTitle("Détails de la Consultation");
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(true);
            stage.showAndWait();
            
            loadConsultations();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails de la consultation : " + e.getMessage());
        }
    }
    
    private void editConsultation(Consultation consultation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormConsultation.fxml"));
            Parent root = loader.load();
            
            FormConsultationController controller = loader.getController();
            controller.setConsultation(consultation);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier la Consultation");
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(false);
            stage.showAndWait();
            
            loadConsultations();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage());
        }
    }
    
    private void deleteConsultation(Consultation consultation) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette consultation ?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceConsultation.supprimer(consultation.getId());
                    loadConsultations();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void close() {
        Stage stage = (Stage) consultationsContainer.getScene().getWindow();
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