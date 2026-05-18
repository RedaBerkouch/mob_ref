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
 * Generic XML Document wrapper interface, used by all dhtmlx controls to return
 * XML-Documents from the praesentation server to the control.
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IHttpResult {

    /**
     * Returns the XML-Document as string
     * 
     * @return XML Document as String
     */
    String getDocument();

    /**
     * Returns the mime type of the response as string
     * 
     * @return mime type as String
     */
    String getContentType();

    /**
     * Returns the disposition (used for file download) of the response as
     * string
     * 
     * @return disposition as String
     */
    String getContentDisposition();
}
