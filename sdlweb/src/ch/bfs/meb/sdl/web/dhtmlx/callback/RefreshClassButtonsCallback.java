/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: RefreshDeliveryButtonsCallback.java 305 2009-12-03 10:25:28Z jfu $

 */
package ch.bfs.meb.sdl.web.dhtmlx.callback;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackBase;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackConstants;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Refresh callback to enable/disable the class table buttons
 * 
 * @author $Author: jfu $
 * @version $Revision: 305 $
 */
public class RefreshClassButtonsCallback extends CallbackBase {
    public static final Integer BUTTON_EXPORT_CSV = 8;
    public static final Integer BUTTON_SWITCH_MASTER = 7;
    public static final Integer BUTTON_VALIDATE = 6;
    public static final Integer BUTTON_UNDO_VALIDATE = 5;
    public static final Integer BUTTON_SAVE = 4;
    public static final Integer BUTTON_UNDO = 3;
    public static final Integer BUTTON_INSERT = 2;
    public static final Integer BUTTON_DELETE = 1;

    private final IDhtmlxControl _parentTable;

    protected final String _stateColumnId;
    protected final String _parentStateColumnId;

    private final MebUser _user;

    public RefreshClassButtonsCallback(IDhtmlxManager manager, IDhtmlxControl parentTable, String stateColumnId, String parentStateColumnId) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _parentTable = parentTable;
        _stateColumnId = stateColumnId;
        _parentStateColumnId = parentStateColumnId;
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();

        Javascript js = new Javascript(buf);
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        Integer stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_stateColumnId);
        Integer parentStateColumnIndex = ((TableManagerBase) _parentTable).getColumnIndexById(_parentStateColumnId);

        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("if(pane){");

        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var isMaster = " + getManager().getControlName() + "IsMaster;");

        // enable/disable validate, undoValidate and edit buttons according to the states of the selected rows
        js.append("var disableValidate=true;var disableUndoValidate=true;var disableDelete=true;");
        js.append("if(hasSelected)");
        js.append("{var rowId=" + getManager().getControlName() + ".selectedRows[0].idd;");
        js.append("var minState=" + getManager().getControlName() + ".cells(rowId," + stateColumnIndex.toString() + ").getValue();");
        js.append("var maxState=minState;");
        js.append("for(var i=1;i<" + getManager().getControlName() + ".selectedRows.length;i++)");
        js.append("{rowId=" + getManager().getControlName() + ".selectedRows[i].idd;");
        js.append("var state=" + getManager().getControlName() + ".cells(rowId," + stateColumnIndex.toString() + ").getValue();");
        js.append("if(state<minState){minState=state;}");
        js.append("if(state>maxState){maxState=state;}}");
        if (_user.isInRole(SecurityConstants.ROLE_SDL_DL)) {
            if (_user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
                if (_user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
                    // EV or EA
                    js.append("disableDelete=minState>=" + CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED + ";");
                } else {
                    // DV
                    js.append("disableDelete=minState>=" + CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED + ";");
                }
                js.append("disableUndoValidate=minState<" + CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED + "||maxState>"
                        + CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED + ";");
            } else {
                // DL
                js.append("disableDelete=minState>=" + CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED + ";");
            }
            js.append("disableValidate=minState<" + CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED + "||maxState>"
                    + CodegroupUtility.MEB_DELIVERYSTATUS_DELIVERED + ";");
        }
        js.append("}");
        js.append("var disableInsert=" + _parentTable.getControlName() + ".selectedRows.length!=1;");
        js.append("if(!disableInsert){");
        js.append("var rowId=" + _parentTable.getControlName() + ".selectedRows[0].idd;");
        js.append("var state=" + _parentTable.getControlName() + ".cells(rowId," + parentStateColumnIndex.toString() + ").getValue();");
        if (_user.isInRole(SecurityConstants.ROLE_SDL_EV)) {
            // EV or EA
            js.append("disableInsert=state>=" + CodegroupUtility.MEB_DELIVERYSTATUS_FINALIZED + ";");
        } else if (_user.isInRole(SecurityConstants.ROLE_SDL_DV)) {
            // DV
            js.append("disableInsert=state>=" + CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED + ";");
        } else if (_user.isInRole(SecurityConstants.ROLE_SDL_DL)) {
            // DL
            js.append("disableInsert=state>=" + CodegroupUtility.MEB_DELIVERYSTATUS_PREVALIDATED + ";");
        } else {
            // RO
            js.append("disableInsert=true;");
        }
        js.append("}");

        js.append("pane.setButtonState(" + BUTTON_VALIDATE.toString() + ",disableValidate || !inSync);");
        js.append("pane.setButtonState(" + BUTTON_UNDO_VALIDATE.toString() + ",disableUndoValidate || !inSync);");

        js.append("pane.setButtonState(" + BUTTON_SWITCH_MASTER.toString() + ",isMaster);");
        js.append("pane.setButtonState(" + BUTTON_UNDO.toString() + ",inSync);");

        if (_user.isInRole(SecurityConstants.ROLE_SDL_DL)) {
            js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
            js.append("pane.setButtonState(" + BUTTON_INSERT.toString() + ",isMaster || disableInsert);");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",!hasSelected || disableDelete);");
        } else {
            js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",true);");
            js.append("pane.setButtonState(" + BUTTON_INSERT.toString() + ",true);");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",true);");
        }
        js.append("}");
        js.append("pane.showButtons();");

        return buf.toString();
    }
}
