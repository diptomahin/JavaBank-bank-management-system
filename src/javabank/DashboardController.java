package javabank;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;

/**
 * Dashboard Controller for JavaBank Application
 * Handles all banking operations including deposits, withdrawals, transfers, and transaction history
 */
public class DashboardController implements Initializable {

    // Header labels
    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private Label lastTransactionLabel;

    // Deposit tab controls
    @FXML private TextField depositAmountField;
    @FXML private TextArea depositDescriptionArea;
    @FXML private Button depositButton;

    // Withdrawal tab controls
    @FXML private TextField withdrawalAmountField;
    @FXML private TextArea withdrawalDescriptionArea;
    @FXML private Button withdrawalButton;

    // Transfer tab controls
    @FXML private TextField recipientAccountField;
    @FXML private TextField transferAmountField;
    @FXML private TextArea transferDescriptionArea;
    @FXML private Button transferButton;

    // Transaction history controls
    @FXML private TableView<TransactionRow> transactionHistoryTable;
    @FXML private TableColumn<TransactionRow, String> dateColumn;
    @FXML private TableColumn<TransactionRow, String> typeColumn;
    @FXML private TableColumn<TransactionRow, String> amountColumn;
    @FXML private TableColumn<TransactionRow, String> descriptionColumn;
    @FXML private TableColumn<TransactionRow, String> balanceAfterColumn;

    @FXML private ComboBox<String> transactionTypeFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button applyFilterButton;
    @FXML private Button refreshHistoryButton;

    // Current user data
    private Document currentUser;
    private ObservableList<TransactionRow> transactionData;

    /**
     * Initialize the dashboard controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get current user from login controller
        currentUser = FXMLDocumentController.getCurrentUser();
        
        if (currentUser == null) {
            showErrorAlert("Session Error", "No user session found. Please login again.");
            return;
        }

        // Initialize UI components
        initializeUserInterface();
        initializeTransactionTable();
        setupTransactionTypeFilter();
        
        // Load initial data
        refreshUserData();
        loadTransactionHistory();
        
        System.out.println("Dashboard initialized for user: " + currentUser.getString("fullName"));
    }

    /**
     * Initialize user interface with current user data
     */
    private void initializeUserInterface() {
        // Set welcome message
        welcomeLabel.setText("Welcome, " + currentUser.getString("fullName") + "!");
        
        // Set initial balance
        updateBalanceDisplay();
        
        // Setup input validation
        setupInputValidation();
    }

    /**
     * Setup input validation for numeric fields
     */
    private void setupInputValidation() {
        // Add input validation for amount fields
        addNumericValidation(depositAmountField);
        addNumericValidation(withdrawalAmountField);
        addNumericValidation(transferAmountField);
    }

