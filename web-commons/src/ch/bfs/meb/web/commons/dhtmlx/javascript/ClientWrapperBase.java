/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id$

 */
package ch.bfs.meb.web.commons.dhtmlx.javascript;

import ch.bfs.meb.web.commons.dhtmlx.IDhtmlxControl;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public abstract class ClientWrapperBase {

    private final IDhtmlxControl _control;

    private final StringBuilder _buf;

    public ClientWrapperBase(IDhtmlxControl control, StringBuilder buf) {

        _control = control;
        _buf = buf;
    }

    public IDhtmlxControl getControl() {
        return _control;
    }

    public StringBuilder getBuf() {

        return _buf;
    }
}
