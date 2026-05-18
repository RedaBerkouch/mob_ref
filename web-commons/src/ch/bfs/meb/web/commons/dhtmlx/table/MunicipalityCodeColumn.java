/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: MunicipalityCodeColumn.java  13.04.2010 09:40:12 msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

public class MunicipalityCodeColumn extends Column {
    private String _codeGroup;

    public MunicipalityCodeColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, int width) throws DhtmlxException {
        super(name, header, manager, width);
        _codeGroup = codeGroup;
        setDefault(null);
        setEditType(EditType.readonly);
        setColor(COLOR.LIGHTGREY);
    }

    @Override
    public void toObject(Object object, Object value) throws DhtmlxException {
        // These columns are read only, so no need to set new value.
    }

    @Override
    public Object toValue(Object row) throws OgnlException {
        Object value = ognl.Ognl.getValue(expression, row);
        if (value != null && value instanceof Long) {
            String codeText = getLocalizationManager().searchValueInAllCantons(_codeGroup, (Long) value);
            return (codeText == null || codeText.trim().length() == 0) ? value.toString() : codeText + " (" + value + ")";
        } else {
            return "";
        }
    }
}
