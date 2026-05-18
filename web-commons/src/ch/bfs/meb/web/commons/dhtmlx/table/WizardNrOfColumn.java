/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: WizardNrOfColumn.java 843 2010-02-26 09:22:15Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import java.text.MessageFormat;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * TODO Describe this class
 * 
 * @author $Author: msc $
 * @version $Revision: 843 $
 */
public class WizardNrOfColumn extends Column {
    protected long _nrOfPersons = 0L;
    protected String _text;

    public WizardNrOfColumn(String name, String header, String text, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        _text = text;

        setEditorType(EDITOR.READ_ONLY);
        setEditType(EditType.readonly);
    }

    public WizardNrOfColumn(String name, String header, String text, IWebLocalizationManager manager, int width) throws DhtmlxException {
        this(name, header, text, manager);

        setWidth(width);
    }

    public void setNrOfPersons(long nrOfPersons) {
        _nrOfPersons = nrOfPersons;
    }

    public String getHeaderText() {
        return MessageFormat.format(getLocalizationManager().getMessage(getHeader()), _nrOfPersons);
    }

    public Object toValue(Object row) throws OgnlException {
        Object value = ognl.Ognl.getValue(getExpression(), row);
        return MessageFormat.format(getLocalizationManager().getMessage(_text), value);
    }
}
