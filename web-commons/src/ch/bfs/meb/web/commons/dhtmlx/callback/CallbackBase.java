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
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class CallbackBase extends JavaScriptFunctionBase {
    private final IDhtmlxManager _manager;

    public CallbackBase(String methodName, IDhtmlxManager manager) {
        // Add manager name to create a unique method name
        super(manager.getName() + methodName);
        _manager = manager;
    }

    public IDhtmlxManager getManager() {
        return _manager;
    }

    public void alertAndReturnWhenNotSynchronized(final Javascript js, DataProcessorClientWrapper tableDP, final String message) throws DhtmlxException {
        js.ifnotc(tableDP.getSyncState()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Alert
                js.alert(new JSString(getManager().getLocalizationManager().getMessage(message)));
                js.returnc(JSBoolean.isfalse);
            }
        });
    }

    public void returnWhenNotSynchronized(final Javascript js, DataProcessorClientWrapper tableDP) throws DhtmlxException {
        js.ifnotc(tableDP.getSyncState()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Alert
                js.returnc(JSBoolean.isfalse);
            }
        });
    }

    public IJavaScriptFunction getRegisteredCallback(String callbackName) {
        try {
            return getManager().getRegisteredCallback(getManager().getName() + callbackName);
        } catch (DhtmlxException e) {
            // callback not present
            return null;
        }
    }

    public void doEventualRefreshButtons(Javascript js, IDhtmlxControl ctrl) {
        if (getRegisteredCallback(CallbackConstants.RefreshButtonsCallback) != null) {
            js.append(new MethodCall(ctrl.getName() + CallbackConstants.RefreshButtonsCallback).toString());
        }
    }

    public void doEventualDisplayNumbers(Javascript js, IDhtmlxControl ctrl) {
        if (getRegisteredCallback(CallbackConstants.DisplayNumbersCallback) != null) {
            js.append(new MethodCall(ctrl.getName() + CallbackConstants.DisplayNumbersCallback).toString());
        }
    }
}
