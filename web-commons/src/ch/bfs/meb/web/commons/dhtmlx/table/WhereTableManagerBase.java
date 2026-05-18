/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.callback.DeleteRowCallback;
import ch.bfs.meb.web.commons.dhtmlx.callback.InsertRowCallback;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;

/**
 * Abstract base class for dhtmlx table managers for ad-hoc where filters.
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class WhereTableManagerBase extends TableManagerBase {
    protected HashMap<String, WebWhereFilter> _whereFilter = new HashMap<String, WebWhereFilter>();

    protected static final String[] OPERATORS = { "=", ">", "<", "<=", ">=", "<>", "LIKE" };
    protected static final String[] RELATIONS = { "AND", "OR" };
    protected static final String COLUMN_ATTRIBUTE_ID = "attribute";
    protected static final String COLUMN_ATTRIBUTE_NAME_KEY = "whereTable.column.attribute.name";
    private static final String COLUMN_OPERATOR_ID = "operator";
    private static final String COLUMN_OPERATOR_NAME_KEY = "whereTable.column.operator.name";
    protected static final String COLUMN_VALUE_ID = "value";
    protected static final String COLUMN_VALUE_NAME_KEY = "whereTable.column.value.name";
    private static final String COLUMN_RELATION_ID = "relation";
    private static final String COLUMN_RELATION_NAME_KEY = "whereTable.column.relation.name";
    private static final String COLUMN_ID_ID = "id";
    private static final String COLUMN_ID_NAME_KEY = "whereTable.column.id.name";

    /**
     * Replaces operator names by real sql operators. This is necessary because the xml special characters 
     * '<' and '>' cannot be set directly as Combo option (the transformation into an XML document would fail)
     */
    static public void replaceWhereFilterOperators(List<WebWhereFilter> whereFilters) {
        for (WebWhereFilter wf : whereFilters) {
            if (wf.getOperator() != null && wf.getOperator().trim().length() > 0) {
                String op = OPERATORS[Integer.valueOf(wf.getOperator())];
                wf.setOperator(op);
            }
        }
    }

    protected abstract void addAttributeColumn(TableManagerBase target) throws DhtmlxException;

    protected abstract IJavaScriptFunction createOnRowSelectCallback(TableManagerBase target);

    protected abstract IJavaScriptFunction createOnCellChangedCallback(TableManagerBase target);

    protected void setOperators(ComboColumn comboColumn) {
        for (int i = 0; i < OPERATORS.length; ++i) {
            comboColumn.addComboItem(new Long(i), OPERATORS[i]);
        }
    }

    protected void setRelations(ComboColumn comboColumn) {
        for (int i = 0; i < RELATIONS.length; ++i) {
            comboColumn.addComboItem(new Long(i), RELATIONS[i]);
        }
    }

    public void create() throws DhtmlxException {}

    public void create(TableManagerBase targetManager) throws DhtmlxException {
        addAttributeColumn(targetManager);

        ComboColumn operator = new ComboColumn(COLUMN_OPERATOR_ID, COLUMN_OPERATOR_NAME_KEY, getLocalizationManager(), 15);
        setOperators(operator);
        addColumn(operator);

        Column valueColumn = new Column(COLUMN_VALUE_ID, COLUMN_VALUE_NAME_KEY, getLocalizationManager(), 35);
        valueColumn.setDefault("");
        addColumn(valueColumn);

        ComboColumn relation = new ComboColumn(COLUMN_RELATION_ID, COLUMN_RELATION_NAME_KEY, getLocalizationManager(), 18);
        setRelations(relation);
        addColumn(relation);

        addColumn(new IdentityColumn(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager()));

        // Callback
        IJavaScriptFunction onRowSelectCallback = createOnRowSelectCallback(targetManager);
        IJavaScriptFunction onCellChangedCallback = createOnCellChangedCallback(targetManager);

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
        addBeforeOption(new Option(table.setOnSelectStateChangedHandler(onRowSelectCallback)));
        addBeforeOption(new Option(table.setOnCellChanged(onCellChangedCallback)));

        // Register callbacks
        registerCallback(new InsertRowCallback(this));
        registerCallback(new DeleteRowCallback(this));
        registerCallback(onRowSelectCallback);
        registerCallback(onCellChangedCallback);
    }

    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        return toXMLStream(new FilterListTableResultMapper(new WebFilterListResult(new ArrayList<WebFilter>()), getLocalizationManager()), true, true);
    }

    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        return toXMLStream(new FilterListTableResultMapper(new WebFilterListResult(new ArrayList<WebFilter>()), getLocalizationManager()), true, false);
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<WebFilter> getExportRows(ParameterList params) {
        return new ArrayList<WebFilter>();
    }
}
