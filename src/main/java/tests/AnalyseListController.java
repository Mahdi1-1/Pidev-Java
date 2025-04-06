package tests;

import entities.Analyse;
import services.ServiceAnalyse;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AnalyseListController {
    @FXML private TableView<Analyse> tableAnalyses;
    @FXML private TableColumn<Analyse, Integer> colDossierId;
    @FXML private TableColumn<Analyse, String> colType;
    @FXML private TableColumn<Analyse, String> colDateAnalyse;
    @FXML private TableColumn<Analyse, String> colDonneesAnalyse;
    @FXML private TableColumn<Analyse, String> colDiagnostic;
    @FXML private TableColumn<Analyse, Void> colDetails;

    private ServiceAnalyse serviceAnalyse;

    @FXML
    public void initialize() {
        try {
            serviceAnalyse = new ServiceAnalyse();
            System.out.println("ServiceAnalyse initialisé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de ServiceAnalyse : " + e.getMessage());
            e.printStackTrace();
        }

        colDossierId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDossierId()).asObject());
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        colDateAnalyse.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateAnalyse().toString()));
        colDonneesAnalyse.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDonneesAnalyse()));
        colDiagnostic.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiagnostic()));
        colDetails.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Voir");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    detailsButton.setOnAction(event -> {
                        Analyse analyse = getTableView().getItems().get(getIndex());
                        showDetails(analyse);
                    });
                    setGraphic(detailsButton);
                }
            }
        });

        // Ajuster les largeurs des colonnes pour qu'elles occupent tout l'espace
        tableAnalyses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colDossierId.setMaxWidth(1f * Integer.MAX_VALUE * 15); // 15% de la largeur
        colType.setMaxWidth(1f * Integer.MAX_VALUE * 15); // 15% de la largeur
        colDateAnalyse.setMaxWidth(1f * Integer.MAX_VALUE * 15); // 15% de la largeur
        colDonneesAnalyse.setMaxWidth(1f * Integer.MAX_VALUE * 25); // 25% de la largeur
        colDiagnostic.setMaxWidth(1f * Integer.MAX_VALUE * 25); // 25% de la largeur
        colDetails.setMaxWidth(1f * Integer.MAX_VALUE * 5); // 5% de la largeur

        loadAnalyses();
    }

    @FXML
    private void addAnalyse() {
        showAnalyseForm("Ajouter Analyse", null);
    }

    @FXML
    private void editAnalyse() {
        Analyse selectedAnalyse = tableAnalyses.getSelectionModel().getSelectedItem();
        if (selectedAnalyse != null) {
            showAnalyseForm("Modifier Analyse", selectedAnalyse);
        } else {
            showAlert("Erreur", "Veuillez sélectionner une analyse à modifier.");
        }
    }

    @FXML
    private void deleteAnalyse() {
        Analyse selectedAnalyse = tableAnalyses.getSelectionModel().getSelectedItem();
        if (selectedAnalyse != null) {
            try {
                serviceAnalyse.supprimer(selectedAnalyse.getId());
                loadAnalyses();
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression : " + e.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une analyse à supprimer.");
        }
    }

    private void showAnalyseForm(String title, Analyse analyse) {
        try {
            // Charger FormAnalyse.fxml pour Patient (ou ajuster selon le rôle)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/FormAnalyse.fxml"));
            Parent root = loader.load();
            AnalyseFormController controller = loader.getController();
            controller.setAnalyse(analyse);
            controller.setListController(this);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            loadAnalyses();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire : " + e.getMessage());
        }
    }

    private void showDetails(Analyse analyse) {
        try {
            // Charger AnalyseDetails.fxml en fonction du rôle
            String fxmlPath = "/fxml/Patient/AnalyseDetails.fxml";
            if (getClass().getResource("/fxml/Doctor/AnalyseListDoctor.fxml") != null) {
                fxmlPath = "/fxml/Doctor/AnalyseDetailsDoctor.fxml";
            } else if (getClass().getResource("/fxml/Admin/AnalyseListAdmin.fxml") != null) {
                fxmlPath = "/fxml/Admin/AnalyseDetailsAdmin.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            AnalyseDetailsController controller = loader.getController();
            controller.setAnalyse(analyse);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'Analyse");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger les détails : " + e.getMessage());
        }
    }

    public void filterByDossierId(int dossierId) {
        try {
            List<Analyse> analyses = serviceAnalyse.getByDossierId(dossierId);
            tableAnalyses.getItems().setAll(analyses);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des analyses pour le dossier : " + e.getMessage());
        }
    }

    private void loadAnalyses() {
        try {
            List<Analyse> analyses = serviceAnalyse.afficher();
            tableAnalyses.getItems().setAll(analyses);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des analyses : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}