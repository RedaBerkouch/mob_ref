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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class MasterDetailDelay extends MasterDetailCallbackBase {
    protected final JSNumber _delay;
    protected final IJavaScriptFunction _function;
    protected final JSNumber _timeoutHnd;
    protected final boolean _isMiddleTable;

    public MasterDetailDelay(String methodName, IDhtmlxManager manager, IDhtmlxControl target, IDhtmlxControl target2, boolean isMiddleTable,
            IJavaScriptFunction function, int time, IGlobalJavaScript globals) {
        super(methodName, manager, target, target2, globals);

        _delay = JSNumber.byVal(time);
        _timeoutHnd = JSNumber.byRef(getMethodName() + "TOHnd");
        _function = function;
        _isMiddleTable = isMiddleTable;
    }

    public abstract String initialize(boolean isCallingMaster) throws DhtmlxException;

    @Override
    public String getGlobals() {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        Javascript js = new Javascript(buf);
        js.define(_timeoutHnd);

        return buf.toString();
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();
        final Javascript js = new Javascript(buf);

        // Only call delay when the calling manager is master
        js.ifc(isCallingManagerMaster()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // initialisation code from concrete implementation
                buf.append(initialize(true));

                js.ifc(_timeoutHnd).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        js.clearTimeout(_timeoutHnd);
                    }
                });

                js.assign(_timeoutHnd, js.setTimeout(_function, _delay));
            }
        });

        if (hasThird() && _isMiddleTable) {
            js.elsec(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    // initialisation code from concrete implementation
                    buf.append(initialize(false));

                    js.ifc(_timeoutHnd).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            js.clearTimeout(_timeoutHnd);
                        }
                    });

                    js.assign(_timeoutHnd, js.setTimeout(_function, _delay));
                }
            });
        }

        js.ifnotc(JSString.byRef(getManager().getControlName() + ".onLoading")).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                buf.append(new MethodCall(getManager().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                if (hasThird() && _isMiddleTable) {
                    buf.append(new MethodCall(getOtherTable().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                }
            }
        });

        // Refresh buttons
        doEventualRefreshButtons(js, getManager());

        return buf.toString();
    }
}
