/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.general;

import java.sql.ResultSet;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.guanzon.appdriver.base.GRider;
import org.json.simple.JSONObject;

/**
 *
 * @author MIS-PC
 */
public class QuickSearch extends Application {
    public static String pxeQuickSearch = "QuickSearchFX";
    public static String pxeQuickSearchScreen = "QuickSearchForm.fxml";
  
    private double xOffset = 0.0D;
    private double yOffset = 0.0D;
    private static GRider poGRider;
    private static ResultSet poSource;
    private static String psField; 
    private static String psDescription;
    private static String psColSize;  
    private static String psSQLSource;  
    private static String psReturnVal; 
    private static String psFormCode; 
    private static String psValue; 
    private static int pnSort; 
    private static JSONObject poJSON;
    
    public void setGRider(GRider foGRider) {
        poGRider = foGRider;
    }

    public void setResultSet(ResultSet foRecSource) {
        poSource = foRecSource;
    }

    public void setSQLSource(String fsSQLSource) {
        psSQLSource = fsSQLSource;
    }
    
    public void setConditionValue(String fsValue) {
        psValue = fsValue;
    }

    public void setColumnHeader(String fsColHeader) {
        psDescription = fsColHeader;
    }

    public void setColunmName(String fsColName) {
        psField = fsColName;
    }

    public void setFormCode(String fsFormCode) {
        psFormCode = fsFormCode;
    }

    public void setColumnIndex(int fnSort) {
        pnSort = fnSort;
    }
    
    public void setColumnSize(String fsColSize) {
        psColSize = fsColSize;
    }
    
    public String getResult() {
        return psReturnVal;
    }
  
    public JSONObject getJSON() {
        return poJSON;
    }
  
  public void start(final Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(pxeQuickSearchScreen));
        QuickSearchFormController oSearch = new QuickSearchFormController();
        oSearch.setGRider(poGRider);
        oSearch.setDataSource(poSource);
        oSearch.setSQLSource(psSQLSource);
        oSearch.setFieldValue(psValue);
        oSearch.setFieldName(psField);
        oSearch.setFieldHeader(psDescription);
        oSearch.setFormCode(psFormCode);
        oSearch.setSort(pnSort);
        oSearch.setColSize(psColSize);
        fxmlLoader.setController(oSearch);
        Parent parent = fxmlLoader.<Parent>load();
        
        parent.setOnMousePressed(new EventHandler<MouseEvent>() {
              public void handle(MouseEvent event) {
                    QuickSearch.this.xOffset = event.getSceneX();
                    QuickSearch.this.yOffset = event.getSceneY();
              }
            });
        parent.setOnMouseDragged(new EventHandler<MouseEvent>() {
              public void handle(MouseEvent event) {
                    primaryStage.setX(event.getScreenX() - QuickSearch.this.xOffset);
                    primaryStage.setY(event.getScreenY() - QuickSearch.this.yOffset);
              }
            });
        
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        //primaryStage.getIcons().add(new Image("org/rmj/appdriver/agentfx/styles/64.png"));
        primaryStage.setTitle("Kwik Search v1.0");
        primaryStage.showAndWait();
        if (!oSearch.isCancelled()) {
          poJSON = oSearch.getJSON();
        } else {
          psReturnVal = "";
          poJSON = null;
        } 
  }
  
    public static void main(String[] args) {
        launch(args);
    }

}
