package controller;

import dao.BookingDAO;
import dao.NotificationDAO;
import dao.PaymentMethodDAO;
import dao.RoomDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Booking;
import model.Notification;
import model.PaymentMethod;
import model.Room;
import model.User;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboardController {
    @FXML private Label statsLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label bookedRoomsLabel;
    @FXML private Label totalTaxLabel;
    @FXML private TabPane tabPane;
    @FXML private Label notificationIcon;
    @FXML private ImageView logoImageView;

    // Buttons
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private Button addRoomButton;
    @FXML private Button updateRoomButton;
    @FXML private Button deleteRoomButton;
    @FXML private Button clearRoomButton;
    @FXML private Button addPaymentMethodButton;
    @FXML private Button updatePaymentMethodButton;
    @FXML private Button deletePaymentMethodButton;
    @FXML private Button confirmPaymentButton;
    @FXML private Button cancelBookingButton;
    @FXML private Button confirmPaymentPendingButton;
    @FXML private Button cancelBookingPendingButton;
    @FXML private Button cancelBookingConfirmedButton;
    @FXML private Button handleAddAdminButton;

    // DAOs
    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PaymentMethodDAO paymentMethodDAO = new PaymentMethodDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Other fields
    private User currentUser;

    // Rooms Management Tab
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> roomNumberCol;
    @FXML private TableColumn<Room, String> roomTypeCol;
    @FXML private TableColumn<Room, Double> priceCol;
    @FXML private TableColumn<Room, Integer> capacityCol;
    @FXML private TableColumn<Room, String> statusCol;
    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> capacityCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea amenitiesField;

    // Payment Methods
    @FXML private TableView<PaymentMethod> paymentMethodsTable;
    @FXML private TableColumn<PaymentMethod, String> methodNameCol;
    @FXML private TableColumn<PaymentMethod, String> accountDetailsCol;

    // Bookings Management Tab
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdCol;
    @FXML private TableColumn<Booking, String> customerCol;
    @FXML private TableColumn<Booking, String> bookingRoomCol;
    @FXML private TableColumn<Booking, String> checkInCol;
    @FXML private TableColumn<Booking, String> checkOutCol;
    @FXML private TableColumn<Booking, Double> totalPriceCol;
    @FXML private TableColumn<Booking, String> bookingStatusCol;

    // Pending Bookings Tab
    @FXML private TableView<Booking> pendingBookingsTable;
    @FXML private TableColumn<Booking, Integer> pendingBookingIdCol;
    @FXML private TableColumn<Booking, String> pendingCustomerCol;
    @FXML private TableColumn<Booking, String> pendingRoomCol;
    @FXML private TableColumn<Booking, String> pendingCheckInCol;
    @FXML private TableColumn<Booking, String> pendingCheckOutCol;
    @FXML private TableColumn<Booking, Double> pendingTotalPriceCol;
    @FXML private TableColumn<Booking, String> pendingStatusCol;

    // Confirmed Bookings Tab
    @FXML private TableView<Booking> confirmedBookingsTable;
    @FXML private TableColumn<Booking, Integer> confirmedBookingIdCol;
    @FXML private TableColumn<Booking, String> confirmedCustomerCol;
    @FXML private TableColumn<Booking, String> confirmedRoomCol;
    @FXML private TableColumn<Booking, String> confirmedCheckInCol;
    @FXML private TableColumn<Booking, String> confirmedCheckOutCol;
    @FXML private TableColumn<Booking, Double> confirmedTotalPriceCol;
    @FXML private TableColumn<Booking, String> confirmedStatusCol;

    // Cancelled Bookings Tab
    @FXML private TableView<Booking> cancelledBookingsTable;
    @FXML private TableColumn<Booking, Integer> cancelledBookingIdCol;
    @FXML private TableColumn<Booking, String> cancelledCustomerCol;
    @FXML private TableColumn<Booking, String> cancelledRoomCol;
    @FXML private TableColumn<Booking, String> cancelledCheckInCol;
    @FXML private TableColumn<Booking, String> cancelledCheckOutCol;
    @FXML private TableColumn<Booking, Double> cancelledTotalPriceCol;
    @FXML private TableColumn<Booking, String> cancelledStatusCol;

    // Users Management Tab
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userFirstNameCol;
    @FXML private TableColumn<User, String> userLastNameCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, String> userAddressCol;
    @FXML private TableColumn<User, String> userNationalityCol;
    @FXML private TableColumn<User, String> userPhoneCol;
    @FXML private TableColumn<User, String> userRoleCol;

    // User Booking History Tab
    @FXML private Label selectedUserBookingHistoryLabel;
    @FXML private TableView<Booking> userBookingHistoryTable;
    @FXML private TableColumn<Booking, Integer> userBookingIdCol;
    @FXML private TableColumn<Booking, String> userBookedRoomCol;
    @FXML private TableColumn<Booking, LocalDate> userCheckInCol;
    @FXML private TableColumn<Booking, LocalDate> userCheckOutCol;
    @FXML private TableColumn<Booking, Double> userTotalPriceCol;
    @FXML private TableColumn<Booking, String> userBookingStatusCol;
    @FXML private TableColumn<Booking, String> userPaymentMethodCol;
    @FXML private TableColumn<Booking, String> userPaymentDetailsCol;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateStats();
        loadAllData();
    }

    @FXML
    public void initialize() {
        logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
        
        // Setup Icons
        setupIcons();

        // Setup rooms table
        roomNumberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup status combo
        statusCombo.getItems().addAll("available", "booked", "maintenance");
        statusCombo.setValue("available");
        
        // Setup room type combo
        roomTypeCombo.getItems().addAll("Standard", "Deluxe", "Twin", "Single", "Family", "VIP Room", "Suite");
        roomTypeCombo.setValue("Standard");
        
        // Setup capacity combo
        capacityCombo.getItems().addAll("1", "2", "3", "4", "5");
        capacityCombo.setValue("2");

        // Setup payment methods table
        methodNameCol.setCellValueFactory(new PropertyValueFactory<>("methodName"));
        accountDetailsCol.setCellValueFactory(new PropertyValueFactory<>("accountDetails"));

        // Setup bookings table
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        bookingRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        bookingStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup pending bookings table
        pendingBookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        pendingCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        pendingRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        pendingCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        pendingCheckOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        pendingTotalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        pendingStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup confirmed bookings table
        confirmedBookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        confirmedCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        confirmedRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        confirmedCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        confirmedCheckOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        confirmedTotalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        confirmedStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup cancelled bookings table
        cancelledBookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        cancelledCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        cancelledRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        cancelledCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        cancelledCheckOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        cancelledTotalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        cancelledStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup users table
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userFirstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        userLastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        userNationalityCol.setCellValueFactory(new PropertyValueFactory<>("nationality"));
        userPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Setup user booking history table
        userBookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        userBookedRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        userCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        userCheckOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        userTotalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        userBookingStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        userPaymentMethodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        userPaymentDetailsCol.setCellValueFactory(new PropertyValueFactory<>("paymentDetails"));

        // Add listener for user selection to load their booking history
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUserBookingHistoryLabel.setText("Booking History for: " + newSelection.getName());
                loadUserBookingHistory(newSelection.getUserId());
                tabPane.getSelectionModel().selectLast(); // Switch to the user booking history tab
            } else {
                selectedUserBookingHistoryLabel.setText("Select a user to view their booking history");
                userBookingHistoryTable.setItems(FXCollections.emptyObservableList());
            }
        });

        notificationIcon.setOnMouseClicked(e -> showNotifications());
    }

    private void setupIcons() {
        // Refresh
        FontIcon refreshIcon = new FontIcon("fas-sync");
        refreshIcon.setIconSize(18);
        refreshIcon.setIconColor(Color.WHITE);
        if (refreshButton != null) refreshButton.setGraphic(refreshIcon);

        // Add
        FontIcon addIcon = new FontIcon("fas-plus");
        addIcon.setIconSize(14);
        addIcon.setIconColor(Color.WHITE);
        if (addRoomButton != null) addRoomButton.setGraphic(addIcon);
        
        FontIcon addIcon2 = new FontIcon("fas-plus");
        addIcon2.setIconSize(14);
        addIcon2.setIconColor(Color.WHITE);
        if (addPaymentMethodButton != null) addPaymentMethodButton.setGraphic(addIcon2);

        // Update
        FontIcon updateIcon = new FontIcon("fas-redo");
        updateIcon.setIconSize(14);
        updateIcon.setIconColor(Color.WHITE);
        if (updateRoomButton != null) updateRoomButton.setGraphic(updateIcon);
        
        FontIcon updateIcon2 = new FontIcon("fas-redo");
        updateIcon2.setIconSize(14);
        updateIcon2.setIconColor(Color.WHITE);
        if (updatePaymentMethodButton != null) updatePaymentMethodButton.setGraphic(updateIcon2);

        // Delete
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.setIconSize(14);
        deleteIcon.setIconColor(Color.WHITE);
        if (deleteRoomButton != null) deleteRoomButton.setGraphic(deleteIcon);
        
        FontIcon deleteIcon2 = new FontIcon("fas-trash");
        deleteIcon2.setIconSize(14);
        deleteIcon2.setIconColor(Color.WHITE);
        if (deletePaymentMethodButton != null) deletePaymentMethodButton.setGraphic(deleteIcon2);

        // Confirm
        FontIcon confirmIcon = new FontIcon("fas-check");
        confirmIcon.setIconSize(14);
        confirmIcon.setIconColor(Color.WHITE);
        if (confirmPaymentButton != null) confirmPaymentButton.setGraphic(confirmIcon);
        
        FontIcon confirmIcon2 = new FontIcon("fas-check");
        confirmIcon2.setIconSize(14);
        confirmIcon2.setIconColor(Color.WHITE);
        if (confirmPaymentPendingButton != null) confirmPaymentPendingButton.setGraphic(confirmIcon2);

        // Cancel
        FontIcon cancelIcon = new FontIcon("fas-times");
        cancelIcon.setIconSize(14);
        cancelIcon.setIconColor(Color.WHITE);
        if (cancelBookingButton != null) cancelBookingButton.setGraphic(cancelIcon);
        
        FontIcon cancelIcon2 = new FontIcon("fas-times");
        cancelIcon2.setIconSize(14);
        cancelIcon2.setIconColor(Color.WHITE);
        if (cancelBookingPendingButton != null) cancelBookingPendingButton.setGraphic(cancelIcon2);
        
        FontIcon cancelIcon3 = new FontIcon("fas-times");
        cancelIcon3.setIconSize(14);
        cancelIcon3.setIconColor(Color.WHITE);
        if (cancelBookingConfirmedButton != null) cancelBookingConfirmedButton.setGraphic(cancelIcon3);
    }

    private void loadAllData() {
        loadRooms();
        loadPaymentMethods();
        loadBookings();
        loadUsers();
        updateNotificationIcon();
    }

    private void updateNotificationIcon() {
        int pendingCount = bookingDAO.getPendingBookingsCount();
        int unreadNotifications = notificationDAO.getUnreadNotifications().size();
        int totalUnread = pendingCount + unreadNotifications;

        if (totalUnread > 0) {
            notificationIcon.setVisible(true);
            notificationIcon.setText("🔔 " + totalUnread);
            notificationIcon.setTooltip(new Tooltip(totalUnread + " unread notifications"));
        } else {
            notificationIcon.setVisible(false);
        }
    }

    private void showNotifications() {
        List<Notification> notifications = notificationDAO.getUnreadNotifications();
        List<String> pendingBookings = bookingDAO.getBookingsByStatus("pending").stream()
                .map(b -> "Pending booking #" + b.getBookingId() + " for " + b.getCustomerName())
                .collect(Collectors.toList());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("You have " + (notifications.size() + pendingBookings.size()) + " new notifications.");

        String content = notifications.stream().map(Notification::getMessage).collect(Collectors.joining("\n"));
        content += "\n" + String.join("\n", pendingBookings);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        alert.getDialogPane().setContent(textArea);
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        alert.showAndWait();
        notificationDAO.markAllAsRead();
        updateNotificationIcon();
    }

    private void loadRooms() {
        ObservableList<Room> rooms = FXCollections.observableArrayList(roomDAO.getAllRooms());
        roomsTable.setItems(rooms);
    }

    private void loadPaymentMethods() {
        ObservableList<PaymentMethod> methods = FXCollections.observableArrayList(paymentMethodDAO.getAllPaymentMethods());
        paymentMethodsTable.setItems(methods);
    }

    private void loadBookings() {
        ObservableList<Booking> allBookings = FXCollections.observableArrayList(bookingDAO.getAllBookings());
        bookingsTable.setItems(allBookings);

        ObservableList<Booking> pendingBookings = FXCollections.observableArrayList(bookingDAO.getBookingsByStatus("pending"));
        pendingBookingsTable.setItems(pendingBookings);

        ObservableList<Booking> confirmedBookings = FXCollections.observableArrayList(bookingDAO.getBookingsByStatus("confirmed"));
        confirmedBookingsTable.setItems(confirmedBookings);

        ObservableList<Booking> cancelledBookings = FXCollections.observableArrayList(bookingDAO.getBookingsByStatus("cancelled"));
        cancelledBookingsTable.setItems(cancelledBookings);
    }

    private void loadUsers() {
        ObservableList<User> users = FXCollections.observableArrayList(userDAO.getAllUsers());
        usersTable.setItems(users);
    }

    private void loadUserBookingHistory(int userId) {
        ObservableList<Booking> userBookings = FXCollections.observableArrayList(bookingDAO.getUserBookings(userId));
        userBookingHistoryTable.setItems(userBookings);
    }

    @FXML
    private void handleAddRoom() {
        if (!validateRoomFields()) {
            return;
        }

        Room room = new Room();
        room.setRoomNumber(roomNumberField.getText());
        room.setRoomType(roomTypeCombo.getValue());
        room.setDescription(descriptionField.getText());
        room.setPrice(Double.parseDouble(priceField.getText()));
        room.setCapacity(Integer.parseInt(capacityCombo.getValue()));
        room.setStatus(statusCombo.getValue());
        room.setAmenities(amenitiesField.getText());

        if (roomDAO.addRoom(room)) {
            showAlert("Room added successfully");
            clearRoomFields();
            loadRooms();
            updateStats();
        } else {
            showAlert("Failed to add room");
        }
    }

    @FXML
    private void handleUpdateRoom() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("Please select a room to update");
            return;
        }

        if (!validateRoomFields()) {
            return;
        }

        selectedRoom.setRoomNumber(roomNumberField.getText());
        selectedRoom.setRoomType(roomTypeCombo.getValue());
        selectedRoom.setDescription(descriptionField.getText());
        selectedRoom.setPrice(Double.parseDouble(priceField.getText()));
        selectedRoom.setCapacity(Integer.parseInt(capacityCombo.getValue()));
        selectedRoom.setStatus(statusCombo.getValue());
        selectedRoom.setAmenities(amenitiesField.getText());

        if (roomDAO.updateRoom(selectedRoom)) {
            showAlert("Room updated successfully");
            loadRooms();
            updateStats();
        } else {
            showAlert("Failed to update room");
        }
    }

    @FXML
    private void handleDeleteRoom() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert("Please select a room to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Room");
        alert.setContentText("Are you sure you want to delete room " + selectedRoom.getRoomNumber() + "?");
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (roomDAO.deleteRoom(selectedRoom.getRoomId())) {
                showAlert("Room deleted successfully");
                loadRooms();
                clearRoomFields();
                updateStats();
            } else {
                showAlert("Failed to delete room. Make sure no bookings exist for this room.");
            }
        }
    }

    @FXML
    private void handleRoomSelection() {
        Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
        if (selectedRoom != null) {
            roomNumberField.setText(selectedRoom.getRoomNumber());
            roomTypeCombo.setValue(selectedRoom.getRoomType());
            descriptionField.setText(selectedRoom.getDescription());
            priceField.setText(String.valueOf(selectedRoom.getPrice()));
            capacityCombo.setValue(String.valueOf(selectedRoom.getCapacity()));
            statusCombo.setValue(selectedRoom.getStatus());
            amenitiesField.setText(selectedRoom.getAmenities());
        }
    }

    @FXML
    private void handleAddPaymentMethod() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Payment Method");
        dialog.setHeaderText("Enter new payment method details");
        dialog.setContentText("Method Name:");
        
        // Set the logo for the dialog window
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(methodName -> {
            TextInputDialog detailsDialog = new TextInputDialog();
            detailsDialog.setTitle("Add Payment Method Details");
            detailsDialog.setHeaderText("Enter account details for " + methodName);
            detailsDialog.setContentText("Account Details:");
            
            // Set the logo for the dialog window
            Stage detailsDialogStage = (Stage) detailsDialog.getDialogPane().getScene().getWindow();
            detailsDialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            
            Optional<String> detailsResult = detailsDialog.showAndWait();
            detailsResult.ifPresent(accountDetails -> {
                PaymentMethod newMethod = new PaymentMethod(methodName, accountDetails);
                if (paymentMethodDAO.addPaymentMethod(newMethod)) {
                    showAlert("Payment method added successfully");
                    loadPaymentMethods();
                } else {
                    showAlert("Failed to add payment method. Method name might already exist.");
                }
            });
        });
    }

    @FXML
    private void handleUpdatePaymentMethod() {
        PaymentMethod selectedMethod = paymentMethodsTable.getSelectionModel().getSelectedItem();
        if (selectedMethod == null) {
            showAlert("Please select a payment method to update");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedMethod.getMethodName());
        dialog.setTitle("Update Payment Method");
        dialog.setHeaderText("Update payment method details");
        dialog.setContentText("Method Name:");
        
        // Set the logo for the dialog window
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(methodName -> {
            TextInputDialog detailsDialog = new TextInputDialog(selectedMethod.getAccountDetails());
            detailsDialog.setTitle("Update Payment Method Details");
            detailsDialog.setHeaderText("Update account details for " + methodName);
            detailsDialog.setContentText("Account Details:");
            
            // Set the logo for the dialog window
            Stage detailsDialogStage = (Stage) detailsDialog.getDialogPane().getScene().getWindow();
            detailsDialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            
            Optional<String> detailsResult = detailsDialog.showAndWait();
            detailsResult.ifPresent(accountDetails -> {
                selectedMethod.setMethodName(methodName);
                selectedMethod.setAccountDetails(accountDetails);
                if (paymentMethodDAO.updatePaymentMethod(selectedMethod)) {
                    showAlert("Payment method updated successfully");
                    loadPaymentMethods();
                } else {
                    showAlert("Failed to update payment method.");
                }
            });
        });
    }

    @FXML
    private void handleDeletePaymentMethod() {
        PaymentMethod selectedMethod = paymentMethodsTable.getSelectionModel().getSelectedItem();
        if (selectedMethod == null) {
            showAlert("Please select a payment method to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Payment Method");
        alert.setContentText("Are you sure you want to delete the " + selectedMethod.getMethodName() + " payment method?");
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (paymentMethodDAO.deletePaymentMethod(selectedMethod.getMethodId())) {
                showAlert("Payment method deleted successfully");
                loadPaymentMethods();
            } else {
                showAlert("Failed to delete payment method.");
            }
        }
    }

    @FXML
    private void handleConfirmPayment() {
        Booking selectedBooking = pendingBookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        }
        
        if (selectedBooking == null) {
            showAlert("Please select a booking to confirm payment");
            return;
        }

        if (!selectedBooking.getStatus().equals("pending")) {
            showAlert("This booking is not pending payment");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Payment");
        alert.setHeaderText("Confirm Payment");
        alert.setContentText("Are you sure you want to confirm payment for booking #" + selectedBooking.getBookingId() + "?");
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (bookingDAO.confirmBooking(selectedBooking.getBookingId())) {
                showAlert("Payment confirmed successfully");
                loadBookings();
                updateStats();
                updateNotificationIcon();
                User selectedUser = usersTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null && selectedUser.getUserId() == selectedBooking.getUserId()) {
                    loadUserBookingHistory(selectedUser.getUserId());
                }
            } else {
                showAlert("Failed to confirm payment");
            }
        }
    }

    @FXML
    private void handleCancelBooking() {
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            selectedBooking = pendingBookingsTable.getSelectionModel().getSelectedItem();
        }
        if (selectedBooking == null) {
            selectedBooking = confirmedBookingsTable.getSelectionModel().getSelectedItem();
        }

        if (selectedBooking == null) {
            showAlert("Please select a booking to cancel");
            return;
        }

        if (selectedBooking.getStatus().equals("cancelled")) {
            showAlert("This booking is already cancelled");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancellation");
        alert.setHeaderText("Cancel Booking");
        alert.setContentText("Cancel booking #" + selectedBooking.getBookingId() + "?");
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (bookingDAO.cancelBooking(selectedBooking.getBookingId())) {
                showAlert("Booking cancelled successfully");
                loadBookings();
                updateStats();
                updateNotificationIcon();
                User selectedUser = usersTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null && selectedUser.getUserId() == selectedBooking.getUserId()) {
                    loadUserBookingHistory(selectedUser.getUserId());
                }
            } else {
                showAlert("Cancellation failed");
            }
        }
    }

    @FXML
    private void handleReport() {
        Map<String, Integer> roomStats = bookingDAO.getMostBookedRooms();
        
        if (roomStats.isEmpty()) {
            showAlert("No booking data available for report.");
            return;
        }

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Most Booked Rooms Report:\n\n");
        
        // Find the most booked room
        String mostBookedRoom = "";
        int maxBookings = 0;
        
        for (Map.Entry<String, Integer> entry : roomStats.entrySet()) {
            reportContent.append("Room ").append(entry.getKey())
                         .append(": ").append(entry.getValue()).append(" bookings\n");
            
            if (entry.getValue() > maxBookings) {
                maxBookings = entry.getValue();
                mostBookedRoom = entry.getKey();
            }
        }
        
        reportContent.append("\n----------------------------\n");
        reportContent.append("Top Performing Room: ").append(mostBookedRoom)
                     .append(" (").append(maxBookings).append(" bookings)");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Report");
        alert.setHeaderText("Room Booking Statistics");
        
        TextArea textArea = new TextArea(reportContent.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(200);
        textArea.setPrefWidth(300);
        
        alert.getDialogPane().setContent(textArea);
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        
        alert.showAndWait();
    }

    @FXML
    private void handleAddAdmin() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New Admin");
        dialog.setHeaderText("Create a new administrator account");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstName = new TextField();
        firstName.setPromptText("First Name");
        TextField lastName = new TextField();
        lastName.setPromptText("Last Name");
        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstName, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastName, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(password, 1, 3);

        dialog.getDialogPane().setContent(grid);
        
        // Set the logo for the dialog window
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (firstName.getText().isEmpty() || lastName.getText().isEmpty() || 
                    email.getText().isEmpty() || password.getText().isEmpty()) {
                    showAlert("All fields are required.");
                    return null;
                }
                
                String hashedPassword = BCrypt.hashpw(password.getText(), BCrypt.gensalt());
                User newAdmin = new User();
                newAdmin.setFirstName(firstName.getText());
                newAdmin.setLastName(lastName.getText());
                newAdmin.setEmail(email.getText());
                newAdmin.setPassword(hashedPassword);
                newAdmin.setRole("admin");
                
                return newAdmin;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(newAdmin -> {
            if (userDAO.addAdmin(newAdmin)) {
                showAlert("New admin added successfully!");
                loadUsers(); // Refresh user list
            } else {
                showAlert("Failed to add admin. Email might already exist.");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadAllData();
        updateStats();
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) statsLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateRoomFields() {
        if (roomNumberField.getText().isEmpty() || roomTypeCombo.getValue() == null ||
                priceField.getText().isEmpty() || capacityCombo.getValue() == null) {
            showAlert("Please fill all required fields");
            return false;
        }

        try {
            Double.parseDouble(priceField.getText());
            Integer.parseInt(capacityCombo.getValue());
        } catch (NumberFormatException e) {
            showAlert("Price and capacity must be valid numbers");
            return false;
        }

        return true;
    }

    private void clearRoomFields() {
        roomNumberField.clear();
        roomTypeCombo.setValue("Standard"); // Reset to default
        descriptionField.clear();
        priceField.clear();
        capacityCombo.setValue("2"); // Reset to default
        statusCombo.setValue("available");
        amenitiesField.clear();
    }

    private void updateStats() {
        double revenue = bookingDAO.getTotalRevenue();
        double tax = revenue * 0.15; // Calculate 15% tax
        int totalBookings = bookingDAO.getTotalBookings();
        statsLabel.setText(String.format("Total Revenue: ETB %.2f | Total Tax: ETB %.2f | Total Bookings: %d", revenue, tax, totalBookings));
        
        int totalRooms = roomDAO.getTotalRooms();
        int availableRooms = roomDAO.getTotalAvailableRooms();
        int bookedRooms = roomDAO.getTotalBookedRooms();

        totalRoomsLabel.setText("Total Rooms: " + totalRooms);
        availableRoomsLabel.setText("Available: " + availableRooms);
        bookedRoomsLabel.setText("Booked: " + bookedRooms);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Set the logo for the alert window
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        alert.showAndWait();
    }
}