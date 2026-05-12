package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField nationalityField;
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    
    // Validation Labels
    @FXML private Label firstNameValidationLabel;
    @FXML private Label lastNameValidationLabel;
    @FXML private Label emailValidationLabel;
    @FXML private Label addressValidationLabel;
    @FXML private Label nationalityValidationLabel;
    @FXML private Label phoneValidationLabel;
    @FXML private Label passwordValidationLabel;
    @FXML private Label confirmPasswordValidationLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        addValidationListener(firstNameField, firstNameValidationLabel, this::validateFirstName);
        addValidationListener(lastNameField, lastNameValidationLabel, this::validateLastName);
        addValidationListener(emailField, emailValidationLabel, this::validateEmail);
        addValidationListener(addressField, addressValidationLabel, this::validateAddress);
        addValidationListener(nationalityField, nationalityValidationLabel, this::validateNationality);
        addValidationListener(phoneNumberField, phoneValidationLabel, this::validatePhoneNumber);
        addValidationListener(passwordField, passwordValidationLabel, this::validatePassword);
        addValidationListener(confirmPasswordField, confirmPasswordValidationLabel, this::validateConfirmPassword);
    }

    private void addValidationListener(TextField field, Label validationLabel, ValidationRule rule) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            rule.validate(newVal, validationLabel);
        });
    }

    @FunctionalInterface
    interface ValidationRule {
        void validate(String value, Label label);
    }

    private void setValidationStatus(Label label, String message, boolean isValid) {
        label.setText(message);
        label.getStyleClass().removeAll("validation-success", "validation-error");
        label.getStyleClass().add(isValid ? "validation-success" : "validation-error");
    }

    // --- Validation Methods ---
    private void validateFirstName(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (value.length() > 25) {
            setValidationStatus(label, "Cannot exceed 25 characters.", false);
        } else if (!value.matches("[a-zA-Z]+")) {
            setValidationStatus(label, "Only letters are allowed.", false);
        } else {
            setValidationStatus(label, "✓ Valid", true);
        }
    }

    private void validateLastName(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (value.length() > 25) {
            setValidationStatus(label, "Cannot exceed 25 characters.", false);
        } else if (!value.matches("[a-zA-Z]+")) {
            setValidationStatus(label, "Only letters are allowed.", false);
        } else {
            setValidationStatus(label, "✓ Valid", true);
        }
    }

    private void validateEmail(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (!value.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            setValidationStatus(label, "Invalid email format.", false);
        } else {
            setValidationStatus(label, "✓ Valid", true);
        }
    }

    private void validateAddress(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (value.length() > 15) {
            setValidationStatus(label, "Cannot exceed 15 characters.", false);
        } else {
            setValidationStatus(label, "✓ Valid", true);
        }
    }

    private void validateNationality(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (value.length() > 15) {
            setValidationStatus(label, "Cannot exceed 15 characters.", false);
        } else if (!value.matches("[a-zA-Z]+")) {
            setValidationStatus(label, "Only letters are allowed.", false);
        } else {
            setValidationStatus(label, "✓ Valid", true);
        }
    }

    private void validatePhoneNumber(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (value.length() > 14) {
            setValidationStatus(label, "Cannot exceed 14 digits.", false);
        } else if (!value.matches("\\d+")) {
            setValidationStatus(label, "Only digits are allowed.", false);
        } else {
            setValidationStatus(label, "✓ Valid", true);
        }
    }

    private void validatePassword(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (value.length() < 6) {
            setValidationStatus(label, "At least 6 characters required.", false);
        } else if (value.length() > 12) {
            setValidationStatus(label, "Cannot exceed 12 characters.", false);
        } else {
            setValidationStatus(label, "✓ Valid", true);
        }
    }

    private void validateConfirmPassword(String value, Label label) {
        if (value.isEmpty()) {
            setValidationStatus(label, "", true);
        } else if (!value.equals(passwordField.getText())) {
            setValidationStatus(label, "Passwords do not match.", false);
        } else {
            setValidationStatus(label, "✓ Passwords match", true);
        }
    }


    @FXML
    private void handleRegister() {
        // Final validation check before submitting
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || emailField.getText().isEmpty() || addressField.getText().isEmpty() || nationalityField.getText().isEmpty() || phoneNumberField.getText().isEmpty() || passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()) {
            errorLabel.setText("All fields are required");
            return;
        }
        
        // Hash the password
        String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());

        // Create new user
        User user = new User(firstNameField.getText(), lastNameField.getText(), emailField.getText(), addressField.getText(), nationalityField.getText(), phoneNumberField.getText(), hashedPassword, "user");
        if (userDAO.registerUser(user)) {
            // Automatically log in the user and go to the dashboard
            User registeredUser = userDAO.authenticate(user.getEmail(), passwordField.getText());
            if (registeredUser != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                    Parent root = loader.load();

                    DashboardController controller = loader.getController();
                    controller.setCurrentUser(registeredUser);

                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); // Add stylesheet

                    Stage stage = (Stage) firstNameField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Hilton Hotel - Dashboard");
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
                    stage.setMaximized(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                errorLabel.setText("Registration successful, but auto-login failed. Please login manually.");
            }
        } else {
            errorLabel.setText("Registration failed. Please try again.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm()); // Add stylesheet
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}