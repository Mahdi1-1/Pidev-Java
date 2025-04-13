package tests;

import entities.Ordonnance;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceOrdonnance;
import tests.Admin.FormOrdonnanceController;

import java.io.IOException;
import java.util.List;

public class OrdonnanceListController {
    @FXML private VBox ordonnanceList;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button addButton;

    private ServiceOrdonnance serviceOrdonnance;
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;
    private String utilisateurId;

    public void initialize() {
        try {
            serviceOrdonnance = new ServiceOrdonnance();
            setupPagination();
            
            // Setup search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> loadOrdonnances());
            
            // Setup add button
            addButton.setOnAction(e -> handleAddOrdonnance());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUtilisateurId(String id) {
        this.utilisateurId = id;
        loadOrdonnances(); // Load ordonnances after setting the user ID
    }

    private void setupPagination() {
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadOrdonnances();
            }
        });

        nextButton.setOnAction(e -> {
            currentPage++;
            loadOrdonnances();
        });
    }

    private void loadOrdonnances() {
        try {
            ordonnanceList.getChildren().clear();
            
            String searchText = searchField.getText();

            // Get ordonnances for the current patient
            List<Ordonnance> ordonnances = serviceOrdonnance.filterOrdonnances(
                searchText, utilisateurId, currentPage, PAGE_SIZE
            );

            int totalCount = serviceOrdonnance.countOrdonnances(searchText, utilisateurId);
            int totalPages = (int) Math.ceil(totalCount / (double) PAGE_SIZE);

            for (Ordonnance ordonnance : ordonnances) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ordonnancecard.fxml"));
                VBox ordonnanceCard = loader.load();
                OrdonnanceCardController controller = loader.getController();
                
                controller.setData(
                    ordonnance,
                    () -> loadOrdonnances(), // Refresh after delete
                    () -> handleEditOrdonnance(ordonnance) // Edit callback
                );
                
                ordonnanceList.getChildren().add(ordonnanceCard);
            }

            // Update pagination
            pageLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
            prevButton.setDisable(currentPage <= 1);
            nextButton.setDisable(currentPage >= totalPages || ordonnances.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors du chargement des ordonnances.");
        }
    }

    private void handleAddOrdonnance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormOrdonnance.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Ordonnance");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Refresh the list after adding
            loadOrdonnances();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout d'ordonnance");
        }
    }

    private void handleEditOrdonnance(Ordonnance ordonnance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormOrdonnance.fxml"));
            Parent root = loader.load();
            
            FormOrdonnanceController controller = loader.getController();
            controller.setOrdonnance(ordonnance);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier l'Ordonnance");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Refresh the list after editing
            loadOrdonnances();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}