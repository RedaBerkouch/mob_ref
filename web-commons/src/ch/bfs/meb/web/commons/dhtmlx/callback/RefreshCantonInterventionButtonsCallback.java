/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: RefreshInterventionButtonsCallback.java 1648 2010-05-20 07:30:25Z jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import org.springframework.security.core.context.SecurityContextHolder;

import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * Refresh callback to enable/disable the intervention table buttons
 * 
 * @author $Author: jfu $
 * @version $Revision: 1648 $
 */
public abstract class RefreshCantonInterventionButtonsCallback extends CallbackBase {
    public static final Integer BUTTON_DOWNLOAD_FILE = 7;
    public static final Integer BUTTON_SHOW_PLAUSIREPORT = 5;
    public static final Integer BUTTON_SAVE = 4;
    public static final Integer BUTTON_UNDO = 3;
    public static final Integer BUTTON_INSERT = 2;
    public static final Integer BUTTON_DELETE = 1;

    protected JSNumber _state = JSNumber.byRef("state");
    protected final String _typeColumnId;
    private final MebUser _user;

    public RefreshCantonInterventionButtonsCallback(IDhtmlxManager manager, String typeColumnId) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _typeColumnId = typeColumnId;
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        Integer typeIndex = ((TableManagerBase) getManager()).getColumnIndexById(_typeColumnId);
        Integer typeIndex2 = ((TableManagerBase) getManager()).getColumnIndexById("interventionId");
        Javascript js = new Javascript(buf);
        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var rowId = !hasSelected ? 0 : " + getManager().getControlName() + ".selectedRows[0].idd;");
        js.append("var value = rowId == 0 ? '': " + getManager().getControlName() + ".cells(rowId," + typeIndex.toString() + ").getValue();");
        js.append("var type = value.lastIndexOf('(') >= 0 ? value.substring(value.lastIndexOf('(')+1,value.lastIndexOf(')')) : value;");
        js.append("var isManual = type >= " + getCantonInterventionTypeManual() + ";");
        js.append("if (rowId==0){");
        js.append("for (var i=" + BUTTON_DELETE.toString() + ";i<=" + BUTTON_SHOW_PLAUSIREPORT.toString() + ";i++){");
        js.append("pane.setButtonState(i,true);console.log(rowId);}");
        js.append("}else{");
        js.append("pane.setButtonState(" + BUTTON_SHOW_PLAUSIREPORT.toString() + ",type != " + getCantonInterventionTypeCreatePlausi() + ");");
        js.append("pane.setButtonState(" + BUTTON_DOWNLOAD_FILE.toString() + ",type != " + getCantonInterventionTypeUpload() + ");");
        if (_user.isInRole(SecurityConstants.ROLE_SDL_DL) || _user.isInRole(SecurityConstants.ROLE_SSP_DL) || _user.isInRole(SecurityConstants.ROLE_SBA_DL)) {
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",!isManual);");
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

    protected abstract long getCantonInterventionTypeCreatePlausi();

    protected abstract long getCantonInterventionTypeManual();

    protected abstract long getCantonInterventionTypeUpload();
}
