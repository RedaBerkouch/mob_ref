/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$
 */
package ch.bfs.meb.web.commons.dhtmlx.javascript.types;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class JSType implements IJSType {
    private String _ref;

    protected void setRef(String ref) {
        _ref = ref;
    }

    protected String asRef() {
        return _ref;
    }

    public boolean isRef() {
        return _ref != null;
    }
}
