package ch.admin.bfs.sbg.transfer;

/**
 * Transfer context for sorting entities on service layer.  
 * 
 * @author lsc
 */
public class SortContext {

    protected String _sortColumn;
    protected boolean _ascSortOrder;
    protected String _locale;

    /**
     * @return Returns the sortColumn.
     */
    public String getSortColumn() {
        return _sortColumn;
    }

    /**
     * @param sortColumn The sortColumn to set.
     */
    public void setSortColumn(String sortColumn) {
        this._sortColumn = sortColumn;
    }

    /**
     * @return Returns the ascSortOrder.
     */
    public boolean getAscSortOrder() {
        return _ascSortOrder;
    }

    /**
     * @param ascSortOrder The ascSortOrder to set.
     */
    public void setAscSortOrder(boolean ascSortOrder) {
        this._ascSortOrder = ascSortOrder;
    }

    /**
     * @return Returns the locale.
     */
    public String getLocale() {
        return _locale;
    }

    /**
     * @param locale The locale to set.
     */
    public void setLocale(String locale) {
        this._locale = locale;
    }
}
