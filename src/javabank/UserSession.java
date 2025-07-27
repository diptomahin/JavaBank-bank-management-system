/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javabank;


import org.bson.Document;
/**
 *
 * @author User
 */
public class UserSession {
    private static UserSession instance;
    private String currentUserId;
    private Document currentUser;
    private String sessionToken;
    private long loginTime;
    
    // Private constructor for singleton pattern
    private UserSession() {
        // Initialize session
    }
    
    /**
     * Get singleton instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    /**
     * Start a new user session
     */
    public static void startSession(String userId, Document user) {
        UserSession session = getInstance();
        session.currentUserId = userId;
        session.currentUser = user;
        session.sessionToken = generateSessionToken();
        session.loginTime = System.currentTimeMillis();
        
        System.out.println("Session started for user: " + userId);
    }
    
    /**
     * Get current user ID
     */
    public static String getCurrentUserId() {
        UserSession session = getInstance();
        return session.currentUserId;
    }
    
    /**
     * Get current user document
     */
    public static Document getCurrentUser() {
        UserSession session = getInstance();
        return session.currentUser;
    }
    
    /**
     * Get session token
     */
    public static String getSessionToken() {
        UserSession session = getInstance();
        return session.sessionToken;
    }
    
    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn() {
        UserSession session = getInstance();
        return session.currentUserId != null && session.currentUser != null;
    }
    
    /**
     * Get login time
     */
    public static long getLoginTime() {
        UserSession session = getInstance();
        return session.loginTime;
    }
    
    /**
     * Update current user data (refresh from database)
     */
    public static void refreshUserData() {
        UserSession session = getInstance();
        if (session.currentUserId != null) {
            session.currentUser = MongoDBConnection.findUserById(session.currentUserId);
        }
    }
    
    /**
     * End current session
     */
    public static void endSession() {
        UserSession session = getInstance();
        System.out.println("Session ended for user: " + session.currentUserId);
        
        session.currentUserId = null;
        session.currentUser = null;
        session.sessionToken = null;
        session.loginTime = 0;
    }
    
    /**
     * Check if session is expired (24 hours)
     */
    public static boolean isSessionExpired() {
        UserSession session = getInstance();
        if (session.loginTime == 0) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - session.loginTime;
        long maxSessionDuration = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
        
        return sessionDuration > maxSessionDuration;
    }
    
    /**
     * Generate a simple session token
     */
    private static String generateSessionToken() {
        return "SESSION_" + System.currentTimeMillis() + "_" + 
               Math.random() * 1000;
    }
    
    /**
     * Get session duration in minutes
     */
    public static long getSessionDurationMinutes() {
        UserSession session = getInstance();
        if (session.loginTime == 0) {
            return 0;
        }
        
        return (System.currentTimeMillis() - session.loginTime) / (1000 * 60);
    }
    
}
