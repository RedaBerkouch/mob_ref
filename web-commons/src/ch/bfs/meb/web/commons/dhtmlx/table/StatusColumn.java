/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.util.StringUtils;
import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.ILocalizedCode;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;

/**
 * Dhtmlx table column to display a column with a drop down selection box, without the code displayed.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class StatusColumn extends ComboCodeGroupColumn {
    public StatusColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, int width) throws DhtmlxException {
        super(name, header, manager, codeGroup, null, true, width);
    }

    @Override
    public String getDisplayString(Object row) throws ognl.OgnlException {
        Object value = ognl.Ognl.getValue(expression, row);
        if (value == null) {
            return "";
        } else if (value instanceof Long) {
            Long longValue = (Long) value;
            String codeGroupValue = getLocalizationManager().getCodeGroupValueById(_codeGroup, (Long) value, _canton);
            if (StringUtils.isEmpty(codeGroupValue)) {
                return longValue.toString();
            } else {
                return codeGroupValue;
            }
        }

        return "";
    }

    /**
     * Get the text for an option. There are two formatting options, depending
     * on the codeFirst parameter.
     * 
     * @return Returns the _option.
     */
    @Override
    protected String getOptionText(ILocalizedCode code) {
        return code.getValue();
    }
}
