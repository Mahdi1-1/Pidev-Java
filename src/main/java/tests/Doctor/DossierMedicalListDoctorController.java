package tests.Doctor;

import entities.DossierMedical;
import services.ServiceDossierMedical;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DossierMedicalListDoctorController {
    @FXML private TableView<DossierMedical> dossierTable;
    @FXML private TableColumn<DossierMedical, Integer> idColumn;
    @FXML private TableColumn<DossierMedical, Integer> utilisateurIdColumn;
    @FXML private TableColumn<DossierMedical, String> dateColumn;
    @FXML private TableColumn<DossierMedical, String> fichierColumn;
    @FXML private TableColumn<DossierMedical, String> uniteColumn;
    @FXML private TableColumn<DossierMedical, String> mesureColumn;
    @FXML private TableColumn<DossierMedical, Void> detailsColumn;

    private ObservableList<DossierMedical> dossierList;
    // Éléments pour le filtrage et la recherche
    @FXML private ChoiceBox<String> uniteFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TextField searchField;

    // Éléments pour la pagination
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageLabel;
    private final ServiceDossierMedical serviceDossierMedical;
    private String doctorEmail;
    private int currentPage = 1;
    private final int pageSize = 3; // Nombre de dossiers par page
    private int totalDossiers = 0;
    public DossierMedicalListDoctorController() throws SQLException {
        this.serviceDossierMedical = new ServiceDossierMedical();
    }
    @FXML
    public void initialize() {
        // Configurer les colonnes du TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        utilisateurIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        fichierColumn.setCellValueFactory(new PropertyValueFactory<>("fichier"));
        uniteColumn.setCellValueFactory(new PropertyValueFactory<>("unite"));
        mesureColumn.setCellValueFactory(new PropertyValueFactory<>("mesure"));

        // Configurer le double-clic pour ouvrir les détails
        dossierTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                DossierMedical selectedDossier = dossierTable.getSelectionModel().getSelectedItem();
                if (selectedDossier != null) {
                    showDetails(selectedDossier);
                }
            }
        });

        // Remplir le ChoiceBox avec les unités distinctes
        try {
            uniteFilter.setItems(FXCollections.observableArrayList(serviceDossierMedical.getDistinctUnites()));
            uniteFilter.getItems().add(0, ""); // Ajouter une option vide pour "aucun filtre"
            uniteFilter.setValue(""); // Sélectionner l'option vide par défaut
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Ajouter un écouteur sur le TextField pour une recherche dynamique
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1; // Réinitialiser à la première page lors d'une nouvelle recherche
            loadDossiers();
        });

        // Ajouter des écouteurs sur les autres filtres pour déclencher loadDossiers
        uniteFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1;
            loadDossiers();
        });

        dateFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 1;
            loadDossiers();
        });

        // Charger les dossiers initiaux
        loadDossiers();
    }
    private void chargerDossiers() {
        try {
            ServiceDossierMedical service = new ServiceDossierMedical();
            List<DossierMedical> dossiers = service.afficher();
            dossierList.clear();
            dossierList.addAll(dossiers);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des dossiers : " + e.getMessage());
        }
    }

    private void showDetails(DossierMedical dossier) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Doctor/DossierMedicalDetailsDoctor.fxml"));
            Parent root = loader.load();

            DossierMedicalDetailsDoctorController controller = loader.getController();
            controller.setDossier(dossier);

            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(true);
            stage.showAndWait();

            chargerDossiers();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du dossier : " + e.getMessage());
        }
    }

    private void loadDossiers() {
        try {
            // Récupérer les critères de filtrage
            String unite = uniteFilter.getValue();
            LocalDate date = dateFilter.getValue();
            String searchText = searchField.getText();

            // Compter le nombre total de dossiers pour la pagination
            totalDossiers = serviceDossierMedical.countDossiers(unite, date, searchText);
            int totalPages = (int) Math.ceil((double) totalDossiers / pageSize);

            // S'assurer que la page actuelle est valide
            if (currentPage < 1) {
                currentPage = 1;
            } else if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }

            // Charger les dossiers pour la page actuelle
            dossierTable.getItems().setAll(serviceDossierMedical.filterDossiers(unite, date, searchText, currentPage, pageSize));

            // Mettre à jour l'interface de pagination
            pageLabel.setText("Page " + currentPage + " / " + (totalPages == 0 ? 1 : totalPages));
            prevPageButton.setDisable(currentPage == 1);
            nextPageButton.setDisable(currentPage == totalPages || totalPages == 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void applyFilters() {
        currentPage = 1; // Réinitialiser à la première page lors de l'application des filtres
        loadDossiers();
    }

    @FXML
    private void resetFilters() {
        uniteFilter.setValue("");
        dateFilter.setValue(null);
        searchField.setText("");
        currentPage = 1;
        loadDossiers();
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadDossiers();
        }
    }

    @FXML
    private void nextPage() {
        int totalPages = (int) Math.ceil((double) totalDossiers / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            loadDossiers();
        }
    }
    @FXML
    private void close() {
        Stage stage = (Stage) dossierTable.getScene().getWindow();
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