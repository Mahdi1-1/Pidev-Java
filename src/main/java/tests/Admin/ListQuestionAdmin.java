package tests.Admin;

import entities.Question;
import enums.Specialite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.QuestionService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ListQuestionAdmin {
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> titleColumn;
    @FXML private TableColumn<Question, Specialite> specialiteColumn;
    @FXML private TableColumn<Question, String> imageColumn;
    @FXML private TableColumn<Question, LocalDateTime> dateColumn;
    @FXML private TableColumn<Question, Boolean> visibleColumn;
    @FXML private TableColumn<Question, Void> actionsColumn;
    @FXML private TextField searchField;

    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionsList = FXCollections.observableArrayList();
    private ObservableList<Question> filteredQuestionsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        specialiteColumn.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        visibleColumn.setCellValueFactory(new PropertyValueFactory<>("visible"));

        // Configure Specialite column
        setupSpecialiteColumn();

        // Configure Date column
        setupDateColumn();

        // Configure Image column
        setupImageColumn();

        // Configure Actions column
        setupActionsColumn();

        // Initialize filtered list
        filteredQuestionsList.addAll(questionsList);
        questionsTable.setItems(filteredQuestionsList);

        // Load data
        try {
            loadQuestions();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'initialisation", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSpecialiteColumn() {
        specialiteColumn.setCellFactory(column -> new TableCell<Question, Specialite>() {
            @Override
            protected void updateItem(Specialite specialite, boolean empty) {
                super.updateItem(specialite, empty);
                if (empty || specialite == null) {
                    setText(null);
                } else {
                    setText(specialite.getValue());
                }
            }
        });
    }

    private void setupDateColumn() {
        dateColumn.setCellFactory(column -> new TableCell<Question, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });
    }

    private void setupImageColumn() {
        imageColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Question, String> call(TableColumn<Question, String> param) {
                return new TableCell<>() {
                    private final ImageView imageView = new ImageView();
                    {
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);
                        imageView.setPreserveRatio(true);
                    }

                    @Override
                    protected void updateItem(String imagePath, boolean empty) {
                        super.updateItem(imagePath, empty);
                        if (empty || imagePath == null || imagePath.isEmpty()) {
                            setGraphic(null);
                        } else {
                            try {
                                String resourcePath = "/" + imagePath;
                                Image image = new Image(getClass().getResourceAsStream(resourcePath));
                                if (image.isError()) {
                                    throw new IOException("Image not found: " + resourcePath);
                                }
                                imageView.setImage(image);
                                setGraphic(imageView);
                            } catch (Exception e) {
                                setGraphic(new Label("Image non trouvée"));
                            }
                        }
                    }
                };
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");
            private final Button responsesButton = new Button("Voir réponses");
            private final HBox hBox = new HBox(10, deleteButton, responsesButton);

            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                responsesButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

                deleteButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    handleDeleteQuestion(question);
                });

                responsesButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    handleShowResponses(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hBox);
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        filteredQuestionsList.clear();

        if (searchText.isEmpty()) {
            filteredQuestionsList.addAll(questionsList);
        } else {
            filteredQuestionsList.addAll(questionsList.stream()
                    .filter(q -> q.getTitre().toLowerCase().contains(searchText) ||
                            (q.getSpecialite() != null && q.getSpecialite().getValue().toLowerCase().contains(searchText)))
                    .toList());
        }
    }

    private void loadQuestions() throws SQLException {
        questionsList.clear();
        questionsList.addAll(questionService.afficher());
        handleSearch();
    }

    private void handleDeleteQuestion(Question question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la question");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette question ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                questionService.supprimer(question.getId());
                loadQuestions();
                showAlert("Succès", "Question supprimée", "La question a été supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleShowResponses(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/ListResponsesAdmin.fxml"));
            Parent root = loader.load();

            ListResponsesAdminController controller = loader.getController();
            controller.setQuestion(question);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Réponses à la question: " + question.getTitre());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture des réponses", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}