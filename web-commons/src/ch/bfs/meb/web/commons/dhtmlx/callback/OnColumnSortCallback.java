/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.callback;

import java.util.ArrayList;
import java.util.List;

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
public class OnColumnSortCallback extends MasterDetailCallbackBase {
    private final JSNumber _columnIndex = JSNumber.byRef("colindex");

    private final JSString _grid = JSString.byRef("grid");

    private final JSString _direction = JSString.byRef("direction");

    private TableClientWrapper _refManagerTable;
    private TableClientWrapper _refManagerTable2;

    protected final boolean _isMiddleTable;

    private TableClientWrapper _managerTable;

    private Javascript _js;

    public static final String SORT_MASTERTABLE_LOCK_MESSAGE_KEY = "sort.mastertable.lock.message";

    public static final String SORT_DETAILTABLE_LOCK_MESSAGE_KEY = "sort.detailtable.lock.message";

    private JSBoolean _callingManagerIsMaster;
    private JSBoolean _otherManagerIsMaster = null;
    private JSBoolean _thirdManagerIsMaster = null;

    private final List<Integer> _ignoreColumns = new ArrayList<Integer>();

    public OnColumnSortCallback(IDhtmlxManager manager, IDhtmlxControl refManager, IDhtmlxControl refManager2, boolean isMiddleTable,
            IGlobalJavaScript globals) {
        super(CallbackConstants.OnColumnSortCallback, manager, refManager, refManager2, globals);
        _isMiddleTable = isMiddleTable;

        // add parameters
        addParameter(_columnIndex);
        addParameter(_grid);
        addParameter(_direction);
    }

    public OnColumnSortCallback(IDhtmlxManager manager, IDhtmlxControl refManager, IGlobalJavaScript globals) {
        this(manager, refManager, null, false, globals);
    }

    public void addIgnoreColumn(Integer ignoreColumn) {
        _ignoreColumns.add(ignoreColumn);
    }

    protected void ignoreColumns(StringBuilder buf) {
        if (_ignoreColumns.size() > 0) {
            for (Integer i : _ignoreColumns) {
                buf.append("if(colindex==" + i + ")return false;");
            }
        }
    }

