/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package javabank;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

}
