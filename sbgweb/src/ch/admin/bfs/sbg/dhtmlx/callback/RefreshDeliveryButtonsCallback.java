/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: RefreshDeliveryButtonsCallback.java 625 2010-11-15 09:14:20Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.dhtmlx.callback;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackBase;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackConstants;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Refreshes the buttons of the delivery table (disabling according to role and
 * state)
 * 
 * @author $Author: dzw $
 * @version $Revision: 625 $
 */
public class RefreshDeliveryButtonsCallback extends CallbackBase {

    private static final Integer BUTTON_AMEND = 12;
    private static final Integer BUTTON_REPLACE = 11;
    private static final Integer BUTTON_CONFIRM = 10;
    private static final Integer BUTTON_CANCEL = 9;
    private static final Integer BUTTON_CREATE_PLAUSIREPORT = 8;
    private static final Integer BUTTON_SHOW_PLAUSIREPORT = 7;
    private static final Integer BUTTON_VALIDATE = 6;
    private static final Integer BUTTON_UNDO_VALIDATE = 5;
    private static final Integer BUTTON_SAVE = 4;
    private static final Integer BUTTON_DELETE = 3;
    private static final Integer BUTTON_FINALIZE = 2;
    private static final Integer BUTTON_UNDO_FINALIZE = 1;

    protected JSNumber _state = JSNumber.byRef("state");
    protected final String _stateColumnId;

    public RefreshDeliveryButtonsCallback(IDhtmlxManager manager, String stateColumnId) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _stateColumnId = stateColumnId;
    }

    public String getScriptingBody() {
        // resulting Javascript:
        // function deliveryRefreshButtons (rowId) {
        // if (rowId==0) {
        // enable_all_buttons();
        // } else {
        // disable_all_buttons();
        // var state = cells(rowId, stateColumnId).getValue();
        // if (state == AMENDREPLACE) {
        // button_amend.enable();
        // button_replace.enable();
        // button_cancel.enable();
        // } else if (state == COMFIRM) {
        // button_confirm.enable();
        // button_cancel.enable();
        // }
        // if (state >= CONFIRMATION) {
        // button_show_plausireport.enable();
        // }
        // if (state > CONFIRMATION) {
        // button_create_plausireport.enable();
        // }
        // if (state == DELIVERED) {
        // button_validate.enable();
        // }
        // if (state != FINALIZED) {
        // button_save.enable();
        // button_delete.enable();
        // }
        // if (state == VALIDATED) {
        // button_finalize.enable();
        // }
        // if (state == FINALIZED) {
        // button_undo_finalize.enable();
        // }
        // }
        // }

        StringBuilder buf = new StringBuilder();

        Integer stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_stateColumnId);
        //		DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        Javascript js = new Javascript(buf);
        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("if(pane){");
        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        //		js.append ("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var rowId = !hasSelected ? 0 : " + getManager().getControlName() + ".selectedRows[0].idd;");
        js.append("var state = !hasSelected ? 0 : " + getManager().getControlName() + ".cells(rowId," + stateColumnIndex.toString() + ").getValue();");

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            js.append("for (var i=" + BUTTON_UNDO_FINALIZE.toString() + ";i<=" + BUTTON_AMEND.toString() + ";i++){");
            js.append("pane.setButtonState(i,true);");
            js.append("}");
            // Enable the button for getting plausi report
            js.append("if (state >= " + CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION + "){");
            js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",false);");
            js.append("}");
        } else {
            js.append("if (rowId==0){");
            js.append("for (var i=" + BUTTON_UNDO_FINALIZE.toString() + ";i<=" + BUTTON_AMEND.toString() + ";i++){");
            js.append("pane.setButtonState(i,false);}");
            js.append("}else{");
            js.append("for (var i=" + BUTTON_UNDO_FINALIZE.toString() + ";i<=" + BUTTON_AMEND.toString() + ";i++){");
            js.append("pane.setButtonState(i,true);}");
            js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
            js.append("var state = !hasSelected ? 0 : " + getManager().getControlName() + ".cells(rowId," + stateColumnIndex.toString() + ").getValue();");
            js.append("if (state == " + CodegroupUtility.SBG_DELIVERYSTATUS_AMENDREPLACE + "){");
            js.append("pane.setButtonState(" + BUTTON_AMEND.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_REPLACE.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_CANCEL.toString() + ",false);");
            js.append("}else if (state == " + CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION + "){");
            js.append("pane.setButtonState(" + BUTTON_CONFIRM.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_CANCEL.toString() + ",false);");
            js.append("} if (state >= " + CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION + "){");
            js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",false);");
            js.append("} if (state > " + CodegroupUtility.SBG_DELIVERYSTATUS_CONFIRMATION + "){");
            js.append("pane.setButtonState(" + BUTTON_CREATE_PLAUSIREPORT.toString() + ",false);");
            js.append("} if (state == " + CodegroupUtility.SBG_DELIVERYSTATUS_DELIVERED + "){");
            js.append("pane.setButtonState(" + BUTTON_VALIDATE.toString() + ",false);");
            js.append("} if (state == " + CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED + "){");
            js.append("pane.setButtonState(" + BUTTON_UNDO_VALIDATE.toString() + ",false);");
            js.append("} if (state != " + CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED + " && state != "+ CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED+"){");
            js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);");
            if(user.isInRole(SecurityConstants.ROLE_SBG_EV)){
                js.append("} if (state == " + CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED + "){");
                js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);");
            }
            js.append("} if (state == " + CodegroupUtility.SBG_DELIVERYSTATUS_VALIDATED + "){");
            js.append("pane.setButtonState(" + BUTTON_FINALIZE.toString() + ",false);");
            js.append("} if (state == " + CodegroupUtility.SBG_DELIVERYSTATUS_FINALIZED + "){");
            js.append("pane.setButtonState(" + BUTTON_UNDO_FINALIZE.toString() + ",false);");
            js.append("}}");
        }
        js.append("}");
        js.append("pane.showButtons();");

        return buf.toString();
    }
}
