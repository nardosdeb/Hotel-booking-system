package controller;

import dao.BookingDAO;
import dao.PaymentMethodDAO;
import dao.RoomDAO;
import dao.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.Booking;
import model.PaymentMethod;
import model.Room;
import model.User;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private FlowPane roomsContainer;
    @FXML private Button logoutButton;
    @FXML private ImageView logoImageView;

    // My Bookings Tab
    @FXML private TableView<Booking> myBookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdCol;
    @FXML private TableColumn<Booking, String> bookedRoomCol;
    @FXML private TableColumn<Booking, LocalDate> checkInCol;
    @FXML private TableColumn<Booking, LocalDate> checkOutCol;
    @FXML private TableColumn<Booking, Double> totalPriceCol;
    @FXML private TableColumn<Booking, String> bookingStatusCol;
    @FXML private TableColumn<Booking, String> paymentMethodCol;
    @FXML private TableColumn<Booking, String> paymentDetailsCol;

    private User currentUser;
    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentMethodDAO paymentMethodDAO = new PaymentMethodDAO();
    private final UserDAO userDAO = new UserDAO();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getName() + "!");
        balanceLabel.setText(String.format("Balance: ETB %.2f", user.getBalance()));
        loadAllRoomsIntoContainer();
        loadUserBookings();
    }

    @FXML
    public void initialize() {
        logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        // Setup my bookings table
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookedRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        bookingStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        paymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        paymentDetailsCol.setCellValueFactory(new PropertyValueFactory<>("paymentDetails"));
    }

    private void loadAllRoomsIntoContainer() {
        roomsContainer.getChildren().clear();
        List<Room> allRooms = roomDAO.getAllRooms();
        for (Room room : allRooms) {
            if ("available".equalsIgnoreCase(room.getStatus())) {
                roomsContainer.getChildren().add(createRoomCard(room));
            }
        }
    }

    private void loadUserBookings() {
        if (currentUser != null) {
            ObservableList<Booking> bookings = FXCollections.observableArrayList(bookingDAO.getUserBookings(currentUser.getUserId()));
            myBookingsTable.setItems(bookings);
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox(10);
        
        String roomType = room.getRoomType() != null ? room.getRoomType().toLowerCase() : "";
        String imagePath = null;
        boolean hasCustomBackground = true;

        if (roomType.contains("single")) {
            imagePath = "/images/single.png";
        } else if (roomType.contains("twin")) {
            imagePath = "/images/twin.png";
        } else if (roomType.contains("standard")) {
            imagePath = "/images/standard.png";
        } else if (roomType.contains("deluxe")) {
            imagePath = "/images/Deluxe.png";
        } else if (roomType.contains("suite")) {
            imagePath = "/images/Suite.png";
        } else {
            hasCustomBackground = false;
        }

        if (hasCustomBackground) {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, true, true));
            card.setBackground(new Background(backgroundImage));
        } else {
            card.getStyleClass().add("room-card");
        }
        
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);

        Label roomTypeLabel = new Label(room.getRoomType());
        roomTypeLabel.getStyleClass().add("section-title");

        Label description = new Label(room.getDescription());
        description.setWrapText(true);

        Label price = new Label(String.format("ETB %.2f / night", room.getPrice()));
        price.getStyleClass().add("hero-subtitle");
        
        if (hasCustomBackground) {
            roomTypeLabel.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
            description.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
            price.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
        } else {
            price.setStyle("-fx-text-fill: -amber-700;");
        }

        HBox details = new HBox(15);
        Label capacity = new Label("👤 " + room.getCapacity() + " Guests");
        if(hasCustomBackground){
            capacity.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
        }
        details.getChildren().add(capacity);

        Button bookButton = new Button("Book Now");
        bookButton.getStyleClass().add("btn-primary");
        bookButton.setOnAction(e -> handleBookNow(room));

        card.getChildren().addAll(roomTypeLabel, description, price, details, bookButton);
        return card;
    }

    private void handleBookNow(Room room) {
        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("Book Room " + room.getRoomNumber());
        dialog.setHeaderText("Select dates and payment method.");
        
        // Set the logo for the dialog window
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        ButtonType bookButtonType = new ButtonType("Confirm Booking", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(450);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 20, 10, 10));

        DatePicker checkInPicker = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        
        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today) || date.isAfter(today.plusMonths(1)));
            }
        });

        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = checkInPicker.getValue();
                setDisable(empty || date.isBefore(checkInDate.plusDays(1)));
            }
        });

        List<PaymentMethod> paymentMethods = paymentMethodDAO.getAllPaymentMethods();
        ComboBox<PaymentMethod> paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().addAll(paymentMethods);
        paymentMethodCombo.setConverter(new javafx.util.StringConverter<PaymentMethod>() {
            @Override
            public String toString(PaymentMethod object) {
                return object == null ? "" : object.getMethodName();
            }
            @Override
            public PaymentMethod fromString(String string) {
                return paymentMethodCombo.getItems().stream().filter(p -> p.getMethodName().equals(string)).findFirst().orElse(null);
            }
        });

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField paymentField = new TextField();
        paymentField.setEditable(false);

        paymentMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                paymentField.setText(newVal.getAccountDetails());
            }
        });
        paymentMethodCombo.getSelectionModel().selectFirst();

        Runnable calculateTotal = () -> {
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();
            if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                double subtotal = room.getPrice() * nights;
                double tax = subtotal * 0.15;
                double totalPrice = subtotal + tax;
                totalLabel.setText(String.format("Subtotal: ETB %.2f\nTax (15%%): ETB %.2f\nTotal: ETB %.2f", subtotal, tax, totalPrice));
            } else {
                totalLabel.setText("Total: ETB 0.00");
            }
        };

        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal.run());
        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal.run());
        calculateTotal.run();

        Button incButton = new Button("+");
        incButton.setOnAction(e -> checkOutPicker.setValue(checkOutPicker.getValue().plusDays(1)));
        Button decButton = new Button("-");
        decButton.setOnAction(e -> {
            LocalDate newDate = checkOutPicker.getValue().minusDays(1);
            if (newDate.isAfter(checkInPicker.getValue())) {
                checkOutPicker.setValue(newDate);
            }
        });

        HBox checkOutBox = new HBox(5, checkOutPicker, incButton, decButton);

        grid.add(new Label("Check-in:"), 0, 0);
        grid.add(checkInPicker, 1, 0);
        grid.add(new Label("Check-out:"), 0, 1);
        grid.add(checkOutBox, 1, 1);
        grid.add(new Label("Payment Method:"), 0, 2);
        grid.add(paymentMethodCombo, 1, 2);
        grid.add(new Label("Payment Details:"), 0, 3);
        grid.add(paymentField, 1, 3);
        grid.add(totalLabel, 0, 4, 2, 1);

        VBox policyBox = new VBox(5);
        policyBox.setPadding(new Insets(10, 0, 0, 0));
        Label policyTitle = new Label("Booking Policies:");
        policyTitle.setStyle("-fx-font-weight: bold;");
        Label policy1 = new Label("• If payment is not completed within 1 hour, your booking will be automatically cancelled.");
        Label policy2 = new Label("• A 50% cancellation fee will be applied to all cancelled bookings.");
        Label policy3 = new Label("• Check-in time is 2:00 PM and check-out time is 12:00 PM.");
        policy1.setWrapText(true);
        policy2.setWrapText(true);
        policy3.setWrapText(true);
        policyBox.getChildren().addAll(policyTitle, policy1, policy2, policy3);

        content.getChildren().addAll(grid, policyBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == bookButtonType) {
                // Confirmation Alert
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Booking");
                confirmAlert.setHeaderText("Are you sure you want to confirm this booking?");
                confirmAlert.setContentText("Please review your details before proceeding.");
                
                Stage alertStage = (Stage) confirmAlert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    LocalDate checkIn = checkInPicker.getValue();
                    LocalDate checkOut = checkOutPicker.getValue();

                    if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Dates", "Check-out date must be after check-in date.");
                        return null;
                    }

                    long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                    double subtotal = room.getPrice() * nights;
                    double tax = subtotal * 0.15;
                    double totalPrice = subtotal + tax;

                    if (currentUser.getBalance() < totalPrice) {
                        showAlert(Alert.AlertType.ERROR, "Insufficient Funds", "You do not have enough balance to make this booking.");
                        return null;
                    }

                    return new Booking(currentUser.getUserId(), room.getRoomId(), checkIn, checkOut, room.getCapacity(), totalPrice);
                }
            }
            return null;
        });

        Optional<Booking> result = dialog.showAndWait();
        result.ifPresent(booking -> {
            if (bookingDAO.createPendingBooking(booking, paymentMethodCombo.getValue().getMethodName(), paymentField.getText())) {
                currentUser.setBalance(currentUser.getBalance() - booking.getTotalPrice());
                balanceLabel.setText(String.format("Balance: ETB %.2f", currentUser.getBalance()));
                showAlert(Alert.AlertType.INFORMATION, "Booking Pending", "Your booking is pending payment confirmation.");
                loadAllRoomsIntoContainer();
                loadUserBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Booking Failed", "This room might not be available for the selected dates. Please try again.");
            }
        });
    }

    @FXML
    private void handleModifyBooking() {
        Booking selectedBooking = myBookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to modify.");
            return;
        }

        if (!"confirmed".equalsIgnoreCase(selectedBooking.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Cannot Modify", "Only confirmed bookings can be modified.");
            return;
        }

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Modify Check-out Date");
        dialog.setHeaderText("Booking #" + selectedBooking.getBookingId());
        
        // Set the logo for the dialog window
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        ButtonType modifyButtonType = new ButtonType("Confirm Modification", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(modifyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        DatePicker newCheckOutPicker = new DatePicker(selectedBooking.getCheckOut());
        newCheckOutPicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(selectedBooking.getCheckIn().plusDays(1)));
            }
        });

        Label priceDifferenceLabel = new Label();
        priceDifferenceLabel.setStyle("-fx-font-weight: bold;");

        newCheckOutPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            Room room = roomDAO.getRoomById(selectedBooking.getRoomId());
            if (room != null) {
                long oldNights = ChronoUnit.DAYS.between(selectedBooking.getCheckIn(), selectedBooking.getCheckOut());
                long newNights = ChronoUnit.DAYS.between(selectedBooking.getCheckIn(), newDate);
                double pricePerNight = room.getPrice() * 1.15; // Price with tax
                double priceDifference = (newNights - oldNights) * pricePerNight;

                if (priceDifference > 0) {
                    priceDifferenceLabel.setText(String.format("Additional Charge: ETB %.2f", priceDifference));
                } else {
                    priceDifferenceLabel.setText(String.format("Refund: ETB %.2f", -priceDifference));
                }
            }
        });

        grid.add(new Label("Original Check-out:"), 0, 0);
        grid.add(new Label(selectedBooking.getCheckOut().toString()), 1, 0);
        grid.add(new Label("New Check-out:"), 0, 1);
        grid.add(newCheckOutPicker, 1, 1);
        grid.add(priceDifferenceLabel, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == modifyButtonType) {
                return newCheckOutPicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();
        result.ifPresent(newCheckOutDate -> {
            Room room = roomDAO.getRoomById(selectedBooking.getRoomId());
            if (room != null) {
                long newNights = ChronoUnit.DAYS.between(selectedBooking.getCheckIn(), newCheckOutDate);
                double newSubtotal = room.getPrice() * newNights;
                double newTax = newSubtotal * 0.15;
                double newTotalPrice = newSubtotal + newTax;
                double priceDifference = newTotalPrice - selectedBooking.getTotalPrice();

                if (priceDifference > currentUser.getBalance()) {
                    showAlert(Alert.AlertType.ERROR, "Insufficient Funds", "You do not have enough balance to extend this booking.");
                    return;
                }

                if (bookingDAO.modifyBooking(selectedBooking.getBookingId(), newCheckOutDate, newTotalPrice, priceDifference)) {
                    currentUser.setBalance(currentUser.getBalance() - priceDifference);
                    balanceLabel.setText(String.format("Balance: ETB %.2f", currentUser.getBalance()));
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Booking modified successfully.");
                    loadUserBookings();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Modification Failed", "Failed to modify booking. Please try again.");
                }
            }
        });
    }

    @FXML
    private void handleCancelBooking() {
        Booking selectedBooking = myBookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to cancel.");
            return;
        }

        if (!"pending".equalsIgnoreCase(selectedBooking.getStatus()) && !"confirmed".equalsIgnoreCase(selectedBooking.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Cannot Cancel", "This booking cannot be cancelled.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancellation");
        alert.setHeaderText("Cancel Booking #" + selectedBooking.getBookingId());
        alert.setContentText("Are you sure you want to cancel this booking? A 50% cancellation fee will apply.");
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        // Red circle background
        Circle bg = new Circle(8);
        bg.setFill(Color.RED);
        bg.setScaleX(2.2);
        bg.setScaleY(2.2);

// Question mark icon
        SVGPath questionIcon = new SVGPath();
        questionIcon.setContent(
                "M8 3 " +
                        "C5.5 3 4 4.5 4 6 " +
                        "H6 " +
                        "C6 5.2 6.8 4.5 8 4.5 " +
                        "C9.2 4.5 10 5.2 10 6 " +
                        "C10 6.8 9.2 7.2 8.5 7.8 " +
                        "C7.6 8.5 7 9.3 7 10.8 " +
                        "V11.5 H9 V11 " +
                        "C9 10 10 9.5 10.8 8.8 " +
                        "C11.6 8.2 12 7.2 12 6 " +
                        "C12 4.5 10.5 3 8 3 Z " +
                        "M8 13.5 A1 1 0 1 0 8 15.5 A1 1 0 1 0 8 13.5 Z"
        );
        questionIcon.setFill(Color.WHITE);
        questionIcon.setScaleX(2.2);
        questionIcon.setScaleY(2.2);

// Stack them
        StackPane icon = new StackPane(bg, questionIcon);

// Set graphic correctly
        alert.setGraphic(icon);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (bookingDAO.cancelBooking(selectedBooking.getBookingId())) {
                // Refresh user to get updated balance
                currentUser = userDAO.getUserById(currentUser.getUserId());
                balanceLabel.setText(String.format("Balance: ETB %.2f", currentUser.getBalance()));
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking cancelled successfully. 50% has been refunded to your balance.");
                loadUserBookings();
                loadAllRoomsIntoContainer();
            } else {
                showAlert(Alert.AlertType.ERROR, "Cancellation Failed", "Failed to cancel booking. Please try again.");
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Hotel Booking System - Login");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        alert.showAndWait();
    }
}