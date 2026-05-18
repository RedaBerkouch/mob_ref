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

/**
 * Dhtmlx table column with the custom adesso "corotxtfix" type.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class CodeColumn extends ComboCodeGroupColumn {
    public CodeColumn(String name, String header, IWebLocalizationManager manager, String codeGroup) throws DhtmlxException {
        super(name, header, manager, codeGroup);
        setDefault(null);
        setEditorType(EDITOR.SELECTBOX_RO);
        setColor(COLOR.LIGHTGREY);
    }

    public CodeColumn(String name, String header, IWebLocalizationManager manager, String codeGroup, int width) throws DhtmlxException {
        super(name, header, manager, codeGroup, width);
        setDefault(null);
        setEditorType(EDITOR.SELECTBOX_RO);
        setColor(COLOR.LIGHTGREY);
    }
}
