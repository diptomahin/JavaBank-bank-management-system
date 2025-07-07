/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javabank;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label lastTransactionLabel;
    @FXML
    private TextField depositAmountField;
    @FXML
    private TextArea depositDescriptionArea;
    @FXML
    private Button depositButton;
    @FXML
    private VBox dashboard;
    @FXML
    private TextField withdrawalAmountField;
    @FXML
    private TextArea withdrawalDescriptionArea;
    @FXML
    private Button withdrawalButton;
    @FXML
    private TextField recipientAccountField;
    @FXML
    private TextField transferAmountField;
    @FXML
    private TextArea transferDescriptionArea;
    @FXML
    private Button transferButton;
    @FXML
    private Button refreshHistoryButton;
    @FXML
    private Button applyFilterButton;
    @FXML
    private ComboBox<?> transactionTypeFilter;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TableView<?> transactionHistoryTable;
    @FXML
    private TableColumn<?, ?> dateColumn;
    @FXML
    private TableColumn<?, ?> typeColumn;
    @FXML
    private TableColumn<?, ?> amountColumn;
    @FXML
    private TableColumn<?, ?> descriptionColumn;
    @FXML
    private TableColumn<?, ?> balanceAfterColumn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void handleDeposit(ActionEvent event) {
    }

    @FXML
    private void handleWithdrawal(ActionEvent event) {
    }

    @FXML
    private void handleTransfer(ActionEvent event) {
    }

    @FXML
    private void handleRefreshHistory(ActionEvent event) {
    }

    @FXML
    private void handleApplyFilter(ActionEvent event) {
    }


}
