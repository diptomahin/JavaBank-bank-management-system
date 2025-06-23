/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javabank;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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


    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

}
