package dao;

import model.Room;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public List<Room> getAvailableRooms(Date checkIn, Date checkOut, String roomType, int guests) {
        List<Room> rooms = new ArrayList<>();
        // Convert java.sql.Date to epoch millis for comparison with NUMBER columns
        long checkInMillis = checkIn.getTime();
        long checkOutMillis = checkOut.getTime();

        String query = "SELECT r.* FROM Rooms r " +
            "WHERE r.status = 'available' " +
            "AND r.capacity >= ? " +
            "AND (? IS NULL OR r.room_type = ?) " +
            "AND r.room_id NOT IN ( " +
            "    SELECT b.room_id FROM Bookings b " +
            "    WHERE b.status = 'confirmed' " +
            "    AND ( " +
            "        (b.check_in <= ? AND b.check_out >= ?) " +
            "        OR (b.check_in <= ? AND b.check_out >= ?) " +
            "        OR (b.check_in >= ? AND b.check_out <= ?) " +
            "    ) " +
            ")";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, guests);
            pstmt.setString(2, roomType);
            pstmt.setString(3, roomType);
            pstmt.setLong(4, checkInMillis);
            pstmt.setLong(5, checkInMillis);
            pstmt.setLong(6, checkOutMillis);
            pstmt.setLong(7, checkOutMillis);
            pstmt.setLong(8, checkInMillis);
            pstmt.setLong(9, checkOutMillis);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public Room getRoomById(int roomId) {
        String query = "SELECT * FROM Rooms WHERE room_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractRoomFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM Rooms ORDER BY room_number";

        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public int getTotalRooms() {
        String query = "SELECT COUNT(*) FROM Rooms";
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

    public int getTotalAvailableRooms() {
        String query = "SELECT COUNT(*) FROM Rooms WHERE status = 'available'";
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

    public int getTotalBookedRooms() {
        String query = "SELECT COUNT(*) FROM Rooms WHERE status = 'booked'";
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

    public boolean addRoom(Room room) {
        // Check if room number already exists
        String checkQuery = "SELECT COUNT(*) FROM Rooms WHERE room_number = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, room.getRoomNumber());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Room number " + room.getRoomNumber() + " already exists.");
                return false; // Room already exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String query = "INSERT INTO Rooms (room_number, room_type, description, price, capacity, status, amenities) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setString(3, room.getDescription());
            pstmt.setDouble(4, room.getPrice());
            pstmt.setInt(5, room.getCapacity());
            pstmt.setString(6, room.getStatus());
            pstmt.setString(7, room.getAmenities());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRoom(Room room) {
        String query = "UPDATE Rooms SET room_number = ?, room_type = ?, description = ?, price = ?, capacity = ?, status = ?, amenities = ? WHERE room_id = ?";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setString(3, room.getDescription());
            pstmt.setDouble(4, room.getPrice());
            pstmt.setInt(5, room.getCapacity());
            pstmt.setString(6, room.getStatus());
            pstmt.setString(7, room.getAmenities());
            pstmt.setInt(8, room.getRoomId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoom(int roomId) {
        String query = "DELETE FROM Rooms WHERE room_id = ?";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, roomId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(rs.getString("room_type"));
        room.setDescription(rs.getString("description"));
        room.setPrice(rs.getDouble("price"));
        room.setCapacity(rs.getInt("capacity"));
        room.setStatus(rs.getString("status"));
        room.setAmenities(rs.getString("amenities"));
        return room;
    }
}