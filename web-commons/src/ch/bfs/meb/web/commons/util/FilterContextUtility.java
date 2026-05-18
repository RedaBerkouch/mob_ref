/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

 */
package ch.bfs.meb.web.commons.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ch.bfs.meb.util.CodegroupUtility;
import ch.bfs.meb.web.commons.dhtmlx.table.*;

/** 
 * Utility class for filter contexts
 */
public class FilterContextUtility {
    public static String parameterError(WebFilterContext ctx) {
        for (WebFilter filter : ctx.getFilter()) {
            if (filter.getIsDefault()) {
                for (WebParameter param : filter.getParameters()) {
                    if (param.getDefaultValue() == null || param.getDefaultValue().trim().equals("")) {
                        return "filter.parameter.missing.message";
                    }
                }
            }
        }

        boolean lastRelationEmpty = false;
        for (WebWhereFilter filter : ctx.getWhereFilter()) {
            // check if the previous relation was empty (only the last relation can be empty)
            if (lastRelationEmpty)
                return "filter.value.missing.message";

            if (filter.getValue() == null || filter.getValue().trim().length() == 0 || filter.getAttribute() == null
                    || filter.getAttribute().trim().length() == 0 || filter.getOperator() == null || filter.getOperator().trim().length() == 0) {
                return "filter.value.missing.message";
            }

            if (!("0".equals(filter.getRelation()) || "1".equals(filter.getRelation()))) {
                lastRelationEmpty = true;
            }
        }

        return null;
    }

    public static Long getActVersion(IFilterService filterService, Long refObject) {
        try {
            WebFilterListResult filters = filterService.getFiltersForRefObjectAndNameDe(refObject, CodegroupUtility.MEB_FILTER_ACT_VERSION);
            if (filters.getFilters().size() > 0) {
                return Long.parseLong(filters.getFilters().get(0).getSource());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
            // nothing to do
        }

        return (long) new GregorianCalendar().get(Calendar.YEAR);
    }

    public static Long getInitVersion(IFilterService filterService, Long refObject) {
        try {
            WebFilterListResult filters = filterService.getFiltersForRefObjectAndNameDe(refObject, CodegroupUtility.MEB_FILTER_INIT_VERSION);
            if (filters.getFilters().size() > 0) {
                return Long.parseLong(filters.getFilters().get(0).getSource());
            }
        } catch (Exception e) {
            // nothing to do
        }

        return (long) new GregorianCalendar().get(Calendar.YEAR);
    }
}
