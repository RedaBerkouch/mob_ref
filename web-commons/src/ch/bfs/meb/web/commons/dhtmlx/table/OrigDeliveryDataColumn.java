/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: OrigDeliveryDataColumn.java 843 2010-02-26 09:22:15Z jfu $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

/**
 * TODO Describe this class
 * 
 * @author $Author: jfu $
 * @version $Revision: 843 $
 */
public class OrigDeliveryDataColumn extends Column {
    public OrigDeliveryDataColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        setEditorType(EDITOR.HTML_MULTILINE_READ_ONLY);
        setEditType(EditType.readonly);
        setColor(COLOR.LIGHTGREY);
    }

    public OrigDeliveryDataColumn(String name, String header, IWebLocalizationManager manager, int width) throws DhtmlxException {
        this(name, header, manager);

        setWidth(width);
    }

    @Override
    public String getDisplayString(Object row) throws OgnlException {
        return super.getDisplayString(row).replaceAll("\\n", "");
    }
}