/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sbaweb

  $Id: RefreshDeliveryButtonsCallback.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sba.web.dhtmlx.callback;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackBase;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackConstants;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Refresh callback to enable/disable the delivery table buttons
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class RefreshDeliveryButtonsCallback extends CallbackBase {
    public static final Integer BUTTON_EXPORT_CSV = 11;
    public static final Integer BUTTON_AMEND = 10;
    public static final Integer BUTTON_REPLACE = 9;
    public static final Integer BUTTON_CONFIRM = 8;
    public static final Integer BUTTON_CANCEL = 7;
    public static final Integer BUTTON_VALIDATE = 6;
    public static final Integer BUTTON_UNDO_VALIDATE = 5;
    public static final Integer BUTTON_CREATE_PLAUSIREPORT = 4;
    public static final Integer BUTTON_SHOW_PLAUSIREPORT = 3;
    public static final Integer BUTTON_SAVE = 2;
    public static final Integer BUTTON_DELETE = 1;

    protected final String _stateColumnId;
    protected final String _plausiStateColumnId;

    private final MebUser _user;

    public RefreshDeliveryButtonsCallback(IDhtmlxManager manager, String stateColumnId, String plausiStateColumnId) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _stateColumnId = stateColumnId;
        _plausiStateColumnId = plausiStateColumnId;
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        // } else if (state == CONFIRMATION) {
        // button_confirm.enable();
        // button_cancel.enable();
        // button_create_plausireport.enable();
        // }
        // if (state >= CONFIRMATION) {
        // button_show_plausireport.enable();
        // }
        // if (state == DELIVERED) {
        // button_create_plausireport.enable();
        // button_validate.enable();
        // button_save.enable();
        // button_delete.enable();
        // }
        // if (ROLE_DV) {
        // if (state == PREVALIDATED) {
        // button_create_plausireport.enable();
        // button_validate.enable();
        // button_undo_validate.enable();
        // button_save.enable();
        // button_delete.enable();
        // }
        // }
        // if (ROLE_EV) {
        // if (state >= IMPORTED && state <= VALIDATED) {
        // button_delete.enable();
        // }
        // if (ROLE_EA && state == INITIALIZED) {
        // button_delete.enable();
        // }
        // if (state == VALIDATED) {
        // button_create_plausireport.enable();
        // button_undo_validate.enable();
        // button_save.enable();
        // }
        // }
        // }
        // }

        StringBuilder buf = new StringBuilder();
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        Integer stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_stateColumnId);
        Integer plausiStateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_plausiStateColumnId);

        Javascript js = new Javascript(buf);
        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("if(pane){");
        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var rowId = !hasSelected ? 0 : " + getManager().getControlName() + ".selectedRows[0].idd;");
        js.append("var state = !hasSelected ? 0 : " + getManager().getControlName() + ".cells(rowId," + stateColumnIndex.toString() + ").getValue();");
        js.append("var plausi = !hasSelected ? '' : " + getManager().getControlName() + ".cells(rowId," + plausiStateColumnIndex.toString() + ").getValue();");
        js.append("var plausiUndef = plausi == '' || plausi == '"
                + getManager().getLocalizationManager().getCodeGroupValueById(CodegroupUtility.MEB_PLAUSISTATUS, CodegroupUtility.MEB_PLAUSISTATUS_UNDEFINED)
                + "';");

        if (_user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
            js.append("if (!hasSelected){");
            js.append("for (var i=" + BUTTON_DELETE.toString() + ";i<=" + BUTTON_AMEND.toString() + ";i++){");
            js.append("pane.setButtonState(i,true);}");
            js.append("}else{");
            js.append("for (var i=" + BUTTON_DELETE.toString() + ";i<=" + BUTTON_AMEND.toString() + ";i++){");
            js.append("pane.setButtonState(i,true);}");
            // Mantis 1286: Enable Delete in state IMPORTED for DL too
            js.append("if (state == " + CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED + "){");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);} ");

            js.append("if (state == " + CodegroupUtility.MEB_DELIVERYSTATUS_AMENDREPLACE + "){");
            js.append("pane.setButtonState(" + BUTTON_AMEND.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_REPLACE.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_CANCEL.toString() + ",false);");
            js.append("} else if (state == " + CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION + "){");
            js.append("pane.setButtonState(" + BUTTON_CONFIRM.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_CANCEL.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_CREATE_PLAUSIREPORT.toString() + ",false);");
            js.append("} if (state >= " + CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION + "){");
            js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",false);");
            js.append("} if (state == " + CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED + "){");
            js.append("pane.setButtonState(" + BUTTON_CREATE_PLAUSIREPORT.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_VALIDATE.toString() + ",plausiUndef);");
            js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);");

            if (_user.isInRole(SecurityConstants.ROLE_SBA_DV)) {
                js.append("} if (state == " + CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED + "){");
                js.append("  pane.setButtonState(" + BUTTON_UNDO_VALIDATE.toString() + ",false);");
                js.append("  pane.setButtonState(" + BUTTON_CREATE_PLAUSIREPORT.toString() + ",false);");
                js.append("  pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
                js.append("  pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);");
                js.append("  pane.setButtonState(" + BUTTON_VALIDATE.toString() + ",false);");
            }

            if (_user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
                js.append("} if (state >= " + CodegroupUtility.MEB_DELIVERYSTATUS_IMPORTED + " && state <= " + CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED
                        + "){");
                js.append("  pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);");
                js.append("} if (state == " + CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED + "){");
                js.append("  pane.setButtonState(" + BUTTON_CREATE_PLAUSIREPORT.toString() + ",false);");
                js.append("  pane.setButtonState(" + BUTTON_UNDO_VALIDATE.toString() + ",false);");
                js.append("  pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
            }

            if (_user.isInRole(SecurityConstants.ROLE_SBA_EA)) {
                js.append("} if (state == " + CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED + "){");
                js.append("  pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);");
            }

            js.append("}}");
        } else {
            js.append("for (var i=" + BUTTON_DELETE.toString() + ";i<=" + BUTTON_AMEND.toString() + ";i++){");
            js.append("pane.setButtonState(i,true);");
            js.append("}");
            // Enable the button for getting plausi report
            js.append("if (state >= " + CodegroupUtility.MEB_DELIVERYSTATUS_CONFIRMATION + "){");
            js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",false);");
            js.append("}");
        }
        js.append("}");

        js.append("pane.showButtons();");

        return buf.toString();
    }
}
