package org.example.Controllers;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Entities.CartItem;
import org.example.Entities.Produit;
import org.example.Services.CartItemService;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the shopping cart view
 */
public class CartViewController implements Initializable {

    @FXML
    private TableView<CartItem> cartTableView;

    @FXML
    private TableColumn<CartItem, ImageView> colImage;

    @FXML
    private TableColumn<CartItem, String> colName;

    @FXML
    private TableColumn<CartItem, String> colPrice;

    @FXML
    private TableColumn<CartItem, Spinner<Integer>> colQuantity;

    @FXML
    private TableColumn<CartItem, String> colTotal;

    @FXML
    private TableColumn<CartItem, Button> colAction;

    @FXML
    private Label itemCountLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Button clearCartButton;

    @FXML
    private Button checkoutButton;

    private CartItemService cartItemService;
    private ObservableList<CartItem> cartItems;
    private Runnable cartUpdatedCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup table columns
        setupTableColumns();

        // Listen for changes in the table to update totals
        cartTableView.getItems().addListener((ListChangeListener<CartItem>) c -> {
            updateCartSummary();
        });
    }

    /**
     * Set up the cart item service
     * @param service CartItemService instance
     */
    public void setCartItemService(CartItemService service) {
        this.cartItemService = service;

        // Load cart items
        loadCartItems();

        // Update cart summary
        updateCartSummary();
    }

    /**
     * Set a callback to run when cart is updated
     * @param callback Runnable to execute
     */
    public void setCartUpdatedCallback(Runnable callback) {
        this.cartUpdatedCallback = callback;
    }

    /**
     * Load cart items into the table
     */
    private void loadCartItems() {
        cartItems = FXCollections.observableArrayList(cartItemService.getAllCartItems());

        if (cartItems.isEmpty()) {
            // Show empty cart message
            showEmptyCartMessage();
            clearCartButton.setDisable(true);
            checkoutButton.setDisable(true);
        } else {
            // Show cart items
            cartTableView.setItems(cartItems);
            clearCartButton.setDisable(false);
            checkoutButton.setDisable(false);

            // Add with fade-in effect
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cartTableView);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    /**
     * Show message when cart is empty
     */
    private void showEmptyCartMessage() {
        cartTableView.setPlaceholder(new Label("Your cart is empty. Add some products to get started!"));
        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);

        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.getStyleClass().add("empty-cart-message");

        ImageView emptyCartImage = new ImageView(new Image(getClass().getResourceAsStream("/empty-cart.png")));
        emptyCartImage.setFitHeight(150);
        emptyCartImage.setFitWidth(150);
        emptyCartImage.setPreserveRatio(true);

        placeholder.getChildren().addAll(emptyCartImage, emptyLabel);
        cartTableView.setPlaceholder(placeholder);
    }

    /**
     * Setup table columns with cell factories
     */
    private void setupTableColumns() {
        // Setup image column
        colImage.setCellValueFactory(param -> {
            Produit product = param.getValue().getProduit();
            ImageView imageView = new ImageView();
            imageView.getStyleClass().add("product-image-small");

            // Try to load image if available
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                try {
                    File imageFile = new File(product.getImage());
                    if (imageFile.exists()) {
                        String imageUrl = imageFile.toURI().toURL().toString();
                        Image image = new Image(imageUrl, 60, 60, true, true, true);
                        imageView.setImage(image);
                    } else {
                        // Use placeholder image
                        imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
                    }
                } catch (Exception e) {
                    // Use placeholder image
                    imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
                }
            } else {
                // Use placeholder image
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }

            return new SimpleObjectProperty<>(imageView);
        });

        // Setup name column
        colName.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getProduit().getNom()));

        // Setup price column
        colPrice.setCellValueFactory(param ->
                new SimpleStringProperty(String.format("€%.2f", param.getValue().getProduit().getPrix())));

        // Setup quantity column with spinners
        colQuantity.setCellValueFactory(param -> {
            CartItem item = param.getValue();
            Spinner<Integer> spinner = new Spinner<>(1, 100, item.getQuantity());
            spinner.getStyleClass().add("quantity-spinner");
            spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                item.setQuantity(newValue);
                cartItemService.updateCartItemQuantity(item.getProduit().getId(), newValue);
                updateCartSummary();
                if (cartUpdatedCallback != null) {
                    cartUpdatedCallback.run();
                }
            });
            return new SimpleObjectProperty<>(spinner);
        });

        // Setup total column
        colTotal.setCellValueFactory(param ->
                new SimpleStringProperty(String.format("€%.2f", param.getValue().getTotal())));

        // Setup action column with remove button
        colAction.setCellValueFactory(param -> {
            Button removeBtn = new Button("✕");
            removeBtn.getStyleClass().add("remove-btn");

            removeBtn.setOnAction(event -> {
                CartItem item = param.getValue();
                cartItemService.removeCartItem(item.getProduit().getId());
                cartItems.remove(item);

                if (cartItems.isEmpty()) {
                    showEmptyCartMessage();
                    clearCartButton.setDisable(true);
                    checkoutButton.setDisable(true);
                }

                updateCartSummary();
                if (cartUpdatedCallback != null) {
                    cartUpdatedCallback.run();
                }
            });

            return new SimpleObjectProperty<>(removeBtn);
        });
    }

    /**
     * Update cart summary (item count, subtotal, total)
     */
    private void updateCartSummary() {
        int itemCount = 0;
        float subtotal = 0.0f;

        for (CartItem item : cartItems) {
            itemCount += item.getQuantity();
            subtotal += item.getTotal();
        }

        // Update labels
        itemCountLabel.setText(itemCount + (itemCount == 1 ? " item" : " items"));
        subtotalLabel.setText(String.format("€%.2f", subtotal));

        // For now, total equals subtotal, but could include shipping, taxes, etc.
        float total = subtotal;
        totalLabel.setText(String.format("€%.2f", total));
    }

    /**
     * Handle continue shopping button click
     */
    @FXML
    private void handleContinueShopping() {
        closeWindow();
    }

    /**
     * Handle clear cart button click
     */
    @FXML
    private void handleClearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Clear Shopping Cart");
        alert.setContentText("Are you sure you want to remove all items from your cart?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cartItemService.clearCart();
            cartItems.clear();
            showEmptyCartMessage();
            updateCartSummary();

            clearCartButton.setDisable(true);
            checkoutButton.setDisable(true);

            if (cartUpdatedCallback != null) {
                cartUpdatedCallback.run();
            }
        }
    }

    /**
     * Handle checkout button click
     */
    @FXML
    private void handleCheckout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Checkout");
        alert.setHeaderText("Proceed to Checkout");
        alert.setContentText("This is where you would continue to the checkout process.\n" +
                "For now, we'll just show this message.\n\n" +
                "Thank you for shopping with us!");

        alert.showAndWait();
    }

    /**
     * Close the cart window
     */
    private void closeWindow() {
        Stage stage = (Stage) cartTableView.getScene().getWindow();
        stage.close();
    }
}