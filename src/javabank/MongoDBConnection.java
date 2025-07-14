/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javabank;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author User
 */
public class MongoDBConnection {

    private static final String uri = "mongodb+srv://javabank:NIz7VWpIVljWmOMH@cluster0.abcd.mongodb.net/?retryWrites=true&w=majority";

    public static MongoClient connect() {
        return MongoClients.create(uri);
    }

    public static MongoDatabase getDatabase(String javabank) {
        return connect().getDatabase(javabank);
    }

}
