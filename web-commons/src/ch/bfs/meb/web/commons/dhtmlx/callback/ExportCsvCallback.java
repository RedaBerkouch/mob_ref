/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: RunExportCallback.java 305 2009-12-03 10:25:28Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import ch.bfs.meb.web.commons.dhtmlx.CommandConstants;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;
import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager;
import ch.bfs.meb.web.commons.dhtmlx.javascript.*;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.ParameterConstants;

/**
 * TODO Describe this class
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public class ExportCsvCallback extends MasterDetailCallbackBase {
    protected final boolean _isMiddleTable;

    public ExportCsvCallback(IDhtmlxManager manager, IDhtmlxControl other, IDhtmlxControl third, boolean isMiddleTable, IGlobalJavaScript globals) {
        super(CallbackConstants.ExportCsvCallback, manager, other, third, globals);
        _isMiddleTable = isMiddleTable;
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        // Create wrapper
        final TableClientWrapper other = hasOther() ? new TableClientWrapper(getOtherTable(), buf) : null;
        final TableClientWrapper third = hasThird() ? new TableClientWrapper(getThirdTable(), buf) : null;

        // Javascript wrapper (Sprache)
        final Javascript js = new Javascript(buf);

        if (hasOther()) {
            // If not master, get selected ids of parent
            js.ifnotc(isCallingManagerMaster()).thenc(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    if (_isMiddleTable && hasThird()) {
                        js.ifc(isOtherManagerMaster()).thenc(new CodeBlock() {
                            @Override
                            public void code(StringBuilder buf) throws DhtmlxException {
                                Command exportCsv = new Command(CommandConstants.EXPORT_CSV);
                                exportCsv.setControl(getManager());

                                // Map function parameter id with selRowId
                                exportCsv.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, other.getSelectedId());
                                exportCsv.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getOtherTable().getName()));

                                // Generate call for download
                                MethodCall call = new MethodCall("window", "open");
                                call.param(exportCsv).param(new JSString("_self"));

                                buf.append(call);
                            }
                        }).elsec(new CodeBlock() {
                            @Override
                            public void code(StringBuilder buf) throws DhtmlxException {
                                Command exportCsv = new Command(CommandConstants.EXPORT_CSV);
                                exportCsv.setControl(getManager());

                                // Map function parameter id with selRowId
                                exportCsv.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, third.getSelectedId());
                                exportCsv.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getThirdTable().getName()));

                                // Generate call for download
                                MethodCall call = new MethodCall("window", "open");
                                call.param(exportCsv).param(new JSString("_self"));

                                buf.append(call);
                            }
                        });
                    } else {
                        Command exportCsv = new Command(CommandConstants.EXPORT_CSV);
                        exportCsv.setControl(getManager());

                        // Map function parameter id with selRowId
                        exportCsv.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, other.getSelectedId());
                        exportCsv.param(ParameterConstants.PARAM_SELECTED_MASTER, JSString.byVal(getOtherTable().getName()));

                        // Generate call for download
                        MethodCall call = new MethodCall("window", "open");
                        call.param(exportCsv).param(new JSString("_self"));

                        buf.append(call);
                    }
                }
            }).elsec(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    Command exportCsv = new Command(CommandConstants.EXPORT_CSV);
                    exportCsv.setControl(getManager());

                    // Generate call for download
                    MethodCall call = new MethodCall("window", "open");
                    call.param(exportCsv).param(new JSString("_self"));

                    buf.append(call);
                }
            });
        } else {
            Command exportCsv = new Command(CommandConstants.EXPORT_CSV);
            exportCsv.setControl(getManager());

            // Generate call for download
            MethodCall call = new MethodCall("window", "open");
            call.param(exportCsv).param(new JSString("_self"));

            buf.append(call);
        }

        return buf.toString();
    }
}
