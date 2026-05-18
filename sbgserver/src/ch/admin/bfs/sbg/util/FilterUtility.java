package ch.admin.bfs.sbg.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.admin.bfs.sbg.transfer.FilterContext;
import ch.admin.bfs.sbg.transfer.WhereFilter;
import ch.bfs.meb.sbg.server.integration.dto.SbgFilter;
import ch.bfs.meb.sbg.server.integration.repository.IFilterRepository;
import ch.bfs.meb.security.MebUser;
import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.util.SecurityConstants;

/* ----------------------------------------------------------------------------
 *
 * SBG-Projekt
 *
 * Copyright (c) 2006 GLANCE AG, Switzerland
 *
 * $Id: FilterUtility.java 558 2008-10-08 10:36:32Z lsc $
 *
 * ------------------------------------------------------------------------- */

/**
 * Utility class for accessing code groups. This class also provides cached
 * access to codegroups. Don't use CodegroupDAO directly!
 *
 * @author lsc
 * @version $Revision: 558 $
 */
public class FilterUtility {

    private static final Log LOGGER = LogFactory.getLog(FilterUtility.class);

    // date format
    public static final String DATE_FORMAT = "DD.MM.YYYY";

    // table names
    public final static String DELIVERY_TABLE = "Delivery";
    public final static String PERSON_TABLE = "Person";
    public final static String EVENT_TABLE = "Event";

    // Canton Filters SQL Statements
    private static final String CANTON_FILTER_ON_PERSON = "select p.* from Person p where p.canton in (%1)";
    private static final String CANTON_FILTER_ON_EVENT = "select e.* from Event e, Person p where e.pid = p.pid and p.canton in (%1)";
    private static final String CANTON_FILTER_ON_DELIVERY = "select d.* from Delivery d where d.canton in (%1)";

    protected static String getRelation(String relation) {
        if (relation.equals("0")) {
            return "and";
        } else if (relation.equals("1")) {
            return "or";
        } else {
            return relation;
        }
    }

    /**
     * Generates the SQL for the given where filter conditions
     *
     * @param filterContext containing where filter objects
     * @param stringColumns string columns of the according table (where values need to be
     *                      enclosed by ')
     * @return oracle sql string containing all the where filter conditions
     */
    public static String getWhereFilterSelection(FilterContext filterContext, List<String> stringColumns, List<String> dateColumns) {
        String whereSelection = "";

        WhereFilter previousWhereFilter = null;
        List<WhereFilter> wfa = filterContext.getWhereFilter();
        // A filter was defined
        if (wfa != null) {
            for (int i = 0; i < wfa.size(); i++) {
                whereSelection += (previousWhereFilter == null) ? "(" : " " + getRelation(previousWhereFilter.getRelation()) + " ";
                whereSelection += "model." + wfa.get(i).getAttribute() + " " + wfa.get(i).getOperator() + " "
                        + adaptColumnValueToSql(wfa.get(i).getAttribute(), wfa.get(i).getValue(), stringColumns, dateColumns);
                previousWhereFilter = wfa.get(i);
            }
        }
        whereSelection += (previousWhereFilter == null) ? "" : ")";
        return whereSelection;
    }

    /**
     * Generates the SQL subquery for the given predefined filters
     *
     * @param filterContext     containing predefined filter objects
     * @param filteredTableName Name of the according db table
     * @return oracle sql string containing all the concatenated predefined
     * filters
     */
    public static String getPredefinedFilterSubquery(FilterContext filterContext, String filteredTableName) {
        String predefinedSubquery = " " + filteredTableName; // so that replacement by qualified name works

        // Create filter list
        List<Filter> predefinedFilters = new ArrayList<Filter>();
        List<Filter> fa = filterContext.getFilter();
        if (fa != null) {
            for (Filter filter : fa) {
                predefinedFilters.add(filter);
            }
        }
        // add one filter for each canton dependent tables! @todo: Also other
        // tables as action, plausierrors
        predefinedFilters = addCantonFilter(predefinedFilters, FilterUtility.EVENT_TABLE);
        predefinedFilters = addCantonFilter(predefinedFilters, FilterUtility.PERSON_TABLE);
        predefinedFilters = addCantonFilter(predefinedFilters, FilterUtility.DELIVERY_TABLE);

        // Generate nested subquery for each filter
        for (Filter filter : predefinedFilters) {
            if (filter.getIsDefault() == true) {
                String query = "(" + filter.getSource() + ")";

                // Parameter replacement
                for (Parameter p : filter.getParameters()) {
                    query = query.replaceAll(p.getUniqueName().trim(), p.getDefaultValue());
                }

                // Replace table names by subquery
                if (filter.getSource().equals(CANTON_FILTER_ON_EVENT)) {
                    predefinedSubquery = predefinedSubquery.replaceAll(FilterUtility.EVENT_TABLE, query);
                } else if (filter.getSource().equals(CANTON_FILTER_ON_PERSON)) {
                    predefinedSubquery = predefinedSubquery.replaceAll(FilterUtility.PERSON_TABLE, query);
                } else if (filter.getSource().equals(CANTON_FILTER_ON_DELIVERY)) {
                    predefinedSubquery = predefinedSubquery.replaceAll(FilterUtility.DELIVERY_TABLE, query);
                } else {
                    predefinedSubquery = predefinedSubquery.replaceAll(filteredTableName, query);
                }
            }

        }

        return predefinedSubquery;
    }

