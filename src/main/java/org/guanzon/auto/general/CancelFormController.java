/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.general;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;

/**
 * FXML Controller class
 *
 * @author Auto Group Programmers
 */
public class CancelFormController implements Initializable {

    private GRider oApp;
////    private MasterCallback oListener;
//    private CancellationMaster oTrans;
    private boolean pbState;
    private String psSourceNox;
    private String psTransNo;
    private String psSourceCD;

    private final String pxeModuleName = "Cancellation / Deactivation Remarks";
    @FXML
    private Button btnCancel, btnDCancel;
    @FXML
    private Label lblFormNo;
    @FXML
    private TextArea textArea01;

    public void setGRider(GRider foValue) {
        oApp = foValue;
    }

    public void setsSourceNox(String fsValue) {
        psSourceNox = fsValue;
    }

    public void setsSourceCD(String fsValue) {
        psSourceCD = fsValue;
    }

    public void setTransNo(String fsValue) {
        psTransNo = fsValue;
    }

    public boolean setState() {
        return pbState;
    }

    private Stage getStage() {
        return (Stage) btnCancel.getScene().getWindow();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

//        oTrans = new CancellationMaster(oApp, oApp.getBranchCode(), true); //Initialize ClientMaster
////        oTrans.setCallback(oListener);
//        oTrans.setWithUI(true);
        lblFormNo.setText(psTransNo);
        setCapsLockBehavior(textArea01);

        Pattern loPattern = Pattern.compile("^[a-zA-Z0-9 ]*");
        textArea01.setTextFormatter(createTextFormatter(loPattern));

        btnCancel.setOnAction(this::handleButtonAction);
        btnDCancel.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnCancel":
                if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to cancel/deactivate?")) {
                } else {
                    return;
                }
                if (textArea01.getText().length() < 20) {
                    ShowMessageFX.Warning(null, pxeModuleName, "Please enter at least 20 characters.");
                    textArea01.requestFocus();
                    return;
                }
//                if (oTrans.CancelForm(psTransNo, textArea01.getText(), psSourceCD, psSourceNox)) {
//                    pbState = true;
//                } else {
//                    return;
//                }
                CommonUtils.closeStage(btnCancel);
                break;
            case "btnDCancel":
                pbState = false;
                CommonUtils.closeStage(btnDCancel);
                break;
            default:
                ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                break;
        }
    }
    
    public static void setCapsLockBehavior(TextArea textArea) {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (textArea.getText() != null) {
                textArea.setText(newValue.toUpperCase());
            }
        });
    }
    
   // Method to create a TextFormatter with a pattern-based validator
    public static TextFormatter<String> createTextFormatter(Pattern pattern) {
        return new TextFormatter<>(createTextUnaryOperator(pattern));
    }

    // Method to create a UnaryOperator for TextFormatter
    private static UnaryOperator<TextFormatter.Change> createTextUnaryOperator(Pattern pattern) {
        return change -> {
            if (pattern.matcher(change.getControlNewText()).matches()) {
                return change;
            } else {
                return null;
            }
        };
    }
}
