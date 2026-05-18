/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;

/**
 * Callback with master detail functionality
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class MasterDetailCallbackBase extends CallbackBase {

    public final static String MASTER_SUFFIX = "IsMaster";

    protected final IGlobalJavaScript _globals;

    private final IDhtmlxControl _otherTable;
    private final IDhtmlxControl _thirdTable;

    /**
     * Creates a new callback
     * 
     * @param methodName
     *            javascript method name
     * @param manager
     *            calling manager
     * @param otherManagerName
     *            related managers name
     * @param globals
     *            Global javascript definitions
     */
    public MasterDetailCallbackBase(String methodName, IDhtmlxManager manager, IDhtmlxControl otherTable, IDhtmlxControl thirdTable,
            IGlobalJavaScript globals) {
        super(methodName, manager);

        _otherTable = otherTable;
        _thirdTable = thirdTable;
        _globals = globals;
    }

    /**
     * Returns the other manager
     * 
     * @return the other manager
     */
    public IDhtmlxControl getOtherTable() {
        return _otherTable;
    }

    /**
     * Returns the third manager
     * 
     * @return the other manager or null, if no third manager is attached
     */
    public IDhtmlxControl getThirdTable() {
        return _thirdTable;
    }

    /**
     * Returns a javascript boolean reference of the calling managers master
     * state
     * 
     * @return A Javascript boolean with the reference
     * @throws DhtmlxException
     *             thrown when the global definition not available
     */
    public JSBoolean isCallingManagerMaster() throws DhtmlxException {
        JSBoolean isMaster = (JSBoolean) _globals.getGlobal(getManager().getControlName() + MASTER_SUFFIX);
        if (isMaster == null) {
            throw new DhtmlxException("Global " + getManager().getControlName() + MASTER_SUFFIX + "not defined");
        }

        return isMaster;
    }

    /**
     * Returns a javascript boolean reference of the other managers master state
     * 
     * @return A Javascript boolean with the reference
     * @throws DhtmlxException
     *             thrown when the global definition not available
     */
    public JSBoolean isOtherManagerMaster() throws DhtmlxException {

        IDhtmlxControl otherTable = getOtherTable();

        JSBoolean isMaster = (JSBoolean) _globals.getGlobal(otherTable.getControlName() + MASTER_SUFFIX);
        if (isMaster == null) {
            throw new DhtmlxException("Global " + otherTable.getControlName() + MASTER_SUFFIX + "not defined");
        }

        return isMaster;
    }

    /**
     * Returns a javascript boolean reference of the third managers master state
     * 
     * @return A Javascript boolean with the reference
     * @throws DhtmlxException
     *             thrown when the global definition not available
     */
    public JSBoolean isThirdManagerMaster() throws DhtmlxException {
        if (hasThird()) {
            IDhtmlxControl thirdTable = getThirdTable();

            JSBoolean isMaster = (JSBoolean) _globals.getGlobal(thirdTable.getControlName() + MASTER_SUFFIX);
            if (isMaster == null) {
                throw new DhtmlxException("Global " + thirdTable.getControlName() + MASTER_SUFFIX + "not defined");
            }

            return isMaster;
        } else {
            return new JSBoolean(false);
        }
    }

    public boolean hasOther() {
        return _otherTable != null;
    }

    public boolean hasThird() {
        return _thirdTable != null;
    }

    public boolean hasGlobals() {
        return _globals != null;
    }
}
