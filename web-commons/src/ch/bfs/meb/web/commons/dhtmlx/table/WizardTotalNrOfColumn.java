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
public class WizardTotalNrOfColumn extends Column {
    protected long _nrOfPersons = 0L;

    public WizardTotalNrOfColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        setEditorType(EDITOR.READ_ONLY);
        setEditType(EditType.readonly);
    }

    public WizardTotalNrOfColumn(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {
        this(name, header, manager);

        setWidth(width);
    }

    public void setNrOfPersons(long nrOfPersons) {
        _nrOfPersons = nrOfPersons;
    }

    public String getHeaderText() {
        return MessageFormat.format(getLocalizationManager().getMessage(getHeader()), _nrOfPersons);
    }

    public Object toValue(Object row) throws OgnlException {
        return "";
    }
}