    /**
     * Add numeric validation to a text field
     */
    private void addNumericValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                textField.setText(oldValue);
            }
        });
    }

    /**
     * Initialize transaction history table
     */
    private void initializeTransactionTable() {
        // Initialize table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        balanceAfterColumn.setCellValueFactory(new PropertyValueFactory<>("balanceAfter"));

        // Initialize observable list
        transactionData = FXCollections.observableArrayList();
        transactionHistoryTable.setItems(transactionData);

        // Set table placeholder
        transactionHistoryTable.setPlaceholder(new Label("No transactions found"));
    }

    /**
     * Setup transaction type filter dropdown
     */
    private void setupTransactionTypeFilter() {
        ObservableList<String> transactionTypes = FXCollections.observableArrayList(
            "All Transactions",
            "DEPOSIT",
            "WITHDRAWAL", 
            "TRANSFER_IN",
            "TRANSFER_OUT"
        );
        transactionTypeFilter.setItems(transactionTypes);
        transactionTypeFilter.setValue("All Transactions");
    }

    /**
     * Update balance display
     */
    private void updateBalanceDisplay() {
        Double currentBalance = MongoDBConnection.getCurrentBalance(currentUser.getString("userId"));
        balanceLabel.setText(String.format("Current Balance: $%.2f", currentBalance));
        
        // Update current user object
        currentUser.put("accountBalance", currentBalance);
    }

    /**
     * Refresh user data from database
     */
    private void refreshUserData() {
        try {
            String userId = currentUser.getString("userId");
            Document updatedUser = MongoDBConnection.findUserById(userId);
            
            if (updatedUser != null) {
                currentUser = updatedUser;
                updateBalanceDisplay();
            }
        } catch (Exception e) {
            System.err.println("Error refreshing user data: " + e.getMessage());
        }
    }

    // ==================== DEPOSIT OPERATIONS ====================

    /**
     * Handle deposit button click
     */
    @FXML
    private void handleDeposit(ActionEvent event) {
        String amountText = depositAmountField.getText().trim();
        String description = depositDescriptionArea.getText().trim();

        // Validate input
        if (!validateDepositInput(amountText)) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            
            // Set loading state
            depositButton.setDisable(true);
            depositButton.setText("Processing...");

            // Process deposit
            boolean success = MongoDBConnection.processDeposit(
                currentUser.getString("userId"), 
                amount, 
                description.isEmpty() ? "Deposit" : description
            );

            if (success) {
                showSuccessAlert("Deposit Successful", 
                    String.format("$%.2f has been deposited to your account.", amount));
                
                // Clear form and refresh data
                clearDepositForm();
                refreshUserData();
                loadTransactionHistory();
                
            } else {
                showErrorAlert("Deposit Failed", "Unable to process deposit. Please try again.");
            }

        } catch (Exception e) {
            showErrorAlert("Deposit Error", "An error occurred: " + e.getMessage());
            System.err.println("Deposit error: " + e.getMessage());
            
        } finally {
            // Reset button state
            depositButton.setDisable(false);
            depositButton.setText("Deposit Funds");
        }
    }

    /**
     * Validate deposit input
     */
    private boolean validateDepositInput(String amountText) {
        if (amountText.isEmpty()) {
            showErrorAlert("Invalid Input", "Please enter a deposit amount.");
            depositAmountField.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showErrorAlert("Invalid Amount", "Deposit amount must be greater than 0.");
                depositAmountField.requestFocus();
                return false;
            }
            if (amount > 10000) {
                showErrorAlert("Amount Limit", "Single deposit cannot exceed $10,000.");
                depositAmountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Input", "Please enter a valid number.");
            depositAmountField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Clear deposit form
     */
    private void clearDepositForm() {
        depositAmountField.clear();
        depositDescriptionArea.clear();
    }

    // ==================== WITHDRAWAL OPERATIONS ====================

    /**
     * Handle withdrawal button click
     */
    @FXML
    private void handleWithdrawal(ActionEvent event) {
        String amountText = withdrawalAmountField.getText().trim();
        String description = withdrawalDescriptionArea.getText().trim();

        // Validate input
        if (!validateWithdrawalInput(amountText)) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            
            // Set loading state
            withdrawalButton.setDisable(true);
            withdrawalButton.setText("Processing...");

            // Process withdrawal
            boolean success = MongoDBConnection.processWithdrawal(
                currentUser.getString("userId"), 
                amount, 
                description.isEmpty() ? "Withdrawal" : description
            );

            if (success) {
                showSuccessAlert("Withdrawal Successful", 
                    String.format("$%.2f has been withdrawn from your account.", amount));
                
                // Clear form and refresh data
                clearWithdrawalForm();
                refreshUserData();
                loadTransactionHistory();
                
            } else {
                showErrorAlert("Withdrawal Failed", 
                    "Insufficient funds or unable to process withdrawal.");
            }

        } catch (Exception e) {
            showErrorAlert("Withdrawal Error", "An error occurred: " + e.getMessage());
            System.err.println("Withdrawal error: " + e.getMessage());
            
        } finally {
            // Reset button state
            withdrawalButton.setDisable(false);
            withdrawalButton.setText("Withdraw Funds");
        }
    }

    /**
     * Validate withdrawal input
     */
    private boolean validateWithdrawalInput(String amountText) {
        if (amountText.isEmpty()) {
            showErrorAlert("Invalid Input", "Please enter a withdrawal amount.");
            withdrawalAmountField.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showErrorAlert("Invalid Amount", "Withdrawal amount must be greater than 0.");
                withdrawalAmountField.requestFocus();
                return false;
            }
            
            double currentBalance = currentUser.getDouble("accountBalance");
            if (amount > currentBalance) {
                showErrorAlert("Insufficient Funds", 
                    String.format("Insufficient balance. Available: $%.2f", currentBalance));
                withdrawalAmountField.requestFocus();
                return false;
            }
            
            if (amount > 5000) {
                showErrorAlert("Amount Limit", "Single withdrawal cannot exceed $5,000.");
                withdrawalAmountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Input", "Please enter a valid number.");
            withdrawalAmountField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Clear withdrawal form
     */
    private void clearWithdrawalForm() {
        withdrawalAmountField.clear();
        withdrawalDescriptionArea.clear();
    }

    // ==================== FUND TRANSFER OPERATIONS ====================

    /**
     * Handle fund transfer button click
     */
    @FXML
    private void handleTransfer(ActionEvent event) {
        String recipientAccount = recipientAccountField.getText().trim();
        String amountText = transferAmountField.getText().trim();
        String description = transferDescriptionArea.getText().trim();

        // Validate input
        if (!validateTransferInput(recipientAccount, amountText)) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            
            // Set loading state
            transferButton.setDisable(true);
            transferButton.setText("Processing...");

            // Find recipient
            Document recipient = findRecipient(recipientAccount);
            if (recipient == null) {
                showErrorAlert("Recipient Not Found", 
                    "No account found with the provided account number/email/phone.");
                return;
            }

            // Confirm transfer
            if (!confirmTransfer(recipient, amount)) {
                return;
            }

            // Process transfer
            boolean success = MongoDBConnection.processFundTransfer(
                currentUser.getString("userId"),
                recipient.getString("userId"),
                amount,
                description.isEmpty() ? "Fund Transfer" : description
            );

            if (success) {
                showSuccessAlert("Transfer Successful", 
                    String.format("$%.2f has been transferred to %s.", 
                        amount, recipient.getString("fullName")));
                
                // Clear form and refresh data
                clearTransferForm();
                refreshUserData();
                loadTransactionHistory();
                
            } else {
                showErrorAlert("Transfer Failed", 
                    "Unable to process transfer. Please check your balance and try again.");
            }

        } catch (Exception e) {
            showErrorAlert("Transfer Error", "An error occurred: " + e.getMessage());
            System.err.println("Transfer error: " + e.getMessage());
            
        } finally {
            // Reset button state
            transferButton.setDisable(false);
            transferButton.setText("Transfer Funds");
        }
    }

    /**
     * Find recipient by account number, email, or phone
     */
    private Document findRecipient(String recipientAccount) {
        Document recipient = null;

        // Try to find by user ID first
        recipient = MongoDBConnection.findUserById(recipientAccount);
        
        if (recipient == null) {
            // Try to find by email
            if (MongoDBConnection.isValidEmail(recipientAccount)) {
                recipient = MongoDBConnection.findUserByEmail(recipientAccount);
            }
        }
        
        if (recipient == null) {
            // Try to find by phone
            recipient = MongoDBConnection.findUserByPhone(recipientAccount);
        }

        return recipient;
    }

    /**
     * Confirm transfer with user
     */
    private boolean confirmTransfer(Document recipient, double amount) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Transfer");
        confirmation.setHeaderText("Transfer Confirmation");
        confirmation.setContentText(String.format(
            "Transfer $%.2f to %s?\nAccount: %s", 
            amount, 
            recipient.getString("fullName"),
            recipient.getString("userId")
        ));

        return confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Validate transfer input
     */
    private boolean validateTransferInput(String recipientAccount, String amountText) {
        if (recipientAccount.isEmpty()) {
            showErrorAlert("Invalid Input", "Please enter recipient account information.");
            recipientAccountField.requestFocus();
            return false;
        }

        if (amountText.isEmpty()) {
            showErrorAlert("Invalid Input", "Please enter transfer amount.");
            transferAmountField.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showErrorAlert("Invalid Amount", "Transfer amount must be greater than 0.");
                transferAmountField.requestFocus();
                return false;
            }
            
            double currentBalance = currentUser.getDouble("accountBalance");
            if (amount > currentBalance) {
                showErrorAlert("Insufficient Funds", 
                    String.format("Insufficient balance. Available: $%.2f", currentBalance));
                transferAmountField.requestFocus();
                return false;
            }
            
            if (amount > 10000) {
                showErrorAlert("Amount Limit", "Single transfer cannot exceed $10,000.");
                transferAmountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Input", "Please enter a valid number.");
            transferAmountField.requestFocus();
            return false;
        }

        // Check if trying to transfer to self
        if (recipientAccount.equals(currentUser.getString("userId")) ||
            recipientAccount.equals(currentUser.getString("email")) ||
            recipientAccount.equals(currentUser.getString("phone"))) {
            showErrorAlert("Invalid Recipient", "Cannot transfer funds to yourself.");
            recipientAccountField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Clear transfer form
     */
    private void clearTransferForm() {
        recipientAccountField.clear();
        transferAmountField.clear();
        transferDescriptionArea.clear();
    }

    // ==================== TRANSACTION HISTORY OPERATIONS ====================

    /**
     * Load transaction history
     */
    private void loadTransactionHistory() {
        try {
            String userId = currentUser.getString("userId");
            List<Document> transactions = MongoDBConnection.getUserTransactions(userId);
            
            // Clear existing data
            transactionData.clear();
            
            // Convert documents to table rows
            for (Document transaction : transactions) {
                TransactionRow row = new TransactionRow(
                    transaction.getString("date"),
                    transaction.getString("type"),
                    String.format("$%.2f", transaction.getDouble("amount")),
                    transaction.getString("description"),
                    String.format("$%.2f", transaction.getDouble("balanceAfter"))
                );
                transactionData.add(row);
            }
            
            System.out.println("Loaded " + transactions.size() + " transactions");
            
        } catch (Exception e) {
            showErrorAlert("Data Error", "Unable to load transaction history: " + e.getMessage());
            System.err.println("Error loading transaction history: " + e.getMessage());
        }
    }

    /**
     * Handle refresh history button click
     */
    @FXML
    private void handleRefreshHistory(ActionEvent event) {
        refreshHistoryButton.setDisable(true);
        refreshHistoryButton.setText("Loading...");
        
        // Refresh in background thread
        Platform.runLater(() -> {
            try {
                refreshUserData();
                loadTransactionHistory();
            } finally {
                refreshHistoryButton.setDisable(false);
                refreshHistoryButton.setText("Refresh");
            }
        });
    }

    /**
     * Handle apply filter button click
     */
    @FXML
    private void handleApplyFilter(ActionEvent event) {
        try {
            String selectedType = transactionTypeFilter.getValue();
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            
            // Validate date range
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                showErrorAlert("Invalid Date Range", "From date cannot be after To date.");
                return;
            }
            
            String userId = currentUser.getString("userId");
            String filterType = "All Transactions".equals(selectedType) ? null : selectedType;
            
            List<Document> transactions = MongoDBConnection.getFilteredUserTransactions(
                userId, filterType, fromDate, toDate);
            
            // Clear and reload data
            transactionData.clear();
            
            for (Document transaction : transactions) {
                TransactionRow row = new TransactionRow(
                    transaction.getString("date"),
                    transaction.getString("type"),
                    String.format("$%.2f", transaction.getDouble("amount")),
                    transaction.getString("description"),
                    String.format("$%.2f", transaction.getDouble("balanceAfter"))
                );
                transactionData.add(row);
            }
            
            System.out.println("Applied filter: " + transactions.size() + " transactions found");
            
        } catch (Exception e) {
            showErrorAlert("Filter Error", "Unable to apply filter: " + e.getMessage());
            System.err.println("Error applying filter: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Show success alert
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning alert
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== NAVIGATION METHODS ====================

    /**
     * Handle logout (if you add a logout button)
     */
    public void handleLogout(ActionEvent event) {
        try {
            // Clear current user session
            FXMLDocumentController.clearCurrentUser();
            
            // Navigate back to login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent loginForm = loader.load();
            
            Scene loginScene = new Scene(loginForm);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            currentStage.setScene(loginScene);
            currentStage.setTitle("JavaBank - Login");
            currentStage.show();
            
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Unable to return to login: " + e.getMessage());
            System.err.println("Logout navigation error: " + e.getMessage());
        }
    }

    // ==================== INNER CLASSES ====================

    /**
     * Transaction row class for table display
     */
    public static class TransactionRow {
        private final SimpleStringProperty date;
        private final SimpleStringProperty type;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty description;
        private final SimpleStringProperty balanceAfter;

        public TransactionRow(String date, String type, String amount, 
                            String description, String balanceAfter) {
            this.date = new SimpleStringProperty(date);
            this.type = new SimpleStringProperty(type);
            this.amount = new SimpleStringProperty(amount);
            this.description = new SimpleStringProperty(description);
            this.balanceAfter = new SimpleStringProperty(balanceAfter);
        }

        // Getters for table columns
        public String getDate() { return date.get(); }
        public String getType() { return type.get(); }
        public String getAmount() { return amount.get(); }
        public String getDescription() { return description.get(); }
        public String getBalanceAfter() { return balanceAfter.get(); }

        // Property getters for table binding
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty balanceAfterProperty() { return balanceAfter; }
    }
}