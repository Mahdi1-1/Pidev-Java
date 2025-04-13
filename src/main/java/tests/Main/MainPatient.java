package tests.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import tests.ConsultationListController;
import tests.OrdonnanceListController;

public class MainPatient extends Application {
    private static final String PATIENT_ID = "1";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create tab pane
        TabPane tabPane = new TabPane();
        
        // Create and load consultations tab
        Tab consultationsTab = new Tab("Consultations");
        FXMLLoader consultationLoader = new FXMLLoader(getClass().getResource("/fxml/consultationlist.fxml"));
        consultationsTab.setContent(consultationLoader.load());
        ConsultationListController consultationController = consultationLoader.getController();
        consultationController.setUtilisateurId(PATIENT_ID);
        
        // Create and load ordonnances tab
        Tab ordonnancesTab = new Tab("Ordonnances");
        FXMLLoader ordonnanceLoader = new FXMLLoader(getClass().getResource("/fxml/ordonnancelist.fxml"));
        ordonnancesTab.setContent(ordonnanceLoader.load());
        OrdonnanceListController ordonnanceController = ordonnanceLoader.getController();
        ordonnanceController.setUtilisateurId(PATIENT_ID);
        
        // Add tabs to pane
        tabPane.getTabs().addAll(consultationsTab, ordonnancesTab);
        
        // Prevent tabs from being closed
        consultationsTab.setClosable(false);
        ordonnancesTab.setClosable(false);

        Scene scene = new Scene(tabPane);
        scene.getStylesheets().add(getClass().getResource("/Styles.css").toExternalForm());

        primaryStage.setTitle("Gestion Médicale - Patient");
        primaryStage.setScene(scene);
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}