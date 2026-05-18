/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: AgeColumn.java  22.05.2012 12:36:09 Administrator $

 */
package ch.bfs.meb.sdl.web.dhtmlx.table;

import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.table.Column;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

public class AgeColumn extends Column {
    protected Object _birthday;

    public AgeColumn(String name, String header, String versionName, String birthdayName, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        try {
            setExpression(versionName);
            _birthday = ognl.Ognl.parseExpression(birthdayName);
        } catch (OgnlException ex) {
            throw new DhtmlxException("AgeColumn, invalid name", ex);
        }

        setEditorType(EDITOR.READ_ONLY);
        setEditType(EditType.readonly);
        setColor(COLOR.LIGHTGREY);
        setSort(Column.SORT.NO_SORT);
    }

    public Object toValue(Object row) throws OgnlException {
        Object version = ognl.Ognl.getValue(getExpression(), row);
        Object birthday = ognl.Ognl.getValue(_birthday, row);

        if (version != null && birthday != null) {
            if (birthday instanceof XMLGregorianCalendar) {
                return "" + (((Long) version).intValue() - ((XMLGregorianCalendar) birthday).toGregorianCalendar().get(Calendar.YEAR));
            }
        }
        return "";
    }

    @Override
    public void toObject(Object object, Object value) throws DhtmlxException {
        // nothing to do
    }
}
