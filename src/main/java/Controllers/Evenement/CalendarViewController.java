package Controllers.Evenement;

import entities.Evenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarViewController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYearLabel;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML
    private Button backButton;

    @FXML
    private ListView<Evenement> eventListView;

    private List<Evenement> eventList;
    private YearMonth currentYearMonth;

    public void setEventList(List<Evenement> eventList) {
        this.eventList = eventList;
        currentYearMonth = YearMonth.now();
        updateCalendar();
        // Initialize ListView
        eventListView.setVisible(false); // Hidden until a date is clicked
        eventListView.setOnMouseClicked(this::handleEventSelection);
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        monthYearLabel.setText(currentYearMonth.getMonth() + " " + currentYearMonth.getYear());

        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("header-label");
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 = Monday
        int daysInMonth = currentYearMonth.lengthOfMonth();

        List<LocalDate> eventDates = eventList.stream()
                .map(Evenement::getDateDebut)
                .filter(date -> date.getYear() == currentYearMonth.getYear() &&
                        date.getMonth() == currentYearMonth.getMonth())
                .collect(Collectors.toList());

        int row = 1;
        int col = dayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setPadding(new Insets(10));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefSize(60, 40);
            if (eventDates.contains(date)) {
                dayLabel.getStyleClass().add("event-day");
                dayLabel.setOnMouseClicked(e -> showEventsForDate(date));
            } else {
                dayLabel.getStyleClass().add("label");
            }

            calendarGrid.add(dayLabel, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private void showEventsForDate(LocalDate date) {
        List<Evenement> eventsOnDate = eventList.stream()
                .filter(e -> e.getDateDebut().equals(date))
                .collect(Collectors.toList());

        eventListView.getItems().clear();
        if (eventsOnDate.isEmpty()) {
            eventListView.setVisible(false);
        } else {
            eventListView.getItems().addAll(eventsOnDate);
            eventListView.setVisible(true);
        }
    }

    private void handleEventSelection(MouseEvent event) {
        Evenement selectedEvent = eventListView.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            try {
                System.out.println("Loading EvenementDetails.fxml from: " + getClass().getResource("/EvenementDetails.fxml"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EvenementDetails.fxml"));
                if (loader.getLocation() == null) {
                    throw new IOException("Cannot find EvenementDetails.fxml");
                }
                Parent detailsView = loader.load();
                EvenementDetails controller = loader.getController();
                controller.setEvenement(selectedEvent);

                Stage stage = (Stage) eventListView.getScene().getWindow();
                Scene scene = new Scene(detailsView);
                stage.setScene(scene);
                stage.setTitle("Détails de l'Événement");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec du chargement des détails");
                alert.setContentText("Impossible de trouver EvenementDetails.fxml. Vérifiez le chemin du fichier.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
        eventListView.setVisible(false); // Hide ListView when changing months
    }

    @FXML
    private void onNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
        eventListView.setVisible(false); // Hide ListView when changing months
    }

    @FXML
    private void onBackButtonClick(ActionEvent event) {
        try {
            String fxmlPath = "/AfficherEvent.fxml"; // Update to "/Controllers/Evenement/AfficherEvent.fxml" if in subdirectory
            System.out.println("Loading AfficherEvent.fxml from: " + getClass().getResource(fxmlPath));
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find AfficherEvent.fxml");
            }
            Parent afficherEventView = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(afficherEventView);
            stage.setScene(scene);
            stage.setTitle("Liste des Événements");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec du chargement de la liste des événements");
            alert.setContentText("Impossible de trouver AfficherEvent.fxml. Vérifiez le chemin du fichier.");
            alert.showAndWait();
        }
    }
}