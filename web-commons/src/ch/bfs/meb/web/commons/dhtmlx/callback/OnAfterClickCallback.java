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
import ch.bfs.meb.web.commons.dhtmlx.javascript.CodeBlock;
import ch.bfs.meb.web.commons.dhtmlx.javascript.DataProcessorClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IGlobalJavaScript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.Javascript;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnAfterClickCallback extends MasterDetailCallbackBase {
    public static final String CLICK_TABLE_LOCK_MESSAGE_KEY = "click.table.lock.message";

    protected final boolean _isMiddleTable;

    public OnAfterClickCallback(IDhtmlxManager manager, IDhtmlxControl targetManager, IDhtmlxControl target2Manager, boolean isMiddleTable,
            IGlobalJavaScript globals) {
        super(CallbackConstants.OnAfterClickCallback, manager, targetManager, target2Manager, globals);
        _isMiddleTable = isMiddleTable;
    }

    public OnAfterClickCallback(IDhtmlxManager manager, IDhtmlxControl targetManager, IGlobalJavaScript globals) {
        this(manager, targetManager, null, false, globals);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final DataProcessorClientWrapper targetDP = new DataProcessorClientWrapper(getOtherTable(), buf);
        final DataProcessorClientWrapper target2DP = hasThird() ? new DataProcessorClientWrapper(getThirdTable(), buf) : null;

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        js.ifc(isCallingManagerMaster()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                alertAndReturnWhenNotSynchronized(js, targetDP, CLICK_TABLE_LOCK_MESSAGE_KEY);

                if (hasThird()) {
                    alertAndReturnWhenNotSynchronized(js, target2DP, CLICK_TABLE_LOCK_MESSAGE_KEY);
                }
            }
        });

        if (_isMiddleTable) {
            js.elsec(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    js.ifc(isOtherManagerMaster()).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            alertAndReturnWhenNotSynchronized(js, target2DP, CLICK_TABLE_LOCK_MESSAGE_KEY);
                        }
                    }).elseifc(isThirdManagerMaster()).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            alertAndReturnWhenNotSynchronized(js, targetDP, CLICK_TABLE_LOCK_MESSAGE_KEY);
                        }
                    });
                }
            });
        }

        js.returnc(JSBoolean.istrue);

        return buf.toString();
    }
}
