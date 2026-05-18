/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx;

import java.io.OutputStream;

/**
 * Implementation of the XmlHTTPResponse for a donwload xsd request
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class BinaryHttpResultBase implements IHttpResult {

    public String getDocument() {
        return null;
    }

    public abstract void writeTo(OutputStream os);
}
