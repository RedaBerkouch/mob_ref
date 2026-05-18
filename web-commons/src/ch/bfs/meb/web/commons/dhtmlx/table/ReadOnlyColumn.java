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
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class ReadOnlyColumn extends Column {
    protected boolean _useLocale = false;
    protected boolean _localizeValue = false;

    public ReadOnlyColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        setEditorType(EDITOR.READ_ONLY);
        setEditType(EditType.readonly);
        setColor(COLOR.LIGHTGREY);
    }

    public ReadOnlyColumn(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {
        this(name, header, manager);

        setWidth(width);
    }

    public ReadOnlyColumn(String name, String header, boolean useLocale, IWebLocalizationManager manager) throws DhtmlxException {
        this(name, header, manager);

        _useLocale = useLocale;
    }

    public ReadOnlyColumn(String name, String header, boolean useLocale, IWebLocalizationManager manager, int width) throws DhtmlxException {
        this(name, header, manager, width);

        _useLocale = useLocale;
    }

    public ReadOnlyColumn(String name, String header, boolean useLocale, boolean localizeValue, IWebLocalizationManager manager, int width)
            throws DhtmlxException {
        this(name, header, manager, width);

        _useLocale = useLocale;
        _localizeValue = localizeValue;
    }

    public Object toValue(Object row) throws OgnlException {
        if (_useLocale) {
            setExpression(getName() + getLocalizationManager().getLanguage());
        }

        if (_localizeValue) {
            String key = super.toValue(row).toString();
            return getLocalizationManager().getMessage(key, null, key);
        } else {
            return super.toValue(row);
        }
    }
}
