/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSString;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowsDocument;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Abstract base class for dhtmlx table managers for predefined filters.
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class FilterTableManagerBase extends TableManagerBase {
    private static final String COLUMN_DEFAULT_ID = "isDefault";
    private static final String COLUMN_DEFAULT_NAME_KEY = "filterTable.column.isDefault.name";
    private static final String COLUMN_NAME_ID = "name";
    private static final String COLUMN_NAME_NAME_KEY = "filterTable.column.name.name";
    private static final String COLUMN_DESCRIPTION_ID = "description";
    private static final String COLUMN_DESCRIPTION_NAME_KEY = "filterTable.column.description.name";
    private static final String COLUMN_PARAMETER_ID = "parameters";
    private static final String COLUMN_PARAMETER_NAME_KEY = "filterTable.column.parameter.name";
    private static final String COLUMN_ID_ID = "filterId";
    private static final String COLUMN_ID_NAME_KEY = "filterTable.column.id.name";

    @Autowired
    private IWebLocalizationManager _localizationManager;

    /**
     * @see ch.bfs.meb.web.commons.dhtmlx.IDhtmlxManager#getLocalizationManager()
     */
    @Override
    public IWebLocalizationManager getLocalizationManager() {
        return _localizationManager;
    }

    public void create() throws DhtmlxException {

        addColumn(new CheckboxColumn(COLUMN_DEFAULT_ID, COLUMN_DEFAULT_NAME_KEY, getLocalizationManager()));
        addColumn(new MultilanguageReadOnlyColumn(COLUMN_NAME_ID, COLUMN_NAME_NAME_KEY, getLocalizationManager()));
        addColumn(new MultilanguageReadOnlyColumn(COLUMN_DESCRIPTION_ID, COLUMN_DESCRIPTION_NAME_KEY, getLocalizationManager(), 46));
        addColumn(new ParameterColumn(COLUMN_PARAMETER_ID, COLUMN_PARAMETER_NAME_KEY, COLUMN_ID_ID, getLocalizationManager()));
        addColumn(new IdentityColumn(COLUMN_ID_ID, COLUMN_ID_NAME_KEY, getLocalizationManager()));

        // Server side options
        TableClientWrapper table = new TableClientWrapper(this);
        addBeforeOption(new Option(table.setSkin(JSString.byRef("bfs"))));
    }

    public abstract WebFilterListResult getAllRows();

    public DhtmlxTableXML init(ParameterList params) throws DhtmlxException {
        FilterListTableResultMapper resultMapper = new FilterListTableResultMapper(getAllRows(), getLocalizationManager());

        return toXMLStream(resultMapper, true, true);
    }

    public DhtmlxTableXML load(ParameterList params) throws DhtmlxException {
        FilterListTableResultMapper resultMapper = new FilterListTableResultMapper(getAllRows(), getLocalizationManager());

        return toXMLStream(resultMapper, true, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> toObjectList(RowsDocument document, Class clazz) throws DhtmlxException {
        List<Object> data = super.toObjectList(document, clazz);

        for (Object obj : data) {
            if (obj instanceof WebFilter) {
                WebFilter changedFilter = (WebFilter) obj;
                Collection origFilters = getData();
                for (Object obj2 : origFilters) {
                    if (obj2 instanceof WebFilter && ((WebFilter) obj2).getFilterId().equals(changedFilter.getFilterId())) {
                        WebFilter origFilter = (WebFilter) obj2;
                        changedFilter.setSource(origFilter.getSource());
                    }
                }
            }
        }
        return data;
    }

    /**
     * Gets all rows for export
     * 
     * @return List with all rows
     */
    @Override
    protected List<WebFilter> getExportRows(ParameterList params) {
        return getAllRows().getFilters();
    }
}
