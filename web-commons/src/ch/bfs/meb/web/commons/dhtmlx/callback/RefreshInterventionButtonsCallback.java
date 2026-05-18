/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Refresh callback to enable/disable the intervention table buttons
 * 
 * @author $Author$
 * @version $Revision$
 */
public class RefreshInterventionButtonsCallback extends CallbackBase {
    public static final Integer BUTTON_SHOW_PLAUSIREPORT = 6;
    public static final Integer BUTTON_SHOW_DELIVERY = 5;
    public static final Integer BUTTON_SAVE = 4;
    public static final Integer BUTTON_UNDO = 3;
    public static final Integer BUTTON_INSERT = 2;
    public static final Integer BUTTON_DELETE = 1;

    protected JSNumber _state = JSNumber.byRef("state");
    protected final String _typeColumnId;
    private final MebUser _user;

    public RefreshInterventionButtonsCallback(IDhtmlxManager manager, String typeColumnId) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _typeColumnId = typeColumnId;
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getScriptingBody() {
        // resulting Javascript:
        // function actionRefreshButtons (rowId) {
        // if (rowId==0) {
        // disable_all_buttons();
        // } else {
        // var type = cells(rowId, typeIndex).getValue();
        // if (type == CREATE_PLAUSIREPORT) {
        // button_plausireport.enable();
        // } else {
        // button_plausireport.disable();
        // }
        // if (type == DELIVER_FILE || type == DELIVERY_WITH_ERRORS) {
        // button_delivery.enable();
        // } else {
        // button_delivery.disable();
        // }
        // if (ROLE_EV) {
        // if (type == MANUAL) {
        // button_delete.enable();
        // }
        // }
        // }
        // if (ROLE_EV) {
        // button_save.enable();
        // button_insert.enable();
        // button_undo.enable();
        // }
        // }

        StringBuilder buf = new StringBuilder();
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        Integer typeIndex = ((TableManagerBase) getManager()).getColumnIndexById(_typeColumnId);

        Javascript js = new Javascript(buf);
        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var rowId = !hasSelected ? 0 : " + getManager().getControlName() + ".selectedRows[0].idd;");
        js.append("if (rowId==0){");
        js.append("for (var i=" + BUTTON_DELETE.toString() + ";i<=" + BUTTON_SHOW_PLAUSIREPORT.toString() + ";i++){");
        js.append("pane.setButtonState(i,true);}");
        js.append("}else{");
        js.append("var type=" + getManager().getControlName() + ".cells(rowId," + typeIndex.toString() + ").getValue();");
        js.append("if (type == " + CodegroupUtility.MEB_INTERVENTIONTYPE_CREATE_PLAUSIREPORT + ") {");
        js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",false);");
        js.append("}else{");
        js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",true);}");
        js.append("if (type == " + CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVER_FILE + " || ");
        js.append("    type == " + CodegroupUtility.MEB_INTERVENTIONTYPE_DELIVERY_WITH_ERRORS + "){");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELIVERY.toString() + ",false);");
        js.append("}else{");
        js.append("pane.setButtonState(" + BUTTON_SHOW_DELIVERY.toString() + ",true);");
        js.append("}");

        if (_user.isInRole(SecurityConstants.ROLE_SDL_DL) || _user.isInRole(SecurityConstants.ROLE_SSP_DL) || _user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
            js.append("if (type == " + CodegroupUtility.MEB_INTERVENTIONTYPE_MANUAL + ") {");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",false);}");
        }

        js.append("}");

        if (_user.isInRole(SecurityConstants.ROLE_SDL_DL) || _user.isInRole(SecurityConstants.ROLE_SSP_DL) || _user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
            js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
            js.append("pane.setButtonState(" + BUTTON_INSERT.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_UNDO.toString() + ",inSync);");
        }

        js.append("pane.showButtons();");

        return buf.toString();
    }
}
