/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: sdlweb

  $Id$
 */
package ch.bfs.meb.web.commons.dhtmlx.taglib;

import ch.bfs.meb.web.commons.dhtmlx.DhtmlxException;

/**
 * TODO Describe this class
 * 
 * @author $Author$
 * @version $Revision$
 */
public class DhtmlxTagException extends DhtmlxException {
    private static final long serialVersionUID = -181133376479653433L;

    /**
     * @param message
     * @param cause
     */
    public DhtmlxTagException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public DhtmlxTagException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     */
    public DhtmlxTagException(String message) {
        super(message);
    }
}
