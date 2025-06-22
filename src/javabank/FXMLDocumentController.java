/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 *
 * @author User
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private ImageView logoImageView;
    @FXML
    private TextField login_acnt_field;
    @FXML
    private PasswordField login_pass_field;
    @FXML
    private Button loginBtn;
    @FXML
    private Button regBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Image logo = new Image(getClass().getResourceAsStream("/javabank/assets/banklogo.png"));
        logoImageView.setImage(logo);
    }

    @FXML
    private void handleRegBtn(ActionEvent event) {
         try {
        // Load the FXML for the new scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("registrationForm.fxml"));
        Parent root = loader.load();

        // Get current stage and set the new scene
        Stage stage = (Stage) regBtn.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Register");
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

}
