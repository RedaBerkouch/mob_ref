/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id: FilterContextUtility.java 419 2007-10-05 12:53:01Z dzw $
 *
 * ------------------------------------------------------------------------- */
package ch.admin.bfs.sbg.util;

import ch.bfs.meb.web.commons.dhtmlx.table.WebFilter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebFilterContext;
import ch.bfs.meb.web.commons.dhtmlx.table.WebParameter;
import ch.bfs.meb.web.commons.dhtmlx.table.WebWhereFilter;

/** 
 * Utility class for filter contexts
 * 
 * @author  $Author: dzw $ 
 * @version $Revision: 419 $ 
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

        for (WebWhereFilter filter : ctx.getWhereFilter()) {
            if (filter.getValue() == null || filter.getValue().trim().equals("") || filter.getAttribute() == null || filter.getAttribute().trim().equals("")
                    || filter.getOperator() == null || filter.getOperator().trim().equals("")) {
                return "filter.value.missing.message";
            }
        }

        return null;
    }
}
