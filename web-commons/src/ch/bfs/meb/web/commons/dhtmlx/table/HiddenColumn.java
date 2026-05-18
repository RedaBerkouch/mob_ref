/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import org.apache.xmlbeans.XmlCursor;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;

/**
 * A column that is part of the dhtmlxgrid table, but is not displayed
 * 
 * @author $Author$
 * @version $Revision$
 */
public class HiddenColumn extends Column {
    public HiddenColumn(String name) throws DhtmlxException {
        super(name, null, null);
        setHidden(true);
    }

    public void createHeader(ch.bfs.meb.web.commons.dhtmlx.table.xmlbeans.ColumnDocument.Column column) {
        column.setWidth("0");
        column.setType(EDITOR.SIMPLE);
        column.setHidden(true);

        XmlCursor cursor = column.newCursor();
        cursor.setTextValue("Hidden");
        cursor.dispose();
    }
}
