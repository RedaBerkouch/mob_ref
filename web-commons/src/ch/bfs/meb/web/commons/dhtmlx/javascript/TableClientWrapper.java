/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxManagerBase;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.callback.CallbackConstants;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSArray;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * Wraps all client object javascript methods of a dhtmlx table control Adds:
 * Basic type safety, parameter safety
 * 
 * @author $Author$
 * @version $Revision$
 */
public class TableClientWrapper extends ClientWrapperBase {

    public TableClientWrapper(IDhtmlxControl control, StringBuilder buf) {

        super(control, buf);
    }

    public TableClientWrapper(IDhtmlxControl control) {

        super(control, new StringBuilder());
    }

    /**
     * Creates a constructor for the wrapped table manager
     */
    public void construct() {

        getBuf().append(getControl().getControlName());
        getBuf().append("= new dhtmlXGridObject(");
        getBuf().append('"');
        getBuf().append(getControl().getName());
        getBuf().append('"');
        getBuf().append(");");
    }

    /**
     * Configures XML serialisation
     * 
     * @param userData
     *            enable/disable user data serialization
     * @param fullXML
     *            enable/disable full XML serialization (selection state)
     */
    public MethodCall setSerializationLevel(JSBoolean userData, JSBoolean fullXML) {

        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setSerializationLevel");

        // Set parameters
        call.param(userData).param(fullXML);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Configures XML serialisation
     * 
     * @param userData
     *            enable/disable user data serialization
     * @param fullXML
     *            enable/disable full XML serialization (selection state)
     * @param config
     *            serialize grid configuration
     * @param changedAttr
     *            include changed attribute
     * @param onlyChanged
     *            nclude only Changed rows in result XML
     * @param asCDATA
     *            output cell values as CDATA sections (prevent invalid XML)
     */
    public MethodCall setSerializationLevel(JSBoolean userData, JSBoolean fullXML, JSBoolean config, JSBoolean changedAttr, JSBoolean onlyChanged,
            JSBoolean asCDATA) {

        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setSerializationLevel");

        // Set parameters
        call.param(userData).param(fullXML).param(config).param(changedAttr).param(onlyChanged).param(asCDATA);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * returns actual xml from the table
     */
    public JSString serialize() {

        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "serialize");

        // return as variable
        return JSString.byRef(call.toRef());
    }

    /**
     * Deletes contents of the table
     * 
     * @param header
     *            deletes header if true
     */
    public MethodCall clearAll(JSBoolean header) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "clearAll");

        // Set parameters
        call.param(header);

        // Append
        getBuf().append(call);

