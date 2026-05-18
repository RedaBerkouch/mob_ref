/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: MebDhtmlxFileException.java  31.05.2013 13:32:53 Administrator $

 */
package ch.bfs.meb.web.commons.exception;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;

public class MebDhtmlxFileException extends DhtmlxException {
    private static final long serialVersionUID = -8143046644019157344L;

    public MebDhtmlxFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public MebDhtmlxFileException(String message) {
        super(message);
    }

    public MebDhtmlxFileException(Throwable cause) {
        super(cause);
    }
}
