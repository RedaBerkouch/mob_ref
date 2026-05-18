/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSBoolean;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class OnRowSelectCallback extends MasterDetailCallbackBase {
    public static final String CLICK_TABLE_LOCK_MESSAGE_KEY = "click.table.lock.message";

    protected final boolean _isMiddleTable;
    protected final boolean _displayNumbers;

    public OnRowSelectCallback(IDhtmlxManager manager, IDhtmlxControl target, IDhtmlxControl target2, boolean isMiddleTable, IGlobalJavaScript globals,
            boolean displayNumbers) {
        super(CallbackConstants.OnRowSelectCallback, manager, target, target2, globals);
        _isMiddleTable = isMiddleTable;
        _displayNumbers = displayNumbers;

        // add parameters
        addParameter(getId());
    }

    public OnRowSelectCallback(IDhtmlxManager manager, IDhtmlxControl target, IDhtmlxControl target2, boolean isMiddleTable, IGlobalJavaScript globals) {
        this(manager, target, target2, isMiddleTable, globals, false);
    }

    public OnRowSelectCallback(IDhtmlxManager manager, IDhtmlxControl target, IGlobalJavaScript globals) {
        this(manager, target, null, false, globals);
    }

    protected JSNumber getId() {
        return JSNumber.byRef("id");
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper target = new TableClientWrapper(getOtherTable(), buf);
        final TableClientWrapper target2 = hasThird() ? new TableClientWrapper(getThirdTable(), buf) : null;
        final TableClientWrapper table = new TableClientWrapper(getManager(), buf);
        final DataProcessorClientWrapper detailDP = new DataProcessorClientWrapper(getOtherTable(), buf);
        final DataProcessorClientWrapper thirdDP = hasThird() ? new DataProcessorClientWrapper(getThirdTable(), buf) : null;

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        JSString rowStatus = table.getUserData(table.getSelectedId(), JSString.byVal("!nativeeditor_status"));
        js.define(JSBoolean.byRef("notIsNew"), JSBoolean.byRef(rowStatus + "!=\"inserted\""));

        // If master, load detail table
        js.ifc(isCallingManagerMaster()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                alertAndReturnWhenNotSynchronized(js, detailDP, CLICK_TABLE_LOCK_MESSAGE_KEY);
                if (thirdDP != null) {
                    alertAndReturnWhenNotSynchronized(js, thirdDP, CLICK_TABLE_LOCK_MESSAGE_KEY);
                }

                // Update target
                target.clearAll(JSBoolean.isfalse);

                // Create loader with selected row ids
                final Command load = new Command(CommandConstants.LOAD);

                // Map function parameter id with selRowId
                load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                load.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getManager().getName()));

                // load data
                js.ifc(JSBoolean.byRef("notIsNew")).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        target.loadXML(load);
                    }
                });

                if (target2 != null) {
                    // Update target
                    target2.clearAll(JSBoolean.isfalse);

                    if (_isMiddleTable) {
                        // load data
                        js.ifc(JSBoolean.byRef("notIsNew")).thenc(new CodeBlock() {
                            @Override
                            public void code(StringBuilder buf) throws DhtmlxException {
                                target2.loadXML(load);
                            }
                        });
                    } else {
                        if (_displayNumbers) {
                            js.ifnotc(JSString.byRef(getManager().getControlName() + ".onLoading")).thenc(new CodeBlock() {
                                @Override
                                public void code(StringBuilder buf) throws DhtmlxException {
                                    buf.append(new MethodCall(getThirdTable().getName() + CallbackConstants.DisplayNumbersCallback).toString());
                                }
                            });
                        }

                        doEventualRefreshButtons(js, getThirdTable());
                    }
                }
            }
        });

        if (thirdDP != null && _isMiddleTable) {
            js.elsec(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    js.ifc(isOtherManagerMaster()).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            alertAndReturnWhenNotSynchronized(js, thirdDP, CLICK_TABLE_LOCK_MESSAGE_KEY);

                            // Update target
                            target2.clearAll(JSBoolean.isfalse);

                            // Create loader with selected row ids
                            final Command load = new Command(CommandConstants.LOAD);

                            // Map function parameter id with selRowId
                            load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                            load.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getManager().getName()));

                            // load data
                            js.ifc(JSBoolean.byRef("notIsNew")).thenc(new CodeBlock() {
                                @Override
                                public void code(StringBuilder buf) throws DhtmlxException {
                                    target2.loadXML(load);
                                }
                            });
                        }
                    }).elseifc(isThirdManagerMaster()).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            alertAndReturnWhenNotSynchronized(js, detailDP, CLICK_TABLE_LOCK_MESSAGE_KEY);

                            // Update target
                            target.clearAll(JSBoolean.isfalse);

                            // Create loader with selected row ids
                            final Command load = new Command(CommandConstants.LOAD);

                            // Map function parameter id with selRowId
                            load.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, table.getSelectedId());
                            load.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getManager().getName()));

                            // load data
                            js.ifc(JSBoolean.byRef("notIsNew")).thenc(new CodeBlock() {
                                @Override
                                public void code(StringBuilder buf) throws DhtmlxException {
                                    target.loadXML(load);
                                }
                            });
                        }
                    });
                }
            });
        }

        return buf.toString();
    }
}
