package controller;

import dao.BookingDAO;
import dao.RoomDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Booking;
import model.Room;
import model.User;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    @FXML
    private DatePicker checkInDatePicker;
    
    @FXML
    private DatePicker checkOutDatePicker;
    
    @FXML
    private ComboBox<String> roomTypeComboBox;
    
    @FXML
    private Spinner<Integer> guestsSpinner;
    
    @FXML
    private TableView<Room> availableRoomsTable;
    
    @FXML
    private TableColumn<Room, String> roomNumberColumn;
    
    @FXML
    private TableColumn<Room, String> roomTypeColumn;
    
    @FXML
    private TableColumn<Room, Double> priceColumn;
    
    @FXML
    private TableColumn<Room, Integer> capacityColumn;
    
    @FXML
    private TableColumn<Room, String> amenitiesColumn;
    
    @FXML
    private TableView<Booking> myBookingsTable;
    
    @FXML
    private TableColumn<Booking, Integer> bookingIdColumn;
    
    @FXML
    private TableColumn<Booking, String> bookedRoomColumn;
    
    @FXML
    private TableColumn<Booking, LocalDate> bookingCheckInColumn;
    
    @FXML
    private TableColumn<Booking, LocalDate> bookingCheckOutColumn;
    
    @FXML
    private TableColumn<Booking, Integer> bookingGuestsColumn;
    
    @FXML
    private TableColumn<Booking, Double> totalPriceColumn;
    
    @FXML
    private TableColumn<Booking, String> bookingStatusColumn;
    
    @FXML
    private Label totalNightsLabel;
    
    @FXML
    private Label totalPriceLabel;
    
    @FXML
    private Label selectedRoomLabel;
    
    @FXML
    private Button bookButton;
    
    @FXML
    private Button cancelBookingButton;
    
    @FXML
    private Button searchButton;
    
    private User currentUser;
    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;
    private Room selectedRoom;

    public BookingController() {
        roomDAO = new RoomDAO();
        bookingDAO = new BookingDAO();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserBookings();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize room type combo box
        roomTypeComboBox.getItems().addAll("All", "Standard", "Deluxe", "Suite");
        roomTypeComboBox.setValue("All");
        
        // Initialize guests spinner
        guestsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
        
        // Set up available rooms table columns
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        amenitiesColumn.setCellValueFactory(new PropertyValueFactory<>("amenities"));
        
        // Set up bookings table columns
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookedRoomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        bookingCheckInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        bookingCheckOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        bookingGuestsColumn.setCellValueFactory(new PropertyValueFactory<>("guests"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set default dates
        checkInDatePicker.setValue(LocalDate.now());
        checkOutDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Add listeners for date changes
        checkInDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updatePriceCalculation());
        checkOutDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updatePriceCalculation());
        
        // Add listener for room selection
        availableRoomsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> handleRoomSelection(newSelection)
        );
        
        // Initialize buttons
        bookButton.setDisable(true);
        cancelBookingButton.setDisable(true);
        
        // Add listener for booking selection
        myBookingsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                cancelBookingButton.setDisable(newSelection == null || 
                    newSelection.getStatus().equals("cancelled"));
            }
        );
    }

    @FXML
    private void handleSearch() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        String roomType = roomTypeComboBox.getValue();
        int guests = guestsSpinner.getValue();
        
        // Validate dates
        if (!validateDates(checkIn, checkOut)) {
            return;
        }
        
        // Search for available rooms
        List<Room> availableRooms = roomDAO.getAvailableRooms(
            Date.valueOf(checkIn),
            Date.valueOf(checkOut),
            roomType.equals("All") ? null : roomType,
            guests
        );
        
        // Update table
        ObservableList<Room> rooms = FXCollections.observableArrayList(availableRooms);
        availableRoomsTable.setItems(rooms);
        
        // Clear previous selection
        selectedRoom = null;
        selectedRoomLabel.setText("No room selected");
        bookButton.setDisable(true);
        updatePriceCalculation();
    }

    @FXML
    private void handleBook() {
        if (selectedRoom == null || currentUser == null) {
            showAlert("Please select a room to book");
            return;
        }
        
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        int guests = guestsSpinner.getValue();
        
        // Calculate total price
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = selectedRoom.getPrice() * nights;
        
        // Create booking object
        Booking booking = new Booking(
            currentUser.getUserId(),
            selectedRoom.getRoomId(),
            checkIn,
            checkOut,
            guests,
            totalPrice
        );
        
        // Save booking to database
        if (bookingDAO.createPendingBooking(booking, "Credit Card", "1234-5678-9012-3456")) { // Example payment details
            showAlert("Booking successful! Your payment is pending confirmation.\nRoom: " + selectedRoom.getRoomNumber() +
                     "\nTotal: $" + String.format("%.2f", totalPrice) + 
                     "\nCheck-in: " + checkIn + 
                     "\nCheck-out: " + checkOut);
            
            // Refresh data
            loadUserBookings();
            handleSearch(); // Refresh available rooms
            
            // Reset selection
            selectedRoom = null;
            selectedRoomLabel.setText("No room selected");
            bookButton.setDisable(true);
            availableRoomsTable.getSelectionModel().clearSelection();
        } else {
            showAlert("Booking failed. Please try again.");
        }
    }

    @FXML
    private void handleCancelBooking() {
        Booking selectedBooking = myBookingsTable.getSelectionModel().getSelectedItem();
        
        if (selectedBooking == null) {
            showAlert("Please select a booking to cancel");
            return;
        }
        
        if (selectedBooking.getStatus().equals("cancelled")) {
            showAlert("This booking is already cancelled");
            return;
        }
        
        // Confirm cancellation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Booking #" + selectedBooking.getBookingId());
        confirmAlert.setContentText("Are you sure you want to cancel this booking?\n" +
                                   "Room: " + selectedBooking.getRoomNumber() + "\n" +
                                   "Check-in: " + selectedBooking.getCheckIn() + "\n" +
                                   "Check-out: " + selectedBooking.getCheckOut());
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            if (bookingDAO.cancelBooking(selectedBooking.getBookingId())) {
                showAlert("Booking cancelled successfully");
                loadUserBookings();
                handleSearch(); // Refresh available rooms
            } else {
                showAlert("Failed to cancel booking");
            }
        }
    }

    @FXML
    private void handleClearSelection() {
        availableRoomsTable.getSelectionModel().clearSelection();
        selectedRoom = null;
        selectedRoomLabel.setText("No room selected");
        bookButton.setDisable(true);
        updatePriceCalculation();
    }

    @FXML
    private void handleViewDetails() {
        Room selected = availableRoomsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
            detailsAlert.setTitle("Room Details");
            detailsAlert.setHeaderText("Room " + selected.getRoomNumber() + " - " + selected.getRoomType());
            detailsAlert.setContentText(
                "Room Number: " + selected.getRoomNumber() + "\n" +
                "Type: " + selected.getRoomType() + "\n" +
                "Price per Night: $" + selected.getPrice() + "\n" +
                "Capacity: " + selected.getCapacity() + " guests\n" +
                "Status: " + selected.getStatus() + "\n" +
                "Amenities: " + selected.getAmenities() + "\n" +
                "Description: " + selected.getDescription()
            );
            detailsAlert.showAndWait();
        }
    }

    private void handleRoomSelection(Room room) {
        selectedRoom = room;
        if (room != null) {
            selectedRoomLabel.setText("Selected: " + room.getRoomNumber() + " - " + room.getRoomType());
            bookButton.setDisable(false);
            updatePriceCalculation();
        } else {
            selectedRoomLabel.setText("No room selected");
            bookButton.setDisable(true);
        }
    }

    private void updatePriceCalculation() {
        if (selectedRoom != null && 
            checkInDatePicker.getValue() != null && 
            checkOutDatePicker.getValue() != null) {
            
            LocalDate checkIn = checkInDatePicker.getValue();
            LocalDate checkOut = checkOutDatePicker.getValue();
            
            if (checkOut.isAfter(checkIn)) {
                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                double totalPrice = selectedRoom.getPrice() * nights;
                
                totalNightsLabel.setText(nights + " night(s)");
                totalPriceLabel.setText("$" + String.format("%.2f", totalPrice));
            } else {
                totalNightsLabel.setText("0 night(s)");
                totalPriceLabel.setText("$0.00");
            }
        } else {
            totalNightsLabel.setText("0 night(s)");
            totalPriceLabel.setText("$0.00");
        }
    }

    private void loadUserBookings() {
        if (currentUser != null) {
            List<Booking> bookings = bookingDAO.getUserBookings(currentUser.getUserId());
            ObservableList<Booking> bookingList = FXCollections.observableArrayList(bookings);
            myBookingsTable.setItems(bookingList);
        }
    }

    private boolean validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            showAlert("Please select both check-in and check-out dates");
            return false;
        }
        
        if (checkIn.isBefore(LocalDate.now())) {
            showAlert("Check-in date cannot be in the past");
            return false;
        }
        
        if (!checkOut.isAfter(checkIn)) {
            showAlert("Check-out date must be after check-in date");
            return false;
        }
        
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Booking System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Additional methods for filtering and sorting
    public void filterByPrice(double maxPrice) {
        if (availableRoomsTable.getItems() != null) {
            ObservableList<Room> filteredRooms = availableRoomsTable.getItems()
                .filtered(room -> room.getPrice() <= maxPrice);
            availableRoomsTable.setItems(filteredRooms);
        }
    }

    public void sortByPrice() {
        availableRoomsTable.getSortOrder().clear();
        availableRoomsTable.getSortOrder().add(priceColumn);
        priceColumn.setSortType(TableColumn.SortType.ASCENDING);
        availableRoomsTable.sort();
    }

    public void sortByCapacity() {
        availableRoomsTable.getSortOrder().clear();
        availableRoomsTable.getSortOrder().add(capacityColumn);
        capacityColumn.setSortType(TableColumn.SortType.ASCENDING);
        availableRoomsTable.sort();
    }

    // Method to check room availability for specific dates
    public boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut) {
        // This would require a more complex query in RoomDAO
        // For now, we'll use the existing search method
        List<Room> availableRooms = roomDAO.getAvailableRooms(
            Date.valueOf(checkIn),
            Date.valueOf(checkOut),
            null, // any room type
            1    // minimum guests
        );
        
        return availableRooms.stream()
            .anyMatch(room -> room.getRoomId() == roomId);
    }

    // Method to calculate booking duration
    public long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    // Method to get booking statistics for the user
    public String getBookingStatistics() {
        if (currentUser == null) return "";
        
        List<Booking> bookings = bookingDAO.getUserBookings(currentUser.getUserId());
        long totalBookings = bookings.size();
        long activeBookings = bookings.stream()
            .filter(b -> b.getStatus().equals("confirmed"))
            .count();
        long cancelledBookings = bookings.stream()
            .filter(b -> b.getStatus().equals("cancelled"))
            .count();
        
        return String.format("Total: %d | Active: %d | Cancelled: %d", 
                           totalBookings, activeBookings, cancelledBookings);
    }
}