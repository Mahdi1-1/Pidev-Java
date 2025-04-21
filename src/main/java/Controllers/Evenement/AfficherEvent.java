package Controllers.Evenement;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.EvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherEvent {

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button calendarButton;

    private final EvenementService service = new EvenementService();
    private List<Evenement> eventList;

    private static final String BUTTON_STYLE = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 10px; -fx-font-size: 14px;";
    private static final String BUTTON_HOVER_STYLE = "-fx-background-color: #1976D2; -fx-text-fill: white;";

    @FXML
    public void initialize() {
        try {
            System.out.println("rootVBox: " + rootVBox); // Debug output
            if (rootVBox == null) {
                System.err.println("Warning: rootVBox is null!");
            }
            eventList = service.recuperer();
            sortComboBox.getItems().addAll("Titre A-Z", "Titre Z-A", "Date de début", "Date de début (décroissant)", "Statut");
            sortComboBox.setValue("Titre A-Z");
            sortComboBox.setOnAction(this::onSort);
            searchField.textProperty().addListener((observable, oldValue, newValue) -> onSearch());
            calendarButton.setOnMouseEntered(e -> calendarButton.setStyle(BUTTON_HOVER_STYLE));
            calendarButton.setOnMouseExited(e -> calendarButton.setStyle(BUTTON_STYLE));
            showEvents(eventList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEvents(List<Evenement> list) {
        cardsContainer.getChildren().clear();
        try {
            for (Evenement event : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
                Node eventCard = loader.load();
                EvenementDetails controller = loader.getController();
                controller.setEvenement(event);
                cardsContainer.getChildren().add(eventCard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        String searchText = searchField.getText().toLowerCase();
        List<Evenement> filteredEvents = eventList.stream()
                .filter(event -> event.getTitre().toLowerCase().contains(searchText) ||
                        event.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        showEvents(filteredEvents);
    }

    @FXML
    private void onSort(ActionEvent event) {
        String selectedOption = sortComboBox.getValue();
        List<Evenement> sortedList = new ArrayList<>(eventList);

        switch (selectedOption) {
            case "Titre A-Z":
                sortedList.sort(Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Titre Z-A":
                sortedList.sort(Comparator.comparing(Evenement::getTitre, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Date de début":
                sortedList.sort(Comparator.comparing(Evenement::getDateDebut));
                break;
            case "Date de début (décroissant)":
                sortedList.sort(Comparator.comparing(Evenement::getDateDebut).reversed());
                break;
            case "Statut":
                sortedList.sort(Comparator.comparing(Evenement::getStatut, String.CASE_INSENSITIVE_ORDER));
                break;
            default:
                break;
        }

        showEvents(sortedList);
    }

    @FXML
    private void onCalendarButtonClick(ActionEvent event) {
        try {
            // Debug: Print resource path
            System.out.println("Loading CalendarView.fxml from: " + getClass().getResource("/CalendarView.fxml"));

            // Load the CalendarView FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CalendarView.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find CalendarView.fxml");
            }
            Parent calendarView = loader.load();

            // Get the CalendarViewController and pass the eventList
            CalendarViewController controller = loader.getController();
            controller.setEventList(eventList);

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(calendarView);
            stage.setScene(scene);
            stage.setTitle("Event Calendar");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec du chargement du calendrier");
            alert.setContentText("Impossible de trouver CalendarView.fxml. Vérifiez le chemin du fichier.");
            alert.showAndWait();
        }
    }
}