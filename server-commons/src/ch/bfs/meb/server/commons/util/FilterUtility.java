/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons
 */
package ch.bfs.meb.server.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import ch.bfs.meb.exception.MebUncheckedNotMonitoredException;
import ch.bfs.meb.server.commons.integration.dto.Filter;
import ch.bfs.meb.server.commons.integration.dto.FilterContext;
import ch.bfs.meb.server.commons.integration.dto.Parameter;
import ch.bfs.meb.server.commons.integration.dto.WhereFilter;
import ch.bfs.meb.util.StringUtils;

@Service
public class FilterUtility implements IFilterUtility {
    public final static String DATE_FORMAT = "DD.MM.YYYY";

    protected final static String FILTER_WRONG_DATE_MESSAGE = "filter.wrongdate.message";
    protected final static String FILTER_WRONG_NUMBER_MESSAGE = "filter.wrongnumber.message";

    protected ISecurityFilters _securityFilters;

    public void setSecurityFilters(ISecurityFilters securityFilters) {
        _securityFilters = securityFilters;
    }

    /**
     * Generates the SQL for the given where filter conditions
     * 
     * @param filterContext
     *            containing where filter objects
     * @param tableAlias
     *            column table alias of column in query
     * @param stringColumns
     *            string columns of the according table (where values need to be
     *            enclosed by ')
     * @return oracle sql string containing all the where filter conditions
     */
    public String getWhereFilterSelection(FilterContext filterContext, String tableAlias, List<String> stringColumns, List<String> dateColumns,
            List<String> underscoreColumns) {
        return getWhereFilterSelection(filterContext, tableAlias, null, null, null, stringColumns, dateColumns, underscoreColumns);
    }

    /**
     * Generates the SQL for the given where filter conditions
     * 
     * @param filterContext
     *            containing where filter objects
     * @param tableAlias
     *            column table alias of column in query
     * @param columnMapping (can be null)
     *            mapping of column names
     * @param stringColumns
     *            string columns of the according table (where values need to be
     *            enclosed by ')
     * @return oracle sql string containing all the where filter conditions
     */
    public String getWhereFilterSelection(FilterContext filterContext, String tableAlias, Map<String, String> columnMapping, List<String> stringColumns,
            List<String> dateColumns, List<String> underscoreColumns) {
        return getWhereFilterSelection(filterContext, tableAlias, null, null, columnMapping, stringColumns, dateColumns, underscoreColumns);
    }

    /**
     * Generates the SQL for the given where filter conditions
     * 
     * @param filterContext
     *            containing where filter objects
     * @param tableAlias
     *            column table alias of column in query
     * @param joinColumns
     *            column name of joined columns
     * @param joinAlias
     *            column table alias of joined column in query
     * @param columnMapping (can be null)
     *            mapping of column names
     * @param stringColumns
     *            string columns of the according table (where values need to be
     *            enclosed by ')
     * @return oracle sql string containing all the where filter conditions
     */
    public String getWhereFilterSelection(FilterContext filterContext, String tableAlias, List<String> joinColumns, String joinAlias,
            Map<String, String> columnMapping, List<String> stringColumns, List<String> dateColumns, List<String> underscoreColumns) {
        String whereSelection = "";

        WhereFilter previousWhereFilter = null;
        for (WhereFilter whereFilter : filterContext.getWhereFilter()) {
            String colName = adaptColumnName(whereFilter.getAttribute(), tableAlias, joinColumns, joinAlias, columnMapping, underscoreColumns);
            whereSelection += (previousWhereFilter == null) ? "(" : " " + getRelation(previousWhereFilter.getRelation()) + " ";
            if (stringColumns.contains(whereFilter.getAttribute()) && (whereFilter.getValue().equals("") || whereFilter.getValue().equals("''"))) {
                if (whereFilter.getOperator().equals("=")) {
                    whereSelection += colName + " is null";
                } else {
                    whereSelection += "not(" + colName + " is null)";
                }
            } else {
                whereSelection += adaptWhereColumnName(whereFilter.getAttribute(), colName, dateColumns) + " " + whereFilter.getOperator() + " "
                        + adaptColumnValueToSql(whereFilter.getAttribute(), whereFilter.getValue(), stringColumns, dateColumns);
            }
            previousWhereFilter = whereFilter;
        }
        whereSelection += (previousWhereFilter == null) ? "" : ")";
        return whereSelection;
    }

