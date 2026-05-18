/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx;

/**
 * Implementation of the XmlHTTPResponse for a simple text response
 * 
 * @author $Author$
 * @version $Revision$
 */
public class TextHttpResult implements IHttpResult {

    private final String _text;

    public TextHttpResult(String text) {
        _text = text;
    }

    public String getDocument() {
        return null;
    }

    public String getContentType() {
        return ("text/html; charset=UTF-8");
    }

    public String getContentDisposition() {
        return null;
    }

    public String getText() {
        return _text;
    }

}
