/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: ValidateCallback.java 556 2008-10-07 12:55:46Z lsc $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackBase;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackConstants;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * TODO Describe this class
 * 
 * @author $Author: lsc $
 * @version $Revision: 556 $
 */
public class ValidateCallback extends CallbackBase {
    public static String LOAD_PERSON = "loadPerson";

    private static final String CONFIRM_VALIDATE_PERSON_MESSAGE = "confirm.validate.person.message";
    private static final String CONFIRM_VALIDATE_PERSONS_MESSAGE = "confirm.validate.persons.message";

    public ValidateCallback(IDhtmlxManager manager) {
        super(CallbackConstants.ValidateCallback, manager);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        final IWebLocalizationManager locMan = getManager().getLocalizationManager();

        // Create table wrapper
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final DataProcessorClientWrapper callingTableDP = new DataProcessorClientWrapper(getManager(), buf);

        // Javascript wrapper
        final Javascript js = new Javascript(buf);

        js.append("var isConfirmed = false;");
        js.ifc(js.compare(table.getNrSelectedRows(), Javascript.GT, JSNumber.byVal(1))).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.append("var isConfirmed = " + js.confirm(new JSString(locMan.getMessage(CONFIRM_VALIDATE_PERSONS_MESSAGE))).asVar() + ";");
            }
        }).elseifc(js.compare(table.getNrSelectedRows(), Javascript.GT, JSNumber.byVal(0))).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                js.append("var isConfirmed = " + js.confirm(new JSString(locMan.getMessage(CONFIRM_VALIDATE_PERSON_MESSAGE))).asVar() + ";");
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

                //				Command command = new Command(CommandConstants.VALIDATE);
                //				command.setControl(getManager());
                //				command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                //
                //				// Call Validate on PersonTable - 1. pass
                //				js.append("var request = new XMLHttpRequest();");
                //				MethodCall open = new MethodCall("request", "open");
                //				open.param(new JSString("GET")).param(command).param(false);
                //				js.append(open.toString());
                //				js.append("request.send(null);");
                //				js.append("var serverResponse = request.responseText;");
                //
                //				js.ifnotc(JSBoolean.byRef("serverResponse || serverResponse == 'null'")).thenc(new CodeBlock()
                //				{
                //					@Override
                //					public void code(StringBuilder buf) throws DhtmlxException
                //					{
                //						js.append("for(var i=0; i<personTableManager.selectedRows.length; i++) {");
                //
                //						// Load validated person ans update validated persons
                //						// thereby - 2. pass
                //						js.append("personTableManagerProc.updatedRows[personTableManagerProc.updatedRows.length] = personTableManager.selectedRows[i].idd;");// personTableProc.setUpdated(personTable.selectedRows[i].idd,true,false);
                //						Command command = new Command(LOAD_PERSON);
                //						command.param(ParameterConstants.PARAM_ROWID, JSString.byRef("personTableManager.selectedRows[i].idd"));
                //						callingTableDP.synchronize(command);
                //
                //						// One Pass Validation: Set Data, Style and user data
                //						// directly
                //						// Set Data of the validated row
                //						// PersonTableManager personTable = (PersonTableManager)
                //						// getManager();
                //						// Integer statusColumnIndex =
                //						// personTable.getColumnIndexById(PersonTableManager.COLUMN_STATUS_ID);
                //						// js.append("personTable.cells(personTable.selectedRows[i].idd, "
                //						// + statusColumnIndex.toString() + ").setValue(" +
                //						// String.valueOf(CodegroupUtility.SBG_PERSONSTATUS_VALIDATED)
                //						// + ");");
                //						// Integer valUserColumnIndex =
                //						// personTable.getColumnIndexById(PersonTableManager.COLUMN_VALIDUSER_ID);
                //						// js.append("personTable.cells(personTable.selectedRows[i].idd, "
                //						// + valUserColumnIndex.toString() + ").setValue(" + new
                //						// JSString(personTable.getSessionContext().getUserName())
                //						// + ");");
                //						// Integer valDateColumnIndex =
                //						// personTable.getColumnIndexById(PersonTableManager.COLUMN_VALIDDATE_ID);
                //						// js.append("personTable.cells(personTable.selectedRows[i].idd, "
                //						// + valDateColumnIndex.toString() + ").setValue(" + new
                //						// JSString(new SimpleDateFormat
                //						// ("dd.MM.yyyy").format(new Date())) + ");");
                //						// Set style and user data
                //						// js.append("personTable.selectedRows[i].className=personTable.selectedRows[i].className +"
                //						// + new JSString(" " +
                //						// PersonTableManager.ROW_VALID_STYLE) + ";"); s
                //						// Obsolete:
                //						// js.append("personTable.setRowTextStyle(personTable.selectedRows[i].idd,"
                //						// + new JSString("color:green;") + ");");
                //						// // Set User Data of the row
                //						// String readOnlyCells =
                //						// PersonTableManager.READONLYCOLUMNS_VALIDATED_EV;
                //						// if
                //						// (personTable.getSessionContext().getUserRole().equals(CodegroupUtility.SBG_ROLE_DL))
                //						// {
                //						// readOnlyCells =
                //						// PersonTableManager.READONLYCOLUMNS_VALIDATED_DL;
                //						// }
                //						// MethodCall setUserData = new
                //						// MethodCall("personTable", "setUserData");
                //						// setUserData.param(JSString.byRef("personTable.selectedRows[i].idd")).param(new
                //						// JSString("readOnlyCells")).param(new
                //						// JSString(readOnlyCells));
                //						// js.append(setUserData.toString());
                //
                //						js.append("};");
                //					}
                //				}).elsec(new CodeBlock()
                //				{
                //					@Override
                //					public void code(StringBuilder buf) throws DhtmlxException
                //					{
                //						js.alert(JSString.byRef("serverResponse"));
                //					}
                //				});
            }
        });

        return buf.toString();
    }
}
