package utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GoogleApiUtil {
    private static final String APPLICATION_NAME = "Tbibi Medical Platform";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "config/google_credentials.json";

    /**
     * Global instance of the scopes required by this application.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Create a Google Meet link for a consultation
     *
     * @param consultationTitle The title of the consultation
     * @param patientName The name of the patient
     * @param doctorName The name of the doctor
     * @param startTime The start time of the consultation
     * @param endTime The end time of the consultation
     * @return The Google Meet link
     */
    public static String createGoogleMeetLink(String consultationTitle, String patientName, String doctorName,
                                              java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Create event with detailed information
            Event event = new Event()
                    .setSummary("Consultation: " + consultationTitle)
                    .setDescription("Consultation virtuelle entre Dr. " + doctorName + " et " + patientName);

            // Set start time
            DateTime startDateTime = new DateTime(java.sql.Timestamp.valueOf(startTime).getTime());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("UTC");
            event.setStart(start);

            // Set end time
            DateTime endDateTime = new DateTime(java.sql.Timestamp.valueOf(endTime).getTime());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("UTC");
            event.setEnd(end);

            // Add conference data request
            ConferenceData conferenceData = new ConferenceData()
                    .setCreateRequest(new CreateConferenceRequest().setRequestId(UUID.randomUUID().toString()));
            event.setConferenceData(conferenceData);

            // Insert the event and get the created event with the meet link
            Event createdEvent = service.events().insert("primary", event)
                    .setConferenceDataVersion(1)
                    .execute();

            // Return the Google Meet link
            return createdEvent.getHangoutLink();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to a Jitsi link in case of errors
            return "https://meet.jit.si/tbibi-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    /**
     * Test the Google Calendar API connection
     *
     * @return true if the connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // List the next 10 events from the primary calendar.
            service.events().list("primary").setMaxResults(1).execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}