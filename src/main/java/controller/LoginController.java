package controller;

import dao.UserDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private FlowPane imageContainer;
    @FXML private ScrollPane mainScrollPane;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Maximize window on startup
        Platform.runLater(() -> {
            if (emailField.getScene() != null) {
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setMaximized(true);
            }
        });

        String[] descriptions = {
            "Experience luxury and comfort in our spacious rooms.",
            "Enjoy breathtaking views from our premium suites.",
            "Relax and unwind in our world-class spa facilities.",
            "Savor exquisite culinary delights at our fine dining restaurant.",
            "Host memorable events in our elegant banquet halls."
        };

        for (int i = 1; i <= 5; i++) {
            addImageToGrid("/images/login" + i + ".png", descriptions[i - 1]);
        }

        // New additional images
        String[] newDescriptions = {
            "Our modern and fully equipped fitness center.",
            "Relax by our stunning outdoor pool.",
            "Enjoy a cocktail at our rooftop bar.",
            "Our spacious and comfortable lobby area.",
            "A view of our standard room interior.",
            "Our dedicated concierge team ready to assist you."
        };

        for (int i = 1; i <= 6; i++) {
            addImageToGrid("/images/" + i + ".png", newDescriptions[i - 1]);
        }

        // Add hotel.png at the end
        StackPane hotelImagePane = new StackPane();
        hotelImagePane.setAlignment(Pos.CENTER);
        hotelImagePane.setPrefWidth(400); // Adjusted width

        ImageView hotelImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/hotel.png")));
        hotelImageView.setFitWidth(380);
        hotelImageView.setPreserveRatio(true);

        Button bookNowButton = new Button("Book Now");
        bookNowButton.getStyleClass().add("btn-primary");
        bookNowButton.setOnAction(e -> {
            if (mainScrollPane != null) {
                mainScrollPane.setVvalue(0.0);
            }
        });

        hotelImagePane.getChildren().addAll(hotelImageView, bookNowButton);
        imageContainer.getChildren().add(hotelImagePane);
    }

    private void addImageToGrid(String imagePath, String description) {
        VBox itemBox = new VBox(10);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setStyle("-fx-padding: 10; -fx-background-color: transparent;");
        itemBox.setPrefWidth(400); // Adjusted width to fit 2 per row

        try {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            imageView.setFitWidth(380); // Adjusted image width
            imageView.setPreserveRatio(true);

            // Add hover effect
            imageView.setOnMouseEntered(e -> {
                imageView.setScaleX(1.05);
                imageView.setScaleY(1.05);
            });
            imageView.setOnMouseExited(e -> {
                imageView.setScaleX(1.0);
                imageView.setScaleY(1.0);
            });

            Label descriptionLabel = new Label(description);
            descriptionLabel.setWrapText(true);
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -primary-text-color; -fx-alignment: CENTER;");
            descriptionLabel.setMaxWidth(380);

            itemBox.getChildren().addAll(imageView, descriptionLabel);
            imageContainer.getChildren().add(itemBox);
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields");
            return;
        }

        User user = userDAO.authenticate(email, password);
        if (user != null) {
            try {
                FXMLLoader loader;
                String fxmlPath;
                String title;

                if (user.getRole().equals("admin")) {
                    fxmlPath = "/view/adminDashboard.fxml";
                    title = "Hilton Hotel - Admin Dashboard";
                } else {
                    fxmlPath = "/view/dashboard.fxml";
                    title = "Hilton Hotel - Dashboard";
                }

                loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); // Add stylesheet

                // Pass user data to dashboard
                if (user.getRole().equals("admin")) {
                    AdminDashboardController controller = loader.getController();
                    controller.setCurrentUser(user);
                } else {
                    DashboardController controller = loader.getController();
                    controller.setCurrentUser(user);
                }

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle(title);
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
                stage.setMaximized(true); // Maximize the window for a full-screen experience
                stage.setResizable(true); // Ensure the user can still resize if needed
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid email or password");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); // Add stylesheet

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setMaximized(true); // Ensure register screen is also maximized
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showAmenitiesDialog() {
        showInfoDialog("Amenities", 
            "• High-speed Wi-Fi\n" +
            "• 24-hour room service\n" +
            "• Access to our state-of-the-art gym and pool\n" +
            "• Air-conditioned rooms\n" +
            "• Comfortable beds with fresh linens\n" +
            "• Private bathroom with toiletries\n" +
            "• Flat-screen TV with cable/satellite channels\n" +
            "• Work desk and chair\n" +
            "• Wardrobe and luggage storage\n" +
            "• Tea and coffee making facilities"
        );
    }

    @FXML
    private void showBookingPoliciesDialog() {
        showInfoDialog("Booking Policies", 
            "• Check-in time: 2:00 PM\n" +
            "• Check-out time: 12:00 PM\n" +
            "• All bookings require payment confirmation within 1 hour.\n" +
            "• Cancellations are subject to a 50% fee.\n" +
            "• No smoking in rooms or public areas.\n" +
            "• Pets are not allowed.\n" +
            "• Guests must be at least 18 years old to make a reservation.\n" +
            "• A valid government-issued ID is required at check-in.\n" +
            "• Reservations can be made online through the hotel booking system.\n" +
            "• Full or partial payment may be required at the time of booking, depending on the selected rate plan."
        );
    }

    @FXML
    private void showPopularFacilitiesDialog() {
        showInfoDialog("Most Popular Facilities", 
            "• Airport shuttle (free)\n" +
            "• Non-smoking rooms\n" +
            "• Spa and wellness centre\n" +
            "• Fitness centre\n" +
            "• Facilities for disabled guests\n" +
            "• Free parking\n" +
            "• Free WiFi\n" +
            "• Tea/coffee maker in all rooms\n" +
            "• Bar\n" +
            "• 24/7 front desk and customer support\n" +
            "• Daily housekeeping service\n" +
            "• Elevator access\n" +
            "• On-site restaurant or breakfast service\n" +
            "• Swimming pool\n" +
            "• Business center / meeting rooms\n" +
            "• Laundry and dry-cleaning services\n" +
            "• Free or paid parking"
        );
    }

    @FXML
    private void showBathroomFacilitiesDialog() {
        showInfoDialog("Bathroom Facilities", 
            "• Toilet paper\n" +
            "• Towels\n" +
            "• Bidet\n" +
            "• Towels/sheets (extra fee)\n" +
            "• Additional toilet\n" +
            "• Bath or shower\n" +
            "• Slippers\n" +
            "• Private bathroom\n" +
            "• Toilet\n" +
            "• Free toiletries\n" +
            "• Hairdryer\n" +
            "• Shower"
        );
    }

    @FXML
    private void showBedroomFacilitiesDialog() {
        showInfoDialog("Bedroom Facilities", 
            "• Linen\n" +
            "• Wardrobe or closet\n" +
            "• Alarm clock"
        );
    }

    @FXML
    private void showAdditionalServicesDialog() {
        showInfoDialog("Additional Services", 
            "• Airport shuttle (on request)\n" +
            "• Room service\n" +
            "• Concierge and tour assistance\n" +
            "• Special assistance for guests with disabilities"
        );
    }

    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        
        alert.showAndWait();
    }
}