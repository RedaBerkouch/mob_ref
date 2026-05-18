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
import ch.bfs.meb.util.SecurityConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;

/**
 * Refresh callback to enable/disable the canton table buttons
 * 
 * @author $Author: jfu $
 * @version $Revision: 891 $
 */
public class RefreshBurSchoolButtonsCallback extends CallbackBase {
    public static final Integer BUTTON_EXPORT_CSV = 7;
    public static final Integer BUTTON_SWITCH_MASTER = 6;
    public static final Integer BUTTON_SYNCH_BUR = 5;
    public static final Integer BUTTON_GET_ALL_BUR = 4;
    public static final Integer BUTTON_GET_BUR = 3;
    public static final Integer BUTTON_SAVE = 2;
    public static final Integer BUTTON_UNDO = 1;

    private final MebUser _user;

    public RefreshBurSchoolButtonsCallback(IDhtmlxManager manager) {
        super(CallbackConstants.RefreshButtonsCallback, manager);
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getScriptingBody() {
        StringBuilder buf = new StringBuilder();
        Javascript js = new Javascript(buf);
        DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);

        js.append("var pane = dijit.byId('" + getManager().getName() + "Panel');");
        js.append("if(pane){");

        js.append("var hasSelected = " + getManager().getControlName() + ".selectedRows.length > 0;");
        js.append("var inSync = " + managerDP.getSyncState().asVar() + ";");
        js.append("var isMaster = " + getManager().getControlName() + "IsMaster;");
        js.append("var synchBur = " + getManager().getControlName() + ".getUserData('','synchBur') == '1';");
        js.append("pane.setButtonState(" + BUTTON_SWITCH_MASTER + ",isMaster);");
        if (_user.isInRole(SecurityConstants.ROLE_SDL_EV) || _user.isInRole(SecurityConstants.ROLE_SSP_EV) || _user.isInRole(SecurityConstants.ROLE_SBA_EV)) {
            js.append("pane.setButtonState(" + BUTTON_SYNCH_BUR + ",!isMaster);");
            js.append("pane.setButtonState(" + BUTTON_GET_ALL_BUR + ",!(isMaster && synchBur));");
            js.append("pane.setButtonState(" + BUTTON_GET_BUR + ",!(isMaster && hasSelected && synchBur));");
        } else {
            js.append("pane.setButtonState(" + BUTTON_SYNCH_BUR + ",true);");
            js.append("pane.setButtonState(" + BUTTON_GET_ALL_BUR + ",true);");
            js.append("pane.setButtonState(" + BUTTON_GET_BUR + ",true);");
        }
        js.append("pane.setButtonState(" + BUTTON_SAVE + ",inSync);");
        js.append("pane.setButtonState(" + BUTTON_UNDO + ",inSync);");

        js.append("}");

        js.append("pane.showButtons();");

        return buf.toString();
    }
}
