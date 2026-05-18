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
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class IdentityColumn extends Column {

    public IdentityColumn(String name, String header, IWebLocalizationManager manager) throws DhtmlxException {
        super(name, header, manager);

        setEditorType(EDITOR.READ_ONLY);
        setIdentity(true);
        setVisible(false);
    }
}
