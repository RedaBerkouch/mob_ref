/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.util.CodegroupUtility;
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
 * Callback for undo of (pre-)validation of a delivery.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class UndoValidateDeliveryCallback extends SimpleDeliveryButtonCallback {
    private static final String CONFIRM_UNDO_PREVALIDATE_MESSAGE = "confirm.undo.prevalidate.message";
    private static final String CONFIRM_UNDO_VALIDATE_MESSAGE = "confirm.undo.validate.message";
    private static final String DELIVERY_WRONG_STATE_MESSAGE = "delivery.wrong.state.message";

    private final String _stateColumnId;

    public UndoValidateDeliveryCallback(IDhtmlxManager manager, String stateColumnId) {
        super(manager, CallbackConstants.UndoValidateCallback, CommandConstants.UNDO_VALIDATE, true);
        _stateColumnId = stateColumnId;
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
                Integer stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_stateColumnId);
                JSString confirmMessage = JSString.byRef("confirmMessage");
                js.define(confirmMessage, new JSString(localization.getMessage(DELIVERY_WRONG_STATE_MESSAGE)));
                js.append("var state=" + getManager().getControlName() + ".cells(selectedRow," + stateColumnIndex + ").getValue();");
                js.append("if(state==" + CodegroupUtility.MEB_DATASTATUS_PREVALIDATED+ "){");
                js.assign(confirmMessage, new JSString(localization.getMessage(CONFIRM_UNDO_PREVALIDATE_MESSAGE)));
                js.append("}else if(state==" + CodegroupUtility.MEB_DELIVERYSTATUS_VALIDATED + "){");
                js.assign(confirmMessage, new JSString(localization.getMessage(CONFIRM_UNDO_VALIDATE_MESSAGE)));
                js.append("}else{");
                js.alert(confirmMessage);
                js.returnc();
                js.append("}");
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
