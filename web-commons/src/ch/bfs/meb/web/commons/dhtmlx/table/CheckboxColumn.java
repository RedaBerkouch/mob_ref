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
public class CheckboxColumn extends Column {

    protected static final int PARAM_CHECKBOX_WIDTH = 4;

    public CheckboxColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        setEditorType(EDITOR.CHECKBOX);
        setWidth(PARAM_CHECKBOX_WIDTH);
    }

    public void toObject(Object object, Object value) throws DhtmlxException, OgnlException {
        if (value instanceof String) {
            value = new Boolean(((String) value).equals("1"));
        }
        super.toObject(object, value);
    }

}
