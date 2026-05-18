/* ----------------------------------------------------------------------------
 * 
 * SBG-Projekt
 * 
 * Copyright (c) 2006 GLANCE AG, Switzerland
 * 
 * $Id$
 *
 * ------------------------------------------------------------------------- */
package ch.bfs.meb.web.commons.dhtmlx;

/**
 * Implementation of the XmlHTTPResponse for a dojo IframeIO request
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DojoIframeIOHTML implements IHttpResult {

    private final String _htmlDocument;

    public DojoIframeIOHTML() {
        _htmlDocument = "<html><body><textarea id='ok'>ok</textarea></body></html>";
    }

    public DojoIframeIOHTML(String errorText) {
        _htmlDocument = "<html><body><textarea id='error'>" + errorText + "</textarea></body></html>";
    }

    public String getDocument() {
        return _htmlDocument;
    }

    public String getContentType() {
        return "text/html";
    }

    public String getContentDisposition() {
        return null;
    }
}
