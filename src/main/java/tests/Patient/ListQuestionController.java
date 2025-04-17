package tests.Patient;

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

public class ListQuestionController {
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> titleColumn;
    @FXML private TableColumn<Question, Specialite> specialiteColumn;
    @FXML private TableColumn<Question, String> imageColumn;
    @FXML private TableColumn<Question, LocalDateTime> dateColumn;
    @FXML private TableColumn<Question, Boolean> visibleColumn;
    @FXML private TableColumn<Question, Void> actionsColumn;
    @FXML private Button addButton;
    @FXML private TextField searchField;

    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionsList = FXCollections.observableArrayList();
    private ObservableList<Question> filteredQuestionsList = FXCollections.observableArrayList();
    private final int currentPatientId = 1;

    @FXML
    public void initialize() {
        // Appliquer le style CSS de base
        String basicStyle = "-fx-padding: 5 10; -fx-font-weight: bold; -fx-background-radius: 4;";
        addButton.setStyle(basicStyle + "-fx-background-color: #2d5985; -fx-text-fill: white;");
        searchField.setStyle("-fx-background-radius: 15; -fx-padding: 5 10;");

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

        // Style de base pour le tableau
        questionsTable.setStyle("-fx-background-color: white; -fx-table-cell-border-color: transparent;");

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
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final Button responsesButton = new Button("Réponses");
            private final HBox hBox = new HBox(5, editButton, deleteButton, responsesButton);

            {
                // Style de base pour tous les boutons
                String buttonStyle = "-fx-padding: 3 8; -fx-font-size: 12; -fx-font-weight: bold; -fx-background-radius: 4;";

                editButton.setStyle(buttonStyle + "-fx-background-color: #2d5985; -fx-text-fill: white;");
                deleteButton.setStyle(buttonStyle + "-fx-background-color: #2d5985; -fx-text-fill: white;");
                responsesButton.setStyle(buttonStyle + "-fx-background-color: #2d5985; -fx-text-fill: white;");

                // Effet hover simple
                setupHoverEffect(editButton, "#3a6b9c");
                setupHoverEffect(deleteButton, "#e53935");
                setupHoverEffect(responsesButton, "#3a6b9c");

                editButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    handleEditQuestion(question);
                });

                deleteButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    handleDeleteQuestion(question);
                });

                responsesButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    handleShowResponses(question);
                });
            }

            private void setupHoverEffect(Button button, String hoverColor) {
                String originalStyle = button.getStyle();
                button.setOnMouseEntered(e -> button.setStyle(originalStyle + "-fx-background-color: " + hoverColor + ";"));
                button.setOnMouseExited(e -> button.setStyle(originalStyle));
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
        questionsList.addAll(questionService.afficher().stream()
                .filter(q -> q.getPatient().getId() == currentPatientId)
                .toList());
        handleSearch();
    }

    @FXML
    private void handleAddQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/AddQuestion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Poser une nouvelle question");
            stage.showAndWait();

            loadQuestions();
        } catch (IOException | SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditQuestion(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/EditQuestion.fxml"));
            Parent root = loader.load();

            EditQuestionController controller = loader.getController();
            controller.setQuestion(question);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier la question");
            stage.showAndWait();

            loadQuestions();
        } catch (IOException | SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire", e.getMessage(), Alert.AlertType.ERROR);
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Patient/ListResponses.fxml"));
            Parent root = loader.load();

            ListResponsesController controller = loader.getController();
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