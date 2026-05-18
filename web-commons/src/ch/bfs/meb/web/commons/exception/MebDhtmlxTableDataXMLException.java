/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: web-commons

  $Id: MebDhtmlxTableDataXMLException.java 483 2010-01-21 09:46:48Z jfu $

 */
package ch.bfs.meb.web.commons.exception;

import ch.bfs.meb.exception.MebUncheckedException;

/**
 * Exception class for special error handling for single row entries
 * 
 * @author $Author: jfu $
 * @version $Revision: 483 $
 */
@SuppressWarnings("serial")
public class MebDhtmlxTableDataXMLException extends MebUncheckedException {
    /**
     * @param message
     * @param cause
     */
    public MebDhtmlxTableDataXMLException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MebDhtmlxTableDataXMLException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MebDhtmlxTableDataXMLException(Throwable cause) {
        super(cause);
    }
}
