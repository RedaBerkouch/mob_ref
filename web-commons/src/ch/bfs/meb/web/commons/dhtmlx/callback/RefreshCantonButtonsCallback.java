/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: RefreshInterventionButtonsCallback.java 891 2010-03-03 15:00:28Z jfu $

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
 * Refresh callback to enable/disable the canton table buttons
 * 
 * @author $Author: jfu $
 * @version $Revision: 891 $
 */
public class RefreshCantonButtonsCallback extends CallbackBase {
    public static final Integer BUTTON_EXPORT_CSV = 11;
    public static final Integer BUTTON_FINALIZE = 10;
    public static final Integer BUTTON_UNDO_FINALIZE = 9;
    public static final Integer BUTTON_VALIDATE = 8;
    public static final Integer BUTTON_UNDO_VALIDATE = 7;
    public static final Integer BUTTON_CREATE_PLAUSIREPORT = 6;
    public static final Integer BUTTON_SHOW_LAST_PLAUSIREPORT = 5;
    public static final Integer BUTTON_SAVE = 4;
    public static final Integer BUTTON_UNDO = 3;
    public static final Integer BUTTON_INSERT = 2;
    public static final Integer BUTTON_DELETE = 1;

    protected JSNumber _state = JSNumber.byRef("state");
    protected final String _stateColumnId;
    protected final String _plausiUserColumnId;

    private final MebUser _user;

    final String role_ev;

    public RefreshCantonButtonsCallback(IDhtmlxManager manager, String stateColumnId, String plausiUserColumnId, Long application) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _stateColumnId = stateColumnId;
        _plausiUserColumnId = plausiUserColumnId;
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (application.equals(CodegroupUtility.MEB_APPLICATION_SBA)) {
            role_ev = SecurityConstants.ROLE_SBA_EV;
        } else if (application.equals(CodegroupUtility.MEB_APPLICATION_SSP)) {
            role_ev = SecurityConstants.ROLE_SSP_EV;
        } else // SDL
        {
            role_ev = SecurityConstants.ROLE_SDL_EV;
        }
    }

    public String getScriptingBody() {
        // resulting Javascript:
        // function cantonRefreshButtons (rowId) {
        // if (rowId==0) {
        // disable_all_buttons_except_export();
        // } else {
        // disable_all_buttons();
        // var state = cells(rowId, stateColumnId).getValue();
        // button_create_plausireport.enable();
        // if (plausi_user != null) {
        // button_show_plausireport.enable();
        // }
        // if (state == DELIVERED) {
        // button_validate.enable();
        // }
        // if (state <= DELIVERED) {
        // button_save.enable();
        // }
        // if (ROLE_EV) {
        // if (state == VALIDATED) {
        // button_save.enable();
        // button_undo_validate.enable();
        // button_finalize.enable();
        // }
        // else if (state == FINALIZED) {
        // button_undo_finalize.enable();
        // }
        // }
        // }}

        StringBuilder buf = new StringBuilder();
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        Integer stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_stateColumnId);
        Integer plausiUserColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_plausiUserColumnId);

        Javascript js = new Javascript(buf);
        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("if(pane){");
        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var missingCantons = " + getManager().getControlName() + ".getUserData(null,\"missingCantons\") == 'true';");

        js.append("pane.setButtonState(" + BUTTON_INSERT.toString() + ",!missingCantons);");
        js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",!hasSelected);");
        js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
        js.append("pane.setButtonState(" + BUTTON_UNDO.toString() + ",inSync);");
        js.append("if (!hasSelected){");
        js.append("for (var i=" + BUTTON_SHOW_LAST_PLAUSIREPORT.toString() + ";i<=" + BUTTON_FINALIZE.toString() + ";i++){");
        js.append("pane.setButtonState(i,true);}");
        js.append("}else{");
        js.append("var rowId = " + getManager().getControlName() + ".selectedRows[0].idd;");
        js.append("var state = " + getManager().getControlName() + ".cells(rowId," + stateColumnIndex.toString() + ").getValue();");
        js.append("var plausiUser = " + getManager().getControlName() + ".cells(rowId," + plausiUserColumnIndex.toString() + ").getValue();");
        js.append("for (var i=" + BUTTON_SHOW_LAST_PLAUSIREPORT.toString() + ";i<=" + BUTTON_FINALIZE.toString() + ";i++){");
        js.append("pane.setButtonState(i,true);}");

        js.append("pane.setButtonState(" + BUTTON_CREATE_PLAUSIREPORT.toString() + ",false);");

        js.append("if (plausiUser != null && plausiUser.replace(/^\\s\\s*/, '').replace(/\\s\\s*$/, '').length > 0){");
        js.append("pane.setButtonState(" + BUTTON_SHOW_LAST_PLAUSIREPORT.toString() + ",false);");

        js.append("} if (state == " + CodegroupUtility.MEB_CANTONSTATUS_DELIVERED + "){");
        js.append("pane.setButtonState(" + BUTTON_VALIDATE.toString() + ",false);");

        js.append("} if (state <= " + CodegroupUtility.MEB_CANTONSTATUS_DELIVERED + "){");

        if (_user.isInRole(role_ev)) {
            js.append("} if (state == " + CodegroupUtility.MEB_CANTONSTATUS_VALIDATED + "){");
            js.append("pane.setButtonState(" + BUTTON_UNDO_VALIDATE.toString() + ",false);");
            js.append("pane.setButtonState(" + BUTTON_FINALIZE.toString() + ",false);");
            js.append("} else if (state == " + CodegroupUtility.MEB_CANTONSTATUS_FINALIZED + "){");
            js.append("pane.setButtonState(" + BUTTON_UNDO_FINALIZE.toString() + ",false);");
        }

        js.append("}}");

        js.append("}");

        js.append("pane.showButtons();");

        return buf.toString();
    }
}
