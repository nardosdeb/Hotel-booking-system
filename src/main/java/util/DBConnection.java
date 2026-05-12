package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DBConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "system";
    private static final String PASSWORD = "system";

    private static Connection connection = null;

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.setAutoCommit(true);
                createTablesAndInitialData();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static void createTablesAndInitialData() {
        String hashedPassword = "$2a$10$Jz4zNZpFFcYjlj1e.4jcauNMCFPYbB4zJp/84PtlOqpUyeX5PdSHC";

        // Always attempt to create tables to ensure they exist
        createAllTables();

        // Only insert initial data if Users table is empty
        if (isTableEmpty("Users")) {
            insertInitialData(hashedPassword);
        }
    }

    private static boolean isTableEmpty(String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    private static void createAllTables() {
        String[] sequences = {
            "CREATE SEQUENCE Users_SEQ START WITH 1 INCREMENT BY 1",
            "CREATE SEQUENCE Rooms_SEQ START WITH 1 INCREMENT BY 1",
            "CREATE SEQUENCE Bookings_SEQ START WITH 1 INCREMENT BY 1",
            "CREATE SEQUENCE Payments_SEQ START WITH 1 INCREMENT BY 1",
            "CREATE SEQUENCE PaymentMethods_SEQ START WITH 1 INCREMENT BY 1",
            "CREATE SEQUENCE Notifications_SEQ START WITH 1 INCREMENT BY 1"
        };

        String[] tables = {
            // Users table
            "CREATE TABLE Users (" +
                    "   user_id NUMBER PRIMARY KEY," +
                    "   firstName VARCHAR2(50) NOT NULL," +
                    "   lastName VARCHAR2(50) NOT NULL," +
                    "   email VARCHAR2(100) UNIQUE NOT NULL," +
                    "   address VARCHAR2(255)," +
                    "   nationality VARCHAR2(50)," +
                    "   phoneNumber VARCHAR2(20)," +
                    "   password VARCHAR2(255) NOT NULL," +
                    "   role VARCHAR2(20) DEFAULT 'user' CHECK(role IN ('user', 'admin'))," +
                    "   balance NUMBER DEFAULT 10000.0," +
                    "   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")",

            // Rooms table
            "CREATE TABLE Rooms (" +
                    "   room_id NUMBER PRIMARY KEY," +
                    "   room_number VARCHAR2(20) UNIQUE NOT NULL," +
                    "   room_type VARCHAR2(50) NOT NULL," +
                    "   description VARCHAR2(255)," +
                    "   price NUMBER NOT NULL," +
                    "   capacity NUMBER NOT NULL," +
                    "   status VARCHAR2(20) DEFAULT 'available' CHECK(status IN ('available', 'booked', 'maintenance'))," +
                    "   amenities VARCHAR2(255)" +
                    ")",

            // Bookings table
            "CREATE TABLE Bookings (" +
                    "   booking_id NUMBER PRIMARY KEY," +
                    "   user_id NUMBER NOT NULL," +
                    "   room_id NUMBER NOT NULL," +
                    "   check_in NUMBER NOT NULL," +
                    "   check_out NUMBER NOT NULL," +
                    "   guests NUMBER NOT NULL," +
                    "   total_price NUMBER NOT NULL," +
                    "   status VARCHAR2(20) DEFAULT 'pending' CHECK(status IN ('pending', 'confirmed', 'cancelled', 'completed'))," +
                    "   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "   FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE," +
                    "   FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE" +
                    ")",

            // Payments table
            "CREATE TABLE Payments (" +
                    "   payment_id NUMBER PRIMARY KEY," +
                    "   booking_id NUMBER NOT NULL," +
                    "   amount NUMBER NOT NULL," +
                    "   payment_method VARCHAR2(50)," +
                    "   payment_details VARCHAR2(255)," +
                    "   status VARCHAR2(20) DEFAULT 'pending' CHECK(status IN ('pending', 'completed', 'failed'))," +
                    "   transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "   FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)" +
                    ")",

            // PaymentMethods table
            "CREATE TABLE PaymentMethods (" +
                    "   method_id NUMBER PRIMARY KEY," +
                    "   method_name VARCHAR2(50) UNIQUE NOT NULL," +
                    "   account_details VARCHAR2(255) NOT NULL" +
                    ")",

            // Notifications table
            "CREATE TABLE Notifications (" +
                    "   notification_id NUMBER PRIMARY KEY," +
                    "   message VARCHAR2(255) NOT NULL," +
                    "   is_read NUMBER(1) DEFAULT 0," +
                    "   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
        };

        String[] triggers = {
            "CREATE OR REPLACE TRIGGER Users_TRG BEFORE INSERT ON Users FOR EACH ROW BEGIN SELECT Users_SEQ.NEXTVAL INTO :new.user_id FROM dual; END;",
            "CREATE OR REPLACE TRIGGER Rooms_TRG BEFORE INSERT ON Rooms FOR EACH ROW BEGIN SELECT Rooms_SEQ.NEXTVAL INTO :new.room_id FROM dual; END;",
            "CREATE OR REPLACE TRIGGER Bookings_TRG BEFORE INSERT ON Bookings FOR EACH ROW BEGIN SELECT Bookings_SEQ.NEXTVAL INTO :new.booking_id FROM dual; END;",
            "CREATE OR REPLACE TRIGGER Payments_TRG BEFORE INSERT ON Payments FOR EACH ROW BEGIN SELECT Payments_SEQ.NEXTVAL INTO :new.payment_id FROM dual; END;",
            "CREATE OR REPLACE TRIGGER PaymentMethods_TRG BEFORE INSERT ON PaymentMethods FOR EACH ROW BEGIN SELECT PaymentMethods_SEQ.NEXTVAL INTO :new.method_id FROM dual; END;",
            "CREATE OR REPLACE TRIGGER Notifications_TRG BEFORE INSERT ON Notifications FOR EACH ROW BEGIN SELECT Notifications_SEQ.NEXTVAL INTO :new.notification_id FROM dual; END;"
        };

        try (Statement stmt = connection.createStatement()) {
            // Create Sequences
            for (String sql : sequences) {
                try {
                    stmt.execute(sql);
                    System.out.println("Sequence created: " + sql.split(" ")[2]);
                } catch (SQLException e) {
                    if (e.getErrorCode() == 955) { // Name already used
                        System.out.println("Sequence already exists, skipping...");
                    } else {
                        System.err.println("Error creating sequence: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Create Tables
            // Disable foreign key constraints temporarily
            stmt.execute("ALTER SESSION SET CONSTRAINTS = DEFERRED");

            for (String query : tables) {
                try {
                    stmt.execute(query);
                    System.out.println("Table created successfully");
                } catch (SQLException e) {
                    if (e.getErrorCode() == 955) { // Table already exists
                        System.out.println("Table already exists, skipping...");
                    } else {
                        System.err.println("Error creating table: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // Create Triggers
            for (String sql : triggers) {
                try {
                    stmt.execute(sql);
                    System.out.println("Trigger created successfully");
                } catch (SQLException e) {
                    System.err.println("Error creating trigger: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertInitialData(String hashedPassword) {
        String[] initialDataQueries = {
                "INSERT INTO Users (firstName, lastName, email, password, role) VALUES " +
                        "('Admin', 'User', 'admin@hotel.com', '" + hashedPassword + "', 'admin')",
                "INSERT INTO Users (firstName, lastName, email, password, role) VALUES " +
                        "('John', 'Doe', 'john@example.com', '" + hashedPassword + "', 'user')",

                "INSERT INTO Rooms (room_number, room_type, description, price, capacity, amenities) VALUES " +
                        "('101', 'Standard', 'Comfortable standard room with basic amenities', 80.00, 2, 'WiFi, TV, AC')",
                "INSERT INTO Rooms (room_number, room_type, description, price, capacity, amenities) VALUES " +
                        "('102', 'Standard', 'Comfortable standard room', 80.00, 2, 'WiFi, TV, AC')",
                "INSERT INTO Rooms (room_number, room_type, description, price, capacity, amenities) VALUES " +
                        "('201', 'Deluxe', 'Spacious deluxe room with city view', 120.00, 3, 'WiFi, TV, AC, Mini-bar')",
                "INSERT INTO Rooms (room_number, room_type, description, price, capacity, amenities) VALUES " +
                        "('202', 'Deluxe', 'Deluxe room with balcony', 120.00, 3, 'WiFi, TV, AC, Mini-bar, Balcony')",
                "INSERT INTO Rooms (room_number, room_type, description, price, capacity, amenities) VALUES " +
                        "('301', 'Suite', 'Luxury suite with living area', 200.00, 4, 'WiFi, TV, AC, Mini-bar, Jacuzzi')",
                "INSERT INTO Rooms (room_number, room_type, description, price, capacity, amenities) VALUES " +
                        "('302', 'Suite', 'Presidential suite', 250.00, 4, 'WiFi, TV, AC, Mini-bar, Jacuzzi, Butler Service')",

                "INSERT INTO PaymentMethods (method_name, account_details) VALUES " +
                        "('Credit Card', '1234-5678-9012-3456')",
                "INSERT INTO PaymentMethods (method_name, account_details) VALUES " +
                        "('Bank', '1000123456789')",
                "INSERT INTO PaymentMethods (method_name, account_details) VALUES " +
                        "('Mobile Banking', '0912345678')",
                "INSERT INTO PaymentMethods (method_name, account_details) VALUES " +
                        "('Telebirr', '0912345678')"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String query : initialDataQueries) {
                try {
                    stmt.execute(query);
                    System.out.println("Initial data inserted");
                } catch (SQLException e) {
                    System.err.println("Error inserting data: " + e.getMessage());
                }
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getNewConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}