/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javabank;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import org.bson.Document;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Optional;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardController implements Initializable {

    // User session data
    private Document currentUser;
    private String currentUserId;

    // Header controls
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;

    // Cash In controls
    @FXML
    private ComboBox<String> cashInServiceCombo;
    @FXML
    private TextField cashInMobileField;
    @FXML
    private Button loadSavedNumberButton;
    @FXML
    private CheckBox saveNumberCheckbox;
    @FXML
    private TextField cashInAmountField;
    @FXML
    private TextField cashInPinField;
    @FXML
    private TextArea cashInDescriptionArea;
    @FXML
    private Button cashInButton;

    // Cash Out controls
    @FXML
    private ComboBox<String> cashOutServiceCombo;
    @FXML
    private TextField cashOutMobileField;
    @FXML
    private Button loadSavedNumberButtonCashOut;
    @FXML
    private CheckBox saveNumberCheckboxCashOut;
    @FXML
    private TextField cashOutAmountField;
    @FXML
    private TextField cashOutPinField;
    @FXML
    private TextArea cashOutDescriptionArea;
    @FXML
    private Button cashOutButton;

    // Fund Transfer controls
    @FXML
    private TextField recipientAccountField;
    @FXML
    private TextField transferAmountField;
    @FXML
    private TextArea transferDescriptionArea;
    @FXML
    private Button transferButton;

    // Transaction History controls
    @FXML
    private Button refreshHistoryButton;
    @FXML
    private ComboBox<String> transactionTypeFilter;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private Button applyFilterButton;
    @FXML
    private TableView<TransactionRow> transactionHistoryTable;
    @FXML
    private TableColumn<TransactionRow, String> dateColumn;
    @FXML
    private TableColumn<TransactionRow, String> typeColumn;
    @FXML
    private TableColumn<TransactionRow, String> amountColumn;
    @FXML
    private TableColumn<TransactionRow, String> descriptionColumn;
    @FXML
    private TableColumn<TransactionRow, String> balanceAfterColumn;

    // Saved Numbers controls
    @FXML
    private ComboBox<String> saveServiceCombo;
    @FXML
    private TextField saveNumberField;
    @FXML
    private TextField saveNumberLabelField;
    @FXML
    private Button addNumberButton;
    @FXML
    private TableView<SavedNumberRow> savedNumbersTable;
    @FXML
    private TableColumn<SavedNumberRow, String> serviceColumn;
    @FXML
    private TableColumn<SavedNumberRow, String> numberColumn;
    @FXML
    private TableColumn<SavedNumberRow, String> labelColumn;
    @FXML
    private TableColumn<SavedNumberRow, String> actionColumn;
    @FXML private Button logoutButton;
    // Data lists
    private ObservableList<TransactionRow> transactionData = FXCollections.observableArrayList();
    private ObservableList<SavedNumberRow> savedNumberData = FXCollections.observableArrayList();

    // Mobile banking services
    private final String[] mobileServices = {"bKash", "Nagad", "Rocket", "Upay", "SureCash"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();
        setupTableColumns();
        setupTransactionTypeFilter();
    }

    // Initialize the dashboard with user data
    public void initializeUserData(Document user) {
        this.currentUser = user;
        this.currentUserId = user.getString("userId");

        // Update header labels
        welcomeLabel.setText("Welcome, " + user.getString("fullName") + "!");
        updateBalanceLabel();

        // Load user data
        loadTransactionHistory();
        loadSavedNumbers();
    }

    private void setupComboBoxes() {
        // Setup mobile service combo boxes
        cashInServiceCombo.getItems().addAll(mobileServices);
        cashOutServiceCombo.getItems().addAll(mobileServices);
        saveServiceCombo.getItems().addAll(mobileServices);
    }

    private void setupTableColumns() {
        // Setup transaction history table
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        balanceAfterColumn.setCellValueFactory(new PropertyValueFactory<>("balanceAfter"));
        transactionHistoryTable.setItems(transactionData);

        // Setup saved numbers table
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("service"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        savedNumbersTable.setItems(savedNumberData);

        // Add delete buttons to saved numbers table
        actionColumn.setCellFactory(col -> {
            TableCell<SavedNumberRow, String> cell = new TableCell<SavedNumberRow, String>() {
                private final Button deleteButton = new Button("Delete");

                {
                    deleteButton.setOnAction(event -> {
                        SavedNumberRow row = getTableView().getItems().get(getIndex());
                        deleteSavedNumber(row);
                    });
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            };
            return cell;
        });
    }

    private void setupTransactionTypeFilter() {
        transactionTypeFilter.getItems().addAll(
                "All Transactions", "DEPOSIT", "WITHDRAWAL", "TRANSFER_IN", "TRANSFER_OUT"
        );
        transactionTypeFilter.setValue("All Transactions");
    }

    private void updateBalanceLabel() {
        Double balance = MongoDBConnection.getCurrentBalance(currentUserId);
        balanceLabel.setText("Current Balance: $" + String.format("%.2f", balance));
    }

    // ==================== CASH IN OPERATIONS ====================
    @FXML
    private void handleCashIn(ActionEvent event) {
        try {
            // Validate inputs
            if (!validateCashInInputs()) {
                return;
            }

            String service = cashInServiceCombo.getValue();
            String mobile = cashInMobileField.getText().trim();
            String pin = cashInPinField.getText().trim();
            double amount = Double.parseDouble(cashInAmountField.getText().trim());
            String description = cashInDescriptionArea.getText().trim();

            // Simulate mobile banking validation (in real app, this would call API)
            if (!simulateMobileBankingAuth(service, mobile, pin)) {
                showAlert(Alert.AlertType.ERROR, "Authentication Failed",
                        "Invalid mobile banking credentials. Please check your number and PIN.");
                return;
            }

            // Process deposit
            String transactionDesc = "Cash In via " + service + " (" + mobile + ")"
                    + (description.isEmpty() ? "" : " - " + description);

            if (MongoDBConnection.processDeposit(currentUserId, amount, transactionDesc)) {
                // Save number if requested
                if (saveNumberCheckbox.isSelected() && !mobile.isEmpty()) {
                    saveMobileNumber(service, mobile, "Cash In");
                }

                // Clear form and refresh
                clearCashInForm();
                updateBalanceLabel();
                loadTransactionHistory();

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Cash In successful! Amount: $" + String.format("%.2f", amount));
            } else {
                showAlert(Alert.AlertType.ERROR, "Transaction Failed",
                        "Failed to process cash in. Please try again.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                    "Please enter a valid amount.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An error occurred: " + e.getMessage());
        }
    }

    private boolean validateCashInInputs() {
        if (cashInServiceCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select a mobile banking service.");
            return false;
        }

        if (cashInMobileField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter mobile number.");
            return false;
        }

        if (!MongoDBConnection.isValidBangladeshiPhone(cashInMobileField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Mobile Number", "Please enter a valid Bangladeshi mobile number.");
            return false;
        }

        if (cashInAmountField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter amount.");
            return false;
        }

        try {
            double amount = Double.parseDouble(cashInAmountField.getText().trim());
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Amount must be greater than 0.");
                return false;
            }
            if (amount > 50000) {
                showAlert(Alert.AlertType.WARNING, "Amount Limit Exceeded", "Maximum cash in limit is $50,000.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Please enter a valid amount.");
            return false;
        }

        if (cashInPinField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter your mobile banking PIN.");
            return false;
        }

        return true;
    }

    private void clearCashInForm() {
        cashInServiceCombo.setValue(null);
        cashInMobileField.clear();
        cashInAmountField.clear();
        cashInPinField.clear();
        cashInDescriptionArea.clear();
        saveNumberCheckbox.setSelected(false);
    }

    // ==================== CASH OUT OPERATIONS ====================
    @FXML
    private void handleCashOut(ActionEvent event) {
        try {
            // Validate inputs
            if (!validateCashOutInputs()) {
                return;
            }

            String service = cashOutServiceCombo.getValue();
            String mobile = cashOutMobileField.getText().trim();
            String pin = cashOutPinField.getText().trim();
            double amount = Double.parseDouble(cashOutAmountField.getText().trim());
            String description = cashOutDescriptionArea.getText().trim();

            // Check sufficient balance
            Double currentBalance = MongoDBConnection.getCurrentBalance(currentUserId);
            if (currentBalance < amount) {
                showAlert(Alert.AlertType.ERROR, "Insufficient Funds",
                        "Your current balance is insufficient for this transaction.");
                return;
            }

            // Simulate mobile banking validation
            if (!simulateMobileBankingAuth(service, mobile, pin)) {
                showAlert(Alert.AlertType.ERROR, "Authentication Failed",
                        "Invalid mobile banking credentials. Please check your number and PIN.");
                return;
            }

            // Process withdrawal
            String transactionDesc = "Cash Out via " + service + " (" + mobile + ")"
                    + (description.isEmpty() ? "" : " - " + description);

            if (MongoDBConnection.processWithdrawal(currentUserId, amount, transactionDesc)) {
                // Save number if requested
                if (saveNumberCheckboxCashOut.isSelected() && !mobile.isEmpty()) {
                    saveMobileNumber(service, mobile, "Cash Out");
                }

                // Clear form and refresh
                clearCashOutForm();
                updateBalanceLabel();
                loadTransactionHistory();

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Cash Out successful! Amount: $" + String.format("%.2f", amount));
            } else {
                showAlert(Alert.AlertType.ERROR, "Transaction Failed",
                        "Failed to process cash out. Please try again.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                    "Please enter a valid amount.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An error occurred: " + e.getMessage());
        }
    }

    private boolean validateCashOutInputs() {
        if (cashOutServiceCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select a mobile banking service.");
            return false;
        }

        if (cashOutMobileField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter mobile number.");
            return false;
        }

        if (!MongoDBConnection.isValidBangladeshiPhone(cashOutMobileField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Mobile Number", "Please enter a valid Bangladeshi mobile number.");
            return false;
        }

        if (cashOutAmountField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter amount.");
            return false;
        }

        try {
            double amount = Double.parseDouble(cashOutAmountField.getText().trim());
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Amount must be greater than 0.");
                return false;
            }
            if (amount > 25000) {
                showAlert(Alert.AlertType.WARNING, "Amount Limit Exceeded", "Maximum cash out limit is $25,000.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Please enter a valid amount.");
            return false;
        }

        if (cashOutPinField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter your mobile banking PIN.");
            return false;
        }

        return true;
    }

    private void clearCashOutForm() {
        cashOutServiceCombo.setValue(null);
        cashOutMobileField.clear();
        cashOutAmountField.clear();
        cashOutPinField.clear();
        cashOutDescriptionArea.clear();
        saveNumberCheckboxCashOut.setSelected(false);
    }

    // ==================== FUND TRANSFER OPERATIONS ====================
    @FXML
    private void handleTransfer(ActionEvent event) {
        try {
            // Validate inputs
            if (!validateTransferInputs()) {
                return;
            }

            String recipientAccount = recipientAccountField.getText().trim();
            double amount = Double.parseDouble(transferAmountField.getText().trim());
            String description = transferDescriptionArea.getText().trim();

            // Check if recipient exists
            Document recipient = MongoDBConnection.findUserById(recipientAccount);
            if (recipient == null) {
                showAlert(Alert.AlertType.ERROR, "Recipient Not Found",
                        "The recipient account does not exist.");
                return;
            }

            // Check if not transferring to self
            if (recipientAccount.equals(currentUserId)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Transfer",
                        "You cannot transfer funds to your own account.");
                return;
            }

            // Check sufficient balance
            Double currentBalance = MongoDBConnection.getCurrentBalance(currentUserId);
            if (currentBalance < amount) {
                showAlert(Alert.AlertType.ERROR, "Insufficient Funds",
                        "Your current balance is insufficient for this transfer.");
                return;
            }

            // Confirm transfer
            String recipientName = recipient.getString("fullName");
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Transfer");
            confirmation.setHeaderText("Transfer Confirmation");
            confirmation.setContentText("Transfer $" + String.format("%.2f", amount)
                    + " to " + recipientName + " (" + recipientAccount + ")?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Process transfer
                String transactionDesc = description.isEmpty() ? "Fund Transfer" : description;

                if (MongoDBConnection.processFundTransfer(currentUserId, recipientAccount, amount, transactionDesc)) {
                    // Clear form and refresh
                    clearTransferForm();
                    updateBalanceLabel();
                    loadTransactionHistory();

                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Fund transfer successful!\nAmount: $" + String.format("%.2f", amount)
                            + "\nRecipient: " + recipientName);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Transfer Failed",
                            "Failed to process fund transfer. Please try again.");
                }
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount",
                    "Please enter a valid amount.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An error occurred: " + e.getMessage());
        }
    }

    private boolean validateTransferInputs() {
        if (recipientAccountField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter recipient account number.");
            return false;
        }

        if (transferAmountField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter transfer amount.");
            return false;
        }

        try {
            double amount = Double.parseDouble(transferAmountField.getText().trim());
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Amount must be greater than 0.");
                return false;
            }
            if (amount > 100000) {
                showAlert(Alert.AlertType.WARNING, "Amount Limit Exceeded", "Maximum transfer limit is $100,000.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Please enter a valid amount.");
            return false;
        }

        return true;
    }

    private void clearTransferForm() {
        recipientAccountField.clear();
        transferAmountField.clear();
        transferDescriptionArea.clear();
    }

    // ==================== TRANSACTION HISTORY OPERATIONS ====================
    @FXML
    private void handleRefreshHistory(ActionEvent event) {
        loadTransactionHistory();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Transaction history has been refreshed.");
    }

    @FXML
    private void handleApplyFilter(ActionEvent event) {
        String selectedType = transactionTypeFilter.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        // Validate date range
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date Range",
                    "From date cannot be after to date.");
            return;
        }

        loadFilteredTransactionHistory(selectedType, fromDate, toDate);
    }

    private void loadTransactionHistory() {
        transactionData.clear();
        List<Document> transactions = MongoDBConnection.getUserTransactions(currentUserId);

        for (Document transaction : transactions) {
            TransactionRow row = new TransactionRow(
                    transaction.getString("date"),
                    transaction.getString("type"),
                    "$" + String.format("%.2f", transaction.getDouble("amount")),
                    transaction.getString("description"),
                    "$" + String.format("%.2f", transaction.getDouble("balanceAfter"))
            );
            transactionData.add(row);
        }
    }

    private void loadFilteredTransactionHistory(String type, LocalDate fromDate, LocalDate toDate) {
        transactionData.clear();
        List<Document> transactions = MongoDBConnection.getFilteredUserTransactions(
                currentUserId, type, fromDate, toDate);

        for (Document transaction : transactions) {
            TransactionRow row = new TransactionRow(
                    transaction.getString("date"),
                    transaction.getString("type"),
                    "$" + String.format("%.2f", transaction.getDouble("amount")),
                    transaction.getString("description"),
                    "$" + String.format("%.2f", transaction.getDouble("balanceAfter"))
            );
            transactionData.add(row);
        }
    }

    // ==================== SAVED NUMBERS OPERATIONS ====================
    @FXML
    private void handleLoadSavedNumber(ActionEvent event) {
        loadSavedNumberForCashIn();
    }

    @FXML
    private void handleLoadSavedNumberCashOut(ActionEvent event) {
        loadSavedNumberForCashOut();
    }

    @FXML
    private void handleAddSavedNumber(ActionEvent event) {
        try {
            String service = saveServiceCombo.getValue();
            String number = saveNumberField.getText().trim();
            String label = saveNumberLabelField.getText().trim();

            if (service == null || service.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please select a service.");
                return;
            }

            if (number.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Information", "Please enter a mobile number.");
                return;
            }

            if (!MongoDBConnection.isValidBangladeshiPhone(number)) {
                showAlert(Alert.AlertType.WARNING, "Invalid Number", "Please enter a valid Bangladeshi mobile number.");
                return;
            }

            if (label.isEmpty()) {
                label = "Saved Number";
            }

            if (saveMobileNumber(service, number, label)) {
                saveServiceCombo.setValue(null);
                saveNumberField.clear();
                saveNumberLabelField.clear();
                loadSavedNumbers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Number saved successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save number. Number may already exist.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private void loadSavedNumberForCashIn() {
        List<Document> savedNumbers = getSavedNumbers();
        if (savedNumbers.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Saved Numbers", "You have no saved numbers.");
            return;
        }

        // Create choice dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Load Saved Number");
        dialog.setHeaderText("Select a saved number:");

        List<String> choices = new ArrayList<>();
        for (Document number : savedNumbers) {
            String choice = number.getString("service") + " - "
                    + number.getString("number") + " ("
                    + number.getString("label") + ")";
            choices.add(choice);
        }

        dialog.getItems().addAll(choices);
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            int index = choices.indexOf(result.get());
            Document selectedNumber = savedNumbers.get(index);

            cashInServiceCombo.setValue(selectedNumber.getString("service"));
            cashInMobileField.setText(selectedNumber.getString("number"));
        }
    }

    private void loadSavedNumberForCashOut() {
        List<Document> savedNumbers = getSavedNumbers();
        if (savedNumbers.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Saved Numbers", "You have no saved numbers.");
            return;
        }

        // Create choice dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Load Saved Number");
        dialog.setHeaderText("Select a saved number:");

        List<String> choices = new ArrayList<>();
        for (Document number : savedNumbers) {
            String choice = number.getString("service") + " - "
                    + number.getString("number") + " ("
                    + number.getString("label") + ")";
            choices.add(choice);
        }

        dialog.getItems().addAll(choices);
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            int index = choices.indexOf(result.get());
            Document selectedNumber = savedNumbers.get(index);

            cashOutServiceCombo.setValue(selectedNumber.getString("service"));
            cashOutMobileField.setText(selectedNumber.getString("number"));
        }
    }

    private void loadSavedNumbers() {
        savedNumberData.clear();
        List<Document> savedNumbers = getSavedNumbers();

        for (Document number : savedNumbers) {
            SavedNumberRow row = new SavedNumberRow(
                    number.getString("service"),
                    number.getString("number"),
                    number.getString("label"),
                    "Delete"
            );
            savedNumberData.add(row);
        }
    }

    private void deleteSavedNumber(SavedNumberRow row) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Saved Number");
        confirmation.setHeaderText("Delete Confirmation");
        confirmation.setContentText("Are you sure you want to delete this saved number?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (removeSavedNumber(row.getService(), row.getNumber())) {
                loadSavedNumbers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Saved number deleted successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete saved number.");
            }
        }
    }

    // ==================== UTILITY METHODS ====================
    private boolean simulateMobileBankingAuth(String service, String mobile, String pin) {
        // Simulate mobile banking authentication
        // In real application, this would make API calls to respective services
        return pin.length() >= 4; // Simple validation for demo
    }

    private boolean saveMobileNumber(String service, String number, String label) {
        try {
            // Check if number already exists for this user
            List<Document> existing = getSavedNumbers();
            for (Document doc : existing) {
                if (doc.getString("service").equals(service)
                        && doc.getString("number").equals(number)) {
                    return false; // Already exists
                }
            }

            // Save to database (using a separate collection)
            MongoDatabase db = MongoDBConnection.getDatabase("javabank");
            if (db != null) {
                MongoCollection<Document> collection = db.getCollection("saved_numbers");

                Document savedNumber = new Document()
                        .append("userId", currentUserId)
                        .append("service", service)
                        .append("number", number)
                        .append("label", label)
                        .append("dateAdded", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

                return collection.insertOne(savedNumber).wasAcknowledged();
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error saving mobile number: " + e.getMessage());
            return false;
        }
    }

    private List<Document> getSavedNumbers() {
        List<Document> savedNumbers = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConnection.getDatabase("javabank");
            if (db != null) {
                MongoCollection<Document> collection = db.getCollection("saved_numbers");
                Document query = new Document("userId", currentUserId);

                for (Document doc : collection.find(query)) {
                    savedNumbers.add(doc);
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving saved numbers: " + e.getMessage());
        }
        return savedNumbers;
    }

    private boolean removeSavedNumber(String service, String number) {
        try {
            MongoDatabase db = MongoDBConnection.getDatabase("javabank");
            if (db != null) {
                MongoCollection<Document> collection = db.getCollection("saved_numbers");
                Document query = new Document()
                        .append("userId", currentUserId)
                        .append("service", service)
                        .append("number", number);

                return collection.deleteOne(query).getDeletedCount() > 0;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error removing saved number: " + e.getMessage());
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== INNER CLASSES FOR TABLE ROWS ====================
    public static class TransactionRow {

        private final SimpleStringProperty date;
        private final SimpleStringProperty type;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty description;
        private final SimpleStringProperty balanceAfter;

        public TransactionRow(String date, String type, String amount, String description, String balanceAfter) {
            this.date = new SimpleStringProperty(date);
            this.type = new SimpleStringProperty(type);
            this.amount = new SimpleStringProperty(amount);
            this.description = new SimpleStringProperty(description);
            this.balanceAfter = new SimpleStringProperty(balanceAfter);
        }

        public String getDate() {
            return date.get();
        }

        public String getType() {
            return type.get();
        }

        public String getAmount() {
            return amount.get();
        }

        public String getDescription() {
            return description.get();
        }

        public String getBalanceAfter() {
            return balanceAfter.get();
        }

        public SimpleStringProperty dateProperty() {
            return date;
        }

        public SimpleStringProperty typeProperty() {
            return type;
        }

        public SimpleStringProperty amountProperty() {
            return amount;
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public SimpleStringProperty balanceAfterProperty() {
            return balanceAfter;
        }
    }

    public static class SavedNumberRow {

        private final SimpleStringProperty service;
        private final SimpleStringProperty number;
        private final SimpleStringProperty label;
        private final SimpleStringProperty action;

        public SavedNumberRow(String service, String number, String label, String action) {
            this.service = new SimpleStringProperty(service);
            this.number = new SimpleStringProperty(number);
            this.label = new SimpleStringProperty(label);
            this.action = new SimpleStringProperty(action);
        }

        public String getService() {
            return service.get();
        }

        public String getNumber() {
            return number.get();
        }

        public String getLabel() {
            return label.get();
        }

        public String getAction() {
            return action.get();
        }

        public SimpleStringProperty serviceProperty() {
            return service;
        }

        public SimpleStringProperty numberProperty() {
            return number;
        }

        public SimpleStringProperty labelProperty() {
            return label;
        }

        public SimpleStringProperty actionProperty() {
            return action;
        }
    }

    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Show confirmation dialog
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Logout Confirmation");
            confirmation.setHeaderText("Are you sure you want to logout?");
            confirmation.setContentText("You will be redirected to the login screen.");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                // Clear current user session from FXMLDocumentController
                FXMLDocumentController.clearCurrentUser();

                // Clear local user data
                clearUserSession();

                // Navigate back to login screen
                navigateToLogin(event);

                // Show logout success message
                showAlert(Alert.AlertType.INFORMATION, "Logged Out",
                        "You have been successfully logged out.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Logout Error",
                    "An error occurred during logout: " + e.getMessage());
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    /**
     * Navigate back to login screen
     */
    private void navigateToLogin(ActionEvent event) {
        try {
            // Load login form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent loginForm = loader.load();

            // Create new scene and stage
            Scene loginScene = new Scene(loginForm);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            currentStage.setScene(loginScene);
            currentStage.setTitle("JavaBank - Login");

            // Reset window to normal size for login
            currentStage.setMaximized(false);
            currentStage.setWidth(800);
            currentStage.setHeight(600);
            currentStage.centerOnScreen();

            currentStage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Unable to return to login screen: " + e.getMessage());
            System.err.println("Login navigation error: " + e.getMessage());
        }
    }

    /**
     * Clear user session data
     */
    private void clearUserSession() {
        // Clear user data
        this.currentUser = null;
        this.currentUserId = null;

        // Clear all form fields
        clearAllForms();

        // Clear table data
        transactionData.clear();
        savedNumberData.clear();

        // Reset labels
        welcomeLabel.setText("Welcome!");
        balanceLabel.setText("Current Balance: $0.00");
    }

    /**
     * Clear all form fields
     */
    private void clearAllForms() {
        // Clear Cash In form
        clearCashInForm();

        // Clear Cash Out form
        clearCashOutForm();

        // Clear Transfer form
        clearTransferForm();

        // Clear Saved Numbers form
        if (saveServiceCombo != null) {
            saveServiceCombo.setValue(null);
        }
        if (saveNumberField != null) {
            saveNumberField.clear();
        }
        if (saveNumberLabelField != null) {
            saveNumberLabelField.clear();
        }

        // Reset filters
        if (transactionTypeFilter != null) {
            transactionTypeFilter.setValue("All Transactions");
        }
        if (fromDatePicker != null) {
            fromDatePicker.setValue(null);
        }
        if (toDatePicker != null) {
            toDatePicker.setValue(null);
        }
    }

    /**
     * Quick logout method (without confirmation) - useful for session timeout
     */
    public void quickLogout() {
        try {
            // Clear sessions
            FXMLDocumentController.clearCurrentUser();
            clearUserSession();

            // Create a dummy ActionEvent for navigation
            // Note: This method should be used carefully as it doesn't have proper event context
            System.out.println("Session cleared - user should be redirected to login");

        } catch (Exception e) {
            System.err.println("Quick logout error: " + e.getMessage());
        }
    }
}
