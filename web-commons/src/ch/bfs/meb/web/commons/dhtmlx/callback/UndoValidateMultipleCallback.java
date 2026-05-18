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
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Callback for undo of (pre-)validation of multiple selected rows
 * 
 * @author $Author$
 * @version $Revision$
 */
public class UndoValidateMultipleCallback extends CallbackBase {
    private static final String CONFIRM_UNDO_PREVALIDATE_MESSAGE = "confirm.undo.prevalidate.message";
    private static final String CONFIRM_UNDO_VALIDATE_MESSAGE = "confirm.undo.validate.message";

    private final String _stateColumnId;

    public UndoValidateMultipleCallback(IDhtmlxManager manager, String stateColumnId) {
        super(CallbackConstants.UndoValidateCallback, manager);
        _stateColumnId = stateColumnId;
    }

    public UndoValidateMultipleCallback(IDhtmlxManager manager) {
        this(manager, null);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create table wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final DataProcessorClientWrapper callingTableDP = new DataProcessorClientWrapper(getManager(), buf);

        // Javascript wrapper
        final Javascript js = new Javascript(buf);

        final IWebLocalizationManager localization = getManager().getLocalizationManager();

        final JSBoolean isConfirmed = JSBoolean.byRef("isConfirmed");
        js.define(isConfirmed, JSBoolean.isfalse);
        js.ifc(js.compare(table.getNrSelectedRows(), Javascript.GT, JSNumber.byVal(0))).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                JSString confirmMessage = JSString.byRef("confirmMessage");
                js.define(confirmMessage, new JSString(localization.getMessage(CONFIRM_UNDO_PREVALIDATE_MESSAGE)));
                if (_stateColumnId != null) {
                    Integer stateColumnIndex = ((TableManagerBase) getManager()).getColumnIndexById(_stateColumnId);
                    js.append("var firstSelectedRow=" + table.getSelectedId() + ".split(',')[0];");
                    js.append("var state=" + getManager().getControlName() + ".cells(firstSelectedRow," + stateColumnIndex + ").getValue();");
                    js.append("if(state==" + CodegroupUtility.MEB_DATASTATUS_VALIDATED + "){");
                    js.assign(confirmMessage, new JSString(localization.getMessage(CONFIRM_UNDO_VALIDATE_MESSAGE)));
                    js.append("}");
                }

                js.assign(isConfirmed, js.confirm(confirmMessage));
            }
        });

        js.ifc(isConfirmed).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.incSaveNr();

                // Generate command
                Command command = new Command(CommandConstants.UNDO_VALIDATE);
                command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                callingTableDP.synchronize(command, table.getSelectedId());
            }

        });

        return buf.toString();
    }
}