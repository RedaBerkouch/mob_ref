/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.tab;

import ch.bfs.meb.web.commons.dhtmlx.IHttpResult;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DhtmlxTabXML extends DhtmlxXMLBase implements IHttpResult {

    public static final String XML_HEADER = "<?xml version=" + '"' + "1.0" + '"' + " encoding=" + '"' + "UTF-8" + '"' + "?>" + "\n" + "<!DOCTYPE rows SYSTEM " + '"'
            + "dhtmlxtab.dtd" + '"' + ">" + "\n";

    private final String _document;

    public DhtmlxTabXML(String document) {
        _document = document;
    }

    public String getDocument() {
        return XML_HEADER + _document;
    }
}
