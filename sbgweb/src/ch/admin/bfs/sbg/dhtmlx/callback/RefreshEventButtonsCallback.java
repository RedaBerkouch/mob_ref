/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: RefreshEventButtonsCallback.java 557 2008-10-08 10:36:30Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.dhtmlx.callback;

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

/**
 * Refreshes the buttons of personPanel (disabling according to role)
 * 
 * @author $Author: lsc $
 * @version $Revision: 557 $
 */
public class RefreshEventButtonsCallback extends CallbackBase {
    private static final Integer BUTTON_DUPLICATE = 7;
    private static final Integer BUTTON_SWITCH_MASTER = 5;
    private static final Integer BUTTON_SAVE = 4;
    private static final Integer BUTTON_UNDO = 3;
    private static final Integer BUTTON_INSERT = 2;
    private static final Integer BUTTON_DELETE = 1;

    private final IDhtmlxControl _parentTable;

    private final MebUser _user;

    public RefreshEventButtonsCallback(IDhtmlxManager manager, IDhtmlxControl parentTable) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _parentTable = parentTable;
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();
        Javascript js = new Javascript(buf);
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        js.append("var pane=dijit.byId('" + getManager().getName() + "Panel');");

        if (!_user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            js.append("for(var i=" + BUTTON_DELETE.toString() + ";i<" + BUTTON_SWITCH_MASTER.toString() + ";i++){");
            js.append("pane.setButtonState(i,true);");
            js.append("}");
            js.append("pane.setButtonState(" + BUTTON_DUPLICATE + ",true);");
        } else {
            js.append("var hasSelected=" + getManager().getControlName() + ".selectedRows.length>0;");
            js.append("var hasOnlyOneSelected=" + getManager().getControlName() + ".selectedRows.length===1;");
            js.append("var inSync=" + managerDP.getSyncState().asVar() + ";");
            js.append("var isMaster=" + getManager().getControlName() + "IsMaster;");

            js.append("var disableDelete=true;var disableInsert=true;");

            js.append("if(" + _parentTable.getControlName() + ".selectedRows.length==1){");
            js.append("var rowId=" + _parentTable.getControlName() + ".selectedRows[0].idd;");
            if (_user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
                // EV or EA
                js.append("disableInsert=" + _parentTable.getControlName() + ".getUserData(rowId,'personState')>" + CodegroupUtility.SBG_PERSONSTATUS_VALIDATED
                        + ";");
            } else {
                // DL
                js.append("disableInsert=" + _parentTable.getControlName() + ".getUserData(rowId,'personState')>" + CodegroupUtility.SBG_PERSONSTATUS_DELIVERED
                        + ";");
            }
            js.append("}");

            js.append("if(hasSelected){");
            js.append("var rowId=" + getManager().getControlName() + ".selectedRows[0].idd;");
            js.append("var maxState=" + getManager().getControlName() + ".getUserData(rowId,'personState');");
            js.append("for(var i=1;i<" + getManager().getControlName() + ".selectedRows.length;i++){");
            js.append("rowId=" + getManager().getControlName() + ".selectedRows[i].idd;");
            js.append("var state=" + getManager().getControlName() + ".getUserData(rowId,'personState');");
            js.append("if(state>maxState){maxState=state;}}");

            if (_user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
                // EV or EA
                js.append("disableDelete=maxState>" + CodegroupUtility.SBG_PERSONSTATUS_VALIDATED + ";");
            } else {
                // DL
                js.append("disableDelete=maxState>" + CodegroupUtility.SBG_PERSONSTATUS_DELIVERED + ";");
            }
            js.append("}");
            js.append("pane.setButtonState(" + BUTTON_SWITCH_MASTER.toString() + ",isMaster);");
            js.append("pane.setButtonState(" + BUTTON_UNDO.toString() + ",inSync);");
            js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
            js.append("pane.setButtonState(" + BUTTON_INSERT.toString() + ",isMaster || disableInsert);");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",!hasSelected || disableDelete);");
            js.append("pane.setButtonState(" + BUTTON_DUPLICATE.toString() + ",!hasOnlyOneSelected || disableInsert);");
        }
        js.append("pane.showButtons();");

        return buf.toString();
    }
}
