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
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class SearchDialog {
    /**
     * 
     * @param foGRider 
     * @param fsSQL = SQL Statement
     * @param fCondition = search value / group by
     * 
     * @param fsHeader = Header Name
     * @param fsColName = Column Name based on header to show data result
     * @param fsFormCode = Form Name or Code based on form_list_retrieval table
     * @param fsColSize = Array List for header size
     * @param fsFormCode = Form Name
     * @param fnSort = sorting based on criteria
     * @return 
     */
    public static JSONObject jsonSearch(GRider foGRider, 
                                        String fsSQL, 
                                        String fCondition, 
                                        String fsHeader, 
                                        String fsColName,
                                        String fsColSize, 
                                        String fsFormCode,
                                        int fnSort) {
        
        try {
            System.out.println("SEARCH EXECUTE : " + fsSQL + " " + fCondition);
            ResultSet loRS = foGRider.executeQuery(fsSQL + " " + fCondition);
            if (MiscUtil.RecordCount(loRS) == 1L)
                return CommonUtils.loadJSON(loRS); 
            if (MiscUtil.RecordCount(loRS) > 1L) {
                //Clear condition when it contains "WHERE" stmt
                if(fCondition.contains("WHERE")){
                    fCondition = ""; 
                }
                
                loRS.first();
                QuickSearch loSearch = new QuickSearch();
                loSearch.setGRider(foGRider);
                loSearch.setResultSet(loRS);
                loSearch.setSQLSource(fsSQL);
                loSearch.setConditionValue(fCondition);
                loSearch.setColumnHeader(fsHeader);
                loSearch.setColunmName(fsColName);
                loSearch.setFormCode(fsFormCode);
                loSearch.setColumnIndex(fnSort);
                loSearch.setColumnSize(fsColSize);
                CommonUtils.showModal(loSearch);
                return loSearch.getJSON();
            } 
            
        } catch (SQLException ex) {
            Logger.getLogger(ShowDialogFX.class.getName()).log(Level.SEVERE, (String)null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ShowDialogFX.class.getName()).log(Level.SEVERE, (String)null, ex);
        } 
        return null;
    }
    
}
