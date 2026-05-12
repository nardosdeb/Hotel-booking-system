
-- Create database
CREATE DATABASE IF NOT EXISTS hotel_db;
USE hotel_db;

-- Users table
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rooms table
CREATE TABLE Rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    status ENUM('available', 'booked', 'maintenance') DEFAULT 'available',
    amenities TEXT
);

-- Bookings table
CREATE TABLE Bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guests INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status ENUM('confirmed', 'cancelled', 'completed') DEFAULT 'confirmed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE
);

-- Payments table (optional)
CREATE TABLE Payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    status ENUM('pending', 'completed', 'failed') DEFAULT 'pending',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
);

-- Insert sample data
INSERT INTO Users (name, email, password, role) VALUES
('Admin User', 'admin@hotel.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV5UiC', 'admin'),
('John Doe', 'john@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV5UiC', 'user');

-- Note: Password is 'password123' hashed with BCrypt

INSERT INTO Rooms (room_number, room_type, description, price, capacity, status, amenities) VALUES
('101', 'Standard', 'Comfortable standard room with basic amenities', 80.00, 2, 'available', 'WiFi, TV, AC'),
('102', 'Standard', 'Comfortable standard room', 80.00, 2, 'available', 'WiFi, TV, AC'),
('201', 'Deluxe', 'Spacious deluxe room with city view', 120.00, 3, 'available', 'WiFi, TV, AC, Mini-bar'),
('202', 'Deluxe', 'Deluxe room with balcony', 120.00, 3, 'available', 'WiFi, TV, AC, Mini-bar, Balcony'),
('301', 'Suite', 'Luxury suite with living area', 200.00, 4, 'available', 'WiFi, TV, AC, Mini-bar, Jacuzzi'),
('302', 'Suite', 'Presidential suite', 250.00, 4, 'available', 'WiFi, TV, AC, Mini-bar, Jacuzzi, Butler Service');

INSERT INTO Bookings (user_id, room_id, check_in, check_out, guests, total_price, status) VALUES
(2, 1, '2024-01-15', '2024-01-18', 2, 240.00, 'completed');