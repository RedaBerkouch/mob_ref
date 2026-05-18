/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: WizardLinkColumn.java 843 2010-02-26 09:22:15Z msc $

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
public class WizardLinkColumn extends Column {
    protected String _url;
    protected String _text;
    protected String _nrOfName;
    protected boolean _validated;

    public WizardLinkColumn(String name, String header, String idName, String nrOfName, String text, String url, IWebLocalizationManager manager)
            throws DhtmlxException {
        super(name, header, manager);

        try {
            setExpression(idName);
        } catch (OgnlException ex) {
            throw new DhtmlxException("invalid idName", ex);
        }

        _nrOfName = nrOfName;
        _text = text;
        _url = url;

        setEditorType(EDITOR.LINK);
        setEditType(EditType.readonly);
    }

    public WizardLinkColumn(String name, String header, String idName, String nrOfName, String text, String url, IWebLocalizationManager manager, int width)
            throws DhtmlxException {
        this(name, header, idName, nrOfName, text, url, manager);

        setWidth(width);
    }

    public void setValidate(boolean validated) {
        _validated = validated;
    }

    public Object toValue(Object row) throws OgnlException {
        Object nrOf = ognl.Ognl.getValue(ognl.Ognl.parseExpression(_nrOfName), row);

        if (_validated || nrOf.toString().equals("0")) {
            return "";
        } else {
            Object value = ognl.Ognl.getValue(getExpression(), row);
            return getLocalizationManager().getMessage(_text) + "^" + MessageFormat.format(_url, value.toString()) + "^_self";
        }
    }
}
