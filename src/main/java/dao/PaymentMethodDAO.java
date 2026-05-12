package dao;

import model.PaymentMethod;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDAO {

    public List<PaymentMethod> getAllPaymentMethods() {
        List<PaymentMethod> methods = new ArrayList<>();
        String query = "SELECT * FROM PaymentMethods";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                PaymentMethod method = new PaymentMethod();
                method.setMethodId(rs.getInt("method_id"));
                method.setMethodName(rs.getString("method_name"));
                method.setAccountDetails(rs.getString("account_details"));
                methods.add(method);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return methods;
    }

    public boolean addPaymentMethod(PaymentMethod method) {
        String query = "INSERT INTO PaymentMethods (method_name, account_details) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, method.getMethodName());
            pstmt.setString(2, method.getAccountDetails());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentMethod(PaymentMethod method) {
        String query = "UPDATE PaymentMethods SET method_name = ?, account_details = ? WHERE method_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, method.getMethodName());
            pstmt.setString(2, method.getAccountDetails());
            pstmt.setInt(3, method.getMethodId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePaymentMethod(int methodId) {
        String query = "DELETE FROM PaymentMethods WHERE method_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, methodId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}