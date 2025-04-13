package tests.Admin;

import entities.Ordonnance;
import services.ServiceOrdonnance;
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
import java.util.List;

public class OrdonnanceListAdminController {
    @FXML private FlowPane ordonnancesContainer;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    
    private ObservableList<Ordonnance> ordonnanceList;
    private int currentPage = 1;
    private int pageSize = 6;
    private int totalPages = 1;
    
    private ServiceOrdonnance serviceOrdonnance;
    
    @FXML
    public void initialize() {
        try {
            serviceOrdonnance = new ServiceOrdonnance();
            ordonnanceList = FXCollections.observableArrayList();
            
            // Configurer la recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                loadOrdonnances();
            });
            
            // Charger les ordonnances
            loadOrdonnances();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation : " + e.getMessage());
        }
    }
    
    private void loadOrdonnances() {
        try {
            // Récupérer les valeurs des filtres
            String searchText = searchField.getText().trim().isEmpty() ? null : searchField.getText().trim();
            // For admin view, pass null as patientId to see all ordonnances
            String patientId = null;
            
            // Compter le nombre total d'ordonnances pour la pagination
            int totalOrdonnances = serviceOrdonnance.countOrdonnances(searchText, patientId);
            totalPages = (int) Math.ceil((double) totalOrdonnances / pageSize);
            
            // Mettre à jour le label de pagination
            pageLabel.setText("Page " + currentPage + " / " + totalPages);
            
            // Activer/désactiver les boutons de pagination
            prevPageButton.setDisable(currentPage <= 1);
            nextPageButton.setDisable(currentPage >= totalPages);
            
            // Récupérer les ordonnances filtrées et paginées
            List<Ordonnance> ordonnances = serviceOrdonnance.filterOrdonnances(searchText, patientId, currentPage, pageSize);
            ordonnanceList.clear();
            ordonnanceList.addAll(ordonnances);
            
            // Afficher les ordonnances dans le conteneur
            displayOrdonnances();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des ordonnances : " + e.getMessage());
        }
    }
    
    private void displayOrdonnances() {
        ordonnancesContainer.getChildren().clear();
        
        for (Ordonnance ordonnance : ordonnanceList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ordonnancecard.fxml"));
                loader.setController(new OrdonnanceCardController());
                AnchorPane ordonnanceCard = loader.load();
                
                // Récupérer le contrôleur et configurer la carte
                OrdonnanceCardController cardController = loader.getController();
                cardController.setOrdonnance(ordonnance);
                
                // Configurer les actions des boutons
                cardController.setOnDetailsAction(this::showDetails);
                cardController.setOnEditAction(this::editOrdonnance);
                cardController.setOnDeleteAction(this::deleteOrdonnance);
                
                ordonnancesContainer.getChildren().add(ordonnanceCard);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'affichage d'une ordonnance : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadOrdonnances();
        }
    }
    
    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadOrdonnances();
        }
    }
    
    @FXML
    private void addOrdonnance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormOrdonnance.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Ordonnance");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(false);
            stage.showAndWait();
            
            loadOrdonnances();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout : " + e.getMessage());
        }
    }
    
    private void showDetails(Ordonnance ordonnance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/OrdonnanceDetailsAdmin.fxml"));
            Parent root = loader.load();
            
            OrdonnanceDetailsAdminController controller = loader.getController();
            controller.setOrdonnance(ordonnance);
            
            Stage stage = new Stage();
            stage.setTitle("Détails de l'Ordonnance");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(true);
            stage.showAndWait();
            
            loadOrdonnances();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails de l'ordonnance : " + e.getMessage());
        }
    }
    
    private void editOrdonnance(Ordonnance ordonnance) {
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
            
            loadOrdonnances();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage());
        }
    }
    
    private void deleteOrdonnance(Ordonnance ordonnance) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette ordonnance ?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceOrdonnance.supprimer(ordonnance.getId());
                    loadOrdonnances();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void close() {
        Stage stage = (Stage) ordonnancesContainer.getScene().getWindow();
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