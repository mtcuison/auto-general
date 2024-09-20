/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.general;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author MIS-PC
 */
public class QuickSearchFormController implements Initializable {
    private String psSQLSrce = "";
    private String psCondition = "";
    private ArrayList<String> psValue = new ArrayList<>(); 
    private ArrayList<String> psColName = new ArrayList<>(); 
    private ArrayList<String> psColSort = new ArrayList<>(); 
    private ArrayList<String> psColType = new ArrayList<>(); 
    private ArrayList<String> psLogOprtr = new ArrayList<>();
    private ArrayList<String> psCriName = new ArrayList<>();
    private ArrayList<String> psOpenPar = new ArrayList<>();
    private ArrayList<String> psClosePar = new ArrayList<>();
    private ArrayList<String> psOperator = new ArrayList<>();
    private ArrayList<Double> pdColSize;
    private String psFormCode;
    private String psFldName;
    private String psColHead;
    private String psColSize;
    private String[] paColHead;
    private String[] paColName;
    private String[] paFldName;
    private String[] paColSize;
    private int pnSort = 0;
    private int pnRow = 0;
    private long pnSelectd = -1L;
    private boolean pbActivated = false;
    private boolean pbCancelled = true;
    private JSONObject poJSON = null;
    private ObservableList<QuickSearchTable> data = FXCollections.observableArrayList();
    //private ObservableList<QuickSearchTable> colcriteria = FXCollections.observableArrayList();
    private ObservableList<QuickSearchTable> criteria = FXCollections.observableArrayList();
    // Create a list of options
    ObservableList<String> operatorOptions = FXCollections.observableArrayList(
        "=",
        ">",
        "<",
        ">=",
        "<=",
        "!=",
        "LIKE",
        "NOT LIKE"
    );
    
    private QuickSearchTable model;
    private final String pxeModuleName = "QuickSearchFX";
    private GRider poGRider;
    private ResultSet poSource;
    @FXML
    private Label glyphExit;
    @FXML
    private TableView<QuickSearchTable> table;
    @FXML
    private TableView<QuickSearchTable> table_criteria;
    @FXML
    private Button btnSearch;
    @FXML
    private TableColumn tblColRow;
    @FXML
    private TableColumn tblColCriteria;
    @FXML
    private TableColumn tblColValue;
    @FXML
    private TableColumn tblColOpenPar;
    @FXML
    private TableColumn tblColOperator;
    @FXML
    private TableColumn tblColClosePar;
    @FXML
    private TableColumn tblColUserGuide;
    
    public void setGRider(GRider foGRider) {
        poGRider = foGRider;
    }

    public void setSQLSource(String fsSource) {
        psSQLSrce = fsSource;
    }

    public void setFieldValue(String fsCondition) {
        psCondition = " " + fsCondition + " ";
    }

    public void setFieldName(String fsField) {
        psFldName = fsField;
    }

    public void setFieldHeader(String fsDescript) {
        psColHead = fsDescript;
    }

    public void setFormCode(String fsFormCode) {
        psFormCode = fsFormCode;
    }

    public void setSort(int fnSort) {
        pnSort = fnSort;
    }

    public void setDataSource(ResultSet foSource) {
        poSource = foSource;
    }

    public boolean isCancelled() {
        return pbCancelled;
    }
    
    public void setColSize(String fsColSize) {
        psColSize = fsColSize;
    }

    public JSONObject getJSON() {
        return poJSON;
    }
    
    private Stage getStage() {
        Stage stage = (Stage)btnSearch.getScene().getWindow();
        return stage;
    }

    public void initialize(URL url, ResourceBundle rb) {
        if (!pbActivated) {
            paColHead = psColHead.split("»");
            paFldName = psFldName.split("»");
            paColSize = psColSize.split("»");
            if (paColHead.length != paFldName.length) {
                ShowMessageFX.Error(getStage(), "Column size discrepancy. Application will close.", "QuickSearchFX", "Please inform MIS Department.");
                System.exit(1);
            } 
            initGrid();
            initCriteria();
            loadCriteria();
            //executeQuery();
        } 
        pbActivated = !pbActivated;
    }
    
    private void cmdLoad_Click(ActionEvent event) {
        loadData();
        pbCancelled = false;
        unloadScene(event);
    }

    private void closeForm(ActionEvent event) {
        pbCancelled = true;
        unloadScene(event);
    }

