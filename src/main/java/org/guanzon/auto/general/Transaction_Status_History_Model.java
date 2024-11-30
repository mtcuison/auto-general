/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.general;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.appdriver.iface.GEntity;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Transaction_Status_History_Model implements GEntity{
final String XML = "Model_Transaction_Status_History.xml";
    private final String psDefaultDate = "1900-01-01";
    private String psBranchCd;
    private String psExclude = ""; //» partially added to exclude sPayloadx
    
    GRider poGRider;                //application driver
    CachedRowSet poEntity;          //rowset
    JSONObject poJSON;              //json container
    int pnEditMode;                 //edit mode

    /**
     * Entity constructor
     *
     * @param foValue - GhostRider Application Driver
     */
    public Transaction_Status_History_Model(GRider foValue) {
        if (foValue == null) {
            System.err.println("Application Driver is not set.");
            System.exit(1);
        }

        poGRider = foValue;

        initialize();
    }
    
    private void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);        
            poEntity.updateObject("dApproved", SQLUtil.toDate(psDefaultDate, SQLUtil.FORMAT_SHORT_DATE)); 
            poEntity.updateObject("dModified", poGRider.getServerDate()); 
            poEntity.updateString("cTranStat", TransactionStatus.STATE_OPEN); 

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Gets the column index name.
     *
     * @param fnValue - column index number
     * @return column index name
     */
    @Override
    public String getColumn(int fnValue) {
        try {
            return poEntity.getMetaData().getColumnLabel(fnValue);
        } catch (SQLException e) {
        }
        return "";
    }

    /**
     * Gets the column index number.
     *
     * @param fsValue - column index name
     * @return column index number
     */
    @Override
    public int getColumn(String fsValue) {
        try {
            return MiscUtil.getColumnIndex(poEntity, fsValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Gets the total number of column.
     *
     * @return total number of column
     */
    @Override
    public int getColumnCount() {
        try {
            return poEntity.getMetaData().getColumnCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    @Override
    public String getTable() {
        return "transaction_status_history";
    }
    
    /**
     * Gets the value of a column index number.
     *
     * @param fnColumn - column index number
     * @return object value
     */
    @Override
    public Object getValue(int fnColumn) {
        try {
            return poEntity.getObject(fnColumn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the value of a column index name.
     *
     * @param fsColumn - column index name
     * @return object value
     */
    @Override
    public Object getValue(String fsColumn) {
        try {
            return poEntity.getObject(MiscUtil.getColumnIndex(poEntity, fsColumn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets column value.
     *
     * @param fnColumn - column index number
     * @param foValue - value
     * @return result as success/failed
     */
    @Override
    public JSONObject setValue(int fnColumn, Object foValue) {
        try {
            poJSON = MiscUtil.validateColumnValue(System.getProperty("sys.default.path.metadata") + XML, MiscUtil.getColumnLabel(poEntity, fnColumn), foValue);
            if ("error".equals((String) poJSON.get("result"))) {
                System.out.println("ERROR : "+ MiscUtil.getColumnLabel(poEntity, fnColumn) + " : "+  poJSON.get("message"));
                return poJSON;
            }

            poEntity.updateObject(fnColumn, foValue);
            poEntity.updateRow();

            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("value", getValue(fnColumn));
        } catch (SQLException e) {
            e.printStackTrace();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }
    
    /**
     * Sets column value.
     *
     * @param fsColumn - column index name
     * @param foValue - value
     * @return result as success/failed
     */
    @Override
    public JSONObject setValue(String fsColumn, Object foValue) {
        poJSON = new JSONObject();

        try {
            return setValue(MiscUtil.getColumnIndex(poEntity, fsColumn), foValue);
        } catch (SQLException e) {
            e.printStackTrace();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }

    /**
     * Set the edit mode of the entity to new.
     *
     * @return result as success/failed
     */
    @Override
    public JSONObject newRecord() {
        pnEditMode = EditMode.ADDNEW;

        //replace with the primary key column info
        setTransNo(MiscUtil.getNextCode(getTable(), "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode()));
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Opens a record.
     *
     * @param fsValue - filter values
     * @return result as success/failed
     */
    @Override
    public JSONObject openRecord(String fsValue) {
        poJSON = new JSONObject();

        String lsSQL = getSQL(); //MiscUtil.makeSelect(this, psExclude); //exclude the columns called thru left join
        //replace the condition based on the primary key column of the record
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sTransNox = " + SQLUtil.toSQL(fsValue)
                                                );

        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()) {
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++) {
                    setValue(lnCtr, loRS.getObject(lnCtr));
                }

                pnEditMode = EditMode.UPDATE;

                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No record to load.");
            }
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }

    /**
     * Save the entity.
     *
     * @return result as success/failed
     */
    @Override
    public JSONObject saveRecord() {
        poJSON = new JSONObject();

        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            String lsSQL; 
            if (pnEditMode == EditMode.ADDNEW) {
                //replace with the primary key column info
                setTransNo(MiscUtil.getNextCode(getTable(), "sTransNox", true, poGRider.getConnection(), poGRider.getBranchCode()));
                setModifiedBy(poGRider.getUserID());
                setModifiedDte(poGRider.getServerDate());
                System.out.println("JSON Payload : " + getPayload());
                
                if(getPayload() == null){
                    setPayload("{}");
                    if(psExclude.isEmpty()){ //Try to exclude when payload is empty
                        psExclude = "sPayloadx";
                    } else {
                        psExclude = psExclude + "»" + "sPayloadx";
                    }
                } else {
                    if(getPayload().isEmpty()){
                        setPayload("{}");
                        if(psExclude.isEmpty()){ //Try to exclude when payload is empty
                            psExclude = "sPayloadx";
                        } else {
                            psExclude = psExclude + "»" + "sPayloadx";
                        }
                    }
                }
                
                lsSQL = MiscUtil.makeSQL(this, psExclude);
                
               // lsSQL = "Select * FROM " + getTable() + " a left join (" + makeSQL() + ") b on a.column1 = b.column "
                if (!lsSQL.isEmpty()) {
                    if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), getTargetBranchCd()) > 0) {
                        poJSON.put("result", "success");
                        poJSON.put("message", "Record saved successfully.");
                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("message", poGRider.getErrMsg());
                    }
                } else {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record to save.");
                }
            } else {
                Transaction_Status_History_Model loOldEntity = new Transaction_Status_History_Model(poGRider);
                
                //replace with the primary key column info
                JSONObject loJSON = loOldEntity.openRecord(this.getTransNo());

                if ("success".equals((String) loJSON.get("result"))) {
                    setModifiedBy(poGRider.getUserID());
                    setModifiedDte(poGRider.getServerDate());
                    System.out.println("JSON Payload : " + getPayload());
                    if(getPayload() == null){
                        setPayload("{}");
                        if(psExclude.isEmpty()){ //Try to exclude when payload is empty
                            psExclude = "sPayloadx";
                        } else {
                            psExclude = psExclude + "»" + "sPayloadx";
                        }
                    } else {
                        if(getPayload().isEmpty()){
                            setPayload("{}");
                            if(psExclude.isEmpty()){ //Try to exclude when payload is empty
                                psExclude = "sPayloadx";
                            } else {
                                psExclude = psExclude + "»" + "sPayloadx";
                            }
                        }
                    }
                    
                    //replace the condition based on the primary key column of the record
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, "sTransNox = " + SQLUtil.toSQL(this.getTransNo()), psExclude);

                    if (!lsSQL.isEmpty()) {
                        if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), getTargetBranchCd()) > 0) {
                            poJSON.put("result", "success");
                            poJSON.put("message", "Record saved successfully.");
                        } else {
                            poJSON.put("result", "error");
                            poJSON.put("message", poGRider.getErrMsg());
                        }
                    } else {
                        poJSON.put("result", "success");
                        poJSON.put("message", "No updates has been made.");
                    }
                } else {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Record discrepancy. Unable to save record.");
                }
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid update mode. Unable to save record.");
            return poJSON;
        }

        return poJSON;
    }
    
    public JSONObject cancelRecord(String fsSourceNo, String fsStatus ){
        poJSON = new JSONObject();
        
        String lsSQL = " UPDATE "+getTable()
                    + " SET cTranStat = "+SQLUtil.toSQL(TransactionStatus.STATE_CANCELLED)
                    + " , sModified = "+SQLUtil.toSQL(poGRider.getUserID())
                    + " , dModified = "+SQLUtil.toSQL(poGRider.getServerDate())
                    + " WHERE sSourceNo = " + SQLUtil.toSQL(fsSourceNo) 
                    + " AND cRefrStat = "+SQLUtil.toSQL(fsStatus) ; 
        
        if (!lsSQL.isEmpty()) {
            if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0) {
                poJSON.put("result", "success");
                poJSON.put("message", "Record cancelled successfully.");
            } else {
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", poGRider.getErrMsg());
            }
        }
        return poJSON;
    }
    
    private String getTargetBranchCd(){
//        if (!poGRider.getBranchCode().equals(getBranchCD())){
//            return getBranchCD();
//        } else {
            return "";
//        }
    }

    /**
     * Prints all the public methods used<br>
     * and prints the column names of this entity.
     */
    @Override
    public void list() {
        Method[] methods = this.getClass().getMethods();

        System.out.println("--------------------------------------------------------------------");
        System.out.println("LIST OF PUBLIC METHODS FOR " + this.getClass().getName() + ":");
        System.out.println("--------------------------------------------------------------------");
        for (Method method : methods) {
            System.out.println(method.getName());
        }

        try {
            int lnRow = poEntity.getMetaData().getColumnCount();

            System.out.println("--------------------------------------------------------------------");
            System.out.println("ENTITY COLUMN INFO");
            System.out.println("--------------------------------------------------------------------");
            System.out.println("Total number of columns: " + lnRow);
            System.out.println("--------------------------------------------------------------------");

            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++) {
                System.out.println("Column index: " + (lnCtr) + " --> Label: " + poEntity.getMetaData().getColumnLabel(lnCtr));
                if (poEntity.getMetaData().getColumnType(lnCtr) == Types.CHAR
                        || poEntity.getMetaData().getColumnType(lnCtr) == Types.VARCHAR) {

                    System.out.println("Column index: " + (lnCtr) + " --> Size: " + poEntity.getMetaData().getColumnDisplaySize(lnCtr));
                }
            }
        } catch (SQLException e) {
        }

    }
    
    /**
     * Gets the SQL statement for this entity.
     *
     * @return SQL Statement
     */
    public String makeSQL() {
        return MiscUtil.makeSQL(this, psExclude);
    }
    
    /**
     * Gets the SQL Select statement for this entity.
     *
     * @return SQL Select Statement
     */
    public String makeSelectSQL() {
        return MiscUtil.makeSelect(this, psExclude);
    }
    
    public String getSQL(){
        return    " SELECT "                          
                + "    sTransNox "                    
                + "  , sTableNme "                    
                + "  , sSourceNo "                    
                + "  , sPayloadx "                    
                + "  , sRemarksx "                    
                + "  , sApproved "                    
                + "  , dApproved "                    
                + "  , cRefrStat "                    
                + "  , cTranStat "                    
                + "  , sModified "                    
                + "  , dModified "                    
                + " FROM transaction_status_history " ;                          
    }
    
    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private static String xsDateShort(String fsValue) throws org.json.simple.parser.ParseException, java.text.ParseException {
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
    
    /**
     * Description: Sets the ID of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setTransNo(String fsValue) {
        return setValue("sTransNox", fsValue);
    }

    /**
     * @return The ID of this record.
     */
    public String getTransNo() {
        return (String) getValue("sTransNox");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setTableNme(String fsValue) {
        return setValue("sTableNme", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getTableNme() {
        return (String) getValue("sTableNme");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setSourceNo(String fsValue) {
        return setValue("sSourceNo", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setPayload(String fsValue) {
        return setValue("sPayloadx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getPayload() {
        return (String) getValue("sPayloadx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setRemarks(String fsValue) {
        return setValue("sRemarksx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setApproved(String fsValue) {
        return setValue("sApproved", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getApproved() {
        return (String) getValue("sApproved");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fdValue
     * @return result as success/failed
     */
    public JSONObject setApprovedDte(Date fdValue) {
        JSONObject loJSON = new JSONObject();
        return setValue("dApproved", fdValue);
    }

    /**
     * @return The Value of this record.
     */
    public Date getApprovedDte() {
        Date date = null;
        if(getValue("dApproved") == null || getValue("dApproved").equals("")){
            date = SQLUtil.toDate(psDefaultDate, SQLUtil.FORMAT_SHORT_DATE);
        } else {
            date = SQLUtil.toDate(xsDateShort((Date) getValue("dApproved")), SQLUtil.FORMAT_SHORT_DATE);
        }
            
        return date;
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setRefrStat(String fsValue) {
        return setValue("cRefrStat", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getRefrStat() {
        return (String) getValue("cRefrStat");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setTranStat(String fsValue) {
        return setValue("cTranStat", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getTranStat() {
        return (String) getValue("cTranStat");
    }
    
//    /**
//     * Sets record as active.
//     *
//     * @param fbValue
//     * @return result as success/failed
//     */
//    public JSONObject setActive(boolean fbValue) {
//        return setValue("cTranStat", fbValue ? "1" : "0");
//    }
//
//    /**
//     * @return If record is active.
//     */
//    public boolean isActive() {
//        return ((String) getValue("cTranStat")).equals("1");
//    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setModifiedBy(String fsValue) {
        return setValue("sModified", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getModifiedBy() {
        return (String) getValue("sModified");
    }
    
    /**
     * Sets the date and time the record was modified.
     *
     * @param fdValue
     * @return result as success/failed
     */
    public JSONObject setModifiedDte(Date fdValue) {
        return setValue("dModified", fdValue);
    }

    /**
     * @return The date and time the record was modified.
     */
    public Date getModifiedDte() {
        return (Date) getValue("dModified");
    }
    
//    /**
//     * Description: Sets the Value of this record.
//     *
//     * @param fsValue
//     * @return result as success/failed
//     */
//    public JSONObject setTranStatus(String fsValue) {
//        return setValue("sTranStat", fsValue);
//    }
//
//    /**
//     * @return The Value of this record.
//     */
//    public String getTranStatus() {
//        return (String) getValue("sTranStat");
//    }
    
    
}
