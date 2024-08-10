/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.general;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class LockTransaction { 
    GRider poGRider;
    String psMessage;
    /**
     * Entity constructor
     *
     * @param foValue - GhostRider Application Driver
     */
    public LockTransaction(GRider foValue){
        if (foValue == null) {
            System.err.println("Application Driver is not set.");
            System.exit(1);
        }

        poGRider = foValue;
    }
    
    public JSONObject saveLockTransaction(String fsTableName, String fsWhereCol, String fsTransNo){
        JSONObject lObj = new JSONObject();
        
        poGRider.beginTrans();
        
        String lsSQL = " UPDATE "+fsTableName+" SET "
                + "   sLockedBy = " + SQLUtil.toSQL(poGRider.getUserID())
                + " , dLockedDt = " + SQLUtil.toSQL(poGRider.getServerDate())
                + " WHERE "+fsWhereCol+" = "+ SQLUtil.toSQL(fsTransNo);
        if (!lsSQL.isEmpty()) {
            if (poGRider.executeQuery(lsSQL, fsTableName, poGRider.getBranchCode(), "") > 0) {
                lObj.put("result", "success");
                lObj.put("message", "Record updated locked status successfully.");
            } else {
                lObj.put("result", "error");
                lObj.put("continue", true);
                lObj.put("message", poGRider.getErrMsg());
            }
        }
        
        poGRider.commitTrans();
        
        return lObj;
    }
    
    public boolean checkLockTransaction(String fsTableName, String fsWhereCol, String fsTransNo){
        psMessage = "";
        String lsID = "";
        String lsLockedBy = "";
        String lsLockedDate = "";
        try {
            String lsSQL =    " SELECT "
                            + "   a.sLockedBy "
                            + " , a.dLockedDt "
                            + " , b.sCompnyNm "
                            + " FROM "+fsTableName+ " a"
                            + " LEFT JOIN ggc_isysdbf.client_master b ON b.sClientID = a.sLockedBy "
                            + " WHERE a."+fsWhereCol.trim()+" = " + SQLUtil.toSQL(fsTransNo);
            if (!lsSQL.isEmpty()) {
                System.out.println("LOCKED BY CHECK: " + lsSQL);
                ResultSet loRS = poGRider.executeQuery(lsSQL);

                if (MiscUtil.RecordCount(loRS) > 0){
                        while(loRS.next()){
                            lsID = loRS.getString("sLockedBy");
                            lsLockedBy = loRS.getString("sCompnyNm");
                            lsLockedDate = loRS.getString("dLockedDt");
                        }

                        MiscUtil.close(loRS);
                        if(lsID != null){
                            if(!lsID.isEmpty()){
                                if(!lsID.equals(poGRider.getUserID())){
                                    psMessage = "Transaction Locked By : " +lsLockedBy+ "\nLocked Date : " + lsLockedDate + "\nPlease Contact System Administrator to address this issue.";
                                    return false;
                                }
                            }
                        }
                }
            }

        
        } catch (SQLException ex) {
            Logger.getLogger(LockTransaction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public String getMessage(){
        return psMessage;
    }
}
