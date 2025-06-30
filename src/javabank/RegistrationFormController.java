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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

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
    private ToggleGroup reg_gender;
    @FXML
    private RadioButton others;
    @FXML
    private RadioButton female;
    @FXML
    private DatePicker reg_datefiled;
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

    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        try {
            // Load the FXML for the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();

            // Get current stage and set the new scene
            Stage stage = (Stage) comp_reg_btn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("JavaBank Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
