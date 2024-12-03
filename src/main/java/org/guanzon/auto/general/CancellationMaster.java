/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.general;

import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class CancellationMaster {
    
    private GRider poGRider;
    private String psBranchCd;
    public String psMessage;
    private String psAction;
    public String psType;
    private int pnEditMode;
    private boolean pbWithParent;
       
    private boolean pbWithUI;

    public CancellationMaster(GRider foGRider, String fsBranchCd, boolean fbWithParent){            
        
        poGRider = foGRider;
        psBranchCd = fsBranchCd;
        pbWithParent = fbWithParent;                       
    }    
    
    public void setWithUI(boolean fbValue){
        pbWithUI = fbValue;
    }
    
    public int getEditMode(){
        return pnEditMode;
    }
    
    public String getMessage(){
        return psMessage;
    }
    
    public void setAction(String fsAction, String fsType){
        psAction = fsAction;
        psType = fsType;
    }
    
    public boolean CancelForm(String fsReferNox,String fsRemarks,String fsTableName){ //, String fsSourceNo fsSourceCD
        JSONObject loJSON = new JSONObject();
        TransactionStatusHistory loEntity = new TransactionStatusHistory(poGRider);
        if(psAction.isEmpty()){
            loJSON = loEntity.updateStatusHistory(fsReferNox, fsTableName, fsRemarks, TransactionStatus.STATE_CANCELLED, "CANCELLED");
        } else {
            loJSON = loEntity.updateStatusHistory(fsReferNox, fsTableName, fsRemarks, psAction, psType);
        }
        
        if("error".equals((String) loJSON.get("result"))){
            psMessage = (String) loJSON.get("message");
            return false;
        }
        
//        String lsSQL ="INSERT INTO cancellation_Master SET" +
//                        " sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("cancellation_master", "sTransNox", true, poGRider.getConnection(), psBranchCd)) +
//                        " ,dTransact = " + SQLUtil.toSQL(poGRider.getServerDate()) + 
//                        " ,sRemarksx = " + SQLUtil.toSQL(fsRemarks) +
//                        " ,sReferNox = " + SQLUtil.toSQL(fsReferNox) +
//                        " ,sSourceCD = " + SQLUtil.toSQL(fsSourceCD) +
//                        " ,sSourceNo = " + SQLUtil.toSQL(fsSourceNo) +                        
//                        " ,sEntryByx = " + SQLUtil.toSQL(poGRider.getUserID()) +
//                        " ,dEntryDte = " + SQLUtil.toSQL(poGRider.getServerDate());
        //Update to cancel all previous approvements
//        loJSON = loEntity.cancelTransaction(fsReferNox, TransactionStatus.STATE_CANCELLED);
//        if(!"error".equals((String) loJSON.get("result"))){
//            loJSON = loEntity.newTransaction();
//            if(!"error".equals((String) loJSON.get("result"))){
//                loEntity.getMasterModel().setApproved(poGRider.getUserID());
//                loEntity.getMasterModel().setApprovedDte(poGRider.getServerDate());
//                loEntity.getMasterModel().setSourceNo(fsReferNox);
//                loEntity.getMasterModel().setTableNme(fsTableName);
//                loEntity.getMasterModel().setRemarks(fsRemarks);
//                loEntity.getMasterModel().setRefrStat(TransactionStatus.STATE_CANCELLED);
//
//                loJSON = loEntity.saveTransaction();
//                if("error".equals((String) loJSON.get("result"))){
//                    psMessage = (String) loJSON.get("message");
//                    return false;
//                }
//            }
//        }
        
//        String lsSQL ="INSERT INTO cancellation_Master SET" +
//                        " sTransNox = " + SQLUtil.toSQL(MiscUtil.getNextCode("cancellation_master", "sTransNox", true, poGRider.getConnection(), psBranchCd)) +
//                        " ,dTransact = " + SQLUtil.toSQL(poGRider.getServerDate()) + 
//                        " ,sRemarksx = " + SQLUtil.toSQL(fsRemarks) +
//                        " ,sReferNox = " + SQLUtil.toSQL(fsReferNox) +
//                        " ,sSourceCD = " + SQLUtil.toSQL(fsSourceCD) +
//                        " ,sSourceNo = " + SQLUtil.toSQL(fsSourceNo) +                        
//                        " ,sEntryByx = " + SQLUtil.toSQL(poGRider.getUserID()) +
//                        " ,dEntryDte = " + SQLUtil.toSQL(poGRider.getServerDate());
//        
//        if (poGRider.executeQuery(lsSQL, "cancellation_master", psBranchCd,"") <= 0){
//            psMessage = poGRider.getErrMsg() + "; " + poGRider.getMessage();
//            return false;
//        }
                        
        psMessage = "Transaction successfully cancelled";
        pnEditMode = EditMode.UNKNOWN;
        return true;        
    }
}