    @FXML
    private void table_click(MouseEvent event) {
        model = table.getSelectionModel().getSelectedItem();
        pnSelectd = table.getSelectionModel().getSelectedIndex();
        if (event.getClickCount() >= 2) {
            loadData();
            pbCancelled = false;
            unloadScene(event);
        } 
    }

    private void initGrid() {
        TableColumn<QuickSearchTable, Object> index01 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index02 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index03 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index04 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index05 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index06 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index07 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index08 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index09 = new TableColumn<>("");
        TableColumn<QuickSearchTable, Object> index10 = new TableColumn<>("");
        index01.setSortable(false);
        //index01.setResizable(false);
        index02.setSortable(false);
        //index02.setResizable(false);
        index03.setSortable(false);
        //index03.setResizable(false);
        index04.setSortable(false);
        //index04.setResizable(true);
        index05.setSortable(false);
        //index05.setResizable(true);
        index06.setSortable(false);
        //index06.setResizable(true);
        index07.setSortable(false);
        //index07.setResizable(true);
        index08.setSortable(false);
        //index08.setResizable(true);
        index09.setSortable(false);
        //index09.setResizable(true);
        index10.setSortable(false);
        //index10.setResizable(true);
        
        table.getColumns().clear();
        for (int lnCtr = 1; lnCtr <= paColHead.length; lnCtr++) {
            switch (lnCtr) {
                case 1:
                    index01.setText(paColHead[lnCtr - 1]);
                    table.getColumns().add(index01);
                    index01.setCellValueFactory(new PropertyValueFactory<>("index01"));
                    switch (paColHead.length) {
                        case 1:
                            index01.prefWidthProperty().bind(table.widthProperty().multiply(1));
                            break;
                        case 2:
                        case 3:
                        case 4:
                            index01.prefWidthProperty().bind(table.widthProperty().multiply(0.25D));
                            break;
                    } 
                index01.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1])));//0.2D;
                break;
            case 2:
                index02.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index02);
                index02.setCellValueFactory(new PropertyValueFactory<>("index02"));
                switch (paColHead.length) {
                    case 2:
                        index02.prefWidthProperty().bind(table.widthProperty().multiply(0.75D));
                        break;
                    case 3:
                        index02.prefWidthProperty().bind(table.widthProperty().multiply(0.5D));
                        break;
                    case 4:
                        index02.prefWidthProperty().bind(table.widthProperty().multiply(0.4D));
                        break;
                } 
                index02.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1])));//0.3D
                break;
            case 3:
                index03.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index03);
                index03.setCellValueFactory(new PropertyValueFactory<>("index03"));
                switch (paColHead.length) {
                    case 3:
                        index03.prefWidthProperty().bind(table.widthProperty().multiply(0.25D));
                        break;
                    case 4:
                        index03.prefWidthProperty().bind(table.widthProperty().multiply(0.2D));
                        break;
                } 
                index03.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1])));//0.25D
                break;
            case 4:
                index04.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index04);
                index04.prefWidthProperty().bind(table.widthProperty().multiply(0.2D));
                index04.setCellValueFactory(new PropertyValueFactory<>("index04"));
                switch (paColHead.length) {
                    case 4:
                        index04.prefWidthProperty().bind(table.widthProperty().multiply(0.15D));
                        break;
                } 
                index04.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1])));//0.2D
                break;
            case 5:
                index05.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index05);
                index05.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1]))); //0.15D
                index05.setCellValueFactory(new PropertyValueFactory<>("index05"));
                break;
            case 6:
                index06.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index06);
                index06.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1]))); //0.15D
                index06.setCellValueFactory(new PropertyValueFactory<>("index06"));
                break;
            case 7:
                index07.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index07);
                index07.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1]))); //0.15D
                index07.setCellValueFactory(new PropertyValueFactory<>("index07"));
                break;
            case 8:
                index08.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index08);
                index08.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1]))); //0.15D
                index08.setCellValueFactory(new PropertyValueFactory<>("index08"));
                break;
            case 9:
                index09.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index09);
                index09.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1]))); //0.15D
                index09.setCellValueFactory(new PropertyValueFactory<>("index09"));
                break;
            case 10:
                index10.setText(paColHead[lnCtr - 1]);
                table.getColumns().add(index10);
                index10.prefWidthProperty().bind(table.widthProperty().multiply(Double.valueOf(paColSize[lnCtr - 1]))); //0.15D
                index10.setCellValueFactory(new PropertyValueFactory<>("index10"));
                break;
            default:
                ShowMessageFX.Error(getStage(), "Column index not supported. [" + lnCtr + "]", "QuickSearchFX", "Please inform MIS Department.");
                System.exit(1);
                break;
            } 
        } 
        
        table.setItems(data);
    }

    private void executeQuery() {
        String lsSQL = psSQLSrce;
        if (!lsSQL.isEmpty()) {
            String lsFormat = "";
            String lsQuery = "";
            String lsOpenPar, lsColName, lsOperator, lsValue, lsClosePar, lsLogOprtr;
            int size = pnRow - 1;
            for(int ctr = 0; ctr <= size; ctr++){
                if(psOpenPar.get(ctr).equals("»")){ lsOpenPar = ""; } else { lsOpenPar = psOpenPar.get(ctr); }
                if(psColName.get(ctr).equals("»")){ lsColName = ""; } else { lsColName = psColName.get(ctr); }
                if(psOperator.get(ctr).equals("»")){ lsOperator = ""; } else { lsOperator = psOperator.get(ctr); }
                if(psValue.get(ctr).equals("»")){ lsValue = ""; } else { lsValue = psValue.get(ctr); }
                if(psClosePar.get(ctr).equals("»")){ lsClosePar = ""; } else { lsClosePar = psClosePar.get(ctr); }
                if(psLogOprtr.get(ctr).equals("»")){ lsLogOprtr = ""; } else { lsLogOprtr = psLogOprtr.get(ctr); }
                
                lsFormat = lsOpenPar + " " 
                            + lsColName + " " 
                            + lsOperator + " " 
                            + SQLUtil.toSQL( lsValue) 
                            + lsClosePar + " " 
                            + lsLogOprtr ;
                
                if(lsQuery.isEmpty()){
                    lsQuery = lsFormat;
                } else {
                    lsQuery = lsQuery + " " + lsFormat ;
                }
                
                //Clear    
                lsOpenPar = "";lsColName = "";lsOperator = "";
                lsValue = "";lsClosePar = "";lsLogOprtr = "";
                
            }
            
            lsSQL =  MiscUtil.addCondition( lsSQL,   lsQuery) + psCondition + " ORDER BY " +  psColSort.get(pnSort) + " ASC ";
            System.out.println(lsSQL);
            try {
                poSource = poGRider.executeQuery(lsSQL);
                loadDetail();
            } catch (SQLException ex) {
                Logger.getLogger(QuickSearchFormController.class.getName()).log(Level.SEVERE, (String)null, ex);
            } 
        } else {
            try {
                loadDetail();
            } catch (SQLException ex) {
                Logger.getLogger(QuickSearchFormController.class.getName()).log(Level.SEVERE, (String)null, ex);
                ShowMessageFX.Error(getStage(), ex.getMessage(), "QuickSearchFX", "Please inform MIS Department.");
                System.exit(1);
            } 
        } 
    }

    private void loadDetail() throws SQLException {
        if (poSource == null)
            if (psSQLSrce != null && !psSQLSrce.isEmpty()) {
                poSource = poGRider.executeQuery(psSQLSrce);
            } else {
                ShowMessageFX.Error(getStage(), "Both query and resulset is null.", "QuickSearchFX", "Please inform MIS Department.");
                System.exit(1);
            }  
        data.clear();
        if (MiscUtil.RecordCount(poSource) > 0L)
            poSource.beforeFirst(); 
        while (poSource.next())
          data.add(new QuickSearchTable((paFldName.length <= 0) ? "" : poSource.getString(paFldName[0]), (paFldName.length <= 1) ? "" : poSource
                .getString(paFldName[1]), (paFldName.length <= 2) ? "" : poSource
                .getString(paFldName[2]), (paFldName.length <= 3) ? "" : poSource
                .getString(paFldName[3]), (paFldName.length <= 4) ? "" : poSource
                .getString(paFldName[4]), (paFldName.length <= 5) ? "" : poSource
                .getString(paFldName[5]), (paFldName.length <= 6) ? "" : poSource
                .getString(paFldName[6]), (paFldName.length <= 7) ? "" : poSource
                .getString(paFldName[7]), (paFldName.length <= 8) ? "" : poSource
                .getString(paFldName[8]), (paFldName.length <= 9) ? "" : poSource
                .getString(paFldName[9]))); 
        table.getSelectionModel().selectFirst();
        pnSelectd = table.getSelectionModel().getSelectedIndex();
    }

    private void loadData() {
        JSONObject loJSON = new JSONObject();
        try {
            if (MiscUtil.RecordCount(poSource) <= 0L)
                return; 
            poSource.absolute((int)pnSelectd + 1);
            for (int lnCtr = 1; lnCtr <= poSource.getMetaData().getColumnCount(); lnCtr++)
                loJSON.put(poSource.getMetaData().getColumnLabel(lnCtr), poSource.getString(lnCtr)); 
        } catch (SQLException ex) {
            Logger.getLogger(QuickSearchFormController.class.getName()).log(Level.SEVERE, (String)null, ex);
            ShowMessageFX.Error(getStage(), ex.getMessage(), "QuickSearchFX", "Please inform MIS Department.");
            System.exit(1);
        } 
      poJSON = loJSON;
    }

    private void unloadScene(ActionEvent event) {
        Node source = (Node)event.getSource();
        Stage stage = (Stage)source.getScene().getWindow();
        stage.close();
    }

    private void unloadScene(MouseEvent event) {
        Node source = (Node)event.getSource();
        Stage stage = (Stage)source.getScene().getWindow();
        stage.close();
    }
    
    private void loadCriteria(){
        criteria.clear();
        pnRow = 0;
        try {
            String lsColName = "";
            String lsValue = "";
            String lsSQL = MiscUtil.addCondition(getSQ_FormListRetrieval(), " sFormCode = " + SQLUtil.toSQL(psFormCode))
                            + " ORDER BY nRowNoxxx ASC";
            ResultSet loRS;
            System.out.println(lsSQL);
            loRS = poGRider.executeQuery(lsSQL);
            if (MiscUtil.RecordCount(loRS) > 0){
                psOpenPar.clear();
                psCriName.clear();
                psOperator.clear();
                psValue.clear();
                psClosePar.clear();
                psLogOprtr.clear();
                psColName.clear();
                psColSort.clear();
                psColType.clear();
                
                while (loRS.next()){
                    pnRow++;

                    if(loRS.getString("sRetColTp").toLowerCase().equals("date")){
                        lsColName = "DATE(" + loRS.getString("sRetColNm") + ")" ;
                        
                        if(loRS.getString("sOperator").equals(">=")){
                            lsValue =  String.valueOf(LocalDate.of(  strToDate(xsDateShort((Date) poGRider.getServerDate())).getYear(), 
                                                    strToDate(xsDateShort((Date) poGRider.getServerDate())).getMonth(), 
                                                    1));
                        } else {
                            lsValue = xsDateShort((Date) poGRider.getServerDate());
                        }
                        
                    } else {
                        lsColName = loRS.getString("sRetColNm");
                        lsValue  = loRS.getString("sCriValue");     
                    }

                    criteria.add(new QuickSearchTable(
                                loRS.getString("nRowNoxxx"), //1
                                loRS.getString("sDivOpenx"), //(
                                loRS.getString("sCriteria"), //FROM
                                loRS.getString("sOperator"), //AND
                                lsValue, 
                                loRS.getString("sDivClose"), //)
                                loRS.getString("sLogOprtr"), //AND
                                lsColName, 
                                loRS.getString("sRetColTp"), //date
                                loRS.getString("sInputGde")  //format
                        ));
                    
                    if(loRS.getString("sDivOpenx").isEmpty()){ psOpenPar.add("»"); } else { psOpenPar.add(loRS.getString("sDivOpenx")); }
                    if(loRS.getString("sCriteria").isEmpty()){ psCriName.add("»"); } else { psCriName.add(loRS.getString("sCriteria")); }
                    if(loRS.getString("sOperator").isEmpty()){ psOperator.add("»"); } else { psOperator.add(loRS.getString("sOperator")); }
                    if(lsValue.isEmpty()){ psValue.add("%"); } else { psValue.add(lsValue); }
                    if(loRS.getString("sDivClose").isEmpty()){ psClosePar.add("»"); } else { psClosePar.add(loRS.getString("sDivClose")); }
                    if(loRS.getString("sLogOprtr").isEmpty()){ psLogOprtr.add("»"); } else { psLogOprtr.add(loRS.getString("sLogOprtr")); }
                    if(lsColName.isEmpty()){ psColName.add("»"); } else { psColName.add(lsColName); }
                    if(loRS.getString("sRetColNm").isEmpty()){ psColSort.add("»"); } else { psColSort.add(loRS.getString("sRetColNm")); }
                    if(loRS.getString("sRetColTp").isEmpty()){ psColType.add("»"); } else { psColType.add(loRS.getString("sRetColTp")); }
                }
                MiscUtil.close(loRS);
                return;
            }
        
        } catch (SQLException ex) {
            Logger.getLogger(QuickSearchFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    public static String xsDateShort(String fsValue) throws ParseException, java.text.ParseException {
        SimpleDateFormat fromUser = new SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        String lsResult = "";
        lsResult = myFormat.format(fromUser.parse(fsValue));
        return lsResult;
    }
    
    /*Convert Date to String*/
    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    private String getSQ_FormListRetrieval(){
        return  " SELECT      	"+                                    
                "   IFNULL(sFormCode,'') AS  sFormCode "+     
                " , IFNULL(sCriteria,'') AS  sCriteria "+     
                " , IFNULL(sDivOpenx,'') AS  sDivOpenx "+     
                " , IFNULL(sRetColNm,'') AS  sRetColNm "+     
                " , IFNULL(sRetColTp,'') AS  sRetColTp "+     
                " , IFNULL(sOperator,'') AS  sOperator "+     
                " , IFNULL(sCriValue,'') AS  sCriValue "+     
                " , IFNULL(sDivClose,'') AS  sDivClose "+     
                " , IFNULL(sLogOprtr,'') AS  sLogOprtr "+     
                " , nRowNoxxx   "+                            
                " , IFNULL(sInputGde,'') AS  sInputGde "+     
                " FROM form_list_retrieval ";                 

    }
    
    private void initCriteria() {
        
        table_criteria.setEditable(true);
        
        tblColOperator.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), operatorOptions)); 
        tblColOperator.setCellValueFactory(new PropertyValueFactory<>("index04"));
          
        tblColValue.setCellFactory(TextFieldTableCell.forTableColumn());
        tblColValue.setCellValueFactory(new PropertyValueFactory<>("index05"));
        
        // Set the event handler to store the edited value
        tblColOperator.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<QuickSearchTable, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<QuickSearchTable, String> event) {
                // Code to handle edit event
                QuickSearchTable detail = event.getRowValue();
                detail.setIndex04(event.getNewValue());
                
                int rowNumber = event.getTablePosition().getRow();
                psOperator.set(rowNumber, event.getNewValue());
                
                TableView.TableViewSelectionModel<QuickSearchTable> selectionModel = tblColOperator.getTableView().getSelectionModel();
                selectionModel.clearSelection(); // Clear selection to commit edit without pressing Enter
            }
        });
        
        tblColValue.setEditable(true); // make the column editable
        
        
        // Set the event handler to store the edited value
        tblColValue.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<QuickSearchTable, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<QuickSearchTable, String> event) {
                // Code to handle edit event
                QuickSearchTable detail = event.getRowValue();
                detail.setIndex05(event.getNewValue());
                
                int rowNumber = event.getTablePosition().getRow();
                psValue.set(rowNumber, event.getNewValue());
                
                TableView.TableViewSelectionModel<QuickSearchTable> selectionModel = tblColValue.getTableView().getSelectionModel();
                selectionModel.clearSelection(); // Clear selection to commit edit without pressing Enter
            }
        });

        tblColRow.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblColOpenPar.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblColCriteria.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblColClosePar.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblColUserGuide.setCellValueFactory(new PropertyValueFactory<>("index10"));
        
        table_criteria.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) table_criteria.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });
        
        table_criteria.setItems(criteria);
    }

    @FXML
    private void exitSearch(MouseEvent event) {
        unloadScene(event);
    }

    @FXML
    private void btnSearchClicked(ActionEvent event) {
        executeQuery() ;
    }

    @FXML
    private void tblCiriteriaClicked(MouseEvent event) {
        TableRow<QuickSearchTable> row = new TableRow<>();
        
        //if (!row.isEmpty() && event.getClickCount() >= 1) {
            // Check if the event source is tblColOperator
            if (event.getSource() instanceof TableColumn && ((TableColumn<?, ?>) event.getSource()).equals(tblColOperator)) {
                tblColOperator.getTableView().edit(row.getIndex(), tblColOperator);
            }
        //}
        
        //if (!row.isEmpty() && event.getClickCount() >= 1) {
            // Check if the event source is tblColValue
            if (event.getSource() instanceof TableColumn && ((TableColumn<?, ?>) event.getSource()).equals(tblColValue)) {
                tblColValue.getTableView().edit(row.getIndex(), tblColValue);
            }
       // }
    }
    
}