    public boolean containsJoinField(FilterContext filterContext, List<String> joinColumns) {
        for (WhereFilter whereFilter : filterContext.getWhereFilter()) {
            if (joinColumns.contains(whereFilter.getAttribute())) {
                return true;
            }
        }
        return false;
    }

    protected String getRelation(String relation) {
        if (relation.equals("0")) {
            return "and";
        } else if (relation.equals("1")) {
            return "or";
        } else {
            return relation;
        }
    }

    /**
     * Generates the SQL subquery for the given predefined filters
     * 
     * @param filterContext
     *            containing predefined filter objects
     * @param filteredTableName
     *            Name of the according db table
     * @return oracle sql string containing all the concatenated predefined
     *         filters
     */
    public String getPredefinedFilterSubquery(FilterContext filterContext, String filteredTableName, boolean useSecurityFilters) {
        String predefinedSubquery = " " + filteredTableName; // so that replacement by qualified name works

        // Create filter list
        ArrayList<Filter> predefinedFilters = new ArrayList<Filter>(filterContext.getFilter());

        if (useSecurityFilters) {
            // add security filters
            for (int i = 0; i < _securityFilters.getNrOfFilters(); ++i) {
                predefinedFilters.add(0, _securityFilters.getFilter(i));
            }
        }

        // Generate nested subquery for each filter
        for (Filter filter : predefinedFilters) {
            if (filter.getIsDefault() == true) {
                String query = "(" + filter.getSource() + ")";

                // Parameter replacement
                for (Parameter p : filter.getParameters()) {
                    query = query.replaceAll(p.getUniqueName().trim(), p.getDefaultValue());
                }

                String tableName = _securityFilters.getTableName(filter);
                if (tableName != null) {
                    predefinedSubquery = predefinedSubquery.replaceAll(tableName, query);
                } else {
                    predefinedSubquery = predefinedSubquery.replaceAll(filteredTableName, query);
                }
            }

        }

        return predefinedSubquery;
    }

    /**
     * On client side, column names are in camel case, on server side, column names
     * can contain underscores. Change camel case names to underscore names
     * 
     * @param colName
     *            column name of database table in camel case
     * @param tableAlias
     *            column table alias of column in query
     * @param stringColumns
     *            underscore columns of the according table
     * @return column name with underscores instead camel case
     */
    public String adaptColumnName(String colName, String tableAlias, List<String> underscoreColumns) {
        return adaptColumnName(colName, tableAlias, null, null, null, underscoreColumns);
    }

    /**
     * On client side, column names are in camel case, on server side, column names
     * can contain underscores. Change camel case names to underscore names
     * 
     * @param colName
     *            column name of database table in camel case
     * @param tableAlias
     *            column table alias of column in query
     * @param columnMapping
     *            mapping of column names (can be null)
     * @param stringColumns
     *            underscore columns of the according table
     * @return column name with underscores instead camel case
     */
    public String adaptColumnName(String colName, String tableAlias, Map<String, String> columnMapping, List<String> underscoreColumns) {
        return adaptColumnName(colName, tableAlias, null, null, columnMapping, underscoreColumns);
    }

    /**
     * On client side, column names are in camel case, on server side, column names
     * can contain underscores. Change camel case names to underscore names
     * 
     * @param colName
     *            column name of database table in camel case
     * @param tableAlias
     *            column table alias of column in query
     * @param joinColumns
     *            column name of joined columns
     * @param joinAlias
     *            column table alias of joined column in query
     * @param columnMapping
     *            mapping of column names (can be null)
     * @param stringColumns
     *            underscore columns of the according table
     * @return column name with underscores instead camel case
     */
    public String adaptColumnName(String colName, String tableAlias, List<String> joinColumns, String joinAlias, Map<String, String> columnMapping,
            List<String> underscoreColumns) {
        String alias = (joinColumns == null || !joinColumns.contains(colName)) ? tableAlias : joinAlias;
        String mappedColName = mapColumnName(columnMapping, colName);
        for (String uc : underscoreColumns) {
            if (StringUtils.asCamelCase(uc).equals(colName)) {
                return alias + "." + uc;
            }
        }
        return alias + "." + mappedColName;
    }

    /**
     * In where clause, date values have to be stripped from time values
     * @param origColName
     *            column name of database table in client
     * @param colName
     *            column name of database table
     * @param dateColumns
     *            date columns of the according table
     * @return column identifier to be compared without time in case of date
     */
    public String adaptWhereColumnName(String origColName, String colName, List<String> dateColumns) {
        if (dateColumns.contains(origColName)) {
            return "TO_DATE(TO_CHAR(" + colName + ", 'DD.MM.YYYY'), 'DD.MM.YYYY')";
        } else {
            return colName;
        }
    }

