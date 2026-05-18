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
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Javascript function to delete a delivery
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DeleteDeliveryCallback extends SimpleDeliveryButtonCallback {
    private static final String CONFIRM_DELETE_DELIVERY_MESSAGE = "confirm.delete.delivery.message";
    private static final String CONFIRM_DELETE_PERMANENT_DELIVERY_MESSAGE = "confirm.delete.permanent.delivery.message";

    private final Integer _stateColumnIndex;
    private final MebUser _user;
    final String _role_ea;

    public DeleteDeliveryCallback(IDhtmlxManager manager, String stateColumnId, Long application) {
        super(manager, CallbackConstants.DeleteCallback, CommandConstants.DELETE, true);

        _stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(stateColumnId);
        _user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (application.equals(CodegroupUtility.MEB_APPLICATION_SBA)) {
            _role_ea = SecurityConstants.ROLE_SBA_EA;
        } else if (application.equals(CodegroupUtility.MEB_APPLICATION_SSP)) {
            _role_ea = SecurityConstants.ROLE_SSP_EA;
        } else if (application.equals(CodegroupUtility.MEB_APPLICATION_SBG)) {
            _role_ea = SecurityConstants.ROLE_SBG_EA;
        } else // SDL
        {
            _role_ea = SecurityConstants.ROLE_SDL_EA;
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
                JSString confirmMessage = JSString.byRef("confirmMessage");
                js.define(confirmMessage, new JSString(localization.getMessage(CONFIRM_DELETE_DELIVERY_MESSAGE)));
                if (_user.isInRole(_role_ea)) {
                    js.append("var state=" + getManager().getControlName() + ".cells(selectedRow," + _stateColumnIndex.toString() + ").getValue();");
                    js.append("if(state==" + CodegroupUtility.MEB_DELIVERYSTATUS_INITIALIZED + "){");
                    js.assign(confirmMessage, new JSString(localization.getMessage(CONFIRM_DELETE_PERMANENT_DELIVERY_MESSAGE)));
                    js.append("}");
                }
                js.ifc(js.confirm(confirmMessage)).thenc(new CodeBlock() {
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
