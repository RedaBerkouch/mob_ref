/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import java.util.ArrayList;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.MethodCall;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.*;
import ch.bfs.meb.web.commons.dhtmlx.table.Column;
import ch.bfs.meb.web.commons.dhtmlx.table.TableManagerBase;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnEditCellCallback extends CallbackBase {
    protected final JSNumber stage = JSNumber.byRef("stage");
    protected final JSNumber rowId = JSNumber.byRef("rowId");
    protected final JSNumber cellIndex = JSNumber.byRef("cellIndex");
    protected final JSString newVal = JSString.byRef("newVal");
    protected final JSString oldVal = JSString.byRef("oldVal");

    protected String _javascriptMethodName = null;

    private static final JSNumber EDITABLE = JSNumber.byVal(0);
    private static final JSNumber EDITWHENINSERTED = JSNumber.byVal(1);
    private static final JSNumber READONLY = JSNumber.byVal(2);

    public OnEditCellCallback(IDhtmlxManager manager) {
        super(CallbackConstants.OnEditCellCallback, manager);

        // add parameters
        // stage of editting (0-before start[can be canceled if returns
        // false],1-editor opened,2-editor closed)
        addParameter(stage);
        // ID or row
        addParameter(rowId);
        // index of cell
        addParameter(cellIndex);
        // new value ( only for stage 2 )
        addParameter(newVal);
        // old value ( only for stage 2 )
        addParameter(oldVal);
    }

    public OnEditCellCallback(IDhtmlxManager manager, String javascriptMethodName) {
        this(manager);

        _javascriptMethodName = javascriptMethodName;
    }

    private JSArray getEditTypeArrayEditable() {
        TableManagerBase tableManager = (TableManagerBase) getManager();
        ArrayList<Column> columns = tableManager.getColumns();

        ArrayList<String> editableArray = new ArrayList<String>();

        for (Column col : columns) {
            if (col.isVisible()) {
                switch (col.getEditType()) {
                case editable:
                    editableArray.add(EDITABLE.asVal());
                    break;
                case editwheninserted:
                    editableArray.add(EDITWHENINSERTED.asVal());
                    break;
                case readonly:
                    editableArray.add(READONLY.asVal());
                    break;

                default:
                    break;
                }
            }
        }
        return new JSArray(editableArray);
    }

    /**
     * Generates the JavaScript code which checks: - whether the selected Column
     * Number is part of the arrEditable -> return true to allow editing -
     * whether the selected Column Number is contained in the
     * arrEditWhenInserted and the row has the state inserted -> return true to
     * allow editing
     * 
     * @param arrEditable
     *            The Array which contains the Velues
     * @param colNr
     *            The name of the Method to get the selected column in
     *            JavaScript
     * @param rowStatus
     *            The name of the Method to get the Rowstatus in JavaScript
     * @param roCells
     *            String with the cells that are dynamically set to read-only
     * @return Returns the Javascript Object. The generated Code is appended to
     *         the existing JSCode in this object
     */
    private void checkColumnEditMode(Javascript js, IJSType arrEditable, IJSType colNr, JSString rowStatus, JSString roCells) {
        String code = "if (stage==0) {" + "if (this.cellType[cellIndex]=='" + Column.EDITOR.PLAUSIERROR + "') {return true;}" + "var arrayEd = " + arrEditable
                + ";" + "if ((arrayEd[" + colNr + "]==" + EDITABLE.asVal() + ")||((arrayEd[" + colNr + "]==" + EDITWHENINSERTED.asVal() + ")&&(" + rowStatus
                + "==\"inserted\"))){" + "var roCells=" + roCells + ";" + "return !(roCells && roCells.charAt(" + colNr + ")=='1');" + "}" + "return false;"
                + "}";
        js.append(code);
    }

    private void multiLineEdit(Javascript js, TableClientWrapper table, DataProcessorClientWrapper dataProcessor) {
        JSString roCells = table.getUserData(JSNumber.byRef("this.selectedRows[i].idd"), JSString.byVal("readOnlyCells"));

        String code = "if (stage==2) {" + "if (this.selectedRows.length>1 && newVal!=oldVal) {" + "for (var i=0; i<this.selectedRows.length; i++) {"
                + "if (this.selectedRows[i].idd!=rowId) {" + "if (this.cells(this.selectedRows[i].idd,cellIndex).getValue()!=newVal) {" + "var roCells="
                + roCells + ";" + "if (!(roCells && roCells.charAt(cellIndex)=='1')) {" + "this.cells(this.selectedRows[i].idd,cellIndex).setValue(newVal);"
                + "this.cells(this.selectedRows[i].idd,cellIndex).cell.wasChanged=true;";
        js.append(code);
        dataProcessor.setRowUpdated(JSNumber.byRef("this.selectedRows[i].idd"));
        code = "}" + "}" + "}" + "}" + "}" + "}";
        js.append(code);
    }

    public String getScriptingBody() throws DhtmlxException {

        StringBuilder buf = new StringBuilder();

        // Create wrapper
        TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        DataProcessorClientWrapper client = new DataProcessorClientWrapper(getManager(), buf);

        // Javascript wrapper (Sprache)
        Javascript js = new Javascript(buf);

        if (_javascriptMethodName != null) {
            MethodCall execMethod = new MethodCall(_javascriptMethodName);
            execMethod.param("this");
            execMethod.param("stage");
            execMethod.param("rowId");
            execMethod.param("cellIndex");
            js.append(execMethod.toString());
        }

        // 1. array sammeln aller columns, welche editwheninserted und editable
        // sind
        JSArray editArray = getEditTypeArrayEditable();

        // 2. in js Code einbauen
        JSString rowStatus = table.getUserData(rowId, JSString.byVal("!nativeeditor_status"));
        JSString roCells = table.getUserData(rowId, JSString.byVal("readOnlyCells"));
        checkColumnEditMode(js, editArray, cellIndex, rowStatus, roCells);

        multiLineEdit(js, table, client);

        js.returnc(JSBoolean.istrue);
        return buf.toString();
    }
}
