/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: WizardImageColumn.java 843 2010-02-26 09:22:15Z msc $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * TODO Describe this class
 * 
 * @author $Author: msc $
 * @version $Revision: 843 $
 */
public class WizardImageColumn extends Column {
    public WizardImageColumn(String name, String header, String dataName, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        try {
            setExpression(dataName);
        } catch (OgnlException ex) {
            throw new DhtmlxException("invalid dataName", ex);
        }

        setEditorType(EDITOR.IMAGE);
        setEditType(EditType.readonly);
    }

    public WizardImageColumn(String name, String header, String dataName, IWebLocalizationManager manager, int width) throws DhtmlxException {
        this(name, header, dataName, manager);

        setWidth(width);
    }

    public Object toValue(Object row) throws OgnlException {
        Object value = ognl.Ognl.getValue(getExpression(), row);
        if (value.toString().equals("0")) {
            return "imgs/invisible.gif";
        } else {
            return "imgs/ok.png";
        }
    }
}
