/*
  MEB Portal
  Bundesamt für Statistik

  adesso Schweiz AG
  Copyright (c) 2009, 2010

  Projekt: meb-commons

  $Id$

 */
package ch.bfs.meb.web.commons.exception;

import ch.bfs.meb.exception.MebUncheckedException;

/**
 * Exception class for invalid input from a table.
 * 
 * @author $Author$
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class InputValidationException extends MebUncheckedException {
    /**
     * @param message
     * @param cause
     */
    public InputValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public InputValidationException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InputValidationException(Throwable cause) {
        super(cause);
    }
}
