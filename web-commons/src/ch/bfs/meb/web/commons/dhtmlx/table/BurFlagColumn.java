/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: BurFlagColumn.java  28.11.2012 15:03:42 Administrator $

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.i18n.IWebLocalizationManager;
import ognl.OgnlException;

public class BurFlagColumn extends CheckboxColumn {
    private boolean _synchBur = false;

    public BurFlagColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        setColor(COLOR.LIGHTGREY);
        setEditorType(EDITOR.READ_ONLY);
    }

    public void setSynchBur(boolean synchBur) {
        _synchBur = synchBur;
        setEditorType(_synchBur ? EDITOR.CHECKBOX : EDITOR.READ_ONLY);
    }

    public Object toValue(Object row) throws OgnlException {
        if (!_synchBur) {
            return "";
        } else {
            return super.toValue(row);
        }
    }
}
