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
import ch.bfs.meb.web.commons.dhtmlx.javascript.types.IJSType;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface IJavaScriptFunction {
    public void addParameter(IJSType parameter);

    public String getMethodCall();

    public String getMethodName();

    public String getScriptingPart() throws DhtmlxException;

    public String getScriptingBody() throws DhtmlxException;

    public String getGlobals();
}
