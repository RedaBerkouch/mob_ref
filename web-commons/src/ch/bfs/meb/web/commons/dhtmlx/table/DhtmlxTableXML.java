/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.table;

import ch.bfs.meb.web.commons.dhtmlx.IHttpResult;
import ch.bfs.meb.web.commons.dhtmlx.tab.DhtmlxXMLBase;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DhtmlxTableXML extends DhtmlxXMLBase implements IHttpResult {

    public static final String XML_HEADER = "<?xml version=" + '"' + "1.0" + '"' + " encoding=" + '"' + "UTF-8" + '"' + "?>" + "\n" + "<!DOCTYPE rows SYSTEM " + '"'
            + "dhtmlxgrid.dtd" + '"' + ">" + "\n";

    protected final String _document;

    public DhtmlxTableXML(String document) {
        _document = document;
    }

    public String getDocument() {
        return XML_HEADER + _document;
    }
}