    public String getScriptingBody() throws DhtmlxException {
        StringBuilder buf = new StringBuilder();

        ignoreColumns(buf);

        _managerTable = new TableClientWrapper(getManager(), buf);

        _refManagerTable = new TableClientWrapper(getOtherTable(), buf);
        _refManagerTable2 = hasThird() ? new TableClientWrapper(getThirdTable(), buf) : null;

        final IDhtmlxControl _otherTable = getOtherTable();
        final IDhtmlxControl _thirdTable = getThirdTable();

        _callingManagerIsMaster = isCallingManagerMaster();
        if (hasThird() && _isMiddleTable) {
            _otherManagerIsMaster = isOtherManagerMaster();
            _thirdManagerIsMaster = isThirdManagerMaster();
        }

        // Javascript wrapper (Sprache)
        _js = new Javascript(buf);

        final DataProcessorClientWrapper managerDP = new DataProcessorClientWrapper(getManager(), buf);
        final DataProcessorClientWrapper detailDP = new DataProcessorClientWrapper(_otherTable, buf);
        final DataProcessorClientWrapper thirdDP = _thirdTable == null ? null : new DataProcessorClientWrapper(_thirdTable, buf);

        // the table to be sorted and the tables that are cleared (clearAll) must be
        // synchronized - see getSortingCode() for more information

        _js.ifc(_callingManagerIsMaster).thenc(new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                alertAndReturnWhenNotSynchronized(_js, managerDP, SORT_MASTERTABLE_LOCK_MESSAGE_KEY);
                alertAndReturnWhenNotSynchronized(_js, detailDP, SORT_DETAILTABLE_LOCK_MESSAGE_KEY);

                if (_thirdTable != null) {
                    alertAndReturnWhenNotSynchronized(_js, thirdDP, SORT_DETAILTABLE_LOCK_MESSAGE_KEY);
                }
            }
        });

        // if we have two detail tables and we want to sort the middle table when
        // middle table is not master...
        if (_thirdManagerIsMaster != null) {
            _js.elsec(new CodeBlock() {
                @Override
                public void code(StringBuilder buf) throws DhtmlxException {
                    _js.ifc(_otherManagerIsMaster).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            alertAndReturnWhenNotSynchronized(_js, thirdDP, SORT_DETAILTABLE_LOCK_MESSAGE_KEY);
                        }
                    }).elseifc(_thirdManagerIsMaster).thenc(new CodeBlock() {
                        @Override
                        public void code(StringBuilder buf) throws DhtmlxException {
                            alertAndReturnWhenNotSynchronized(_js, detailDP, SORT_DETAILTABLE_LOCK_MESSAGE_KEY);
                        }
                    });
                }
            });
        }

        _js.code(getSortingCode());

        return buf.toString();
    }

    /**
     * Depending on which table is master, which tables are details and which table will
     * be sorted, other tables have to be cleared. it's possible, that second detail table is null
     * 
     * M = Master, D = Detail, s = sort, c = clearAll (read vertical variants 1, 2, 3)
     *
     *   1|2|3     1|2|3     1|2|3
     * M s| |    D s|c|    D c|c|s
     * D c|s|    M  |s|    D c|s|
     * D c|c|s   D  |c|s   M s| |
     */
    private CodeBlock getSortingCode() throws DhtmlxException {
        return new CodeBlock() {
            @Override
            public void code(StringBuilder buf) throws DhtmlxException {
                // clear all values on table
                _managerTable.clearAll(JSBoolean.isfalse);

                final JSString sortState = JSString.byRef("sortState");
                final JSString loc = JSString.byRef("loc");
                _js.define(loc, _direction.toUpperCase());

                JSString conc = JSString.byRef("','");
                _js.define(sortState, _columnIndex.concat(conc).concat(loc));

                // Set sortimage state on header
                _managerTable.setSortImgState(JSString.byVal("true"), _columnIndex, loc);

                _js.ifc(_callingManagerIsMaster).thenc(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        _refManagerTable.clearAll(JSBoolean.isfalse);
                        if (_refManagerTable2 != null) {
                            _refManagerTable2.clearAll(JSBoolean.isfalse);
                        }

                        // Update table
                        Command command = new Command(CommandConstants.SORT);
                        command.param(ParameterConstants.PARAM_SORT_STATE, sortState);
                        _managerTable.loadXML(command);
                    }
                }).elsec(new CodeBlock() {
                    @Override
                    public void code(StringBuilder buf) throws DhtmlxException {
                        final JSString selIds = JSString.byRef("selIds");
                        final JSString selMaster = JSString.byRef("selMaster");

                        // if we have two detail tables and we sort the middle table when
                        // middle table is not master...
                        if (hasThird() && _isMiddleTable) {
                            _js.define(selIds, JSString.byVal(""));
                            _js.define(selMaster, JSString.byVal(""));
                            _js.ifc(_otherManagerIsMaster).thenc(new CodeBlock() {
                                @Override
                                public void code(StringBuilder buf) throws DhtmlxException {
                                    _refManagerTable2.clearAll(JSBoolean.isfalse);
                                    _js.assign(selIds, _refManagerTable.getSelectedId());
                                    _js.assign(selMaster, JSString.byVal(getOtherTable().getName()));
                                }
                            }).elseifc(_thirdManagerIsMaster).thenc(new CodeBlock() {
                                @Override
                                public void code(StringBuilder buf) throws DhtmlxException {
                                    _refManagerTable.clearAll(JSBoolean.isfalse);
                                    _js.assign(selIds, _refManagerTable2.getSelectedId());
                                    _js.assign(selMaster, JSString.byVal(getThirdTable().getName()));
                                }
                            });
                        } else {
                            _js.define(selIds, _refManagerTable.getSelectedId());
                            _js.define(selMaster, JSString.byVal(getOtherTable().getName()));
                        }

                        // Update table
                        Command command = new Command(CommandConstants.SORT);
                        command.param(ParameterConstants.PARAM_SORT_STATE, sortState);
                        command.param(ParameterConstants.PARAM_SELECTED_ROW_IDS, selIds);
                        command.param(ParameterConstants.PARAM_SELECTED_MASTER, selMaster);
                        _managerTable.loadXML(command);
                    }
                });
            }
        };
    }
}