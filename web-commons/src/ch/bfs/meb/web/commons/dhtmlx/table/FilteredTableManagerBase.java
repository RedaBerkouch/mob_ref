/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.util.List;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.callback.OnBeforeSelectCallback;
import ch.bfs.meb.web.commons.dhtmlx.javascript.IJavaScriptFunction;
import ch.bfs.meb.web.commons.dhtmlx.javascript.TableClientWrapper;
import ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.RowsDocument;
import ch.bfs.meb.web.commons.util.FilterContextUtility;

/**
 * Abstract base class for dhtmlx table managers with filtered content.
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class FilteredTableManagerBase extends TableManagerBase {
    private WebFilterContext _filterContext;
    private Long _filterVersion;
    private Long _filterCanton;
    private boolean _inInit;

    private boolean _dummyRegistered = false;

    protected abstract WhereTableManagerBase getWhereTableManager();

    protected abstract FilterTableManagerBase getFilterTableManager();

    protected void registerDummyRowSelect() {
        if (!_dummyRegistered) {
            IJavaScriptFunction onBeforeSelectCallback = new OnBeforeSelectCallback(this);
            TableClientWrapper table = new TableClientWrapper(this);
            addBeforeOption(new Option(table.setOnBeforeSelect(onBeforeSelectCallback)));
            registerCallback(onBeforeSelectCallback);
            _dummyRegistered = true;
        }
    }

    /**
     * Extracts filter parameters from the request parameter list.
     * 
     * @param params	Request parameters
     * @return			null, if everything is ok, an error message property otherwise
     * @throws DhtmlxException 
     */
    protected String extractFilterParams(ParameterList params) throws DhtmlxException {
        if (params.hasParameter(ParameterConstants.PARAM_FILTERVERSION)) {
            _filterVersion = params.getFilterVersion();
        }
        if (params.hasParameter(ParameterConstants.PARAM_FILTERCANTON)) {
            _filterCanton = params.getFilterCanton();
        }

        if (params.hasParameter(ParameterConstants.PARAM_WHEREFILTERDATA)) {
            // Add where filters
            RowsDocument whereDocument = params.getData(ParameterConstants.PARAM_WHEREFILTERDATA);
            _filterContext = new WebFilterContext();
            List<WebWhereFilter> whereFilterList = toArrayList(getWhereTableManager().toObjectList(whereDocument, WebWhereFilter.class));
            replaceAttributeIdsByName(whereFilterList);
            WhereTableManagerBase.replaceWhereFilterOperators(whereFilterList);
            _filterContext.setWhereFilter(whereFilterList);
        }

        if (params.hasParameter(ParameterConstants.PARAM_PREDEFINEDFILTERDATA)) {
            // Add predefined filters
            RowsDocument filterDocument = params.getData(ParameterConstants.PARAM_PREDEFINEDFILTERDATA);
            List<WebFilter> filterList = toArrayList(getFilterTableManager().toObjectList(filterDocument, WebFilter.class));
            _filterContext.setFilter(filterList);
        }

        _filterContext.setLocale(getLocalizationManager().getLocale().getLanguage());

        return FilterContextUtility.parameterError(_filterContext);
    }

    /**
     * @return the filterContext
     */
    public WebFilterContext getFilterContext() {
        return _filterContext;
    }

    public void setFilterContext(WebFilterContext filterContext) {
        _filterContext = filterContext;
    }

    /**
     * @return the filterVersion
     */
    public Long getFilterVersion() {
        return _filterVersion;
    }

    /**
     * @param filterVersion the filterVersion to set
     */
    public void setFilterVersion(Long filterVersion) {
        _filterVersion = filterVersion;
    }

    /**
     * @return the filterCanton
     */
    public Long getFilterCanton() {
        return _filterCanton;
    }

    /**
     * @param filterCanton the filterCanton to set
     */
    public void setFilterCanton(Long filterCanton) {
        _filterCanton = filterCanton;
    }

    public boolean isInInit() {
        return _inInit;
    }

    public void setInInit(boolean inInit) {
        _inInit = inInit;
    }
}
