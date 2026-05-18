/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: RefreshPersonButtonsCallback.java 625 2010-11-15 09:14:20Z dzw $
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
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;

/**
 * Refreshes the buttons of the person table (disabling according to role and
 * state)
 * 
 * @author $Author: dzw $
 * @version $Revision: 625 $
 */
public class RefreshPersonButtonsCallback extends CallbackBase {
    private static final Integer BUTTON_SWITCH_MASTER = 6;
    private static final Integer BUTTON_SAVE = 5;
    private static final Integer BUTTON_UNDO = 4;
    private static final Integer BUTTON_INSERT = 3;
    private static final Integer BUTTON_DELETE = 2;
    private static final Integer BUTTON_VALIDATE = 1;

    private final MebUser _user;

    public RefreshPersonButtonsCallback(IDhtmlxManager manager) {
        super(CallbackConstants.RefreshButtonsCallback, manager);

        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();
        Javascript js = new Javascript(buf);
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        js.append("var pane=dijit.byId('" + getManager().getName() + "Panel');");
        js.append("var hasSelected=" + getManager().getControlName() + ".selectedRows.length>0;");

        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_DL)) {
            js.append("for(var i=" + BUTTON_VALIDATE.toString() + ";i<" + BUTTON_SWITCH_MASTER.toString() + ";i++){");
            js.append("pane.setButtonState(i,true);");
            js.append("}");
        } else {
            js.append("var inSync=" + managerDP.getSyncState().asVar() + ";");
            js.append("var isMaster=" + getManager().getControlName() + "IsMaster;");
            js.append("for(var i=" + BUTTON_INSERT.toString() + ";i<=" + BUTTON_SWITCH_MASTER.toString() + ";i++){");
            js.append("pane.setButtonState(i,false);");
            js.append("}");

            // enable/disable validate and delete buttons according to the
            // states of the selected rows
            js.append("var disableValidate=true;var disableDelete=true;");
            js.append("if(hasSelected){");
            js.append("var rowId=" + getManager().getControlName() + ".selectedRows[0].idd;");
            js.append("var minState=" + getManager().getControlName() + ".getUserData(rowId,'personState');");
            js.append("var maxState=minState;");
            js.append("for(var i=1;i<" + getManager().getControlName() + ".selectedRows.length;i++){");
            js.append("rowId=" + getManager().getControlName() + ".selectedRows[i].idd;");
            js.append("var state=" + getManager().getControlName() + ".getUserData(rowId,'personState');");
            js.append("if(state<minState){minState=state;}");
            js.append("if(state>maxState){maxState=state;}}");
            js.append("disableValidate=minState<" + CodegroupUtility.SBG_PERSONSTATUS_DELIVERED + "||maxState>" + CodegroupUtility.SBG_PERSONSTATUS_DELIVERED
                    + ";");
            if (_user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
                // EV or EA
                js.append("disableDelete=maxState>" + CodegroupUtility.SBG_PERSONSTATUS_VALIDATED + ";");
            } else {
                // DL
                js.append("disableDelete=maxState>" + CodegroupUtility.SBG_PERSONSTATUS_DELIVERED + ";");
            }
            js.append("}");
            js.append("pane.setButtonState(" + BUTTON_VALIDATE.toString() + ",disableValidate || !inSync);");
            js.append("pane.setButtonState(" + BUTTON_SWITCH_MASTER.toString() + ",isMaster);");
            js.append("pane.setButtonState(" + BUTTON_UNDO.toString() + ",inSync);");
            js.append("pane.setButtonState(" + BUTTON_SAVE.toString() + ",inSync);");
            js.append("pane.setButtonState(" + BUTTON_INSERT.toString() + ",!isMaster);");
            js.append("pane.setButtonState(" + BUTTON_DELETE.toString() + ",!hasSelected || disableDelete);");
        }
        js.append("pane.showButtons();");

        return buf.toString();
    }
}
