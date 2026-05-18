/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$
 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.JSType;

public interface IGlobalJavaScript {
    public JSType getGlobal(String name);

    public String getGlobals() throws DhtmlxException;

    public String getScripts() throws DhtmlxException;
}
