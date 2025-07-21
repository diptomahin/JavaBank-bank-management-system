/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javabank;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import javafx.scene.control.Alert.AlertType;

/**
 * FXML Controller class
 *
 * @author User
 */
public class RegistrationFormController implements Initializable {

    @FXML
    private TextField reg_namefield;
    @FXML
    private TextField reg_emailfield;
    @FXML
    private TextField reg_phonefield;
    @FXML
    private RadioButton male;
    @FXML
    private RadioButton female;
    @FXML
    private RadioButton others;
    @FXML
    private ToggleGroup reg_gender;
    @FXML
    private DatePicker reg_datefiled;
    @FXML
    private PasswordField reg_passwordfield;
    @FXML
    private TextArea reg_streetAddress;
    @FXML
    private TextField reg_thana;
    @FXML
    private TextField reg_district;
    @FXML
    private Button comp_reg_btn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Test database connection on initialization
        if (MongoDBConnection.testConnection()) {
            System.out.println("Controller initialized with database connection");
            // Initialize database indexes
            MongoDBConnection.initializeDatabase();
        } else {
            showAlert(AlertType.ERROR, "Database Error",
                    "Failed to connect to database. Please check your connection.");
        }

    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        if (validateForm()) {
            try {
                // Get form data
                String fullName = reg_namefield.getText().trim();
                String email = reg_emailfield.getText().trim();
                String phone = reg_phonefield.getText().trim();
                String gender = getSelectedGender();
                LocalDate dateOfBirth = reg_datefiled.getValue();
                String password = reg_passwordfield.getText();
                String streetAddress = reg_streetAddress.getText().trim();
                String thana = reg_thana.getText().trim();
                String district = reg_district.getText().trim();

                // Register user using MongoDBConnection
                String userId = MongoDBConnection.registerUser(
                        fullName, email, phone, gender, dateOfBirth,
                        password, streetAddress, thana, district
                );

                if (userId != null) {
                    // Show success message
                    showAlert(AlertType.INFORMATION, "Registration Successful",
                            "User registered successfully!\n\nUser ID: " + userId
                            + "\nEmail: " + email
                            + "\n\nPlease remember your User ID for future login.");

                    // Clear form after successful registration
                    clearForm();
                    // Load the FXML for the new scene
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
                    Parent root = loader.load();

                    // Get current stage and set the new scene
                    Stage stage = (Stage) comp_reg_btn.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("JavaBank Login");
                    stage.show();

                } else {
                    showAlert(AlertType.ERROR, "Registration Failed",
                            "Failed to register user. Please try again.");
                }

            } catch (Exception e) {
                System.err.println("Registration error: " + e.getMessage());
                showAlert(AlertType.ERROR, "Registration Error",
                        "An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Validate full name
        String fullName = reg_namefield.getText().trim();
        if (fullName.isEmpty()) {
            errors.append("• Full Name is required\n");
        } else if (fullName.length() < 2) {
            errors.append("• Full Name must be at least 2 characters long\n");
        } else if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            errors.append("• Full Name should contain only letters and spaces\n");
        }

        // Validate email
        String email = reg_emailfield.getText().trim();
        if (email.isEmpty()) {
            errors.append("• Email is required\n");
        } else if (!MongoDBConnection.isValidEmail(email)) {
            errors.append("• Please enter a valid email address\n");
        } else if (MongoDBConnection.emailExists(email)) {
            errors.append("• This email is already registered in the system\n");
        }

        // Validate phone number
        String phone = reg_phonefield.getText().trim();
        if (phone.isEmpty()) {
            errors.append("• Phone number is required\n");
        } else if (!MongoDBConnection.isValidBangladeshiPhone(phone)) {
            errors.append("• Please enter a valid Bangladeshi phone number (01XXXXXXXXX)\n");
        } else if (MongoDBConnection.phoneExists(phone)) {
            errors.append("• This phone number is already registered\n");
        }

        // Validate gender selection
        if (reg_gender.getSelectedToggle() == null) {
            errors.append("• Please select your gender\n");
        }

        // Validate date of birth
        LocalDate dateOfBirth = reg_datefiled.getValue();
        if (dateOfBirth == null) {
            errors.append("• Date of birth is required\n");
        } else {
            LocalDate today = LocalDate.now();
            LocalDate minAge = today.minusYears(100);
            LocalDate maxAge = today.minusYears(13);

            if (dateOfBirth.isAfter(maxAge)) {
                errors.append("• You must be at least 13 years old to register\n");
            } else if (dateOfBirth.isBefore(minAge)) {
                errors.append("• Please enter a valid date of birth\n");
            }
        }

        // Validate password
        String password = reg_passwordfield.getText();
        if (password.isEmpty()) {
            errors.append("• Password is required\n");
        } else if (password.length() < 6) {
            errors.append("• Password must be at least 6 characters long\n");
        } else if (password.length() > 50) {
            errors.append("• Password cannot exceed 50 characters\n");
        }

        // Validate address fields
        String streetAddress = reg_streetAddress.getText().trim();
        if (streetAddress.isEmpty()) {
            errors.append("• Street Address is required\n");
        } else if (streetAddress.length() < 5) {
            errors.append("• Street Address must be at least 5 characters long\n");
        }

        String thana = reg_thana.getText().trim();
        if (thana.isEmpty()) {
            errors.append("• Thana/Upazila is required\n");
        } else if (thana.length() < 2) {
            errors.append("• Thana/Upazila must be at least 2 characters long\n");
        }

        String district = reg_district.getText().trim();
        if (district.isEmpty()) {
            errors.append("• District is required\n");
        } else if (district.length() < 2) {
            errors.append("• District must be at least 2 characters long\n");
        }

        // Show validation errors if any
        if (errors.length() > 0) {
            showAlert(AlertType.ERROR, "Validation Errors",
                    "Please correct the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private String getSelectedGender() {
        RadioButton selectedRadio = (RadioButton) reg_gender.getSelectedToggle();
        return selectedRadio != null ? selectedRadio.getText() : "";
    }

    private void clearForm() {
        reg_namefield.clear();
        reg_emailfield.clear();
        reg_phonefield.clear();
        reg_passwordfield.clear();
        reg_streetAddress.clear();
        reg_thana.clear();
        reg_district.clear();
        reg_datefiled.setValue(null);

        // Clear gender selection
        if (reg_gender.getSelectedToggle() != null) {
            reg_gender.getSelectedToggle().setSelected(false);
        }

        // Focus back to first field
        reg_namefield.requestFocus();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Make alert resizable for longer messages
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(400);

        alert.showAndWait();
    }

    // Method to test database operations (for debugging)
    @FXML
    private void testDatabaseOperations() {
        System.out.println("Testing database operations...");
        System.out.println("Total users in database: " + MongoDBConnection.getUserCount());
        System.out.println("Connection test: " + MongoDBConnection.testConnection());
    }

}
