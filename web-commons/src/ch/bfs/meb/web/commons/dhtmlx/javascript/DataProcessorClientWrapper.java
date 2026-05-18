/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.DataProcessor;

/**
 * Wraps all client object javascript methods of a dhtmlx table control Adds:
 * Basic type safety, parameter safety
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DataProcessorClientWrapper extends ClientWrapperBase {

    public DataProcessorClientWrapper(IDhtmlxControl control, StringBuilder buf) {

        super(control, buf);
    }

    /**
     * Creates a constructor for the wrapped table manager
     */
    public void construct() {

        // Connect manager and dataprocessor using a command object
        Command command = new Command(CommandConstants.SAVE);
        command.setControl(getControl());

        // Create dataprocessor
        getBuf().append(DataProcessor.getControlName(getControl()));
        getBuf().append("= new dataProcessor(");
        getBuf().append(command);
        getBuf().append(");");
    }

    public MethodCall init() {

        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), CommandConstants.INIT);

        // Set parameters
        call.param(getControl().getControlName());

        // Append
        getBuf().append(call);

        return call;
    }

    public MethodCall setUpdateMode(JSString updateMode) {

        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "setUpdateMode");

        // Set parameters
        call.param(updateMode);

        // Append
        getBuf().append(call);

        return call;
    }

    public MethodCall setTransactionMode(JSString transactionMode) {

        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "setTransactionMode");

        // Set parameters
        call.param(transactionMode);

        // Append
        getBuf().append(call);

        return call;
    }

    public MethodCall enableUTFEncoding() {
        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "enableUTFencoding");
        // Set parameters
        call.param(true);
        // Append
        getBuf().append(call);
        return call;
    }

    /**
     * Defines an action, that is called, when the defined label is returned by
     * the server. Used to handle errors
     * 
     * @param label
     *            The label
     * @param handler
     *            The javascript method to call
     * @return
     */
    public MethodCall defineAction(JSString label, IJavaScriptFunction handler) {

        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "defineAction");

        // Set parameters
        call.param(label).param(handler);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Sends all rows to the server to synchronize
     * 
     * @param command
     *            The server command
     * @return The genereated JS-Call
     */
    public MethodCall synchronize(Command command) {

        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "synchronize");

        command.setControl(getControl());

        // Set parameters
        call.param(command);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Sends an updated row to the server to synchronize
     * 
     * @param command
     *            The server command
     * @param rowId
     *            The row to synchronize
     * @return The genereated JS-Call
     */
    public MethodCall synchronize(Command command, JSNumber rowId) {

        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "synchronize");

        command.setControl(getControl());

        // Set parameters
        call.param(command).param(rowId);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Sets function that is called after a row was updated
     * 
     * @param function
     *            The function to call
     * @return the generated call
     */
    public MethodCall setOnAfterUpdate(IJavaScriptFunction function) {

        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "setOnAfterUpdate");

        // Set parameters
        call.param(function);

        // Append
        getBuf().append(call);

        return call;
    }

    public MethodCall setOnRowMark(IJavaScriptFunction function) {
        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "attachEvent");

        // Set parameters
        call.param(JSString.byVal("onRowMark"));
        call.param(function);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Marks row as updated an forces refresh
     * 
     * @param rowId
     *            Updated row
     * @return the generated call
     */
    public MethodCall setRowUpdated(JSNumber rowId) {
        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "setUpdated");

        // Set parameters
        call.param(rowId).param(true).param(false);

        // Append
        getBuf().append(call);

        return call;
    }

    /**
     * Gets the update state of the table
     * 
     * @return true if all rows are synchrinised with the server
     */
    public JSBoolean getSyncState() {
        // Create call
        MethodCall call = new MethodCall(DataProcessor.getControlName(getControl()), "getSyncState");

        // return as variable
        return JSBoolean.byRef(call.toRef());
    }

}
