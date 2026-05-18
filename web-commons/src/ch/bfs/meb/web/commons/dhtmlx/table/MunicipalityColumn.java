/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * Column for municipalities in dhtmlx tables. If a code lookup in the own canton fails, the search is extended
 * to all cantons.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class MunicipalityColumn extends ComboCodeGroupColumn {
    public MunicipalityColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, Long canton, boolean sortByKey, int width)
            throws DhtmlxException {
        super(name, header, manager, codeGroup, canton, sortByKey, width);

        setEditorType(EDITOR.COMBOBOX);
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#toValue(java.lang.Object)
     */
    @Override
    public Object toValue(Object row) throws OgnlException {
        Long code = (Long) ognl.Ognl.getValue(getExpression(), row);
        String value = getLocalizationManager().getCodeGroupValueById(_codeGroup, code, _canton);
        if (value != null) {
            return code;
        } else {
            value = getLocalizationManager().searchValueInAllCantons(_codeGroup, code);
            if (value != null) {
                if (_codeFirstFormat) {
                    return code + ": " + value;
                } else {
                    return value + " (" + code + ")";
                }
            } else {
                return code != null ? code : "";
            }
        }
    }

    /* (non-Javadoc)
     * @see ch.bfs.meb.web.commons.dhtmlx.table.Column#toObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        if (value != null) {
            String s = (String) value;
            int closing = s.lastIndexOf(')');
            int opening = s.lastIndexOf('(');
            if (closing != -1 && opening != -1 && opening < closing) {
                value = s.substring(opening + 1, closing);
            }
        }

        super.toObject(object, value);
    }
}
