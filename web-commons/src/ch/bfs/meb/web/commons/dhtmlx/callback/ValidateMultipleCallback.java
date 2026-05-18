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
import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * Callback for (pre-)validation of multiple selected rows
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ValidateMultipleCallback extends CallbackBase {
    private static final String CONFIRM_PREVALIDATE_DL_MESSAGE = "confirm.prevalidate.dl.message";
    private static final String CONFIRM_PREVALIDATE_MESSAGE = "confirm.prevalidate.message";
    private static final String CONFIRM_VALIDATE_DV_MESSAGE = "confirm.validate.dv.message";
    private static final String CONFIRM_VALIDATE_MESSAGE = "confirm.validate.message";

    private final String _confirmMessage;

    public ValidateMultipleCallback(IDhtmlxManager manager, Long application, boolean prevalidateOnly) {
        super(CallbackConstants.ValidateCallback, manager);
        String role_dv, role_ev;
        if (application.equals(CodegroupUtility.MEB_APPLICATION_SBA)) {
            role_dv = SecurityConstants.ROLE_SBA_DV;
            role_ev = SecurityConstants.ROLE_SBA_EV;
        } else if (application.equals(CodegroupUtility.MEB_APPLICATION_SSP)) {
            role_dv = SecurityConstants.ROLE_SSP_DV;
            role_ev = SecurityConstants.ROLE_SSP_EV;
        } else // SDL
        {
            role_dv = SecurityConstants.ROLE_SDL_DV;
            role_ev = SecurityConstants.ROLE_SDL_EV;
        }
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (prevalidateOnly) {
            if (user.isInRole(role_dv)) {
                _confirmMessage = CONFIRM_PREVALIDATE_MESSAGE;
            } else {
                _confirmMessage = CONFIRM_PREVALIDATE_DL_MESSAGE;
            }
        } else {
            if (user.isInRole(role_ev)) {
                _confirmMessage = CONFIRM_VALIDATE_MESSAGE;
            } else if (user.isInRole(role_dv)) {
                _confirmMessage = CONFIRM_VALIDATE_DV_MESSAGE;
            } else {
                _confirmMessage = CONFIRM_PREVALIDATE_DL_MESSAGE;
            }
        }
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create table wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final DataProcessorClientWrapper callingTableDP = new DataProcessorClientWrapper(getManager(), buf);

        // Javascript wrapper
        final Javascript js = new Javascript(buf);

        js.append("var isConfirmed = false;");
        js.ifc(js.compare(table.getNrSelectedRows(), Javascript.GT, JSNumber.byVal(0))).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.append("isConfirmed = " + js.confirm(new JSString(getManager().getLocalizationManager().getMessage(_confirmMessage))).asVar() + ";");
            }
        });

        js.ifc(JSString.byRef("isConfirmed")).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.incSaveNr();

                // Generate validate command
                Command command = new Command(CommandConstants.VALIDATE);
                command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                callingTableDP.synchronize(command, table.getSelectedId());
            }

        });

        return buf.toString();
    }
}