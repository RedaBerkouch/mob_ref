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

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnRowSelectDelay extends MasterDetailDelay {
    /**
     * @param method
     * @param function
     * @param time
     */
    public OnRowSelectDelay(IDhtmlxManager manager, IDhtmlxControl target, IDhtmlxControl target2, boolean isMiddleTable, IJavaScriptFunction function,
            int time, IGlobalJavaScript globals) {
        super(CallbackConstants.OnRowSelectDelay, manager, target, target2, isMiddleTable, function, time, globals);
    }

    public OnRowSelectDelay(IDhtmlxManager manager, IDhtmlxControl target, IJavaScriptFunction function, int time, IGlobalJavaScript globals) {
        this(manager, target, null, false, function, time, globals);
    }

    @Override
    public String initialize(boolean isCallingMaster) throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        if (hasThird()) {
            if (_isMiddleTable) {
                // Create wrapper
                final TableClientWrapper target = new TableClientWrapper(getOtherTable(), buf);
                final TableClientWrapper target2 = new TableClientWrapper(getThirdTable(), buf);

                if (isCallingMaster) {
                    // Update target
                    target.clearAll(JSBoolean.isfalse);
                    target2.clearAll(JSBoolean.isfalse);
                } else {
                    final Javascript js = new Javascript(buf);
                    js.ifc(isOtherManagerMaster()).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            target2.clearAll(JSBoolean.isfalse);
                        }
                    }).elsec(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            target.clearAll(JSBoolean.isfalse);
                        }
                    });
                }

                return buf.toString();
            } else {
                return "";
            }
        } else {
            // Create wrapper
            TableClientWrapper target = new TableClientWrapper(getOtherTable(), buf);

            // Update target
            target.clearAll(JSBoolean.isfalse);

            return buf.toString();
        }
    }

}