    /**
     * Adds a canton filter where needed (for Datenlieferanten RO and DL), so
     * that a DL cannot access data from other cantons
     *
     * @param predefinedFilters predefined filter objects
     * @return predefined filter objects with eventually added canton filter
     */
    public static List<Filter> addCantonFilter(List<Filter> predefinedFilters, String filteredTableName) {
        // Add filter if user is DL
        MebUser user = (MebUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isInRole(SecurityConstants.ROLE_SBG_EV)) {
            Filter cantonFilter = new Filter();
            cantonFilter.setIsActive(true);
            cantonFilter.setIsDefault(true);

            // Add canton parameter
            List<Parameter> paramArray = new ArrayList<Parameter>();
            Parameter param = new Parameter();
            param.setFilterId(cantonFilter.getFilterId());
            param.setUniqueName("%1");
            param.setDefaultValue(user.getCantonsAsString());
            paramArray.add(param);
            cantonFilter.setParameters(paramArray);

            if (filteredTableName.equals(DELIVERY_TABLE)) {
                cantonFilter.setRefObject(CodegroupUtility.SBG_OBJECTTYPE_DELIVERY);
                cantonFilter.setSource(CANTON_FILTER_ON_DELIVERY);
            } else if (filteredTableName.equals(PERSON_TABLE)) {
                cantonFilter.setRefObject(CodegroupUtility.SBG_OBJECTTYPE_PERSON);
                cantonFilter.setSource(CANTON_FILTER_ON_PERSON);
            } else if (filteredTableName.equals(EVENT_TABLE)) {
                cantonFilter.setRefObject(CodegroupUtility.SBG_OBJECTTYPE_EVENT);
                cantonFilter.setSource(CANTON_FILTER_ON_EVENT);
            } else {
                LOGGER.error("Unexpected Tablename for creating canton filter: " + filteredTableName);
                throw new RuntimeException("Unexpected Tablename for creating canton filter: " + filteredTableName);
            }

            predefinedFilters.add(cantonFilter);
        }
        return predefinedFilters;
    }

    /**
     * Adapt a column value to valid SQL. Encloses a string column value with
     * "'" when necessary. Override in subclasses for specific columns
     *
     * @param colName       column id of database table
     * @param value         value of column
     * @param stringColumns string columns of the according table (where values need to be
     *                      enclosed by ')
     * @return value as oracle string
     */
    protected static String adaptColumnValueToSql(String colName, String value, List<String> stringColumns, List<String> dateColumns) {
        String adaptedValue = value.toString();
        if (stringColumns.contains(colName)) {
            if (!adaptedValue.startsWith("'")) {
                adaptedValue = "'" + adaptedValue;
            }
            if (!adaptedValue.endsWith("'")) {
                adaptedValue = adaptedValue + "'";
            }
        }
        if (dateColumns.contains(colName)) {
            // e.g. TO_DATE('12.03.2007', 'DD.MM.YYYY')
            adaptedValue = "TO_DATE('" + adaptedValue + "', '" + DATE_FORMAT + "')";
        }
        return adaptedValue;
    }

    public static FilterContext createEmptyFilterContext(IFilterRepository filterRepository) {
        FilterContext filters = new FilterContext();
        filters.setWhereFilter(new ArrayList<WhereFilter>());
        List<SbgFilter> defaultFilters = filterRepository.getDefaultPersonFilters();
        List<Filter> filterArr = new ArrayList<Filter>(defaultFilters);
        filters.setFilter(filterArr);
        return filters;
    }
}
