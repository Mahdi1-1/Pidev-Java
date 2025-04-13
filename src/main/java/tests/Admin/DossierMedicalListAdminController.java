package tests.Admin;

import entities.DossierMedical;
import services.ServiceDossierMedical;
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

public class DossierMedicalListAdminController {
    @FXML private FlowPane dossiersContainer;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    
    private ObservableList<DossierMedical> dossierList;
    private int currentPage = 1;
    private int pageSize = 6;
    private int totalPages = 1;
    
    @FXML
    public void initialize() {
        try {
            dossierList = FXCollections.observableArrayList();
            
            // Configurer la recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                try {
                    loadDossiers();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            
            // Charger les dossiers médicaux
            loadDossiers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation : " + e.getMessage());
        }
    }
    
    private void loadDossiers() throws SQLException {
        try {
            // Récupérer les valeurs des filtres
            String searchText = searchField.getText().trim().isEmpty() ? null : searchField.getText().trim();
            
            ServiceDossierMedical service = new ServiceDossierMedical();
            
            // Compter le nombre total de dossiers pour la pagination
            int totalDossiers = service.countDossiers(null, null, searchText);
            totalPages = (int) Math.ceil((double) totalDossiers / pageSize);
            
            // Mettre à jour le label de pagination
            pageLabel.setText("Page " + currentPage + " / " + totalPages);
            
            // Activer/désactiver les boutons de pagination
            prevPageButton.setDisable(currentPage <= 1);
            nextPageButton.setDisable(currentPage >= totalPages);
            
            // Récupérer les dossiers filtrés et paginés
            List<DossierMedical> dossiers = service.filterDossiers(null, null, searchText, currentPage, pageSize);
            dossierList.clear();
            dossierList.addAll(dossiers);
            
            // Afficher les dossiers dans le conteneur
            displayDossiers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des dossiers : " + e.getMessage());
        }
    }
    
    private void displayDossiers() {
        dossiersContainer.getChildren().clear();
        
        for (DossierMedical dossier : dossierList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dossiermedicalcard.fxml"));
                loader.setController(new DossierMedicalCardController());
                AnchorPane dossierCard = loader.load();
                
                // Récupérer le contrôleur et configurer la carte
                DossierMedicalCardController cardController = loader.getController();
                cardController.setDossierMedical(dossier);
                
                // Configurer les actions des boutons
                cardController.setOnDetailsAction(this::showDetails);
                cardController.setOnEditAction(this::editDossier);
                cardController.setOnDeleteAction(this::deleteDossier);
                
                dossiersContainer.getChildren().add(dossierCard);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'affichage d'un dossier : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            try {
                loadDossiers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du chargement des dossiers : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            try {
                loadDossiers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du chargement des dossiers : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void addDossier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormDossierMedicalAdmin.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Dossier Médical");
            stage.setScene(new Scene(root, 400, 500));
            stage.setResizable(false);
            stage.showAndWait();
            
            try {
                loadDossiers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du rechargement des dossiers : " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout : " + e.getMessage());
        }
    }
    
    private void showDetails(DossierMedical dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/DossierMedicalDetailsAdmin.fxml"));
            Parent root = loader.load();
            
            DossierMedicalDetailsAdminController controller = loader.getController();
            controller.setDossier(dossier);
            
            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(true);
            stage.showAndWait();
            
            try {
                loadDossiers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du rechargement des dossiers : " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du dossier : " + e.getMessage());
        }
    }
    
    private void editDossier(DossierMedical dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/FormDossierMedicalAdmin.fxml"));
            Parent root = loader.load();
            
            DossierMedicalFormController controller = loader.getController();
            controller.setDossier(dossier);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier le Dossier Médical");
            stage.setScene(new Scene(root, 400, 500));
            stage.setResizable(false);
            stage.showAndWait();
            
            try {
                loadDossiers();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du rechargement des dossiers : " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire de modification : " + e.getMessage());
        }
    }
    
    private void deleteDossier(DossierMedical dossier) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer ce dossier médical ?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ServiceDossierMedical service = new ServiceDossierMedical();
                    service.supprimer(dossier.getId());
                    
                    try {
                        loadDossiers();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Erreur lors du rechargement des dossiers : " + e.getMessage());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void close() {
        Stage stage = (Stage) dossiersContainer.getScene().getWindow();
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