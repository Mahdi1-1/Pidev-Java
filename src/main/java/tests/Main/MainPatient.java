package tests.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

public class MainPatient extends Application {
    private static String utilisateurIdArg; // Variable statique pour stocker l'ID de l'utilisateur

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger l'interface principale (Main.fxml)
        URL location = getClass().getResource("/fxml/Main.fxml");
        if (location == null) {
            throw new RuntimeException("Impossible de trouver /fxml/Main.fxml dans les ressources");
        }
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();

        // Passer l'utilisateur_id au MainController
        MainController controller = loader.getController();
        controller.setUtilisateurId(utilisateurIdArg);

        Scene scene = new Scene(root, 849, 552);
        scene.setFill(null); // Rendre la scène transparente

        primaryStage.setTitle("Gestion Médicale - Patient");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Récupérer l'ID de l'utilisateur à partir des arguments
        if (args.length > 0) {
            utilisateurIdArg = args[0]; // Premier argument = ID de l'utilisateur
        }
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