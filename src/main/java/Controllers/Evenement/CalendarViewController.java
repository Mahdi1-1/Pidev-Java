package Controllers.Evenement;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarViewController {

    @FXML
    private WebView webView;

    private WebEngine webEngine;
    private List<Evenement> eventList;

    @FXML
    public void initialize() {
        webEngine = webView.getEngine();

        // Charger le HTML contenant FullCalendar
        String htmlContent = getCalendarHtml();
        webEngine.loadContent(htmlContent);

        // Attendre que la page soit chargée pour injecter les événements
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                loadEvents();
            }
        });
    }

    // Setter pour la liste des événements
    public void setEventList(List<Evenement> eventList) {
        this.eventList = eventList;
    }

    // Générer le contenu HTML avec FullCalendar
    private String getCalendarHtml() {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='utf-8' />
            <link href='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.css' rel='stylesheet' />
            <script src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.js'></script>
            <style>
                body {
                    margin: 40px 10px;
                    padding: 0;
                    font-family: Arial, Helvetica Neue, Helvetica, sans-serif;
                    font-size: 14px;
                }
                #calendar {
                    max-width: 900px;
                    margin: 0 auto;
                }
            </style>
        </head>
        <body>
            <div id='calendar'></div>
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    var calendarEl = document.getElementById('calendar');
                    window.calendar = new FullCalendar.Calendar(calendarEl, {
                        initialView: 'dayGridMonth',
                        headerToolbar: {
                            left: 'prev,next today',
                            center: 'title',
                            right: 'dayGridMonth,timeGridWeek,dayGridDay'
                        },
                        events: [], // Les événements seront injectés dynamiquement
                        eventClick: function(info) {
                            window.javaEventHandler.eventClicked(info.event.id);
                        }
                    });
                    calendar.render();
                });

                // Fonction pour charger les événements dynamiquement
                function loadEvents(events) {
                    window.calendar.getEvents().forEach(event => event.remove());
                    events.forEach(event => window.calendar.addEvent(event));
                    window.calendar.render();
                }
            </script>
        </body>
        </html>
        """;
    }

    // Charger les événements dans FullCalendar
    private void loadEvents() {
        if (eventList == null || eventList.isEmpty()) {
            return;
        }

        // Convertir les événements en JSON
        JSONArray eventsArray = new JSONArray();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Evenement event : eventList) {
            JSONObject eventJson = new JSONObject();
            eventJson.put("id", event.getId());
            eventJson.put("title", event.getTitre());
            eventJson.put("start", event.getDateDebut().format(formatter));
            eventJson.put("description", event.getDescription());
            eventJson.put("location", event.getLieu());
            eventsArray.put(eventJson);
        }

        // Injecter les événements dans le calendrier via JavaScript
        webEngine.executeScript("loadEvents(" + eventsArray.toString() + ")");

        // Configurer le handler pour les clics sur les événements
        JSObject window = (JSObject) webEngine.executeScript("window");
        window.setMember("javaEventHandler", new EventHandler());
    }

    // Classe pour gérer les clics sur les événements
    public class EventHandler {
        public void eventClicked(String eventId) {
            Evenement clickedEvent = eventList.stream()
                    .filter(e -> e.getId().toString().equals(eventId))
                    .findFirst()
                    .orElse(null);

            if (clickedEvent != null) {
                try {
                    // Charger la vue EvenementDetails.fxml
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/EvenementDetails.fxml"));
                    if (loader.getLocation() == null) {
                        throw new IOException("Cannot find EvenementDetails.fxml");
                    }
                    Parent eventDetailsView = loader.load();

                    // Passer l'événement au contrôleur EvenementDetails
                    EvenementDetails controller = loader.getController();
                    controller.setEvenement(clickedEvent);

                    // Obtenir la scène actuelle et passer à la vue des détails
                    Stage stage = (Stage) webView.getScene().getWindow();
                    Scene scene = new Scene(eventDetailsView);
                    stage.setScene(scene);
                    stage.setTitle("Détails de l'événement");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Échec du chargement des détails de l'événement");
                    alert.setContentText("Impossible de trouver EvenementDetails.fxml. Vérifiez le chemin du fichier.");
                    alert.showAndWait();
                }
            }
        }
    }

}