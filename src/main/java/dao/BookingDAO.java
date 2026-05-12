package dao;

import model.Booking;
import util.DBConnection;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDAO {
    
    public boolean createPendingBooking(Booking booking, String paymentMethod, String paymentDetails) {
        String bookingQuery = "INSERT INTO Bookings (user_id, room_id, check_in, check_out, guests, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String paymentQuery = "INSERT INTO Payments (booking_id, amount, payment_method, payment_details, status) VALUES (?, ?, ?, ?, ?)";
        String roomUpdateQuery = "UPDATE Rooms SET status = 'booked' WHERE room_id = ?";
        String updateUserBalanceQuery = "UPDATE Users SET balance = balance - ? WHERE user_id = ?";
        Connection conn = DBConnection.getConnection();

        try {
            // Start transaction
            conn.setAutoCommit(false);

            // Create booking with "pending" status
            int bookingId = 0;
            try (PreparedStatement bookingPstmt = conn.prepareStatement(bookingQuery, new String[]{"booking_id"})) {
                bookingPstmt.setInt(1, booking.getUserId());
                bookingPstmt.setInt(2, booking.getRoomId());
                bookingPstmt.setLong(3, booking.getCheckIn().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                bookingPstmt.setLong(4, booking.getCheckOut().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                bookingPstmt.setInt(5, booking.getGuests());
                bookingPstmt.setDouble(6, booking.getTotalPrice());
                bookingPstmt.setString(7, "pending");
                bookingPstmt.executeUpdate();
                
                ResultSet generatedKeys = bookingPstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    bookingId = generatedKeys.getInt(1);
                }
            }

            // Create payment record
            try (PreparedStatement paymentPstmt = conn.prepareStatement(paymentQuery)) {
                paymentPstmt.setInt(1, bookingId);
                paymentPstmt.setDouble(2, booking.getTotalPrice());
                paymentPstmt.setString(3, paymentMethod);
                paymentPstmt.setString(4, paymentDetails);
                paymentPstmt.setString(5, "pending");
                paymentPstmt.executeUpdate();
            }

            // Update room status
            try (PreparedStatement roomPstmt = conn.prepareStatement(roomUpdateQuery)) {
                roomPstmt.setInt(1, booking.getRoomId());
                roomPstmt.executeUpdate();
            }

            // Update user balance
            try (PreparedStatement balancePstmt = conn.prepareStatement(updateUserBalanceQuery)) {
                balancePstmt.setDouble(1, booking.getTotalPrice());
                balancePstmt.setInt(2, booking.getUserId());
                balancePstmt.executeUpdate();
            }

            // Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // Rollback transaction on error
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                // Restore auto-commit mode
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean confirmBooking(int bookingId) {
        String bookingUpdateQuery = "UPDATE Bookings SET status = 'confirmed' WHERE booking_id = ?";
        String paymentUpdateQuery = "UPDATE Payments SET status = 'completed' WHERE booking_id = ?";
        Connection conn = DBConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement bookingPstmt = conn.prepareStatement(bookingUpdateQuery)) {
                bookingPstmt.setInt(1, bookingId);
                bookingPstmt.executeUpdate();
            }

            try (PreparedStatement paymentPstmt = conn.prepareStatement(paymentUpdateQuery)) {
                paymentPstmt.setInt(1, bookingId);
                paymentPstmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, u.firstName || ' ' || u.lastName as customer_name, r.room_number, p.payment_method, p.payment_details " +
            "FROM Bookings b " +
            "JOIN Users u ON b.user_id = u.user_id " +
            "JOIN Rooms r ON b.room_id = r.room_id " +
            "LEFT JOIN Payments p ON b.booking_id = p.booking_id " + // LEFT JOIN to get payment info if available
            "WHERE b.user_id = ? " +
            "ORDER BY b.check_in DESC";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, u.firstName || ' ' || u.lastName as customer_name, r.room_number, p.payment_method, p.payment_details " +
            "FROM Bookings b " +
            "JOIN Users u ON b.user_id = u.user_id " +
            "JOIN Rooms r ON b.room_id = r.room_id " +
            "LEFT JOIN Payments p ON b.booking_id = p.booking_id " + // LEFT JOIN to get payment info if available
            "ORDER BY b.created_at DESC";

        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, u.firstName || ' ' || u.lastName as customer_name, r.room_number, p.payment_method, p.payment_details " +
            "FROM Bookings b " +
            "JOIN Users u ON b.user_id = u.user_id " +
            "JOIN Rooms r ON b.room_id = r.room_id " +
            "LEFT JOIN Payments p ON b.booking_id = p.booking_id " +
            "WHERE b.status = ? " +
            "ORDER BY b.created_at DESC";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean cancelBooking(int bookingId) {
        String selectBookingQuery = "SELECT user_id, total_price FROM Bookings WHERE booking_id = ?";
        String bookingUpdateQuery = "UPDATE Bookings SET status = 'cancelled' WHERE booking_id = ?";
        String roomUpdateQuery = "UPDATE Rooms SET status = 'available' WHERE room_id = (SELECT room_id FROM Bookings WHERE booking_id = ?)";
        String refundUserQuery = "UPDATE Users SET balance = balance + ? WHERE user_id = ?";
        Connection conn = DBConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            // Get booking details for refund
            int userId = -1;
            double totalPrice = 0;
            try (PreparedStatement selectPstmt = conn.prepareStatement(selectBookingQuery)) {
                selectPstmt.setInt(1, bookingId);
                ResultSet rs = selectPstmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    totalPrice = rs.getDouble("total_price");
                }
            }

            if (userId == -1) {
                conn.rollback();
                return false; // Booking not found
            }

            // Update booking status
            try (PreparedStatement bookingPstmt = conn.prepareStatement(bookingUpdateQuery)) {
                bookingPstmt.setInt(1, bookingId);
                bookingPstmt.executeUpdate();
            }

            // Update room status
            try (PreparedStatement roomPstmt = conn.prepareStatement(roomUpdateQuery)) {
                roomPstmt.setInt(1, bookingId);
                roomPstmt.executeUpdate();
            }

            // Refund 50% of the total price to the user's balance
            double refundAmount = totalPrice * 0.5;
            try (PreparedStatement refundPstmt = conn.prepareStatement(refundUserQuery)) {
                refundPstmt.setDouble(1, refundAmount);
                refundPstmt.setInt(2, userId);
                refundPstmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean modifyBooking(int bookingId, LocalDate newCheckOut, double newTotalPrice, double priceDifference) {
        String updateBookingQuery = "UPDATE Bookings SET check_out = ?, total_price = ? WHERE booking_id = ?";
        String updateUserBalanceQuery = "UPDATE Users SET balance = balance - ? WHERE user_id = (SELECT user_id FROM Bookings WHERE booking_id = ?)";
        Connection conn = DBConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            // Update booking
            try (PreparedStatement bookingPstmt = conn.prepareStatement(updateBookingQuery)) {
                bookingPstmt.setLong(1, newCheckOut.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
                bookingPstmt.setDouble(2, newTotalPrice);
                bookingPstmt.setInt(3, bookingId);
                bookingPstmt.executeUpdate();
            }

            // Update user balance
            try (PreparedStatement balancePstmt = conn.prepareStatement(updateUserBalanceQuery)) {
                balancePstmt.setDouble(1, priceDifference);
                balancePstmt.setInt(2, bookingId);
                balancePstmt.executeUpdate();
            }

            // Create notification
            NotificationDAO notificationDAO = new NotificationDAO();
            String message = String.format("Booking #%d has been modified. New check-out date: %s", bookingId, newCheckOut.toString());
            notificationDAO.createNotification(conn, message);

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int cancelExpiredBookings() {
        // Oracle compatible query using SYSTIMESTAMP
        // Changed from 1 HOUR to 2 MINUTE
        String selectQuery = "SELECT booking_id, room_id FROM Bookings WHERE status = 'pending' AND created_at <= SYSTIMESTAMP - INTERVAL '2' MINUTE";
        String updateBookingQuery = "UPDATE Bookings SET status = 'cancelled' WHERE booking_id = ?";
        String updateRoomQuery = "UPDATE Rooms SET status = 'available' WHERE room_id = ?";
        
        int count = 0;
        // Use a new connection for this background task to avoid conflicts with the main UI thread connection
        try (Connection conn = DBConnection.getNewConnection()) {
            if (conn == null) return 0;
            
            conn.setAutoCommit(false);
            
            List<Integer> bookingIds = new ArrayList<>();
            List<Integer> roomIds = new ArrayList<>();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectQuery)) {
                while (rs.next()) {
                    bookingIds.add(rs.getInt("booking_id"));
                    roomIds.add(rs.getInt("room_id"));
                }
            }
            
            if (!bookingIds.isEmpty()) {
                try (PreparedStatement bookingPstmt = conn.prepareStatement(updateBookingQuery);
                     PreparedStatement roomPstmt = conn.prepareStatement(updateRoomQuery)) {
                    
                    for (int i = 0; i < bookingIds.size(); i++) {
                        bookingPstmt.setInt(1, bookingIds.get(i));
                        bookingPstmt.addBatch();
                        
                        roomPstmt.setInt(1, roomIds.get(i));
                        roomPstmt.addBatch();
                    }
                    
                    bookingPstmt.executeBatch();
                    roomPstmt.executeBatch();
                    count = bookingIds.size();
                }
            }
            
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public double getTotalRevenue() {
        String query = "SELECT SUM(total_price) FROM Bookings WHERE status = 'confirmed'";

        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTotalBookings() {
        String query = "SELECT COUNT(*) FROM Bookings WHERE status = 'confirmed'";

        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getPendingBookingsCount() {
        String query = "SELECT COUNT(*) FROM Bookings WHERE status = 'pending'";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean hasPendingBookings() {
        return getPendingBookingsCount() > 0;
    }

    public Map<String, Integer> getMostBookedRooms() {
        Map<String, Integer> roomStats = new HashMap<>();
        String query = "SELECT r.room_number, COUNT(b.booking_id) as booking_count " +
                       "FROM Rooms r " +
                       "JOIN Bookings b ON r.room_id = b.room_id " +
                       "WHERE b.status = 'confirmed' " +
                       "GROUP BY r.room_number " +
                       "ORDER BY booking_count DESC";

        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                roomStats.put(rs.getString("room_number"), rs.getInt("booking_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomStats;
    }

    private Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setRoomId(rs.getInt("room_id"));
        // Retrieve as long (Unix timestamp) and convert to LocalDate
        booking.setCheckIn(Instant.ofEpochMilli(rs.getLong("check_in")).atZone(ZoneId.systemDefault()).toLocalDate());
        booking.setCheckOut(Instant.ofEpochMilli(rs.getLong("check_out")).atZone(ZoneId.systemDefault()).toLocalDate());
        booking.setGuests(rs.getInt("guests"));
        booking.setTotalPrice(rs.getDouble("total_price"));
        booking.setStatus(rs.getString("status"));
        // These might not be present in all queries, handle gracefully if needed
        try {
            booking.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException e) {
            // Column not found, ignore or set to null
            booking.setCustomerName(null);
        }
        try {
            booking.setRoomNumber(rs.getString("room_number"));
        } catch (SQLException e) {
            // Column not found, ignore or set to null
            booking.setRoomNumber(null);
        }
        // New payment fields
        try {
            booking.setPaymentMethod(rs.getString("payment_method"));
            booking.setPaymentDetails(rs.getString("payment_details"));
        } catch (SQLException e) {
            // Payment info might not be in all result sets (e.g., old bookings)
            booking.setPaymentMethod(null);
            booking.setPaymentDetails(null);
        }
        return booking;
    }
}