    /**
     * Adapt a column value to valid SQL. Encloses a string column value with
     * "'" when necessary. Override in subclasses for specific columns
     * 
     * @param origColName
     *            column id of database table
     * @param value
     *            value of column
     * @param stringColumns
     *            string columns of the according table (where values need to be
     *            enclosed by ')
     * @return value as oracle string
     */
    protected String adaptColumnValueToSql(String origColName, String value, List<String> stringColumns, List<String> dateColumns) {
        String adaptedValue = value.toString();
        if (stringColumns.contains(origColName)) {
            if (!adaptedValue.startsWith("'")) {
                adaptedValue = "'" + adaptedValue;
            }
            if (!adaptedValue.endsWith("'")) {
                adaptedValue = adaptedValue + "'";
            }
            if (adaptedValue.equals("'")) {
                adaptedValue = "''";
            }
        } else if (dateColumns.contains(origColName)) {
            checkDate(adaptedValue);
            // e.g. TO_DATE('12.03.2007', 'DD.MM.YYYY')
            adaptedValue = "TO_DATE('" + adaptedValue + "', '" + DATE_FORMAT + "')";
        } else // number expected
        {
            checkNumber(adaptedValue);
        }
        return adaptedValue;
    }

    protected void checkDate(String value) {
        String[] parts = value.split("[\\.]");
        if (parts.length != 3) {
            throw new MebUncheckedNotMonitoredException(FILTER_WRONG_DATE_MESSAGE);
        }
        try {
            int i = Integer.parseInt(parts[0]);
            if (i < 1 || i > 31) {
                throw new MebUncheckedNotMonitoredException(FILTER_WRONG_DATE_MESSAGE);
            }
            i = Integer.parseInt(parts[1]);
            if (i < 1 || i > 12) {
                throw new MebUncheckedNotMonitoredException(FILTER_WRONG_DATE_MESSAGE);
            }
            Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new MebUncheckedNotMonitoredException(FILTER_WRONG_DATE_MESSAGE);
        }
    }

    protected void checkNumber(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new MebUncheckedNotMonitoredException(FILTER_WRONG_NUMBER_MESSAGE);
        }
    }

    public List<Long> getFilterCantonsForActUser() {
        return _securityFilters.getFilterCantonsForActUser();
    }

    public List<Long> getCantonsForUser(String userName) {
        return _securityFilters.getCantonsForUser(userName);
    }

    public Criterion createInExpression(String propertyName, List<Long> values) {
        int maxIn = 1000;

        Criterion resultCrit = null;
        int startIndex = 0;
        int endIndex = maxIn;
        int lastElement = values.size();
        do {
            Criterion subCrit = Restrictions.in(propertyName, values.subList(startIndex, endIndex > lastElement ? lastElement : endIndex));
            if (resultCrit == null) {
                resultCrit = subCrit;
            } else {
                resultCrit = Restrictions.or(resultCrit, subCrit);
            }

            startIndex += maxIn;
            endIndex += maxIn;
        } while (startIndex < values.size());

        return resultCrit;
    }

    public String createSqlInExpression(String propertyName, List<Long> values) {
        if (propertyName == null || propertyName.trim().length() == 0) {
            return "";
        }

        if (values == null || values.isEmpty()) {
            return "";
        }

        int maxIn = 1000;

        StringBuilder sb = new StringBuilder();
        sb.append(" ( ");

        int startIndex = 0;
        int endIndex = maxIn;
        int lastElement = values.size();
        do {
            if (startIndex > 0) {
                sb.append(" or ");
            }
            sb.append(" ").append(propertyName).append(" in (");

            List<Long> localIdsList = values.subList(startIndex, endIndex > lastElement ? lastElement : endIndex);
            for (int j = 0; j < localIdsList.size(); j++) {
                if (j > 0) {
                    sb.append(",");
                }
                sb.append(localIdsList.get(j).toString());
            }

            sb.append(") ");

            startIndex += maxIn;
            endIndex += maxIn;
        } while (startIndex < values.size());

        sb.append(" ) ");

        return sb.toString();
    }

    protected String mapColumnName(Map<String, String> columnMapping, String colName) {
        String mappedName = null;
        if (columnMapping != null) {
            mappedName = columnMapping.get(colName);
        }
        return (mappedName != null) ? mappedName : colName;
    }

}