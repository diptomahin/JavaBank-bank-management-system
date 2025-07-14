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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DashboardController implements Initializable {

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
    @FXML
    private Label activeViewLabel;
    @FXML
    private VBox sidebar;
    @FXML
    private Button depositNavButton;
    @FXML
    private Button withdrawalNavButton;
    @FXML
    private Button transferNavButton;
    @FXML
    private Button historyNavButton;
    @FXML
    private Label accountNumberLabel;
    @FXML
    private Label lastLoginLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox depositView;
    @FXML
    private VBox withdrawalView;
    @FXML
    private VBox transferView;
    @FXML
    private VBox historyView;
    @FXML
    private VBox dashboard;

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

    @FXML
    private void showDepositView(ActionEvent event) {
    }

    @FXML
    private void showWithdrawalView(ActionEvent event) {
    }

    @FXML
    private void showTransferView(ActionEvent event) {
    }

    @FXML
    private void showHistoryView(ActionEvent event) {
    }




    

}
