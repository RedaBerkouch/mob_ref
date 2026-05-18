/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: server-commons
 */
package ch.bfs.meb.server.commons.util;

import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;

import ch.bfs.meb.server.commons.integration.dto.FilterContext;

public interface IFilterUtility {
    public String getWhereFilterSelection(FilterContext filterContext, String tableAlias, List<String> stringColumns, List<String> dateColumns,
            List<String> underscoreColumns);

    public String getWhereFilterSelection(FilterContext filterContext, String tableAlias, Map<String, String> columnMapping, List<String> stringColumns,
            List<String> dateColumns, List<String> underscoreColumns);

    public String getWhereFilterSelection(FilterContext filterContext, String tableAlias, List<String> joinColumns, String joinAlias,
            Map<String, String> columnMapping, List<String> stringColumns, List<String> dateColumns, List<String> underscoreColumns);

    public boolean containsJoinField(FilterContext filterContext, List<String> joinColumns);

    public String getPredefinedFilterSubquery(FilterContext filterContext, String filteredTableName, boolean useSecurityFilters);

    public List<Long> getFilterCantonsForActUser();

    public List<Long> getCantonsForUser(String userName);

    public String adaptColumnName(String colName, String tableAlias, List<String> underscoreColumns);

    public String adaptColumnName(String colName, String tableAlias, Map<String, String> columnMapping, List<String> underscoreColumns);

    public String adaptColumnName(String colName, String tableAlias, List<String> joinColumns, String joinAlias, Map<String, String> columnMapping,
            List<String> underscoreColumns);

    public Criterion createInExpression(String propertyName, List<Long> values);

    public String createSqlInExpression(String propertyName, List<Long> values);
}
