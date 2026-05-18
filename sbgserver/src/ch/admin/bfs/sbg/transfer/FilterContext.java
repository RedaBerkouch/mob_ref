/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilterContext.java 36 2007-05-29 09:45:22Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.transfer;

import java.util.ArrayList;
import java.util.List;

import ch.bfs.meb.server.commons.integration.dto.Filter;

/**
 * TODO Describe this class
 *
 * @author $Author: dzw $
 * @version $Revision: 36 $
 */
public class FilterContext {
    protected List<WhereFilter> _whereFilter = new ArrayList<WhereFilter>();
    protected List<Filter> _filter = new ArrayList<Filter>();
    protected String _locale;

    public List<WhereFilter> getWhereFilter() {
        return _whereFilter;
    }

    public List<Filter> getFilter() {
        return _filter;
    }

    public void setWhereFilter(List<WhereFilter> whereFilter) {
        _whereFilter = whereFilter;
    }

    public void setFilter(List<Filter> filter) {
        _filter = filter;
    }

    public String getLocale() {
        return _locale;
    }

    public void setLocale(String locale) {
        _locale = locale;
    }
}
