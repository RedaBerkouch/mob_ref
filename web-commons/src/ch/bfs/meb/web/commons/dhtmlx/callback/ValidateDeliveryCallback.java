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
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Callback for the (pre-)validation of a delivery.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ValidateDeliveryCallback extends SimpleDeliveryButtonCallback {
    private static final String CONFIRM_PREVALIDATE_DL_MESSAGE = "confirm.prevalidate.dl.message";
    private static final String CONFIRM_VALIDATE_DV_MESSAGE = "confirm.validate.dv.message";
    private static final String CONFIRM_VALIDATE_MESSAGE = "confirm.validate.message";

    private final String _confirmMessage;

    public ValidateDeliveryCallback(IDhtmlxManager manager, Long application) {
        super(manager, CallbackConstants.ValidateCallback, CommandConstants.VALIDATE, true);
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
        if (user.isInRole(role_ev)) {
            _confirmMessage = CONFIRM_VALIDATE_MESSAGE;
        } else if (user.isInRole(role_dv)) {
            _confirmMessage = CONFIRM_VALIDATE_DV_MESSAGE;
        } else {
            _confirmMessage = CONFIRM_PREVALIDATE_DL_MESSAGE;
        }
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        // Javascript wrapper
        final Javascript js = new Javascript(buf);

        final IWebLocalizationManager localization = getManager().getLocalizationManager();

        final JSNumber selectedRow = JSNumber.byRef("selectedRow");
        js.define(selectedRow, table.getSelectedId());

        js.ifnotc(selectedRow).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Error message
                js.alert(new JSString(localization.getMessage(NO_DELIVERY_SELECTED_MESSAGE)));
            }
        }).elsec(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                String confirmText = getManager().getLocalizationManager().getMessage(_confirmMessage);
                js.ifc(js.confirm(new JSString(confirmText))).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        synchCommand(js, selectedRow);
                    }
                });
            }
        });

        return buf.toString();
    }
}
