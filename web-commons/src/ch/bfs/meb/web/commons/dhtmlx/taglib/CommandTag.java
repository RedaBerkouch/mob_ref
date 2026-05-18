/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id: UndoTag.java 305 2009-12-03 10:25:28Z msc $
 */
package ch.bfs.meb.web.commons.dhtmlx.taglib;

/**
 * TODO Describe this class
 * 
 * @author $Author: msc $
 * @version $Revision: 305 $
 */
public class CommandTag extends JavascriptTagBase {
    private static final long serialVersionUID = 8014578189721344913L;

    protected String _name;

    public void doTag() throws DhtmlxTagException {
        createJavascriptTag(_name);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }
}
