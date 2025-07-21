package javabank;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * FXML Controller Class for Login Form Handles user authentication and
 * navigation
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private TextField login_acnt_field;

    @FXML
    private PasswordField login_pass_field;

    @FXML
    private Button loginBtn;

    @FXML
    private Button regBtn;

    @FXML
    private ImageView logoImageView;

    // Store current user data after successful login
    private static Document currentUser = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up the logo image
        setupLogo();

        // Set up event handlers
        setupEventHandlers();

        // Test database connection on startup
        testDatabaseConnection();
    }

    /**
     * Setup logo image for the banking application
     */
    private void setupLogo() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/javabank/assets/banklogo.png"));
            logoImageView.setImage(logo);
            System.out.println("Logo setup completed");
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }
    }

    /**
     * Setup event handlers for form elements
     */
    private void setupEventHandlers() {
        // Add login button action
        loginBtn.setOnAction(this::handleLogin);

        // Add Enter key support for login fields
        login_acnt_field.setOnAction(this::handleLogin);
        login_pass_field.setOnAction(this::handleLogin);
    }

    /**
     * Test database connection on application startup
     */
    private void testDatabaseConnection() {
        if (MongoDBConnection.testConnection()) {
            System.out.println("Database connection successful");
        } else {
            showErrorAlert("Database Connection Error",
                    "Unable to connect to database. Please check your connection.");
        }
    }

    /**
     * Handle login button click and authentication
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String accountInput = login_acnt_field.getText().trim();
        String password = login_pass_field.getText();

        // Validate input fields
        if (!validateLoginInput(accountInput, password)) {
            return;
        }

        try {
            // Show loading state
            loginBtn.setDisable(true);
            loginBtn.setText("Logging in...");

            // Attempt authentication
            Document user = authenticateUser(accountInput, password);

            if (user != null) {
                // Login successful
                currentUser = user;
                showSuccessAlert("Login Successful",
                        "Welcome back, " + user.getString("fullName") + "!");

                // Navigate to dashboard/main application window
                navigateToDashboard(event);

            } else {
                // Login failed
                showErrorAlert("Login Failed",
                        "Invalid credentials. Please check your account number/email and password.");
                clearLoginFields();
            }

        } catch (Exception e) {
            showErrorAlert("Login Error",
                    "An error occurred during login: " + e.getMessage());
            System.err.println("Login error: " + e.getMessage());

        } finally {
            // Reset button state
            loginBtn.setDisable(false);
            loginBtn.setText("Login");
        }
    }

    /**
     * Authenticate user with account number/email and password
     */
    private Document authenticateUser(String accountInput, String password) {
        Document user = null;

        try {
            // Check if input is email format
            if (MongoDBConnection.isValidEmail(accountInput)) {
                // Authenticate using email
                user = MongoDBConnection.authenticateUser(accountInput, password);
            } else {
                // Assume it's a user ID or phone number
                // First try to find by user ID
                Document foundUser = MongoDBConnection.findUserById(accountInput);

                if (foundUser == null) {
                    // If not found by ID, try phone number
                    foundUser = MongoDBConnection.findUserByPhone(accountInput);
                }

                // If user found, check password
                if (foundUser != null) {
                    String email = foundUser.getString("email");
                    user = MongoDBConnection.authenticateUser(email, password);
                }
            }

            // Check account status
            if (user != null) {
                String accountStatus = user.getString("accountStatus");
                if (!"ACTIVE".equals(accountStatus)) {
                    showErrorAlert("Account Inactive",
                            "Your account is currently " + accountStatus.toLowerCase()
                            + ". Please contact support.");
                    return null;
                }
            }

        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
        }

        return user;
    }

    /**
     * Validate login input fields
     */
    private boolean validateLoginInput(String accountInput, String password) {
        // Check if fields are empty
        if (accountInput.isEmpty()) {
            showErrorAlert("Validation Error", "Please enter your account number or email.");
            login_acnt_field.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showErrorAlert("Validation Error", "Please enter your password.");
            login_pass_field.requestFocus();
            return false;
        }

        // Validate password length
        if (password.length() < 6) {
            showErrorAlert("Validation Error", "Password must be at least 6 characters long.");
            login_pass_field.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Clear login input fields
     */
    private void clearLoginFields() {
        login_acnt_field.clear();
        login_pass_field.clear();
        login_acnt_field.requestFocus();
    }

    /**
     * Handle register button click - navigate to registration form
     */
    @FXML
    private void handleRegBtn(ActionEvent event) {
        try {
            // Load registration form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegistrationForm.fxml"));
            Parent registrationForm = loader.load();

            // Create new scene and stage
            Scene registrationScene = new Scene(registrationForm);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            currentStage.setScene(registrationScene);
            currentStage.setTitle("JavaBank - User Registration");
            currentStage.show();

        } catch (IOException e) {
            showErrorAlert("Navigation Error",
                    "Unable to open registration form: " + e.getMessage());
            System.err.println("Registration form navigation error: " + e.getMessage());
        }
    }

    /**
     * Navigate to main dashboard after successful login
     */
    private void navigateToDashboard(ActionEvent event) {
        try {
            // Load dashboard/main application window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Parent dashboard = loader.load();

            // Pass user data to dashboard controller if needed
            // DashboardController dashboardController = loader.getController();
            // dashboardController.setCurrentUser(currentUser);
            // Create new scene and stage
            Scene dashboardScene = new Scene(dashboard);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            currentStage.setScene(dashboardScene);
            currentStage.setTitle("JavaBank - Dashboard");
            currentStage.show();

        } catch (IOException e) {
            showErrorAlert("Navigation Error",
                    "Unable to open dashboard: " + e.getMessage());
            System.err.println("Dashboard navigation error: " + e.getMessage());
        }
    }

    /**
     * Show success alert dialog
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error alert dialog
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning alert dialog
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Get current logged-in user
     */
    public static Document getCurrentUser() {
        return currentUser;
    }

    /**
     * Set current user (for testing purposes)
     */
    public static void setCurrentUser(Document user) {
        currentUser = user;
    }

    /**
     * Clear current user session
     */
    public static void clearCurrentUser() {
        currentUser = null;
    }

    /**
     * Check if user is logged in
     */
    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get current user's full name
     */
    public static String getCurrentUserName() {
        if (currentUser != null) {
            return currentUser.getString("fullName");
        }
        return "Guest";
    }

    /**
     * Get current user's ID
     */
    public static String getCurrentUserId() {
        if (currentUser != null) {
            return currentUser.getString("userId");
        }
        return null;
    }

    /**
     * Get current user's email
     */
    public static String getCurrentUserEmail() {
        if (currentUser != null) {
            return currentUser.getString("email");
        }
        return null;
    }

    /**
     * Get current user's account balance
     */
    public static Double getCurrentUserBalance() {
        if (currentUser != null) {
            return currentUser.getDouble("accountBalance");
        }
        return 0.0;
    }
}
