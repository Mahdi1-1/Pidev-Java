package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import utils.SceneSwitch;

public class BaseWindow {

    @FXML
    private AnchorPane mainRouter;




    @FXML
    void goCategory(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherCategorieEv.fxml");
    }
    @FXML
    void goEvents(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherEventBack.fxml");
    }
    @FXML
    void gofrontEvents(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherEvent.fxml");
    }






}
