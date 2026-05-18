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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSNumber;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class SaveCallback extends MasterDetailCallbackBase {
    protected final boolean _isMiddleTable;
    protected final boolean _isNoPlausiPossible;

    public SaveCallback(IDhtmlxManager manager, IDhtmlxControl relatedManager, IDhtmlxControl relatedManager2, boolean isMiddleTable,
            boolean isNoPlausiPossible, IGlobalJavaScript globals) {
        super(CallbackConstants.SaveCallback, manager, relatedManager, relatedManager2, globals);

        _isMiddleTable = isMiddleTable;
        _isNoPlausiPossible = isNoPlausiPossible;
    }

    public SaveCallback(IDhtmlxManager manager, IDhtmlxControl relatedManager, boolean isNoPlausiPossible, IGlobalJavaScript globals) {
        this(manager, relatedManager, null, false, isNoPlausiPossible, globals);
    }

    public SaveCallback(IDhtmlxManager manager, IDhtmlxControl relatedManager, IDhtmlxControl relatedManager2, boolean isMiddleTable,
            IGlobalJavaScript globals) {
        this(manager, relatedManager, null, isMiddleTable, false, globals);
    }

    public SaveCallback(IDhtmlxManager manager, IDhtmlxControl relatedManager, IGlobalJavaScript globals) {
        this(manager, relatedManager, null, false, false, globals);
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final DataProcessorClientWrapper callingTableDP = new DataProcessorClientWrapper(getManager(), buf);
        final TableClientWrapper otherTable = new TableClientWrapper(getOtherTable(), buf);

        final Javascript js = new Javascript(buf);

        js.ifc(isCallingManagerMaster()).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // Generate save command
                js.incSaveNr();
                Command command = new Command(CommandConstants.SAVE);
                if (_isNoPlausiPossible) {
                    command.param(ParameterConstants.PARAM_NO_PLAUSI, JSNumber.byRef("document.getElementById(\"idNoPlausi\").value"));
                }
                callingTableDP.synchronize(command);

            }
        }).elsec(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                if (hasThird() && _isMiddleTable) {
                    js.ifc(isOtherManagerMaster()).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            // Generate save command
                            js.incSaveNr();
                            Command command = new Command(CommandConstants.SAVE);
                            command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, otherTable.getSelectedId());
                            command.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getOtherTable().getName()));
                            if (_isNoPlausiPossible) {
                                command.param(ParameterConstants.PARAM_NO_PLAUSI, JSNumber.byRef("document.getElementById(\"idNoPlausi\").value"));
                            }
                            callingTableDP.synchronize(command);
                        }
                    }).elsec(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            TableClientWrapper thirdTable = new TableClientWrapper(getThirdTable(), buf);

                            // Generate save command
                            js.incSaveNr();
                            Command command = new Command(CommandConstants.SAVE);
                            command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, thirdTable.getSelectedId());
                            command.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getThirdTable().getName()));
                            if (_isNoPlausiPossible) {
                                command.param(ParameterConstants.PARAM_NO_PLAUSI, JSNumber.byRef("document.getElementById(\"idNoPlausi\").value"));
                            }
                            callingTableDP.synchronize(command);
                        }
                    });
                } else {
                    // Generate save command
                    js.incSaveNr();
                    Command command = new Command(CommandConstants.SAVE);
                    command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, otherTable.getSelectedId());
                    command.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getOtherTable().getName()));
                    if (_isNoPlausiPossible) {
                        command.param(ParameterConstants.PARAM_NO_PLAUSI, JSNumber.byRef("document.getElementById(\"idNoPlausi\").value"));
                    }
                    callingTableDP.synchronize(command);
                }
            }
        });

        return buf.toString();
    }

    protected void addParams(Command cmd) throws DhtmlxException {
        return;
    }
}
