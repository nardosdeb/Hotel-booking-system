package dao;

import model.Notification;
import util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean createNotification(Connection conn, String message) throws SQLException {
        String query = "INSERT INTO Notifications (message) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, message);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Notification> getUnreadNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM Notifications WHERE is_read = 0 ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Notification notification = new Notification();
                notification.setNotificationId(rs.getInt("notification_id"));
                notification.setMessage(rs.getString("message"));
                notification.setRead(rs.getInt("is_read") == 1);
                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public boolean markAllAsRead() {
        String query = "UPDATE Notifications SET is_read = 1 WHERE is_read = 0";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}