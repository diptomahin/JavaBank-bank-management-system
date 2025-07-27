/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javabank;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.eq;

/**
 *
 * @author User
 */
public class MongoDBConnection {

    // Connection configuration
    private static final String uri = "mongodb+srv://javabank:H6hGXdwkJW3uFR1T@cluster0.pwyhut1.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private static final String DATABASE_NAME = "javabank";
    private static final String USERS_COLLECTION = "users";

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    // Connect to MongoDB
    public static MongoClient connect() {
        try {
            if (mongoClient == null) {
                mongoClient = MongoClients.create(uri);
                System.out.println("Connected to MongoDB Atlas successfully");
            }
            return mongoClient;
        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());
            return null;
        }
    }

    // Get database instance
    public static MongoDatabase getDatabase(String databaseName) {
        try {
            if (database == null) {
                MongoClient client = connect();
                if (client != null) {
                    database = client.getDatabase(databaseName);
                    System.out.println("Database '" + databaseName + "' connected successfully");
                }
            }
            return database;
        } catch (Exception e) {
            System.err.println("Failed to get database: " + e.getMessage());
            return null;
        }
    }

    // Get users collection
    private static MongoCollection<Document> getUsersCollection() {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db != null) {
                return db.getCollection(USERS_COLLECTION);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Failed to get users collection: " + e.getMessage());
            return null;
        }
    }

    // ==================== USER REGISTRATION OPERATIONS ====================
    /**
     * Register a new user in the database
     */
    public static String registerUser(String fullName, String email, String phone, String gender,
            LocalDate dateOfBirth, String password, String streetAddress,
            String thana, String district) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                throw new Exception("Unable to connect to database");
            }

            // Check if email already exists
            if (emailExists(email)) {
                throw new Exception("Email already exists in the system");
            }

            // Generate unique user ID
            String userId = generateUserId();

            // Create address document
            Document addressDoc = new Document()
                    .append("streetAddress", streetAddress.trim())
                    .append("thana", thana.trim())
                    .append("district", district.trim());

            // Create user document
            Document userDoc = new Document()
                    .append("userId", userId)
                    .append("fullName", fullName.trim())
                    .append("email", email.trim().toLowerCase())
                    .append("phone", phone.trim())
                    .append("gender", gender)
                    .append("dateOfBirth", dateOfBirth.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .append("password", hashPassword(password))
                    .append("address", addressDoc)
                    .append("registrationDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .append("accountStatus", "ACTIVE")
                    .append("accountBalance", 100.0)
                    .append("lastLogin", null);

            // Insert user into database
            InsertOneResult result = collection.insertOne(userDoc);

            if (result.wasAcknowledged()) {
                System.out.println("User registered successfully with ID: " + userId);
                return userId;
            } else {
                throw new Exception("Failed to insert user into database");
            }

        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if email already exists in database
     */
    public static boolean emailExists(String email) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return false;
            }

            Document query = new Document("email", email.trim().toLowerCase());
            Document result = collection.find(query).first();
            return result != null;

        } catch (Exception e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if phone number already exists in database
     */
    public static boolean phoneExists(String phone) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return false;
            }

            Document query = new Document("phone", phone.trim());
            Document result = collection.find(query).first();
            return result != null;

        } catch (Exception e) {
            System.err.println("Error checking phone existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate unique user ID
     */
    private static String generateUserId() {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return "USER000001";
            }

            long count = collection.countDocuments();
            return "USER" + String.format("%06d", count + 1);

        } catch (Exception e) {
            System.err.println("Error generating user ID: " + e.getMessage());
            return "USER000001";
        }
    }

    // ==================== USER AUTHENTICATION OPERATIONS ====================
    /**
     * Authenticate user login
     */
    public static Document authenticateUser(String email, String password) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return null;
            }

            Document query = new Document()
                    .append("email", email.trim().toLowerCase())
                    .append("password", hashPassword(password));

            Document user = collection.find(query).first();

            if (user != null) {
                // Update last login
                updateLastLogin(user.getString("userId"));
                System.out.println("User authenticated successfully: " + email);
            }

            return user;

        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update user's last login timestamp
     */
    private static void updateLastLogin(String userId) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return;
            }

            Document query = new Document("userId", userId);
            Document update = new Document("$set",
                    new Document("lastLogin", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)));

            collection.updateOne(query, update);

        } catch (Exception e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    // ==================== USER RETRIEVAL OPERATIONS ====================
    /**
     * Find user by email
     */
    public static Document findUserByEmail(String email) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return null;
            }

            Document query = new Document("email", email.trim().toLowerCase());
            return collection.find(query).first();

        } catch (Exception e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find user by user ID
     */
    public static Document findUserById(String userId) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return null;
            }

            Document query = new Document("userId", userId);
            return collection.find(query).first();

        } catch (Exception e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find user by phone number
     */
    public static Document findUserByPhone(String phone) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return null;
            }

            Document query = new Document("phone", phone.trim());
            return collection.find(query).first();

        } catch (Exception e) {
            System.err.println("Error finding user by phone: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all users from database
     */
    public static List<Document> getAllUsers() {
        List<Document> users = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return users;
            }

            for (Document doc : collection.find()) {
                users.add(doc);
            }

            System.out.println("Retrieved " + users.size() + " users from database");

        } catch (Exception e) {
            System.err.println("Error retrieving all users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Get total user count
     */
    public static long getUserCount() {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return 0;
            }

            return collection.countDocuments();

        } catch (Exception e) {
            System.err.println("Error getting user count: " + e.getMessage());
            return 0;
        }
    }

    // ==================== USER UPDATE OPERATIONS ====================
    /**
     * Update user profile information
     */
    public static boolean updateUserProfile(String userId, String fullName, String phone,
            String streetAddress, String thana, String district) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return false;
            }

            Document addressDoc = new Document()
                    .append("streetAddress", streetAddress.trim())
                    .append("thana", thana.trim())
                    .append("district", district.trim());

            Document query = new Document("userId", userId);
            Document update = new Document("$set", new Document()
                    .append("fullName", fullName.trim())
                    .append("phone", phone.trim())
                    .append("address", addressDoc)
            );

            long modifiedCount = collection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("User profile updated successfully: " + userId);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user password
     */
    public static boolean updateUserPassword(String userId, String newPassword) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return false;
            }

            Document query = new Document("userId", userId);
            Document update = new Document("$set",
                    new Document("password", hashPassword(newPassword)));

            long modifiedCount = collection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("Password updated successfully for user: " + userId);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user account status
     */
    public static boolean updateAccountStatus(String userId, String status) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return false;
            }

            Document query = new Document("userId", userId);
            Document update = new Document("$set", new Document("accountStatus", status));

            long modifiedCount = collection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("Account status updated to " + status + " for user: " + userId);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error updating account status: " + e.getMessage());
            return false;
        }
    }

    // ==================== USER DELETE OPERATIONS ====================
    /**
     * Delete user from database
     */
    public static boolean deleteUser(String userId) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return false;
            }

            Document query = new Document("userId", userId);
            long deletedCount = collection.deleteOne(query).getDeletedCount();

            if (deletedCount > 0) {
                System.out.println("User deleted successfully: " + userId);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    // ==================== UTILITY METHODS ====================
    /**
     * Simple password hashing (use BCrypt for production)
     */
    private static String hashPassword(String password) {
        // Simple hash - replace with BCrypt for production
        return Integer.toString(password.hashCode());
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validate Bangladeshi phone number format
     */
    public static boolean isValidBangladeshiPhone(String phone) {
        return phone != null && phone.matches("^01[3-9]\\d{8}$");
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            MongoClient client = connect();
            if (client != null) {
                // Try to ping the database
                MongoDatabase db = getDatabase(DATABASE_NAME);
                if (db != null) {
                    db.runCommand(new Document("ping", 1));
                    System.out.println("Database connection test successful");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close database connection
     */
    public static void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                mongoClient = null;
                database = null;
                System.out.println("MongoDB connection closed successfully");
            }
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Initialize database with indexes for better performance
     */
    public static void initializeDatabase() {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection != null) {
                // Create unique index on email
                collection.createIndex(new Document("email", 1),
                        new com.mongodb.client.model.IndexOptions().unique(true));

                // Create index on userId
                collection.createIndex(new Document("userId", 1));

                // Create index on phone
                collection.createIndex(new Document("phone", 1));

                System.out.println("Database indexes created successfully");
            }
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    // Add these methods to your existing MongoDBConnection.java class
    // ==================== TRANSACTION OPERATIONS ====================
    /**
     * Record a transaction in the database
     */
    public static boolean recordTransaction(String userId, String type, double amount,
            String description, double balanceAfter) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return false;
            }

            MongoCollection<Document> transactionCollection = db.getCollection("transactions");

            // Generate unique transaction ID
            String transactionId = generateTransactionId();

            // Create transaction document
            Document transactionDoc = new Document()
                    .append("transactionId", transactionId)
                    .append("userId", userId)
                    .append("type", type)
                    .append("amount", amount)
                    .append("description", description != null ? description.trim() : "")
                    .append("balanceAfter", balanceAfter)
                    .append("date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .append("timestamp", System.currentTimeMillis())
                    .append("status", "COMPLETED");

            // For transfers, add recipient information
            if (type.equals("TRANSFER_OUT") && description != null && description.contains("to ")) {
                String recipientId = description.substring(description.indexOf("to ") + 3);
                transactionDoc.append("recipientUserId", recipientId.trim());
            }

            InsertOneResult result = transactionCollection.insertOne(transactionDoc);

            if (result.wasAcknowledged()) {
                System.out.println("Transaction recorded: " + transactionId);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error recording transaction: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate unique transaction ID
     */
    private static String generateTransactionId() {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return "TXN000001";
            }

            MongoCollection<Document> transactionCollection = db.getCollection("transactions");
            long count = transactionCollection.countDocuments();
            return "TXN" + String.format("%06d", count + 1);

        } catch (Exception e) {
            System.err.println("Error generating transaction ID: " + e.getMessage());
            return "TXN" + System.currentTimeMillis();
        }
    }

    /**
     * Get user transaction history
     */
    public static List<Document> getUserTransactions(String userId) {
        List<Document> transactions = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return transactions;
            }

            MongoCollection<Document> transactionCollection = db.getCollection("transactions");

            // Find transactions for the user and sort by timestamp descending
            Document query = new Document("userId", userId);
            for (Document doc : transactionCollection.find(query)
                    .sort(new Document("timestamp", -1))) {
                transactions.add(doc);
            }

            System.out.println("Retrieved " + transactions.size() + " transactions for user: " + userId);

        } catch (Exception e) {
            System.err.println("Error retrieving user transactions: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Get filtered user transactions
     */
    public static List<Document> getFilteredUserTransactions(String userId, String type,
            LocalDate fromDate, LocalDate toDate) {
        List<Document> transactions = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return transactions;
            }

            MongoCollection<Document> transactionCollection = db.getCollection("transactions");

            // Build query
            Document query = new Document("userId", userId);

            if (type != null && !type.isEmpty() && !type.equals("All Transactions")) {
                query.append("type", type);
            }

            if (fromDate != null || toDate != null) {
                Document dateQuery = new Document();
                if (fromDate != null) {
                    dateQuery.append("$gte", fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
                if (toDate != null) {
                    dateQuery.append("$lte", toDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
                query.append("date", dateQuery);
            }

            for (Document doc : transactionCollection.find(query)
                    .sort(new Document("timestamp", -1))) {
                transactions.add(doc);
            }

        } catch (Exception e) {
            System.err.println("Error retrieving filtered transactions: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Update user account balance
     */
    public static boolean updateUserBalance(String userId, double newBalance) {
        try {
            MongoCollection<Document> collection = getUsersCollection();
            if (collection == null) {
                return false;
            }

            Document query = new Document("userId", userId);
            Document update = new Document("$set", new Document("accountBalance", newBalance));

            long modifiedCount = collection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("Balance updated for user " + userId + ": $" + newBalance);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error updating user balance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Process deposit transaction
     */
    public static boolean processDeposit(String userId, double amount, String description) {
        try {
            // Get current user
            Document user = findUserById(userId);
            if (user == null) {
                return false;
            }

            // Calculate new balance
            double currentBalance = user.getDouble("accountBalance");
            double newBalance = currentBalance + amount;

            // Update balance
            if (updateUserBalance(userId, newBalance)) {
                // Record transaction
                return recordTransaction(userId, "DEPOSIT", amount, description, newBalance);
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error processing deposit: " + e.getMessage());
            return false;
        }
    }

    /**
     * Process withdrawal transaction
     */
    public static boolean processWithdrawal(String userId, double amount, String description) {
        try {
            // Get current user
            Document user = findUserById(userId);
            if (user == null) {
                return false;
            }

            // Check balance
            double currentBalance = user.getDouble("accountBalance");
            if (currentBalance < amount) {
                System.err.println("Insufficient funds for withdrawal");
                return false;
            }

            // Calculate new balance
            double newBalance = currentBalance - amount;

            // Update balance
            if (updateUserBalance(userId, newBalance)) {
                // Record transaction
                return recordTransaction(userId, "WITHDRAWAL", amount, description, newBalance);
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error processing withdrawal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Process fund transfer transaction
     */
    public static boolean processFundTransfer(String fromUserId, String toUserId,
            double amount, String description) {
        try {
            // Get both users
            Document fromUser = findUserById(fromUserId);
            Document toUser = findUserById(toUserId);

            if (fromUser == null || toUser == null) {
                System.err.println("One or both users not found for transfer");
                return false;
            }

            // Check sender's balance
            double senderBalance = fromUser.getDouble("accountBalance");
            if (senderBalance < amount) {
                System.err.println("Insufficient funds for transfer");
                return false;
            }

            // Calculate new balances
            double senderNewBalance = senderBalance - amount;
            double recipientBalance = toUser.getDouble("accountBalance");
            double recipientNewBalance = recipientBalance + amount;

            // Update both balances
            if (updateUserBalance(fromUserId, senderNewBalance)
                    && updateUserBalance(toUserId, recipientNewBalance)) {

                // Record both transactions
                String senderDesc = description + " to " + toUser.getString("fullName");
                String recipientDesc = description + " from " + fromUser.getString("fullName");

                boolean senderTxn = recordTransaction(fromUserId, "TRANSFER_OUT", amount,
                        senderDesc, senderNewBalance);
                boolean recipientTxn = recordTransaction(toUserId, "TRANSFER_IN", amount,
                        recipientDesc, recipientNewBalance);

                return senderTxn && recipientTxn;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error processing fund transfer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get current account balance for user
     */
    public static Double getCurrentBalance(String userId) {
        try {
            Document user = findUserById(userId);
            if (user != null) {
                return user.getDouble("accountBalance");
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error getting current balance: " + e.getMessage());
            return 0.0;
        }
    }

    // ==================== SAVED NUMBERS OPERATIONS ====================
    /**
     * Initialize saved numbers collection with indexes
     */
    public static void initializeSavedNumbersCollection() {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db != null) {
                MongoCollection<Document> collection = db.getCollection("saved_numbers");

                // Create compound index on userId and service+number for uniqueness
                collection.createIndex(new Document()
                        .append("userId", 1)
                        .append("service", 1)
                        .append("number", 1),
                        new com.mongodb.client.model.IndexOptions().unique(true));

                // Create index on userId for faster queries
                collection.createIndex(new Document("userId", 1));

                System.out.println("Saved numbers collection indexes created successfully");
            }
        } catch (Exception e) {
            System.err.println("Error initializing saved numbers collection: " + e.getMessage());
        }
    }

    /**
     * Save a mobile number for a user
     */
    public static boolean saveMobileNumber(String userId, String service, String number, String label) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return false;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");

            // Check if combination already exists
            Document existingQuery = new Document()
                    .append("userId", userId)
                    .append("service", service)
                    .append("number", number);

            if (collection.find(existingQuery).first() != null) {
                System.out.println("Mobile number already saved for this service");
                return false;
            }

            // Create new saved number document
            Document savedNumber = new Document()
                    .append("userId", userId)
                    .append("service", service)
                    .append("number", number)
                    .append("label", label != null ? label.trim() : "Saved Number")
                    .append("dateAdded", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .append("lastUsed", null);

            InsertOneResult result = collection.insertOne(savedNumber);

            if (result.wasAcknowledged()) {
                System.out.println("Mobile number saved successfully: " + service + " - " + number);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error saving mobile number: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all saved numbers for a user
     */
    public static List<Document> getUserSavedNumbers(String userId) {
        List<Document> savedNumbers = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return savedNumbers;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document("userId", userId);

            // Sort by service, then by dateAdded
            Document sort = new Document()
                    .append("service", 1)
                    .append("dateAdded", -1);

            for (Document doc : collection.find(query).sort(sort)) {
                savedNumbers.add(doc);
            }

            System.out.println("Retrieved " + savedNumbers.size() + " saved numbers for user: " + userId);

        } catch (Exception e) {
            System.err.println("Error retrieving saved numbers: " + e.getMessage());
        }
        return savedNumbers;
    }

    /**
     * Delete a saved number
     */
    public static boolean deleteSavedNumber(String userId, String service, String number) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return false;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document()
                    .append("userId", userId)
                    .append("service", service)
                    .append("number", number);

            long deletedCount = collection.deleteOne(query).getDeletedCount();

            if (deletedCount > 0) {
                System.out.println("Saved number deleted successfully: " + service + " - " + number);
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error deleting saved number: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update last used timestamp for a saved number
     */
    public static boolean updateSavedNumberLastUsed(String userId, String service, String number) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return false;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document()
                    .append("userId", userId)
                    .append("service", service)
                    .append("number", number);

            Document update = new Document("$set",
                    new Document("lastUsed", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)));

            long modifiedCount = collection.updateOne(query, update).getModifiedCount();
            return modifiedCount > 0;

        } catch (Exception e) {
            System.err.println("Error updating saved number last used: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get saved numbers by service for a user
     */
    public static List<Document> getUserSavedNumbersByService(String userId, String service) {
        List<Document> savedNumbers = new ArrayList<>();
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return savedNumbers;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document()
                    .append("userId", userId)
                    .append("service", service);

            Document sort = new Document("dateAdded", -1);

            for (Document doc : collection.find(query).sort(sort)) {
                savedNumbers.add(doc);
            }

        } catch (Exception e) {
            System.err.println("Error retrieving saved numbers by service: " + e.getMessage());
        }
        return savedNumbers;
    }

    /**
     * Check if a mobile number is already saved for a user and service
     */
    public static boolean isMobileNumberSaved(String userId, String service, String number) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return false;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document()
                    .append("userId", userId)
                    .append("service", service)
                    .append("number", number);

            return collection.find(query).first() != null;

        } catch (Exception e) {
            System.err.println("Error checking if mobile number is saved: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get count of saved numbers for a user
     */
    public static long getSavedNumbersCount(String userId) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return 0;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document("userId", userId);

            return collection.countDocuments(query);

        } catch (Exception e) {
            System.err.println("Error getting saved numbers count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Update saved number label
     */
    public static boolean updateSavedNumberLabel(String userId, String service, String number, String newLabel) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return false;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document()
                    .append("userId", userId)
                    .append("service", service)
                    .append("number", number);

            Document update = new Document("$set",
                    new Document("label", newLabel != null ? newLabel.trim() : "Saved Number"));

            long modifiedCount = collection.updateOne(query, update).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("Saved number label updated successfully");
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error updating saved number label: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete all saved numbers for a user (useful when deleting user account)
     */
    public static boolean deleteAllUserSavedNumbers(String userId) {
        try {
            MongoDatabase db = getDatabase(DATABASE_NAME);
            if (db == null) {
                return false;
            }

            MongoCollection<Document> collection = db.getCollection("saved_numbers");
            Document query = new Document("userId", userId);

            long deletedCount = collection.deleteMany(query).getDeletedCount();

            System.out.println("Deleted " + deletedCount + " saved numbers for user: " + userId);
            return true;

        } catch (Exception e) {
            System.err.println("Error deleting all user saved numbers: " + e.getMessage());
            return false;
        }
    }
    
}