        return call;
    }

    public MethodCall loadXML(Command command) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "loadXML");

        // Set manager
        command.setControl(getControl());

        // Set parameters
        call.param(command);

        // Append
        getBuf().append(call);

        return call;
    }

    public MethodCall loadXML(Command command, JSString data) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "loadXML");

        // Set manager
        command.setControl(getControl());

        // Set parameters
        call.param(command).param(JSBoolean.isfalse).param(data);

        // Append
        getBuf().append(call);

        return call;
    }

    public MethodCall loadXML(Command command, JSBoolean post, JSString data, JSString data2) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "loadXML");

        // Set manager
        command.setControl(getControl());

        // Set parameters
        call.param(command).param(post).param(data).param(data2);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Sets the enablement of the context menu.
     * 
     * @param manager
     *            the manager who represents the menu.
     * @return the generated MethodeCall
     */
    public MethodCall setEnableContextMenu(DhtmlxManagerBase manager) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "enableContextMenu");

        // Set parameters
        call.param(manager.getControlName());

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * set path for grid internal images (sort direction, any images used in
     * editors, checkbox, radiobutton)
     * 
     * @param path
     *            path to images folder with closing "/"
     */
    public MethodCall setImagePath(JSString path) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setImagePath");

        // Set parameters
        call.param(path);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * set format for date display
     * 
     * @param format
     *            format string, according to dhtmlx formatting style
     */
    public MethodCall setDateFormat(JSString dateFormat) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setDateFormat");

        // Set parameters
        call.param(dateFormat);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * enables the possibility to load content from server when already loaded
     * content was rendered. Using this you decrease the grid loading time for
     * extremely big amounts of data.
     * 
     * @param command
     *            server command
     * @param bufferSize
     */
    public MethodCall setXMLAutoLoading(Command command, JSNumber bufferSize) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "enableSmartRendering");
        call.param(true).param(bufferSize);

        // Map url
        //command.setControl(getControl());

        // Set parameters
        //call.param(command).param(bufferSize);

        // Append
        getBuf().append(call);

        return call;
    }

    public void setResizeGrid(StringBuilder buf) {
        buf.append("dojo.addOnLoad(function(){").append("dijit.byId('").append(getControl().getName()).append("Panel').resizeGrid=")
                .append(getControl().getControlName()).append(";});");
    }

    /**
     * Sets the (global) error handler for LoadXML errors.
     * 
     */
    public void setLoadErrorHandler() {
        // Create call
        MethodCall call = new MethodCall("dhtmlxError", "catchError");

        // Set parameters
        call.param(JSString.byVal("LoadXML")).param(getControl().getName() + CallbackConstants.LoadErrorCallback);

        // Append
        getBuf().append(call);
    }

    /**
     * Returns the selected row id
     */
    public JSNumber getSelectedId() {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "getSelectedId");

        // Set parameters

        // return as variable
        return JSNumber.byRef(call.toRef());
    }

    /**
     * Returns the number of selected rows
     */
    public JSNumber getNrSelectedRows() {
        // return as variable
        return JSNumber.byRef(getControl().getControlName() + ".selectedRows.length");
    }

    /**
     * Returns the row index by rowId
     * 
     * @param rowId
     *            the row id
     */
    public JSNumber getRowIndex(JSNumber rowId) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "getRowIndex");

        // Set parameters
        call.param(rowId);

        // return as variable
        return JSNumber.byRef(call.toRef());
    }

    /**
     * Adds a new row to the table
     * 
     * @param newId
     *            id for new row
     * @param defaults
     *            Array of values or String(with delimiter as in delimiter
     *            parameter)
     * @param position
     *            index of row (0 by default)
     */
    public MethodCall addRow(JSNumber newId, JSString defaults, JSNumber position) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "addRow");

        // Set parameters
        call.param(newId).param(defaults).param(position);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Duplicates the selected record
     *
     * @param newId
     *            id for new row
     */
    public MethodCall duplicateRecord(JSNumber newId) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "duplicateRecord");

        // Set parameters
        call.param(newId);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Sets the selected row
     * 
     * @param rowId
     *            The row to select
     */
    public MethodCall setSelectedRow(JSNumber rowId) {
        getBuf().append("window.setTimeout('" + getControl().getControlName() + ".setSelectedRow('+" + rowId.asVar() + "+',null,null,true)',260);");
        return null;
        //		// Create call
        //		MethodCall call = new MethodCall(getControl().getControlName(), "setSelectedRow");
        //
        //		// Set parameters
        //		call.param(rowId).param("null").param("null").param(new JSBoolean(true).asVar());
        //
        //		// Append
        //		getBuf().append(call);
        //
        //		return call;
    }

    /**
     * Deletes selected row(s)
     */
    public MethodCall deleteSelectedItem() {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "deleteSelectedItem");

        // Set parameters

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Set style of a single cell
     * 
     * @param rowId
     *            row id
     * @param index
     *            cell index
     * @param style
     *            style string in common format (exmpl: "color:red;border:1px
     *            solid gray;")
     */
    public MethodCall setCellTextStyle(JSNumber rowId, JSNumber index, JSString style) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setCellTextStyle");

        // Set parameters
        call.param(rowId);
        call.param(index);
        call.param(style);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Set row selection mode
     * 
     * @param multi
     *            false: select one row, true: select multiple rows wits
     *            shft/ctrl
     */
    public MethodCall setMultiselect(JSBoolean multi) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setMultiselect");

        // Set parameters
        call.param(multi);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Sets the tables skin
     * 
     * @param skin
     *            to set (sbg, xp, gray) as reference
     */
    public MethodCall setSkin(JSString skin) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setSkin");

        // Set parameters
        call.param(skin);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Installs event handler
     * 
     * @param function
     *            the javascript handler
     * @param event
     *            the event
     * @return the generated javascript call
     */
    public MethodCall attachEvent(IJavaScriptFunction function, String event) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "attachEvent");
        // Set parameters
        call.param(event).param(function);
        // Append
        getBuf().append(call);
        return call;
    }

    /**
     * Installs handler that reacts on before row selection events
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnBeforeSelectHandler(IJavaScriptFunction function) {
        return attachEvent(function, "onBeforeSelect");
    }

    /**
     * Installs handler that reacts on row selection events
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnRowSelectHandler(IJavaScriptFunction function) {
        return attachEvent(function, "onRowSelect");
    }

    /**
     * Installs handler that reacts on change of row selection state
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnSelectStateChangedHandler(IJavaScriptFunction function) {
        return attachEvent(function, "onSelectStateChanged");
    }

    /**
     * Installs handler that reacts on adding or deleting a row
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnGridReconstructedHandler(IJavaScriptFunction function) {
        return attachEvent(function, "onGridReconstructed");
    }

    /**
     * Installs handler that reacts on edit cell events
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnEditCellHandler(IJavaScriptFunction function) {
        return attachEvent(function, "onEditCell");
    }

    /**
     * Gets userdata of a row
     * 
     * @param rowId
     *            The row id
     * @param name
     *            name of te userdata record
     * @return String reference
     */
    public JSString getUserData(JSNumber rowId, JSString name) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "getUserData");

        // Set parameters
        call.param(rowId).param(name);

        // return as variable
        return JSString.byRef(call.toRef());
    }

    /**
     */
    public JSString getSelectedCellIndex() {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "getSelectedCellIndex");

        // return as variable
        return JSString.byRef(call.toRef());
    }

    /**
     * Installs handler, that is called before xml loading starts
     * 
     * @param function	the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnLoadingStart(IJavaScriptFunction function) {
        return attachEvent(function, "onXLS");
    }

    /**
     * Installs handler, that is called after xml loading has ended
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnLoadingEnd(IJavaScriptFunction function) {
        return attachEvent(function, "onXLE");
    }

    /**
     * Installs handler, that is called before the selection of a row is changed
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnBeforeSelect(IJavaScriptFunction function) {
        return attachEvent(function, "onBeforeSelect");
    }

    /**
     * Installs handler, that is called a click into the control
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnAfterClick(IJavaScriptFunction function) {
        return attachEvent(function, "onAfterClick");
    }

    /**
     * Installs handler, that is called when a column is sorted
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnColumnSort(IJavaScriptFunction function) {
        return attachEvent(function, "onBeforeSorting");
    }

    /**
     * Installs handler, that is called when the value of a cell has changed
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setOnCellChanged(IJavaScriptFunction function) {
        return attachEvent(function, "onCellChanged");
    }

    /**
     * Installs handler, that is called when a column is sorted
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public MethodCall setSortImgState(JSString state, JSNumber ind, JSString direction) {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "setSortImgState");

        // Set parameters
        call.param(state).param(ind).param(direction);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Installs handler, that is called when a column is sorted
     * 
     * @param function
     *            the javascript handler
     * @return the generated javascript call
     */
    public JSArray getSortingState() {
        // Create call
        MethodCall call = new MethodCall(getControl().getControlName(), "getSortingState");

        // Set parameters

        // return as variable
        return JSArray.byRef(call.toRef());
    }

}
