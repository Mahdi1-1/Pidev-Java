package tests;

import entities.Consultation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.ServiceConsultation;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ConsultationListController {
    @FXML private VBox consultationList;
    @FXML private ChoiceBox<String> statusFilter;
    @FXML private ChoiceBox<String> typeFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button addButton;

    private ServiceConsultation serviceConsultation;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private String utilisateurId;

    public void initialize() {
        try {
            serviceConsultation = new ServiceConsultation();
            setupFilters();
            setupPagination();
            // loadConsultations will be called after setUtilisateurId
            
            // Setup search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> loadConsultations());
            
            // Setup add button
            addButton.setOnAction(e -> handleAddConsultation());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUtilisateurId(String id) {
        this.utilisateurId = id;
        loadConsultations(); // Load consultations after setting the user ID
    }

    private void setupFilters() throws Exception {
        // Initialize status filter
        statusFilter.getItems().addAll(
            Consultation.STATUS_PENDING,
            Consultation.STATUS_CONFIRMED,
            Consultation.STATUS_COMPLETED,
            Consultation.STATUS_CANCELLED
        );
        statusFilter.setOnAction(e -> loadConsultations());

        // Initialize type filter with properly formatted types
        typeFilter.getItems().addAll("PHYSIQUE", "VIRTUELLE");
        typeFilter.setOnAction(e -> loadConsultations());

        // Initialize date filter
        dateFilter.setOnAction(e -> loadConsultations());
    }

    private void setupPagination() {
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadConsultations();
            }
        });

        nextButton.setOnAction(e -> {
            currentPage++;
            loadConsultations();
            // The next button will be disabled if no results are returned
        });
    }

    private void loadConsultations() {
        try {
            consultationList.getChildren().clear();
            
            String status = statusFilter.getValue();
            String type = typeFilter.getValue();
            LocalDateTime date = dateFilter.getValue() != null ? 
                               dateFilter.getValue().atStartOfDay() : null;
            String searchText = searchField.getText();

            List<Consultation> consultations = serviceConsultation.filterConsultations(
                status, type, date, searchText, utilisateurId, currentPage, PAGE_SIZE
            );

            int totalCount = serviceConsultation.countConsultations(status, type, date, searchText, utilisateurId);
            int totalPages = (int) Math.ceil(totalCount / (double) PAGE_SIZE);

            for (Consultation consultation : consultations) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/consultationcard.fxml"));
                VBox consultationCard = loader.load();
                ConsultationCardController controller = loader.getController();
                
                controller.setData(
                    consultation,
                    () -> loadConsultations(), // Refresh after delete
                    () -> handleEditConsultation(consultation) // Edit callback
                );
                
                consultationList.getChildren().add(consultationCard);
            }

            // Update pagination
            pageLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
            prevButton.setDisable(currentPage <= 1);
            nextButton.setDisable(currentPage >= totalPages);

        } catch (Exception e) {
            e.printStackTrace();
            // Handle error (show alert, etc.)
        }
    }

    @FXML
    private void handleAddConsultation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormConsultation.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Consultation");
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Reload consultations after adding
            loadConsultations();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger le formulaire d'ajout : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleEditConsultation(Consultation consultation) {
        // Implement edit consultation logic
        // This should open a form pre-filled with consultation data
    }
}